package me.sergivb01.giraffe.listeners;

import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.TaskUtil;
import net.veilmc.base.BasePlugin;
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

        TaskUtil.runTaskAsyncNextTick(()->{
            plugin.addToList(player);
            plugin.saveSinglePlayerData(player, true, true);
            plugin.getLogger().info("Saved " + player.getName() + " data as he joined the game.");
            //TabTitleManager.setHeaderAndFooter(player, "§6§lVeilMC Network", "§eveilmc.net §7| §ets.veilmc.net"); //TODO: Need to test if works (1.8)
            if(player.hasPermission("rank.staff")){
                plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + this.plugin.getServerName() + ";" + "joined the server.");
            }
        }, 3 * 20L);

        if(!plugin.getPlayerToSave().contains(player)) plugin.getPlayerToSave().add(player); //Player needs to be added to save-data list :p

    }

    @EventHandler
    public void onUserQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        TaskUtil.runTaskAsyncNextTick(()->{
            plugin.saveSinglePlayerData(player, false, true);
            plugin.getLogger().info("Saved " + player.getName() + " data as he quit the game.");

            if(player.hasPermission("rank.staff")){ //staff notification about server switched
                plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + this.plugin.getServerName() + ";" + "left the server.");
            }
        });


        if(plugin.getPlayerToSave().contains(player)) plugin.getPlayerToSave().remove(player); //We don't need to keep player in
    }

    @EventHandler
    public void onStaffChatChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(!player.hasPermission("rank.staff")){
            return;
        }
        TaskUtil.runTaskAsyncNextTick(()->{
            if(BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId()).isInStaffChat()){//Staffchat
                plugin.getPublisher().write("staffchat;" + player.getName() + ";" + this.plugin.getServerName() + ";" + event.getMessage().replace(";", ":"));
                event.setCancelled(true);
            }
        });
    }


}
