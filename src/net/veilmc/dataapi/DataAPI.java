package net.veilmc.dataapi;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.user.BaseUser;
import com.customhcf.hcf.HCF;
import com.customhcf.hcf.deathban.Deathban;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.hcf.user.FactionUser;
import net.veilmc.dataapi.commands.*;
import net.veilmc.dataapi.listeners.PlayerListener;
import net.veilmc.dataapi.redis.DataPublisher;
import net.veilmc.dataapi.redis.DataSubscriber;
import org.apache.commons.collections4.map.HashedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.File;
import java.util.*;

import static org.bukkit.Bukkit.getScheduler;

public class DataAPI extends JavaPlugin{
    public static boolean debug = true;
    private DataPublisher publisher;
    private DataSubscriber subscriber;
    private JedisPool pool;
    private String serverType;
    private String jedisPrefix;
    private DataAPI instance;
    private List<Player> playerToSave = new ArrayList<>();

    public DataAPI() {
        this.pool = null;
    }

    public void onDisable() {
        this.subscriber.getJedisPubSub().unsubscribe(); //cya redis :d
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

        //For staffserver cmd
        getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        setupJedis();
        registerCommands();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        serverType = this.getConfig().getString("serverType", "hcf");
        jedisPrefix = "data:gamemodes:" + serverType;

        getScheduler().scheduleSyncRepeatingTask(this, this::saveServerData, 5 * 20L, 5 * 20L);
        getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                saveSinglePlayerData(next);
                Collections.rotate(playerToSave, -1);
            }
        }, 10 * 20L, 10 * 20L);


    }

    public void saveSinglePlayerData(Player player){
        final Jedis jedis = getJedisPool().getResource();
        final Deathban deathban = HCF.getInstance().getUserManager().getUser(player.getUniqueId()).getDeathban();
        final FactionUser factionUser = HCF.getInstance().getUserManager().getUser(player.getUniqueId());
        final PlayerFaction playerFaction = HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId());
        final BaseUser baseUser = BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId());

        Map<String, String> playerInfo = new HashedMap<>();
        playerInfo.put("nickname", player.getName());
        playerInfo.put("lastIP", player.getAddress().getHostName());
        playerInfo.put("playtime", String.valueOf(BasePlugin.getPlugin().getPlayTimeManager().getTotalPlayTime(player.getUniqueId())));
        playerInfo.put("kills", String.valueOf(factionUser.getKills()));
        playerInfo.put("deaths", String.valueOf(factionUser.getDeaths()));
        playerInfo.put("diamonds", String.valueOf(factionUser.getDiamondsMined()));
        playerInfo.put("lives", String.valueOf(HCF.getInstance().getDeathbanManager().getLives(player.getUniqueId())));
        playerInfo.put("balance", String.valueOf(HCF.getInstance().getEconomyManager().getBalance(player.getUniqueId())));

        playerInfo.put("deathban_remaining", (deathban == null ? "Not deathbanned" : String.valueOf(deathban.getRemaining())));
        playerInfo.put("deathban_reason", (deathban == null ? "Not deathbanned" : deathban.getReason()));

        playerInfo.put("faction_name", (playerFaction == null ? "No Faction" : playerFaction.getName()));
        playerInfo.put("faction_role", (playerFaction == null ? "No Faction" : playerFaction.getMember(player.getUniqueId()).getRole().getName()));
        playerInfo.put("faction_online", (playerFaction == null ? "No Faction" : playerFaction.getOnlineMembers().size() + "/" + playerFaction.getMembers().size()));
        playerInfo.put("faction_dtr", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getDeathsUntilRaidable())));
        playerInfo.put("faction_dtrregen", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getRemainingRegenerationTime())));

        playerInfo.put("staff_modmode", String.valueOf(baseUser.isStaffUtil()));
        playerInfo.put("staff_vanish", String.valueOf(baseUser.isVanished()));
        playerInfo.put("staff_sc", String.valueOf(baseUser.isInStaffChat()));

        playerInfo.put("options_sounds", String.valueOf(baseUser.isMessagingSounds()));
        playerInfo.put("options_pm", String.valueOf(baseUser.isMessagesVisible()));
        playerInfo.put("options_sc", String.valueOf(baseUser.isStaffChatVisible()));
        playerInfo.put("options_gc", String.valueOf(baseUser.isGlobalChatVisible()));

        jedis.hmset(jedisPrefix + ":playerdata:" + player.getUniqueId().toString(), playerInfo);

        getJedisPool().returnResource(jedis);
        jedis.close();

    }

    public void saveServerData(){

        final Jedis jedis = getJedisPool().getResource();
        Map<String, String> serverStatus = new HashMap<>();
        serverStatus.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        serverStatus.put("max", String.valueOf(Bukkit.getMaxPlayers()));

        serverStatus.put("tps0", String.valueOf(Bukkit.spigot().getTPS()[0]));
        serverStatus.put("tps1", String.valueOf(Bukkit.spigot().getTPS()[1]));
        serverStatus.put("tps2", String.valueOf(Bukkit.spigot().getTPS()[2]));

        jedis.hmset(jedisPrefix + ":serverstatus", serverStatus);

        getJedisPool().returnResource(jedis);
        jedis.close();

    }

    public void savePlayerGlobalData(){
        for(Player p : Bukkit.getOnlinePlayers()) {
           saveSinglePlayerData(p);
        }
    }

    private void handleData(String data) {
        if (data.startsWith("SendMessage;")) {
            String player = data.split(":")[1];
            String message = "";
            for (int i = 2; i < data.split(":").length; i++) {
                message = message + data.split(":")[i] + " ";
            }
            if (Bukkit.getPlayer(player) != null) {
                Player p = Bukkit.getPlayer(player);
                p.sendMessage(message);
            }
        }
    }

    private void registerCommands(){
        this.getCommand("syncdata").setExecutor(new SyncDataCommand(this));
        this.getCommand("sc").setExecutor(new StaffChatCommand(this));
        this.getCommand("request").setExecutor(new RequestCommand(this));
        this.getCommand("report").setExecutor(new ReportCommand(this));
        this.getCommand("staffserver").setExecutor(new StaffServerCommand(this));

        Map<String, Map<String, Object>> map = getDescription().getCommands();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            PluginCommand command = getCommand(entry.getKey());
            command.setPermission("dataapi.command." + entry.getKey());
            command.setPermissionMessage(ChatColor.translateAlternateColorCodes('&', "&e&l⚠ &cYou do not have permissions to execute this command."));
        }
    }

    private void setupJedis(){
        try {

            this.pool = new JedisPool(this.getConfig().getString("redis-server"));
            this.publisher = new DataPublisher(this);
            this.subscriber = new DataSubscriber(this);

            /*this.pool = new JedisPool(new JedisPoolConfig(), this.getConfig().getString("redis-server"), this.getConfig().getInt("redis-port"));
            final Jedis jedis = this.getJedisPool().getResource();
            jedis.ping();
            this.getJedisPool().returnResource(jedis);
            this.publisher = new DataPublisher(this);
            this.subscriber = new DataSubscriber(this);*/
        }catch (JedisConnectionException e) {
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public DataAPI getInstance(){ return instance; }

    public DataPublisher getPublisher() { return publisher; }

    public DataSubscriber getSubscriber() { return subscriber; }

    public JedisPool getJedisPool() {
        return this.pool;
    }

    private Messenger getMessenger() { return Bukkit.getMessenger(); }

    public List<Player> getPlayerToSave(){ return playerToSave; }

    public String getJedisPrefix(){ return jedisPrefix; }
}