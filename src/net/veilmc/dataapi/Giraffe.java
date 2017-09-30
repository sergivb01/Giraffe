package net.veilmc.dataapi;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.user.BaseUser;
import com.customhcf.hcf.HCF;
import com.customhcf.hcf.deathban.Deathban;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.hcf.user.FactionUser;
import lombok.Getter;
import net.minecraft.util.com.google.common.io.ByteArrayDataInput;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
import net.veilmc.dataapi.commands.*;
import net.veilmc.dataapi.listeners.FactionsTabListener;
import net.veilmc.dataapi.listeners.LobbyTabListener;
import net.veilmc.dataapi.listeners.PlayerDataListener;
import net.veilmc.dataapi.redis.DataPublisher;
import net.veilmc.dataapi.redis.DataSubscriber;
import org.apache.commons.collections4.map.HashedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.*;

public class Giraffe extends JavaPlugin implements PluginMessageListener {
    private boolean useTab;
    private String serverType;
    private List<Player> playerToSave;
    private int playerss;
    private static Giraffe instance;
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
        this.subscriber.getJedisPubSub().unsubscribe();
        this.pool.destroy();

        instance = null;
    }

    public void onEnable() {
        instance = this;
        //Configuration stuff
        final File configFile = new File(this.getDataFolder() + "/config.yml");
        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }
        this.getConfig().options().copyDefaults(true);

        this.pool = new JedisPool("95.85.43.227");
        this.publisher = new DataPublisher(this);
        this.subscriber = new DataSubscriber(this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "RedisBungee");
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "RedisBungee", this);

        registerCommands();

        serverType = this.getConfig().getString("serverType", "hcf");

        this.getServer().getPluginManager().registerEvents(new PlayerDataListener(this), this);

        if(serverType.equals("lobby")) {
            this.getServer().getPluginManager().registerEvents(new LobbyTabListener(this), this);
            getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
            getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
            getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
            getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
            getLogger().info("LOBBY MODEEEEEEEEEEEEEE");
        }else{
            this.getServer().getPluginManager().registerEvents(new FactionsTabListener(this), this);
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            saveServerData(true);
        }, 5 * 20L, 5 * 20L);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                saveSinglePlayerData(next, true);
                Collections.rotate(playerToSave, -1);
            }
        }, 5 * 20L, 5 * 20L);


    }

    public void saveSinglePlayerData(Player player, boolean online){
        final String cleanServer = serverType.trim().toLowerCase() + "_";
        Map<String, String> globalInfo = new HashedMap<>();

        globalInfo.put("nickname", player.getDisplayName());
        globalInfo.put("address", player.getAddress().getHostName());
        globalInfo.put("online", (online ? "true" : "false"));
        globalInfo.put("lastServer", serverType);
        globalInfo.put("nickname", player.getDisplayName());
        if(!serverType.equalsIgnoreCase("lobby")) {
            final FactionUser factionUser = HCF.getInstance().getUserManager().getUser(player.getUniqueId());
            globalInfo.put(cleanServer + "playtime", String.valueOf(BasePlugin.getPlugin().getPlayTimeManager().getTotalPlayTime(player.getUniqueId())));
            globalInfo.put(cleanServer + "kills", String.valueOf(factionUser.getKills()));
            globalInfo.put(cleanServer + "deaths", String.valueOf(factionUser.getDeaths()));
            globalInfo.put(cleanServer + "diamonds", String.valueOf(factionUser.getDiamondsMined()));
            globalInfo.put(cleanServer + "lives", String.valueOf(HCF.getInstance().getDeathbanManager().getLives(player.getUniqueId())));
            globalInfo.put(cleanServer + "balance", String.valueOf(HCF.getInstance().getEconomyManager().getBalance(player.getUniqueId())));

            final Deathban deathban = HCF.getInstance().getUserManager().getUser(player.getUniqueId()).getDeathban();
            globalInfo.put(cleanServer + "deathban_remaining", (deathban == null ? "Not deathbanned" : String.valueOf(deathban.getRemaining())));
            globalInfo.put(cleanServer + "deathban_reason", (deathban == null ? "Not deathbanned" : deathban.getReason()));

            final PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId());
            globalInfo.put(cleanServer + "faction_name", (playerFaction == null ? "No Faction" : playerFaction.getName()));
            globalInfo.put(cleanServer + "faction_role", (playerFaction == null ? "No Faction" : playerFaction.getMember(player.getUniqueId()).getRole().getName()));
            globalInfo.put(cleanServer + "faction_online", (playerFaction == null ? "No Faction" : playerFaction.getOnlineMembers().size() + "/" + playerFaction.getMembers().size()));
            globalInfo.put(cleanServer + "faction_dtr", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getDeathsUntilRaidable())));
            globalInfo.put(cleanServer + "faction_dtrregen", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getRemainingRegenerationTime())));
            globalInfo.put(cleanServer + "faction_balance", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getBalance())));

            final BaseUser baseUser = BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId());
            globalInfo.put("staff_modmode", String.valueOf(baseUser.isStaffUtil()));
            globalInfo.put("staff_vanish", String.valueOf(baseUser.isVanished()));
            globalInfo.put("staff_sc", String.valueOf(baseUser.isInStaffChat()));
            globalInfo.put("options_sounds", String.valueOf(baseUser.isMessagingSounds()));
            globalInfo.put("options_pm", String.valueOf(baseUser.isMessagesVisible()));
            globalInfo.put("options_sc", String.valueOf(baseUser.isStaffChatVisible()));
            globalInfo.put("options_gc", String.valueOf(baseUser.isGlobalChatVisible()));
        }

        Jedis jedis = null;
        try {
            jedis = getPool().getResource();
            jedis.hmset("data:players:" + player.getUniqueId().toString(), globalInfo);
            getPool().returnResource(jedis);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        //getJedisPool().returnResource(jedis);
        //jedis.close();

    }

    public void saveServerData(boolean up){
        Map<String, String> serverStatus = new HashMap<>();
        serverStatus.put("up", String.valueOf(up));
        serverStatus.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        serverStatus.put("max", String.valueOf(Bukkit.getMaxPlayers()));
        serverStatus.put("whitelist", String.valueOf(Bukkit.hasWhitelist()));

        serverStatus.put("tps0", String.valueOf(Bukkit.spigot().getTPS()[0]));
        serverStatus.put("tps1", String.valueOf(Bukkit.spigot().getTPS()[1]));
        serverStatus.put("tps2", String.valueOf(Bukkit.spigot().getTPS()[2]));


        Jedis jedis = null;
        try {
            jedis = getPool().getResource();
            jedis.hmset("data:servers:status:" + serverType, serverStatus);
            getPool().returnResource(jedis);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        //getJedisPool().returnResource(jedis);
        //jedis.close();

    }

    public void savePlayerGlobalData(){
        for(Player p : Bukkit.getOnlinePlayers()) {
            saveSinglePlayerData(p, true);
        }
    }

    private void handleData(String data) {
        if (data.startsWith("SendMessage;")) {
            String player = data.split(":")[1];
            StringBuilder message = new StringBuilder();
            for (int i = 2; i < data.split(":").length; i++) {
                message.append(data.split(":")[i]).append(" ");
            }
            if (Bukkit.getPlayer(player) != null) {
                Player p = Bukkit.getPlayer(player);
                p.sendMessage(message.toString());
            }
        }
    }

    private void registerCommands(){
        this.getCommand("syncdata").setExecutor(new SyncDataCommand(this));
        this.getCommand("sc").setExecutor(new StaffChatCommand(this));
        this.getCommand("request").setExecutor(new RequestCommand(this));
        this.getCommand("report").setExecutor(new ReportCommand(this));
        this.getCommand("toggletab").setExecutor(new ToggleTab(this));
        this.getCommand("staffserver").setExecutor(new StaffServerCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("dataapi.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', "&e&l⚠ &cYou do not have permissions to execute this command."));
        }
    }

    /*private void setupJedis(){
        try {
            this.pool = new JedisPool(this.getConfig().getString("redis-server"));
            this.publisher = new DataPublisher(this);
            this.subscriber = new DataSubscriber(this);
            this.jedis = this.getJedisPool().getResource();
            //this.jedis.select(2);
        }catch (JedisConnectionException e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public DataPublisher getPublisher() { return publisher; }

    public JedisPool getJedisPool() {
        return this.pool;
    }

    private Messenger getMessenger() { return Bukkit.getMessenger(); }

    public List<Player> getPlayerToSave(){ return playerToSave; }*/

    public void toggleTab(){ useTab = !useTab;}

    public boolean getToggleTab(){ return useTab;}

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("RedisBungee")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (subchannel.equals("PlayerCount")) {
            String server = in.readUTF();
            int playerCount = in.readInt();

            playerss = playerCount;
            //player.sendMessage("Player count on server " + server + " is equal to " + playerCount);

        }

    }

    public void getCount(Player player, String server) {

        if (server == null) {
            server = "ALL";
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        player.sendPluginMessage(this, "RedisBungee", out.toByteArray());

    }

    public int getPlayerss(){ return playerss; }

    /*public Jedis getJedis() {
        try {
            jedis.set("test:test:test", "testing");
        } catch (Exception ex) {
            //ex.printStackTrace();
            this.subscriber.getJedisPubSub().unsubscribe();
            this.pool.destroy();
            getLogger().info("Fixing IT!");
            try {
                //JedisPoolConfig poolConfig = new JedisPoolConfig();
                //poolConfig.setTestWhileIdle(true);
                //poolConfig.setMinEvictableIdleTimeMillis(60000);
                //poolConfig.setTimeBetweenEvictionRunsMillis(30000);
                //poolConfig.setNumTestsPerEvictionRun(-1);
                //this.pool = new JedisPool(poolConfig, this.getConfig().getString("redis-server"), this.getConfig().getInt("redis-port"));
                this.pool = new JedisPool(this.getConfig().getString("redis-server"), this.getConfig().getInt("redis-port"));
                this.jedis = this.getJedisPool().getResource();
                this.publisher = new DataPublisher(this);
                this.subscriber = new DataSubscriber(this);

            } catch (JedisConnectionException e) {
                e.printStackTrace();
                this.getServer().getPluginManager().disablePlugin(this);
            }
        }
        return jedis;

    }*/

    public static Giraffe getInstance(){ return instance; }

    public List<Player> getPlayerToSave() {
        return playerToSave;
    }

}