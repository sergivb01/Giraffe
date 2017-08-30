package net.veilmc.dataapi.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveFullBlockEvent extends Event {
    private static HandlerList handlerList;
    private final Player player;
    private final Location from;
    private final Location to;
    private final PlayerMoveEvent event;

    public PlayerMoveFullBlockEvent(Player player, Location from, Location to, PlayerMoveEvent event){
        handlerList = new HandlerList();
        this.player = player;
        this.from = from;
        this.to = to;
        this.event = event;
    }

    @Override
    public HandlerList getHandlers(){
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    /**
     * Returns the Player of who moved
     * @see Player
     * @return
     */
    public Player getPlayer(){
        return player;
    }

    /**
     * Returns the location the Player had moved from.
     * @see Location
     * @return
     */
    public Location getFrom(){
        return from;
    }

    /**
     * Returns the location the Player is moving to.
     * @see Location
     * @return
     */
    public Location getTo(){
        return to;
    }

    @Override
    public String getEventName(){
        return "PlayerMoveFullBlock";
    }

    /**
     * Returns the parent PlayerMoveEvent
     * @see PlayerMoveEvent
     * @return
     */
    public PlayerMoveEvent getParentEvent(){
        return event;
    }


}