package net.veilmc.dataapi.listeners;

import net.veilmc.dataapi.events.PlayerMoveFullBlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener{

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            Bukkit.getPluginManager().callEvent(new PlayerMoveFullBlockEvent(player, from, to, event));
        }

    }
}