package net.veilmc.dataapi;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.user.BaseUser;
import com.customhcf.hcf.HCF;
import com.customhcf.hcf.deathban.Deathban;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.hcf.user.FactionUser;
import net.veilmc.dataapi.commands.*;
import net.veilmc.dataapi.listeners.FactionsTabListener;
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
    private boolean useTab = true;
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

        getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        setupJedis();
        registerCommands();

        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        this.getServer().getPluginManager().registerEvents(new FactionsTabListener(this), this);

        serverType = this.getConfig().getString("serverType", "hcf");
        jedisPrefix = "data:gamemodes:" + serverType;


        getScheduler().scheduleSyncRepeatingTask(this, this::saveServerData, 5 * 20L, 5 * 20L);
        getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                saveSinglePlayerData(next, true);
                Collections.rotate(playerToSave, -1);
            }
        }, 10 * 20L, 10 * 20L);

        /*Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            getPublisher().write("staffchat;" + sender.getName() + ";" + Bukkit.getServerName() + ";" + StringUtils.join(args, " ").replace(";", ":"));
        }, 3 * 20L);*/


    }

    public void saveSinglePlayerData(Player player, boolean online){
        final Jedis jedis = getJedisPool().getResource();
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

            final BaseUser baseUser = BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId());
            globalInfo.put("staff_modmode", String.valueOf(baseUser.isStaffUtil()));
            globalInfo.put("staff_vanish", String.valueOf(baseUser.isVanished()));
            globalInfo.put("staff_sc", String.valueOf(baseUser.isInStaffChat()));
            globalInfo.put("options_sounds", String.valueOf(baseUser.isMessagingSounds()));
            globalInfo.put("options_pm", String.valueOf(baseUser.isMessagesVisible()));
            globalInfo.put("options_sc", String.valueOf(baseUser.isStaffChatVisible()));
            globalInfo.put("options_gc", String.valueOf(baseUser.isGlobalChatVisible()));
        }

        jedis.hmset("data:players:" + player.getUniqueId().toString(), globalInfo);
        getJedisPool().returnResource(jedis);
        jedis.close();

    }

    public void saveServerData(){
        final Jedis jedis = getJedisPool().getResource();
        Map<String, String> serverStatus = new HashMap<>();
        serverStatus.put("online", String.valueOf(Bukkit.getOnlinePlayers().size()));
        serverStatus.put("max", String.valueOf(Bukkit.getMaxPlayers()));
        serverStatus.put("whitelist", String.valueOf(Bukkit.hasWhitelist()));

        serverStatus.put("tps0", String.valueOf(Bukkit.spigot().getTPS()[0]));
        serverStatus.put("tps1", String.valueOf(Bukkit.spigot().getTPS()[1]));
        serverStatus.put("tps2", String.valueOf(Bukkit.spigot().getTPS()[2]));

        jedis.hmset("data:servers:status:" + serverType, serverStatus);

        getJedisPool().returnResource(jedis);
        jedis.close();

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

    private void setupJedis(){
        try {
            this.pool = new JedisPool(this.getConfig().getString("redis-server"));
            this.publisher = new DataPublisher(this);
            this.subscriber = new DataSubscriber(this);
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

    public String getServerType(){ return serverType; }

    public void toggleTab(){ useTab = !useTab;}

    public boolean getToggleTab(){ return useTab;}

}