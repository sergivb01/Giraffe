package me.sergivb01.giraffe.listeners;

import lombok.Getter;
import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.TaskUtil;
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

public class LobbyTabListener implements Listener {
    private Giraffe plugin;
    private IConstructLibrary construct;
    @Getter private Map<String, String> kitsServer = new HashedMap<>();
    @Getter private Map<String, String> hcfServer = new HashedMap<>();
    @Getter private Map<String, String> liteServer = new HashedMap<>();

    public LobbyTabListener(Giraffe plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = Construct.getLibrary();

        new Thread(()-> TaskUtil.runTaskTimerAsync(()->{
            if(Bukkit.getOnlinePlayers().size() == 0) return;
            Player p1 = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];
            this.plugin.getCount(p1, "ALL");
            updateServers();
            for(Player player : Bukkit.getOnlinePlayers()){
                if(this.construct.hasTabList(player)){
                    this.initialUpdate(player);
                }
            }
        }, 10L, 20L)).start();
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

        TaskUtil.runTaskNextTick(() -> this.construct.removeTabList(event.getPlayer()));
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
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
            jedis = plugin.getPool().getResource();
            toReturn =  jedis.hgetAll("data:players:" + gamemode + ":" + player.getUniqueId().toString());
            plugin.getPool().returnResource(jedis);
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
        TaskUtil.runTaskAsyncNextTick(()->{

            Map<String, String> kits = getPlayerData(player, "kits");
            if(kits.get("faction_name") != null) kits.putAll(getFactionData(kits.get("faction_name"), "kits"));

            Map<String, String> hcf = getPlayerData(player, "hcf");
            if(hcf.get("faction_name") != null) hcf.putAll(getFactionData(hcf.get("faction_name"), "hcf"));

            Map<String, String> lite = getPlayerData(player, "lite");
            if(lite.get("faction_name") != null) lite.putAll(getFactionData(lite.get("faction_name"), "lite"));


            //Start first row
            tabPlayer.setPosition(4, c("&eStore"));
            tabPlayer.setPosition(5, c("&7store.veilhcf.us"));
            tabPlayer.setPosition(9, c("&eHardcore Kits"));
            tabPlayer.setPosition(10, c("&7Online: &a" + kitsServer.get("online") + "/" + kitsServer.get("max")));
            tabPlayer.setPosition(11, c("&7Kills: &a" + (kits.getOrDefault("kills", "0"))));
            tabPlayer.setPosition(12, c("&7Deaths: &a" + (kits.getOrDefault("deaths", "0"))));
            tabPlayer.setPosition(14, c(existIn(kits) ? "&eFaction Statics" : " "));
            tabPlayer.setPosition(16, c(existIn(kits) ? ("&7Name: &a" +  kits.get("faction_name")) : " "));
            tabPlayer.setPosition(17, c(existIn(kits) ? ("&7DTR: &a" +  kits.get("faction_dtr")) : " "));
            tabPlayer.setPosition(18, c(existIn(kits) ? ("&7Online: &a" +  kits.get("faction_online")) : " "));
            tabPlayer.setPosition(19, c(existIn(kits) ? ("&7Balance: &a$" +  kits.get("faction_balance")) : " "));
            //End first row

            //Start second row
            tabPlayer.setPosition(21, c("&6&lVeil Network"));
            tabPlayer.setPosition(22, c("&7" + this.plugin.getPlayerss() + "/1500"));
            tabPlayer.setPosition(24, c("&eWebsite"));
            tabPlayer.setPosition(25, c("&7veilhcf.us"));
            tabPlayer.setPosition(27, c("&eServer Statics"));
            tabPlayer.setPosition(29, c("&eHardcore Factions"));
            tabPlayer.setPosition(30, c("&7Online: &a" + hcfServer.get("online") + "/" + hcfServer.get("max")));
            tabPlayer.setPosition(31, c("&7Kills: &a" + (hcf.getOrDefault("kills", "0"))));
            tabPlayer.setPosition(32, c("&7Deaths: &a" + (hcf.getOrDefault("deaths", "0"))));
            tabPlayer.setPosition(34, c(existIn(hcf) ? "&eFaction Statics" : " "));
            tabPlayer.setPosition(36, c(existIn(hcf) ? ("&7Name: &a" +  hcf.get("faction_name")) : " "));
            tabPlayer.setPosition(37, c(existIn(hcf) ? ("&7DTR: &a" +  hcf.get("faction_dtr")) : " "));
            tabPlayer.setPosition(38, c(existIn(hcf) ? ("&7Online: &a" +  hcf.get("faction_online")) : " "));
            tabPlayer.setPosition(39, c(existIn(hcf) ? ("&7Balance: &a$" +  hcf.get("faction_balance")) : " "));
            //End second row

            //Start third row
            tabPlayer.setPosition(44, c("&eTeamspeak"));
            tabPlayer.setPosition(45, c("&7ts.veilmc.net"));
            tabPlayer.setPosition(49, c("&eHardcore Lite"));
            tabPlayer.setPosition(50, c("&7Online: &a" + liteServer.get("online") + "/" + liteServer.get("max")));
            tabPlayer.setPosition(51, c("&7Kills: &a" + (lite.getOrDefault("kills", "0"))));
            tabPlayer.setPosition(52, c("&7Deaths: &a" + (lite.getOrDefault("deaths", "0"))));
            tabPlayer.setPosition(54, c((existIn(lite) ? "&eFaction Statics" : " ")));
            tabPlayer.setPosition(56, c(existIn(lite) ? ("&7Name: &a" +  lite.get("faction_name")) : " "));
            tabPlayer.setPosition(57, c((existIn(lite) || lite.isEmpty()) ? ("&7DTR: &a" +  lite.get("faction_dtr")) : " "));
            tabPlayer.setPosition(58, c((existIn(lite) || lite.isEmpty()) ? ("&7Online: &a" +  lite.get("faction_online")) : " "));
            tabPlayer.setPosition(59, c((existIn(lite) || lite.isEmpty()) ? ("&7Balance: &a$" +  lite.get("faction_balance")) : " "));
        });

    }

    private String c(String str){
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private boolean existIn(Map<String, String> map) {
        return map.get("faction_name") != null && !map.get("faction_name").equals("No Faction");
    }

}