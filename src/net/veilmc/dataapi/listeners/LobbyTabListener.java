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

public class LobbyTabListener implements Listener
{
    private DataAPI plugin;
    private IConstructLibrary construct;

    public LobbyTabListener(final DataAPI plugin) {
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
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
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (!this.plugin.getToggleTab()) {
            player.sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }
        TaskUtil.runTaskNextTick(() -> {
            if (!this.construct.hasTabList(player)) {
                this.construct.createTabList(player);
            }
            this.initialUpdate(player);
        });
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        if (!this.plugin.getToggleTab()) {
            return;
        }
        TaskUtil.runTaskNextTick(() -> this.construct.removeTabList(event.getPlayer()));
    }

    @EventHandler
    public void onKick(final PlayerKickEvent event) {
        if (!this.plugin.getToggleTab()) {
            return;
        }
        this.construct.removeTabList(event.getPlayer());
    }

    private String c(final String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private void initialUpdate(final Player player) {
        final IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        final Map<String, String> kits = (Map<String, String>)new HashedMap();
        final Map<String, String> hcf = (Map<String, String>)new HashedMap();
        final Map<String, String> lite = (Map<String, String>)new HashedMap();
        final Map<String, String> profile = (Map<String, String>)new HashedMap();
        kits.putAll(this.plugin.getJedis().hgetAll("data:servers:status:kits"));
        hcf.putAll(this.plugin.getJedis().hgetAll("data:servers:status:hcf"));
        lite.putAll(this.plugin.getJedis().hgetAll("data:servers:status:lite"));
        profile.putAll(this.plugin.getJedis().hgetAll("data:players:" + player.getUniqueId().toString()));
        this.plugin.getJedisPool().returnResource(this.plugin.getJedis());
        tabPlayer.setPosition(4, this.c("&eStore"));
        tabPlayer.setPosition(5, this.c("&7store.veilhcf.us"));
        tabPlayer.setPosition(9, this.c("&eHardcore Kits"));
        tabPlayer.setPosition(10, this.c("&7Online: &a" + kits.get("online") + "/" + kits.get("max")));
        tabPlayer.setPosition(11, this.c("&7Kills: &a" + profile.getOrDefault("kits_kills", "0")));
        tabPlayer.setPosition(12, this.c("&7Deaths: &a" + profile.getOrDefault("kits_deaths", "0")));
        tabPlayer.setPosition(13, this.c(""));
        tabPlayer.setPosition(14, this.c(profile.containsKey("kits_faction_name") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(16, this.c(profile.containsKey("kits_faction_name") ? ("&7Name: &a" + profile.get("kits_faction_name")) : " "));
        tabPlayer.setPosition(17, this.c(profile.containsKey("kits_faction_dtr") ? ("&7DTR: &a" + profile.get("kits_faction_dtr")) : " "));
        tabPlayer.setPosition(18, this.c(profile.containsKey("kits_faction_online") ? ("&7Online: &a" + profile.get("kits_faction_online")) : " "));
        tabPlayer.setPosition(19, this.c(profile.containsKey("kits_faction_balance") ? ("&7Balance: &a$" + profile.get("kits_faction_balance")) : " "));
        tabPlayer.setPosition(21, this.c("&6&lVeil Network"));
        tabPlayer.setPosition(22, this.c("&7" + this.plugin.getPlayerss() + "/1500"));
        tabPlayer.setPosition(24, this.c("&eWebsite"));
        tabPlayer.setPosition(25, this.c("&7veilhcf.us"));
        tabPlayer.setPosition(27, this.c("&eServer Statics"));
        tabPlayer.setPosition(29, this.c("&eHardcore Factions"));
        tabPlayer.setPosition(30, this.c("&7Online: &a" + hcf.get("online") + "/" + hcf.get("max")));
        tabPlayer.setPosition(31, this.c("&7Kills: &a" + profile.getOrDefault("hcf_kills", "0")));
        tabPlayer.setPosition(32, this.c("&7Deaths: &a" + profile.getOrDefault("hcf_deaths", "0")));
        tabPlayer.setPosition(34, this.c(profile.containsKey("hcf_faction_name") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(36, this.c(profile.containsKey("hcf_faction_name") ? ("&7Name: &a" + profile.get("hcf_faction_name")) : " "));
        tabPlayer.setPosition(37, this.c(profile.containsKey("hcf_faction_dtr") ? ("&7DTR: &a" + profile.get("hcf_faction_dtr")) : " "));
        tabPlayer.setPosition(38, this.c(profile.containsKey("hcf_faction_online") ? ("&7Online: &a" + profile.get("hcf_faction_online")) : " "));
        tabPlayer.setPosition(44, this.c("&eTeamspeak"));
        tabPlayer.setPosition(45, this.c("&7ts.veilmc.net"));
        tabPlayer.setPosition(49, this.c("&eHardcore Lite"));
        tabPlayer.setPosition(50, this.c("&7Online: &a" + lite.get("online") + "/" + lite.get("max")));
        tabPlayer.setPosition(51, this.c("&7Kills: &a" + profile.getOrDefault("lite_kills", "0")));
        tabPlayer.setPosition(52, this.c("&7Deaths: &a" + profile.getOrDefault("lite_deaths", "0")));
        tabPlayer.setPosition(54, this.c(profile.containsKey("lite_faction_name") ? "&eFaction Statics" : " "));
        tabPlayer.setPosition(56, this.c(profile.containsKey("lite_faction_name") ? ("&7Name: &a" + profile.get("lite_faction_name")) : " "));
        tabPlayer.setPosition(57, this.c(profile.containsKey("lite_faction_dtr") ? ("&7DTR: &a" + profile.get("lite_faction_dtr")) : " "));
        tabPlayer.setPosition(58, this.c(profile.containsKey("lite_faction_online") ? ("&7Online: &a" + profile.get("lite_faction_online")) : " "));
        tabPlayer.setPosition(59, this.c(profile.containsKey("lite_faction_balance") ? ("&7Balance: &a$" + profile.get("hcf_faction_balance")) : " "));

        if (!tabPlayer.getVersion().equals((Object)ConstructVersion.V1_8)) {
            return;
        }

        tabPlayer.setPosition(70, this.c("&cFor optimal"));
        tabPlayer.setPosition(71, this.c("&cperformance"));
        tabPlayer.setPosition(72, this.c("&cplease use 1.7"));
    }
}
