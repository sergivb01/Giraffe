package net.veilmc.dataapi;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.user.BaseUser;
import com.customhcf.hcf.HCF;
import com.customhcf.hcf.deathban.Deathban;
import com.customhcf.hcf.faction.type.PlayerFaction;
import com.customhcf.hcf.user.FactionUser;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.construct.util.TaskUtil;
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
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
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
        }, 10 * 20L, 10 * 20L);

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { //Save data of single player every 15 seconds.
            if (!playerToSave.isEmpty()) {
                Player next = playerToSave.get(0);
                TaskUtil.runTaskNextTick(()-> saveSinglePlayerData(next, true, false));
                Collections.rotate(playerToSave, -1);
            }
        }, 5 * 20L, 5 * 20L);

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

            globalInfo.put("nickname", player.getDisplayName());
            globalInfo.put("address", player.getAddress().getHostName());
            globalInfo.put("online", (online ? "true" : "false"));
            globalInfo.put("lastServer", serverType);
            globalInfo.put("nickname", player.getDisplayName());

            if(isNotRegular) {
                globalInfo.put(cleanServer + "last_connection", String.valueOf(System.currentTimeMillis()));
            }
            globalInfo.put(cleanServer + "rank", PermissionsEx.getUser(player).getGroups()[0].getName()); //TODO: Add permission here...

            if(!serverType.equalsIgnoreCase("lobby")) {
                final HCF hcf = HCF.getInstance();

                final FactionUser factionUser = hcf.getUserManager().getUser(player.getUniqueId());
                globalInfo.put(cleanServer + "playtime", String.valueOf(BasePlugin.getPlugin().getPlayTimeManager().getTotalPlayTime(player.getUniqueId())));
                globalInfo.put(cleanServer + "kills", String.valueOf(factionUser.getKills()));
                globalInfo.put(cleanServer + "deaths", String.valueOf(factionUser.getDeaths()));
                globalInfo.put(cleanServer + "diamonds", String.valueOf(factionUser.getDiamondsMined()));
                globalInfo.put(cleanServer + "lives", String.valueOf(hcf.getDeathbanManager().getLives(player.getUniqueId())));
                globalInfo.put(cleanServer + "balance", String.valueOf(hcf.getEconomyManager().getBalance(player.getUniqueId())));

                final Deathban deathban = hcf.getUserManager().getUser(player.getUniqueId()).getDeathban();
                globalInfo.put(cleanServer + "deathban_expires", (deathban == null ? "Not deathbanned" : String.valueOf(deathban.getExpiryMillis())));
                globalInfo.put(cleanServer + "deathban_reason", (deathban == null ? "Not deathbanned" : deathban.getReason()));
                globalInfo.put(cleanServer + "deathban_creation", (deathban == null ? "Not deathbanned" : deathban.getReason()));

                final PlayerFaction playerFaction = hcf.getFactionManager().getPlayerFaction(player.getUniqueId());
                globalInfo.put(cleanServer + "faction_name", (playerFaction == null ? "No Faction" : playerFaction.getName()));
                globalInfo.put(cleanServer + "faction_role", (playerFaction == null ? "No Faction" : playerFaction.getMember(player.getUniqueId()).getRole().getName()));
                globalInfo.put(cleanServer + "faction_online", (playerFaction == null ? "No Faction" : playerFaction.getOnlineMembers().size() + "/" + playerFaction.getMembers().size()));
                globalInfo.put(cleanServer + "faction_dtr", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getDeathsUntilRaidable())));
                globalInfo.put(cleanServer + "faction_dtrregen", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getRemainingRegenerationTime())));
                globalInfo.put(cleanServer + "faction_balance", (playerFaction == null ? "No Faction" : String.valueOf(playerFaction.getBalance())));

                ArrayList<String> alliesNames = new ArrayList<>();
                if(playerFaction != null){
                    for(PlayerFaction playerFaction1 : playerFaction.getAlliedFactions())
                        alliesNames.add(playerFaction1.getName());
                }
                globalInfo.put(cleanServer + "faction_allies", (alliesNames.size() <= 0 ? "No allies" : alliesNames.toString().replace("[", "").replace("]", "")));

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

        }).start();

    }

    private void saveServerData(boolean up){
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