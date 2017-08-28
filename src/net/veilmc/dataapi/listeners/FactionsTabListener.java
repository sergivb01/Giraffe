package net.veilmc.dataapi.listeners;

import me.joeleoli.construct.ConstructLibrary;
import me.joeleoli.construct.api.IConstruct;
import me.joeleoli.construct.util.TaskUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class FactionsTabListener implements Listener {

    private IConstruct construct;

    public FactionsTabListener(Plugin plugin) {
        // Define construct before registering event listeners
        this.construct = ConstructLibrary.getApi();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        TaskUtil.runTaskNextTick(() -> {
            this.construct.createTabList(event.getPlayer());
            this.initialUpdate(event.getPlayer());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        ConstructLibrary.getApi().removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!this.construct.hasTabList(player)) {
            return;
        }

        this.construct.setPosition(player, 2, player.getLocation().getX() + ", " + player.getLocation().getZ());
    }

    public void initialUpdate(Player player) {
        if (!this.construct.hasTabList(player)) {
            return;
        }

        this.construct.setPosition(player, 21, ChatColor.GOLD + "" + ChatColor.BOLD + "VeilMC");
        this.construct.setPosition(player, 2, ChatColor.YELLOW + "Location: ");
    }

}
