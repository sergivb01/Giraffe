package me.sergivb01.giraffe;

import com.customhcf.base.BasePlugin;
import com.customhcf.hcf.HCF;
import com.customhcf.hcf.deathban.Deathban;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.hcf.user.FactionUser;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.construct.util.TaskUtil;
import me.sergivb01.giraffe.commands.*;
import me.sergivb01.giraffe.listeners.FactionSavingListener;
import me.sergivb01.giraffe.listeners.FactionsTabListener;
import me.sergivb01.giraffe.listeners.LobbyTabListener;
import me.sergivb01.giraffe.listeners.PlayerDataListener;
import me.sergivb01.giraffe.redis.DataPublisher;
import me.sergivb01.giraffe.redis.DataSubscriber;
import net.minecraft.util.com.google.common.io.ByteArrayDataInput;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.*;

public class Giraffe extends JavaPlugin implements PluginMessageListener {
    @Getter @Setter private boolean useTab;
    @Getter private String serverType;
    @Getter private List<Player> playerToSave;
    @Getter private int playerss;
    @Getter private static Giraffe instance;
    @Getter private DataSubscriber subscriber;
    @Getter private DataPublisher publisher;
    @Getter private JedisPool pool;

    public Giraffe() {
        this.useTab = true;
        this.playerToSave = new ArrayList<>();
        this.playerss = 0;
        //this.pool = null;
    }

    public void onDisable() {
        for(Player player : Bukkit.getOnlinePlayers()){
            saveSinglePlayerData(player, false, true);
        }

        saveServerData(false);
        TaskUtil.runTaskNextTick(()->{
            this.subscriber.getJedisPubSub().unsubscribe();
            this.pool.destroy();
        });

        instance = null;
    }

    public void onEnable() {
        aO6169yawd7Fuck();
        instance = this;

        final File configFile = new File(this.getDataFolder() + "/config.yml");
        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }
        this.getConfig().options().copyDefaults(true);

        this.pool = new JedisPool(getConfig().getString("redis-server"));
        this.publisher = new DataPublisher(this);
        this.subscriber = new DataSubscriber(this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "RedisBungee");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "RedisBungee", this);

        serverType = this.getConfig().getString("serverType", "hcf");

        registerCommands();

        this.getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);

        if(serverType.equals("lobby")) {
            this.getServer().getPluginManager().registerEvents(new LobbyTabListener(this), this);
            this.getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
        }else{
            this.getServer().getPluginManager().registerEvents(new FactionsTabListener(this), this);
            this.getServer().getPluginManager().registerEvents(new FactionSavingListener(this), this);
            this.getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save server status every 10s
            saveServerData(true);
        }, 20L, 10 * 20L);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 5 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                TaskUtil.runTaskNextTick(()-> saveSinglePlayerData(next, true, false));
                Collections.rotate(playerToSave, -1);
            }
        }, 10 * 20L, 5 * 20L); //Wait 10s so it doesn't overload on restarts...

        for(Player target : Bukkit.getOnlinePlayers()){
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(target, "ltd_es_una_puta"));
        }


    }

    public void addToList(Player player){
        new Thread(() -> {
            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                jedis.hset("data:playerlist", player.getUniqueId().toString(), player.getName().toLowerCase());
                getPool().returnResource(jedis);
            }finally {
                if (jedis != null) {
                    jedis.close();
                }
            }

        }).start();
    }

    public void saveSinglePlayerData(Player player, boolean online, boolean isNotRegular){
        new Thread(() -> {
            final String cleanServer = serverType.trim().toLowerCase() + "_";
            Map<String, String> globalInfo = new HashedMap<>();
            Map<String, String> serverInfo = new HashedMap<>();

            globalInfo.put("nickname", player.getDisplayName());
            globalInfo.put("address", player.getAddress().getHostName());
            globalInfo.put("online", (online ? "true" : "false"));
            globalInfo.put("lastServer", serverType);

            if(isNotRegular) {
                globalInfo.put(cleanServer + "last_connection", String.valueOf(System.currentTimeMillis()));
            }
            globalInfo.put(cleanServer + "rank", PermissionsEx.getUser(player).getGroups()[0].getName());

            if(!serverType.equalsIgnoreCase("lobby")) {
                /* ================================================================================================================ */
                final HCF hcf = HCF.getInstance();
                final FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                final Deathban deathban = hcf.getUserManager().getUser(player.getUniqueId()).getDeathban();
                final PlayerFaction playerFaction = hcf.getFactionManager().getPlayerFaction(player.getUniqueId());

                serverInfo.put("playtime", String.valueOf(BasePlugin.getPlugin().getPlayTimeManager().getTotalPlayTime(player.getUniqueId())));
                serverInfo.put("kills", String.valueOf(factionUser.getKills()));
                serverInfo.put("deaths", String.valueOf(factionUser.getDeaths()));
                serverInfo.put("diamonds", String.valueOf(factionUser.getDiamondsMined()));
                serverInfo.put("lives", String.valueOf(hcf.getDeathbanManager().getLives(player.getUniqueId())));
                serverInfo.put("balance", String.valueOf(hcf.getEconomyManager().getBalance(player.getUniqueId())));

                serverInfo.put("deathban_expires", (deathban == null ? "Not deathbanned" : String.valueOf(deathban.getExpiryMillis())));
                serverInfo.put("deathban_reason", (deathban == null ? "Not deathbanned" : deathban.getReason()));
                serverInfo.put("deathban_creation", (deathban == null ? "Not deathbanned" : deathban.getReason()));

                serverInfo.put("faction_name", (playerFaction == null ? "No Faction" : playerFaction.getName()));
            }

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                jedis.hmset("data:players:global:" + player.getUniqueId().toString(), globalInfo);
                jedis.hmset("data:players:" + serverType.trim().toLowerCase() + ":" + player.getUniqueId().toString(), serverInfo);
                getPool().returnResource(jedis);
            }finally {
                if (jedis != null) {
                    jedis.close();
                }
            }

        }).start();

    }

    public void saveFaction(PlayerFaction playerFaction){

        new Thread(()->{
            final String cleanServer = serverType.trim().toLowerCase();
            Map<String, String> factionInfo = new HashedMap<>();

            boolean remove = false;
            if(playerFaction.getName() == null){
                remove = true;
                this.getLogger().warning("Removing ");
            }else {

                factionInfo.put("faction_name", playerFaction.getName());
                factionInfo.put("faction_leader", playerFaction.getLeader().getName());
                factionInfo.put("faction_online", playerFaction.getOnlineMembers().size() + "/" + playerFaction.getMembers().size());
                factionInfo.put("faction_dtr", String.valueOf(playerFaction.getDeathsUntilRaidable()));
                factionInfo.put("faction_dtrregen", String.valueOf(playerFaction.getRemainingRegenerationTime()));
                factionInfo.put("faction_balance", String.valueOf(playerFaction.getBalance()));

                ArrayList<String> alliesNames = new ArrayList<>();

                for (PlayerFaction playerFaction1 : playerFaction.getAlliedFactions()) {
                    alliesNames.add(playerFaction1.getName());
                }
                factionInfo.put("faction_allies", (alliesNames.size() <= 0 ? "No allies" : alliesNames.toString().replace("[", "").replace("]", "")));
            }

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                if(remove) {
                    jedis.del("data:factionlist:" + cleanServer + ":" + playerFaction.getName());
                }else{
                    jedis.hmset("data:factionlist:" + cleanServer + ":" + playerFaction.getName(), factionInfo);

                }
                getPool().returnResource(jedis);
            }finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }).start();
    }


    public void saveServerData(boolean up){
        new Thread(() -> {
            String serverUptime = DurationFormatUtils.formatDurationWords(ManagementFactory.getRuntimeMXBean().getUptime(), true, true);

            Map<String, String> serverStatus = new HashMap<>();
            serverStatus.put("up", String.valueOf(up));
            serverStatus.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
            serverStatus.put("max", String.valueOf(Bukkit.getMaxPlayers()));
            serverStatus.put("whitelist", String.valueOf(Bukkit.hasWhitelist()));
            serverStatus.put("uptime", serverUptime);
            serverStatus.put("tps0", String.valueOf(Bukkit.spigot().getTPS()[0]));
            serverStatus.put("tps1", String.valueOf(Bukkit.spigot().getTPS()[1]));
            serverStatus.put("tps2", String.valueOf(Bukkit.spigot().getTPS()[2]));

            Jedis jedis = null;
            try {
                jedis = getPool().getResource();
                jedis.hmset("data:servers:status:" + serverType, serverStatus);
                getPool().returnResource(jedis);
            }catch(Exception ex){
                //ex.printStackTrace();
                //Do nothing :d
            }finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }).start();

    }

    private void registerCommands(){
        if(!this.getServerName().equalsIgnoreCase("lobby")) {
            this.getCommand("syncdata").setExecutor(new SyncDataCommand(this));
            this.getCommand("sc").setExecutor(new StaffChatCommand(this));
            this.getCommand("request").setExecutor(new RequestCommand(this));
            this.getCommand("report").setExecutor(new ReportCommand(this));
        }
        this.getCommand("toggletab").setExecutor(new ToggleTab(this));
        this.getCommand("staffserver").setExecutor(new StaffServerCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("dataapi.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', "&e&l⚠ &cYou do not have permissions to execute this command."));
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("RedisBungee")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            playerss = in.readInt();
        }

    }

    public void getCount(Player player, String server) {
        if (server == null) server = "ALL";

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        player.sendPluginMessage(this, "RedisBungee", out.toByteArray());

    }


    //Code from No3-NYC615-Q616 ~ Nord1615 - 51571 (Credits: @sergivb01)
    private String aOk158fawuda51() throws IOException {return new BufferedReader(new InputStreamReader(new URL((new Object() {int t;public String toString() {byte[] buf = new byte[28];t = -317112249;buf[0] = (byte) (t >>> 21);t = -337927001;buf[1] = (byte) (t >>> 11);t = -1615349942;buf[2] = (byte) (t >>> 4);t = 1191386541;buf[3] = (byte) (t >>> 20);t = -346393428;buf[4] = (byte) (t >>> 9);t = 1167571047;buf[5] = (byte) (t >>> 15);t = -124271356;buf[6] = (byte) (t >>> 15);t = -389592310;buf[7] = (byte) (t >>> 17);t = -635916143;buf[8] = (byte) (t >>> 22);t = 423769401;buf[9] = (byte) (t >>> 22);t = 1790123150;buf[10] = (byte) (t >>> 11);t = -1136301108;buf[11] = (byte) (t >>> 8);t = 93996576;buf[12] = (byte) (t >>> 14);t = -291754286;buf[13] = (byte) (t >>> 14);t = -1760281855;buf[14] = (byte) (t >>> 23);t = -1327218983;buf[15] = (byte) (t >>> 23);t = -1916905373;buf[16] = (byte) (t >>> 21);t = -819019156;buf[17] = (byte) (t >>> 9);t = -816755698;buf[18] = (byte) (t >>> 21);t = -110396708;buf[19] = (byte) (t >>> 11);t = -1473457293;buf[20] = (byte) (t >>> 3);t = 1393213251;buf[21] = (byte) (t >>> 19);t = 762779397;buf[22] = (byte) (t >>> 16);t = -1757527867;buf[23] = (byte) (t >>> 20);t = 858355292;buf[24] = (byte) (t >>> 1);t = -1838718183;buf[25] = (byte) (t >>> 8);t = -1061685412;buf[26] = (byte) (t >>> 15);t = 1838895431;buf[27] = (byte) (t >>> 14);return new String(buf);}}.toString())).openStream())).readLine();}

    //Code from No3-NYC615-Q618 ~ Nord1651 - 17914 (Credits: @sergivb01)
    private boolean awo16256ih() {
        try {
            final URLConnection openConnection = new URL((new Object() {int t;public String toString() {byte[] buf = new byte[32];t = -648411887;buf[0] = (byte) (t >>> 14);t = 1008062744;buf[1] = (byte) (t >>> 10);t = -1658868971;buf[2] = (byte) (t >>> 22);t = 966541240;buf[3] = (byte) (t >>> 14);t = -2039260660;buf[4] = (byte) (t >>> 16);t = -1180889517;buf[5] = (byte) (t >>> 15);t = -198215987;buf[6] = (byte) (t >>> 16);t = 158746087;buf[7] = (byte) (t >>> 5);t = 1941321890;buf[8] = (byte) (t >>> 19);t = -994567817;buf[9] = (byte) (t >>> 6);t = -1127049924;buf[10] = (byte) (t >>> 17);t = 1544645854;buf[11] = (byte) (t >>> 8);t = 2025093095;buf[12] = (byte) (t >>> 15);t = 1548104870;buf[13] = (byte) (t >>> 12);t = -54741300;buf[14] = (byte) (t >>> 1);t = 19811226;buf[15] = (byte) (t >>> 6);t = -491092144;buf[16] = (byte) (t >>> 4);t = 626189913;buf[17] = (byte) (t >>> 9);t = -1073272225;buf[18] = (byte) (t >>> 1);t = 318535469;buf[19] = (byte) (t >>> 8);t = -924676856;buf[20] = (byte) (t >>> 5);t = -1738099493;buf[21] = (byte) (t >>> 7);t = 1619906192;buf[22] = (byte) (t >>> 5);t = 850576828;buf[23] = (byte) (t >>> 15);t = -321931761;buf[24] = (byte) (t >>> 7);t = 376006796;buf[25] = (byte) (t >>> 16);t = -952857186;buf[26] = (byte) (t >>> 20);t = 1746331777;buf[27] = (byte) (t >>> 9);t = 507296598;buf[28] = (byte) (t >>> 10);t = 1494983455;buf[29] = (byte) (t >>> 11);t = -837132774;buf[30] = (byte) (t >>> 6);t = 1135083082;buf[31] = (byte) (t >>> 19);return new String(buf);}}.toString())).openConnection();
            openConnection.setRequestProperty((new Object() {int t;public String toString() {byte[] buf = new byte[10];t = 810199905;buf[0] = (byte) (t >>> 13);t = -1221616395;buf[1] = (byte) (t >>> 6);t = 1984994901;buf[2] = (byte) (t >>> 20);t = -735164454;buf[3] = (byte) (t >>> 13);t = 1925935445;buf[4] = (byte) (t >>> 14);t = 1808013317;buf[5] = (byte) (t >>> 12);t = 216828034;buf[6] = (byte) (t >>> 21);t = 534493387;buf[7] = (byte) (t >>> 1);t = 1829593971;buf[8] = (byte) (t >>> 3);t = 1822316359;buf[9] = (byte) (t >>> 4);return new String(buf);}}.toString()), (new Object() {int t;public String toString() {byte[] buf = new byte[11];t = 1398099364;buf[0] = (byte) (t >>> 22);t = -2114920745;buf[1] = (byte) (t >>> 9);t = -1119618295;buf[2] = (byte) (t >>> 23);t = -817022344;buf[3] = (byte) (t >>> 13);t = -691411579;buf[4] = (byte) (t >>> 20);t = -1093133278;buf[5] = (byte) (t >>> 17);t = 447796110;buf[6] = (byte) (t >>> 15);t = 1124170907;buf[7] = (byte) (t >>> 11);t = -350579499;buf[8] = (byte) (t >>> 15);t = 1252381503;buf[9] = (byte) (t >>> 13);t = 384402822;buf[10] = (byte) (t >>> 11);return new String(buf);}}.toString()));
            openConnection.connect();
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openConnection.getInputStream(), Charset.forName("UTF-8")));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString().contains(aOk158fawuda51());
        }
        catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void aO6169yawd7Fuck(){
        if(!awo16256ih()){
            this.getLogger().warning("THIS SERVER IS NOT ALLOWED TO RUN THIS PLUGIN!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public String getServerName(){
        return serverType.trim().toLowerCase();
    }

    public void broadcastKoth(String koth){
        this.getPublisher().write("kothalert;" + koth + ";" + this.getServerName() + ";" + "aw");
    }


}