package me.sergivb01.giraffe.redis;

import net.veilmc.base.BasePlugin;
import net.veilmc.util.chat.ClickAction;
import net.veilmc.util.chat.Text;
import me.sergivb01.giraffe.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class DataSubscriber
{
    private JedisPubSub jedisPubSub;
    private Jedis jedis;
    private Giraffe plugin;

    public DataSubscriber(final Giraffe plugin) {
        this.plugin = plugin;
        this.jedis = new Jedis(plugin.getConfig().getString("redis-server"));
        this.subscribe();
    }

    public void subscribe() {
        this.jedisPubSub = this.get();
        new Thread(() -> DataSubscriber.this.jedis.subscribe(DataSubscriber.this.jedisPubSub, "giraffe")).start();
    }

    private JedisPubSub get() {
        return new JedisPubSub() {
            @Override
            public void onMessage(final String channel, final String message) {
                if (channel.equalsIgnoreCase("giraffe")) {
                    final String[] args = message.split(";");
                    if (args.length > 2) {
                        final String command = args[0].toLowerCase();
                        final String sender = args[1];
                        final String server = args[2];
                        final String msg = args[3];
                        switch (command) {
                            case "srvstatus": {
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6&lServer Status Alert&8] &9&l" + sender + " &eis now " + (server.equalsIgnoreCase("up") ? "&eonline" : "&coffline") + "&e!"));
                                break;
                            }
                            case "kothalert": {
                                    Bukkit.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&8[&6&lKoth Alert&8] &9&l" + sender + " &eIS NOW RUNNING ON &a&l" + server.toUpperCase() + "&e."));
                                break;
                            }
                            case "staffchat": {
                                for (final Player staff : Bukkit.getOnlinePlayers()) {
                                    if (staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        staff.sendMessage(ChatColor.BLUE + "(Staff) " +ChatColor.DARK_AQUA + "[" + server + "] " + ChatColor.AQUA + sender + ": " + msg);
                                    }
                                }
                                break;
                            }
                            case "request": {
                                for (final Player staff : Bukkit.getOnlinePlayers()) {
                                    if (staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        new Text(ChatColor.BLUE + "[Request] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " requested assistance.").setHoverText(ChatColor.BLUE + "Click here to teleport to " + ChatColor.GRAY + sender).setClick(ClickAction.RUN_COMMAND, "/staffserver " + server + " " + sender).send(staff);
                                        staff.sendMessage(ChatColor.BLUE + "    Reason: " + ChatColor.GRAY + msg);
                                    }
                                }
                                break;
                            }
                            case "report": {
                                final String target = args[4];
                                for (final Player staff2 : Bukkit.getOnlinePlayers()) {
                                    if (staff2.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff2.getUniqueId()).isStaffChatVisible()) {
                                        new Text(ChatColor.RED + "[Report] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " has reported " + ChatColor.RESET + target + ChatColor.GRAY + ".").setHoverText(ChatColor.RED + "Click here to teleport to " + ChatColor.GRAY + target).setClick(ClickAction.RUN_COMMAND, "/staffserver " + server + " " + target).send(staff2);
                                        staff2.sendMessage(ChatColor.RED + "    Reason:" + ChatColor.GRAY + msg.replace(target, ""));
                                    }
                                }
                                break;
                            }
                            case "staffswitch": {
                                for (final Player staff2 : Bukkit.getOnlinePlayers()) {
                                    if (staff2.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff2.getUniqueId()).isStaffChatVisible()) {
                                        staff2.sendMessage(ChatColor.BLUE + "(Staff) " +ChatColor.DARK_AQUA + "[" + server + "] " + ChatColor.AQUA + sender + " " + msg);
                                    }
                                }
                                break;
                            }
                            default:
                                plugin.getLogger().warning("Recived data but i don't know how to handle it! \"" + message + "\"");
                                break;
                        }
                    }
                }else{
                    plugin.getLogger().warning("Recived data from \"" + channel + "\" i don't know how to handle it! \"" + message + "\"");
                }
            }

            @Override
            public void onPMessage(final String s, final String s1, final String s2) {
            }

            @Override
            public void onSubscribe(final String s, final int i) {
            }

            @Override
            public void onUnsubscribe(final String s, final int i) {
            }

            @Override
            public void onPUnsubscribe(final String s, final int i) {
            }

            @Override
            public void onPSubscribe(final String s, final int i) {
            }
        };
    }

    public JedisPubSub getJedisPubSub() {
        return this.jedisPubSub;
    }

}