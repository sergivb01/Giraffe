package net.veilmc.dataapi.listeners;


import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LobbyTabListener implements Listener {
    //TODO: Update stuff
    private DataAPI plugin;
    private IConstructLibrary construct;

    public LobbyTabListener(DataAPI plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.getToggleTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }

        TaskUtil.runTaskNextTick(() -> {
            if(this.construct.hasTabList(player)){
                if(!this.construct.hasTabList(player)) this.construct.createTabList(event.getPlayer());
                this.initialUpdate(event.getPlayer());
            }
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

    private void initialUpdate(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);

        //Start first row
        tabPlayer.setPosition(1, "");
        tabPlayer.setPosition(2, "");
        tabPlayer.setPosition(3, "");
        tabPlayer.setPosition(4, "");
        tabPlayer.setPosition(5, "");
        tabPlayer.setPosition(6, "");
        tabPlayer.setPosition(7, "");
        tabPlayer.setPosition(8, "");
        tabPlayer.setPosition(9, "");
        tabPlayer.setPosition(10, "");
        tabPlayer.setPosition(11, "");
        tabPlayer.setPosition(12, "");
        tabPlayer.setPosition(13, "");
        tabPlayer.setPosition(14, "");
        tabPlayer.setPosition(15, "");
        tabPlayer.setPosition(16, "");
        tabPlayer.setPosition(17, "");
        tabPlayer.setPosition(18, "");
        tabPlayer.setPosition(19, "");
        tabPlayer.setPosition(20, "");
        //End first row



    }



}
