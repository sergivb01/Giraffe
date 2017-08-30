package net.veilmc.dataapi.listeners;

import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.ConstructVersion;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.DataAPI;
import org.apache.commons.collections4.map.HashedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Random;

import static org.bukkit.Bukkit.getScheduler;

public class LobbyTabListener implements Listener {
    //TODO: Update stuff
    private DataAPI plugin;
    private IConstructLibrary construct;

    public LobbyTabListener(DataAPI plugin) {
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
        }, 5 * 20L, 5 * 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.getToggleTab()){
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
        if(!this.plugin.getToggleTab()) return;

        TaskUtil.runTaskNextTick(() -> {
            this.construct.removeTabList(event.getPlayer());
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.getToggleTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    private String c(String str){
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private void initialUpdate(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        Map<String, String> kits = new HashedMap<>();
        Map<String, String> hcf = new HashedMap<>();
        Map<String, String> lite = new HashedMap<>();
        Map<String, String> profile = new HashedMap<>();
        kits.putAll(this.plugin.getJedis().hgetAll("data:servers:status:kits"));
        hcf.putAll(this.plugin.getJedis().hgetAll("data:servers:status:hcf"));
        lite.putAll(this.plugin.getJedis().hgetAll("data:servers:status:lite"));
        profile.putAll(this.plugin.getJedis().hgetAll("data:players:" + player.getUniqueId().toString()));
        //this.plugin.getJedisPool().returnResource(this.plugin.getJedis());
        //this.plugin.getJedis().close();

        //Start first row
        //tabPlayer.setPosition(1, c(""));
        //tabPlayer.setPosition(2, c(""));
        //tabPlayer.setPosition(3, c(""));
        tabPlayer.setPosition(4, c("&eStore"));
        tabPlayer.setPosition(5, c("&7store.veilhcf.us"));
        //tabPlayer.setPosition(6, c(""));
        //tabPlayer.setPosition(7, c(""));
        //tabPlayer.setPosition(8, c(""));
        tabPlayer.setPosition(9, c("&eHardcore Kits"));
        tabPlayer.setPosition(10, c("&7Online: &a" + kits.get("online") + "/" + kits.get("max")));
        tabPlayer.setPosition(11, c("&7Kills: &a" + (profile.getOrDefault("kits_kills", "0"))));
        tabPlayer.setPosition(12, c("&7Deaths: &a" + (profile.getOrDefault("kits_deaths", "0"))));
        tabPlayer.setPosition(13, c(""));
        tabPlayer.setPosition(14, c(profile.containsKey("kits_faction_name") ? "&eFaction Statics" : " "));
        //tabPlayer.setPosition(15, c(""));
        tabPlayer.setPosition(16, c((profile.containsKey("kits_faction_name") ? ("&7Name: &a" +  profile.get("kits_faction_name")) : " ")));
        tabPlayer.setPosition(17, c((profile.containsKey("kits_faction_dtr") ? ("&7DTR: &a" +  profile.get("kits_faction_dtr")) : " ")));
        tabPlayer.setPosition(18, c((profile.containsKey("kits_faction_online") ? ("&7Online: &a" +  profile.get("kits_faction_online")) : " ")));
        tabPlayer.setPosition(19, c((profile.containsKey("kits_faction_balance") ? ("&7Balance: &a$" +  profile.get("kits_faction_balance")) : " ")));
        //tabPlayer.setPosition(20, c(""));
        //End first row

        //Start second row
        tabPlayer.setPosition(21, c("&6&lVeil Network"));
        tabPlayer.setPosition(22, c("&7" + this.plugin.getPlayerss() + "/1500"));
        //tabPlayer.setPosition(23, c(""));
        tabPlayer.setPosition(24, c("&eWebsite"));
        tabPlayer.setPosition(25, c("&7veilhcf.us"));
        //tabPlayer.setPosition(26, c(""));
        tabPlayer.setPosition(27, c("&eServer Statics"));
        //tabPlayer.setPosition(28, c(""));
        tabPlayer.setPosition(29, c("&eHardcore Factions"));
        tabPlayer.setPosition(30, c("&7Online: &a" + hcf.get("online") + "/" + hcf.get("max")));
        tabPlayer.setPosition(31, c("&7Kills: &a" + (profile.getOrDefault("hcf_kills", "0"))));
        tabPlayer.setPosition(32, c("&7Deaths: &a" + (profile.getOrDefault("hcf_deaths", "0"))));
        //tabPlayer.setPosition(33, c(""));
        tabPlayer.setPosition(34, c(profile.containsKey("hcf_faction_name") ? "&eFaction Statics" : " "));
        //tabPlayer.setPosition(35, c(""));
        tabPlayer.setPosition(36, c((profile.containsKey("hcf_faction_name") ? ("&7Name: &a" +  profile.get("hcf_faction_name")) : " ")));
        tabPlayer.setPosition(37, c((profile.containsKey("hcf_faction_dtr") ? ("&7DTR: &a" +  profile.get("hcf_faction_dtr")) : " ")));
        tabPlayer.setPosition(38, c((profile.containsKey("hcf_faction_online") ? ("&7Online: &a" +  profile.get("hcf_faction_online")) : " ")));
        //tabPlayer.setPosition(39, c((profile.containsKey("hcf_faction_balance") ? ("&7Balance: &a$" +  profile.get("hcf_faction_balance")) : " ")));
        //tabPlayer.setPosition(40, c(""));
        //End second row

        //Start third row
        //tabPlayer.setPosition(41, c(""));
        //tabPlayer.setPosition(42, c(""));
        //tabPlayer.setPosition(43, c(""));
        tabPlayer.setPosition(44, c("&eTeamspeak"));
        tabPlayer.setPosition(45, c("&7ts.veilmc.net"));
        //tabPlayer.setPosition(46, c(""));
        //tabPlayer.setPosition(47, c(""));
        //tabPlayer.setPosition(48, c(""));
        tabPlayer.setPosition(49, c("&eHardcore Lite"));
        tabPlayer.setPosition(50, c("&7Online: &a" + lite.get("online") + "/" + lite.get("max")));
        tabPlayer.setPosition(51, c("&7Kills: &a" + (profile.getOrDefault("lite_kills", "0"))));
        tabPlayer.setPosition(52, c("&7Deaths: &a" + (profile.getOrDefault("lite_deaths", "0"))));
        //tabPlayer.setPosition(53, c(""));
        tabPlayer.setPosition(54, c(profile.containsKey("lite_faction_name") ? "&eFaction Statics" : " "));
        //tabPlayer.setPosition(55, c(""));
        tabPlayer.setPosition(56, c((profile.containsKey("lite_faction_name") ? ("&7Name: &a" +  profile.get("lite_faction_name")) : " ")));
        tabPlayer.setPosition(57, c((profile.containsKey("lite_faction_dtr") ? ("&7DTR: &a" +  profile.get("lite_faction_dtr")) : " ")));
        tabPlayer.setPosition(58, c((profile.containsKey("lite_faction_online") ? ("&7Online: &a" +  profile.get("lite_faction_online")) : " ")));
        tabPlayer.setPosition(59, c((profile.containsKey("lite_faction_balance") ? ("&7Balance: &a$" +  profile.get("hcf_faction_balance")) : " ")));
        //tabPlayer.setPosition(60, c(""));

        if(!tabPlayer.getVersion().equals(ConstructVersion.V1_8)) return;



        tabPlayer.setPosition(70, c("&cFor optimal"));
        tabPlayer.setPosition(71, c("&cperformance"));
        tabPlayer.setPosition(72, c("&cplease use 1.7"));


    }



}