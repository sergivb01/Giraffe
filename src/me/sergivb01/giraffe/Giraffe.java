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

import java.io.File;
import java.lang.management.ManagementFactory;
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
        instance = this;
        //Configuration stuff
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
            this.getServer().getPluginManager().registerEvents(new FactionSavingListener(this), this);
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
            getLogger().info("HCF MODEEEEEEEEEEEEEEEEE (" + serverType + ")");
        }

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            saveServerData(true);
        }, 10 * 20L, 10 * 20L);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                TaskUtil.runTaskNextTick(()-> saveSinglePlayerData(next, true, false));
                Collections.rotate(playerToSave, -1);
            }
        }, 5 * 20L, 5 * 20L);

        for(Player target : Bukkit.getOnlinePlayers()){
            Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(target, "ltd_es_una_puta"));
        }

        saveServerData(true);
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


}