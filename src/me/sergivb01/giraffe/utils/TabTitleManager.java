package me.sergivb01.giraffe.utils;

import net.minecraft.server.v1_7_R4.ChatSerializer;
import net.minecraft.server.v1_7_R4.IChatBaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.spigotmc.ProtocolInjector;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TabTitleManager
{
    private static final int PROTOCOL_VERSION = 47;
    /**
     * @param header The header of the tab list.
     */
    public static void broadcastHeader(String header)
    {
        broadcastHeaderAndFooter(header, null);
    }
    /**
     * @param footer The footer of the tab list.
     */
    public static void broadcastFooter(String footer)
    {
        broadcastHeaderAndFooter(null, footer);
    }
    /**
     * @param header The header of the tab list.
     * @param footer The footer of the tab list.
     */
    public static void broadcastHeaderAndFooter(String header, String footer)
    {
        for (Player player : Bukkit.getOnlinePlayers()) setHeaderAndFooter(player, header, footer);
    }
    /**
     * @param p      The Player.
     * @param header The header.
     */
    public static void setHeader(Player p, String header)
    {
        setHeaderAndFooter(p, header, null);
    }
    /**
     * @param p The Player
     * @param footer The footer.
     */
    public static void setFooter(Player p, String footer)
    {
        setHeaderAndFooter(p, null, footer);
    }
    /**
     * @param p The Player.
     * @param rawHeader The header in raw text.
     * @param rawFooter The footer in raw text.
     */
    public static void setHeaderAndFooter(Player p, String rawHeader, String rawFooter)
    {
        CraftPlayer player = (CraftPlayer) p;
        if (player.getHandle().playerConnection.networkManager.getVersion() != PROTOCOL_VERSION) return;
        IChatBaseComponent header = ChatSerializer.a(TextConverter.convert(rawHeader));
        IChatBaseComponent footer = ChatSerializer.a(TextConverter.convert(rawFooter));
        if (header == null || footer == null)
        {
            TabTitleCache titleCache = TabTitleCache.getTabTitle(p.getUniqueId());
            if (titleCache != null)
            {
                if (header == null)
                {
                    String headerString = titleCache.getHeader();
                    if (headerString != null)
                    {
                        rawHeader = headerString;
                        header = ChatSerializer.a(TextConverter.convert(headerString));
                    }
                }
                if (footer == null)
                {
                    String footerString = titleCache.getFooter();
                    if (footerString != null)
                    {
                        rawHeader = footerString;
                        header = ChatSerializer.a(TextConverter.convert(footerString));
                    }
                }
            }
        }
        TabTitleCache.addTabTitle(p.getUniqueId(), new TabTitleCache(rawHeader, rawFooter));
        ProtocolInjector.PacketTabHeader packet = new ProtocolInjector.PacketTabHeader(header, footer);
        player.getHandle().playerConnection.sendPacket(packet);
    }
    private static class TextConverter {
        public static String convert(String text) {
            if (text == null || text.length() == 0)
            {
                return "\"\"";
            }
            char c;
            int i;
            int len = text.length();
            StringBuilder sb = new StringBuilder(len + 4);
            String t;
            sb.append('"');
            for (i = 0; i < len; i += 1)
            {
                c = text.charAt(i);
                switch (c)
                {
                    case '\\':
                    case '"':
                        sb.append('\\');
                        sb.append(c);
                        break;
                    case '/':
                        sb.append('\\');
                        sb.append(c);
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    default:
                        if (c < ' ') {
                            t = "000" + Integer.toHexString(c);
                            sb.append("\\u").append(t.substring(t.length() - 4));
                        } else {
                            sb.append(c);
                        }
                }
            }
            sb.append('"');
            return sb.toString();
        }
        public static String setPlayerName(Player player, String text) {
            return text.replaceAll("(?i)\\{PLAYER\\}", player.getName());
        }
    }
    private static class TabTitleCache {
        final private static Map<UUID, TabTitleCache> playerTabTitles = new HashMap<>();
        private String header;
        private String footer;
        public TabTitleCache(String header, String footer) {
            this.header = header;
            this.footer = footer;
        }
        public static TabTitleCache getTabTitle(UUID uuid) {
            return playerTabTitles.get(uuid);
        }
        public static void addTabTitle(UUID uuid, TabTitleCache titleCache) {
            playerTabTitles.put(uuid, titleCache);
        }
        public static void removeTabTitle(UUID uuid) {
            playerTabTitles.remove(uuid);
        }
        public String getHeader() {
            return header;
        }
        public String getFooter() {
            return footer;
        }
    }
}