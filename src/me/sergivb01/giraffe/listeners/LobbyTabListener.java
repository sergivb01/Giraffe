package me.sergivb01.giraffe.listeners;

import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.joeleoli.construct.util.TaskUtil;
import me.sergivb01.giraffe.Giraffe;
import org.apache.commons.collections4.map.HashedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getScheduler;

public class LobbyTabListener implements Listener {
    private Giraffe plugin;
    private IConstructLibrary construct;
    private Map<String, String> kitsServer = new HashedMap<>();
    private Map<String, String> hcfServer = new HashedMap<>();
    private Map<String, String> liteServer = new HashedMap<>();

    public LobbyTabListener(Giraffe plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if(Bukkit.getOnlinePlayers().size() == 0) return;
            Player p1 = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
            this.plugin.getCount(p1, "ALL");
            for(Player player : Bukkit.getOnlinePlayers()){
                if(this.construct.hasTabList(player)){
                    this.initialUpdate(player);
                }
            }
        }, 20L, 5 * 20L);
        getScheduler().runTaskTimer(plugin, this::updateServers, 20L, 3 * 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.isUseTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }

        TaskUtil.runTaskNextTick(() -> {
            if(!this.construct.hasTabList(player)) {
                this.construct.createTabList(player);
            }
            this.initialUpdate(player);
        });


    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.isUseTab()) return;

        TaskUtil.runTaskNextTick(() -> {
            this.construct.removeTabList(event.getPlayer());
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    private String c(String str){
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private void updateServers(){
        Jedis jedis = null;
        try {
            jedis = this.plugin.getPool().getResource();
            kitsServer.putAll(jedis.hgetAll("data:servers:status:kits"));
            hcfServer.putAll(jedis.hgetAll("data:servers:status:hcf"));
            liteServer.putAll(jedis.hgetAll("data:servers:status:lite"));
            this.plugin.getPool().returnResource(jedis);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    private Map<String, String> getPlayerData(Player player, String gamemode){
        Map<String, String> toReturn;
        Jedis jedis = null;
        try {
            jedis = this.plugin.getPool().getResource();
            toReturn =  jedis.hgetAll("data:players:" + gamemode + ":" + player.getUniqueId().toString());
            this.plugin.getPool().returnResource(jedis);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return toReturn;
    }

    private Map<String, String> getFactionData(String faction, String gamemode){
        Map<String, String> toReturn;
        Jedis jedis = null;
        try {
            jedis = this.plugin.getPool().getResource();
            toReturn =  jedis.hgetAll("data:factionlist:" + gamemode + ":" + faction);
            this.plugin.getPool().returnResource(jedis);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return toReturn;
    }


    private void initialUpdate(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        Map<String, String> kits = getPlayerData(player, "kits");
        if(kits.get("faction_name") != null) kits.putAll(getFactionData(kits.get("faction_name"), "kits"));

        Map<String, String> hcf = getPlayerData(player, "hcf");
        if(hcf.get("faction_name") != null) hcf.putAll(getFactionData(hcf.get("faction_name"), "hcf"));

        Map<String, String> lite = getPlayerData(player, "lite");
        if(lite.get("faction_name") != null) lite.putAll(getFactionData(lite.get("faction_name"), "lite"));



        //Start first row
        tabPlayer.setPosition(1, c(""));
        tabPlayer.setPosition(2, c(""));
        tabPlayer.setPosition(3, c(""));
        tabPlayer.setPosition(4, c("&eStore"));
        tabPlayer.setPosition(5, c("&7store.veilhcf.us"));
        tabPlayer.setPosition(6, c(""));
        tabPlayer.setPosition(7, c(""));
        tabPlayer.setPosition(8, c(""));
        tabPlayer.setPosition(9, c("&eHardcore Kits"));
        tabPlayer.setPosition(10, c("&7Online: &a" + kitsServer.get("online") + "/" + kitsServer.get("max")));
        tabPlayer.setPosition(11, c("&7Kills: &a" + (kits.getOrDefault("kills", "0"))));
        tabPlayer.setPosition(12, c("&7Deaths: &a" + (kits.getOrDefault("deaths", "0"))));
        tabPlayer.setPosition(13, c(""));
        tabPlayer.setPosition(14, c(!kits.get("faction_name").equals("No Faction") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(15, c(""));
        tabPlayer.setPosition(16, c((!kits.get("faction_name").equals("No Faction") ? ("&7Name: &a" +  kits.get("faction_name")) : " ")));
        tabPlayer.setPosition(17, c((!kits.get("faction_name").equals("No Faction") ? ("&7DTR: &a" +  kits.get("faction_dtr")) : " ")));
        tabPlayer.setPosition(18, c((!kits.get("faction_name").equals("No Faction") ? ("&7Online: &a" +  kits.get("faction_online")) : " ")));
        tabPlayer.setPosition(19, c((!kits.get("faction_name").equals("No Faction") ? ("&7Balance: &a$" +  kits.get("faction_balance")) : " ")));
        tabPlayer.setPosition(20, c(""));
        //End first row

        //Start second row
        tabPlayer.setPosition(21, c("&6&lVeil Network"));
        tabPlayer.setPosition(22, c("&7" + this.plugin.getPlayerss() + "/1500"));
        tabPlayer.setPosition(23, c(""));
        tabPlayer.setPosition(24, c("&eWebsite"));
        tabPlayer.setPosition(25, c("&7veilhcf.us"));
        tabPlayer.setPosition(26, c(""));
        tabPlayer.setPosition(27, c("&eServer Statics"));
        tabPlayer.setPosition(28, c(""));
        tabPlayer.setPosition(29, c("&eHardcore Factions"));
        tabPlayer.setPosition(30, c("&7Online: &a" + hcfServer.get("online") + "/" + hcfServer.get("max")));
        tabPlayer.setPosition(31, c("&7Kills: &a" + (hcf.getOrDefault("kills", "0"))));
        tabPlayer.setPosition(32, c("&7Deaths: &a" + (hcf.getOrDefault("deaths", "0"))));
        tabPlayer.setPosition(33, c(""));
        tabPlayer.setPosition(34, c(!hcf.get("faction_name").equals("No Faction") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(35, c(""));
        tabPlayer.setPosition(36, c((!hcf.get("faction_name").equals("No Faction") ? ("&7Name: &a" +  hcf.get("faction_name")) : " ")));
        tabPlayer.setPosition(37, c((!hcf.get("faction_name").equals("No Faction") ? ("&7DTR: &a" +  hcf.get("faction_dtr")) : " ")));
        tabPlayer.setPosition(38, c((!hcf.get("faction_name").equals("No Faction") ? ("&7Online: &a" +  hcf.get("faction_online")) : " ")));
        tabPlayer.setPosition(39, c((!hcf.get("faction_name").equals("No Faction") ? ("&7Balance: &a$" +  hcf.get("faction_balance")) : " ")));
        tabPlayer.setPosition(40, c(""));
        //End second row

        //Start third row
        tabPlayer.setPosition(41, c(""));
        tabPlayer.setPosition(42, c(""));
        tabPlayer.setPosition(43, c(""));
        tabPlayer.setPosition(44, c("&eTeamspeak"));
        tabPlayer.setPosition(45, c("&7ts.veilmc.net"));
        tabPlayer.setPosition(46, c(""));
        tabPlayer.setPosition(47, c(""));
        tabPlayer.setPosition(48, c(""));
        tabPlayer.setPosition(49, c("&eHardcore Lite"));
        tabPlayer.setPosition(50, c("&7Online: &a" + liteServer.get("online") + "/" + liteServer.get("max")));
        tabPlayer.setPosition(51, c("&7Kills: &a" + (lite.getOrDefault("kills", "0"))));
        tabPlayer.setPosition(52, c("&7Deaths: &a" + (lite.getOrDefault("deaths", "0"))));
        tabPlayer.setPosition(53, c(""));
        tabPlayer.setPosition(54, c(!lite.get("faction_name").equals("No Faction") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(55, c(""));
        tabPlayer.setPosition(56, c((!lite.get("faction_name").equals("No Faction") ? ("&7Name: &a" +  lite.get("faction_name")) : " ")));
        tabPlayer.setPosition(57, c((!lite.get("faction_name").equals("No Faction") ? ("&7DTR: &a" +  lite.get("faction_dtr")) : " ")));
        tabPlayer.setPosition(58, c((!lite.get("faction_name").equals("No Faction") ? ("&7Online: &a" +  lite.get("faction_online")) : " ")));
        tabPlayer.setPosition(59, c((!lite.get("faction_name").equals("No Faction") ? ("&7Balance: &a$" +  lite.get("faction_balance")) : " ")));
        tabPlayer.setPosition(60, c(""));



    }



}