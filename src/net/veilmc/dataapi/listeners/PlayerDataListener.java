package net.veilmc.dataapi.listeners;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.user.BaseUser;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.Giraffe;
import org.apache.commons.collections4.map.HashedMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class PlayerDataListener implements Listener{
    private Giraffe plugin;

    public PlayerDataListener(final Giraffe plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onUserJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        //TODO: Improve it. Removed due lag
        new Thread(()->{
            Jedis jedis = null;
            try {
                jedis = this.plugin.getPool().getResource();
                if(jedis.exists("data:players:" + player.getUniqueId().toString())) {
                    Jedis finalJedis = jedis;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        BaseUser baseUser = BasePlugin.getPlugin().getUserManager().getUser(player.getUniqueId()); //Player data fromm base
                        Map<String, String> playerData = new HashedMap<>();
                        playerData.putAll(finalJedis.hgetAll("data:players:" + player.getUniqueId().toString()));
                        //Staff things
                        if (playerData.get("staff_modmode").equals("true") && !baseUser.isStaffUtil())
                            Bukkit.dispatchCommand(player, "mod");

                        if (playerData.get("staff_sc").equals("true") && !baseUser.isInStaffChat())
                            Bukkit.dispatchCommand(player, "sc");

                        if (playerData.get("staff_vanish").equals("true") && !baseUser.isVanished())
                            Bukkit.dispatchCommand(player, "v");

                        //Options things
                        baseUser.setGlobalChatVisible(playerData.get("options_gc").equals("true"));
                        baseUser.setGlobalChatVisible(playerData.get("options_pm").equals("true"));
                        baseUser.setGlobalChatVisible(playerData.get("options_sc").equals("true"));

                        //Notification message.
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aYour options has been &eloaded &afrom the database."));

                        plugin.saveSinglePlayerData(player, true, true); //Now save the data on database
                        plugin.getLogger().info("Saved " + player.getName() + " data as he joined the game.");

                    }, 3 * 20L);
                }else{
                    plugin.saveSinglePlayerData(player, true, true);
                    plugin.getLogger().info("Saved " + player.getName() + " data as he joined the game.");
                }

                this.plugin.getPool().returnResource(jedis);
            }finally {
                if (jedis != null) {
                    jedis.close();
                }
            }
        }).start();

        if(!plugin.getPlayerToSave().contains(player)) plugin.getPlayerToSave().add(player); //Player needs to be added to save-data list :p

    }

    @EventHandler
    public void onUserQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        TaskUtil.runTaskNextTick(()->{
            try {
                plugin.saveSinglePlayerData(player, false, true);
            }catch(IndexOutOfBoundsException | NullPointerException ex){
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error while trying to save " + ChatColor.GRAY + player.getName() + ChatColor.RED + " data! (NullPointerException or IndexOutOfBoundsException)");
            }
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
