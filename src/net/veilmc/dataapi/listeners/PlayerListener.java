package net.veilmc.dataapi.listeners;

import net.veilmc.dataapi.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

public class PlayerListener implements Listener{
    private DataAPI plugin;

    public PlayerListener(final DataAPI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                plugin.saveSinglePlayerData(player);
                plugin.getLogger().info("Saved " + player.getName() + " data as he joined the game.");
            }
        }.runTaskAsynchronously(this.plugin);

        Jedis jedis = plugin.getJedisPool().getResource();
        if(jedis.exists("data:global:" + player.getUniqueId().toString())){
            Bukkit.dispatchCommand(player, jedis.get("data:global:" + player.getUniqueId()));
            jedis.del("data:global:" + player.getUniqueId());
        }
        plugin.getJedisPool().returnResource(jedis);
        jedis.close();

        if(player.hasPermission("rank.staff")){
            plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + Bukkit.getServerName() + ";" + " joined the server.");
        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        final Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                plugin.saveSinglePlayerData(player);
                plugin.getLogger().info("Saved " + player.getName() + " data as he quit the game.");
            }
        }.runTaskAsynchronously(this.plugin);

        if(player.hasPermission("rank.staff")){
            plugin.getPublisher().write("staffswitch;" + player.getName() + ";" + Bukkit.getServerName() + ";" + " left the server.");
        }
    }



}
