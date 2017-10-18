package me.sergivb01.giraffe.listeners;

import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.TabUtils;
import me.sergivb01.giraffe.utils.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class UHCTabListener implements Listener {
    private Giraffe plugin;
    private IConstructLibrary construct;

    public UHCTabListener(Giraffe plugin) {
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.isUseTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }

        TaskUtil.runTaskAsyncNextTick(() -> {
            if(!this.construct.hasTabList(player)) {
                this.construct.createTabList(player);
            }
            this.initialUpdate(player);

            for(Player p : Bukkit.getOnlinePlayers()){
                if(this.construct.hasTabList(p)) {
                    //updateOnlinePlayers(p);
                }
            }
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());


        TaskUtil.runTaskAsyncNextTick(() -> {
            for(Player p : Bukkit.getOnlinePlayers()){
                //updateOnlinePlayers(p);
            }
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }


    private void initialUpdate(Player player) {
        if (!this.construct.hasTabList(player)) {
            return;
        }
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);

        //Start first row
        tabPlayer.setPosition(1, TabUtils.translate(player, "&ePlayer Info"));

    }



}
