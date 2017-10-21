package me.sergivb01.giraffe.listeners;

import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.TaskUtil;
import net.veilmc.practice.game.cache.Cache;
import net.veilmc.practice.game.party.event.*;
import net.veilmc.practice.game.queue.event.PlayerEnterQueueEvent;
import net.veilmc.practice.game.queue.event.PlayerExitQueueEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PracticeTabListener implements Listener {
    private Giraffe plugin;
    private IConstructLibrary construct;

    public PracticeTabListener(Giraffe plugin) {
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
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onQueueJoin(PlayerEnterQueueEvent event){

    }

    @EventHandler
    public void onQueueQuit(PlayerExitQueueEvent event){

    }

    @EventHandler
    public void onPartyCreate(PlayerCreatePartyEvent event){

    }

    @EventHandler
    public void onPartyDisband(PlayerDisbandPartyEvent event){

    }

    @EventHandler
    public void onPartyJoin(PlayerJoinPartyEvent event){

    }

    @EventHandler
    public void onPartyLeave(PlayerLeavePartyEvent event){

    }

    @EventHandler
    public void onPartyKicked(PlayerKickPlayerPartyEvent event){

    }


    private void updateOnline(IConstructPlayer tabPlayer){
        tabPlayer.setPosition(0, "");
    }

    private void initialUpdate(Player player) {
        if (!this.construct.hasTabList(player)) {
            return;
        }
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);

        for(int i = 1; i <= 60; i++){
            tabPlayer.setPosition(i, "Position #" + i);
        }

    }


    private static int getPlayingAmout(){ return Cache.playingAmount; }

    private static int getQueueingAmout(){ return Cache.queueingAmount; }

    public static int getSpectatingAmout(){ return Cache.spectatingAmount; }

}

