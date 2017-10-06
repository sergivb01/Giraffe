package net.veilmc.dataapi.listeners;

import com.customhcf.base.BasePlugin;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataListener implements Listener{
    private Giraffe plugin;

    public PlayerDataListener(final Giraffe plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onUserJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        TaskUtil.runTaskNextTick(()->{
            plugin.addToList(player);
            plugin.saveSinglePlayerData(player, true, true);
            plugin.getLogger().info("Saved " + player.getName() + " data as he joined the game.");
        });

        if(!plugin.getPlayerToSave().contains(player)) plugin.getPlayerToSave().add(player); //Player needs to be added to save-data list :p

    }

    @EventHandler
    public void onUserQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        TaskUtil.runTaskNextTick(()->{
            plugin.saveSinglePlayerData(player, false, true);
            plugin.getLogger().info("Saved " + player.getName() + " data as he quit the game.");
        });

        if(player.hasPermission("rank.staff")){ //staff notification about server switched
            plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + Bukkit.getServerName() + ";" + "left the server.");
        }

        if(plugin.getPlayerToSave().contains(player)) plugin.getPlayerToSave().remove(player); //We don't need to keep player in
    }

    @EventHandler
    public void onStaffJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(player.hasPermission("rank.staff")){ //staff notification about server switched
            TaskUtil.runTaskNextTick(()->{
                plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + Bukkit.getServerName() + ";" + "joined the server.");
            });
        }

    }

    @EventHandler
    public void onStaffChatChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId()).isInStaffChat()){//Staffchat
            plugin.getPublisher().write("staffchat;" + player.getName() + ";" + Bukkit.getServerName() + ";" + event.getMessage().replace(";", ":"));
            event.setCancelled(true);
        }
    }


}
