package me.sergivb01.giraffe.listeners;

import me.sergivb01.giraffe.utils.TaskUtil;
import net.veilmc.hcf.HCF;
import net.veilmc.hcf.faction.event.*;
import net.veilmc.hcf.faction.type.PlayerFaction;
import me.sergivb01.giraffe.Giraffe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FactionSavingListener implements Listener{
    private Giraffe plugin;

    public FactionSavingListener(Giraffe plugin){
        this.plugin = plugin;
    }

    //@EventHandler //No needed cause when faction is created, it calls PlayerJoinedFactionEvent (as leader/creator joins the faction after it's created)
    //public void onFactionCreate(FactionCreateEvent event){
        //if(!(event.getFaction() instanceof PlayerFaction)) return;
        //this.plugin.saveFaction((PlayerFaction)event.getFaction());
        //this.plugin.getLogger().info("Saving faction " + event.getFaction().getName() + " because it was created.");
    //}

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(event.getEntity().getKiller() != null){
            TaskUtil.runTaskAsyncNextTick(()-> this.plugin.saveSinglePlayerData(event.getEntity().getKiller(), true, false));
        }
        if(event.getEntity() != null){
            TaskUtil.runTaskAsyncNextTick(()-> this.plugin.saveSinglePlayerData(event.getEntity(), true, false));
        }
    }

    @EventHandler
    public void onPlayerJoinFaction(PlayerJoinedFactionEvent event){
        this.plugin.saveFaction(event.getFaction());
        this.plugin.getLogger().info("Saving faction " + event.getFaction().getName() + " because " + event.getPlayer().get().getName() + " joined the faction.");
    }

    @EventHandler
    public void onPlayerLeftFaction(PlayerLeaveFactionEvent event){
        if(!(event.getFaction() instanceof PlayerFaction)) return;
        this.plugin.saveFaction((PlayerFaction) event.getFaction());
        this.plugin.getLogger().info("Saving faction " + event.getFaction().getName() + " because " + event.getPlayer().get().getName() + " left the faction.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        if(getPlayerFaction(event.getPlayer()) != null){
            this.plugin.saveFaction(getPlayerFaction(event.getPlayer()));
            this.plugin.getLogger().info("Saving faction " + getPlayerFaction(event.getPlayer()).getName() + " as " + event.getPlayer().getName() + " joined the game.");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(getPlayerFaction(event.getPlayer()) != null){
            this.plugin.saveFaction(getPlayerFaction(event.getPlayer()));
            this.plugin.getLogger().info("Saving faction " + getPlayerFaction(event.getPlayer()).getName() + " as " + event.getPlayer().getName() + " left the game.");
        }
    }

    @EventHandler
    public void onFactionRename(FactionRenameEvent event){
        if(!(event.getFaction() instanceof PlayerFaction)) return;
        this.plugin.saveFaction((PlayerFaction) event.getFaction());
        this.plugin.getLogger().info("Saving faction " + event.getFaction().getName() + " as it was renamed.");
    }

    @EventHandler
    public void onRelationCreate(FactionRelationCreateEvent event){
        this.plugin.saveFaction(event.getSenderFaction());
        this.plugin.getLogger().info("Saving faction " + event.getSenderFaction().getName() + " as it created a relation with " + event.getTargetFaction().getName() + ".");
        this.plugin.saveFaction(event.getTargetFaction());
        this.plugin.getLogger().info("Saving faction " + event.getTargetFaction().getName() + " as it created a relation with " + event.getSenderFaction().getName() + ".");
    }

    @EventHandler
    public void onRelationRemove(FactionRelationRemoveEvent event){

        this.plugin.saveFaction(event.getSenderFaction());
        this.plugin.getLogger().info("Saving faction " + event.getSenderFaction().getName() + " as it removed a relation with " + event.getTargetFaction().getName() + ".");
        this.plugin.saveFaction(event.getTargetFaction());
        this.plugin.getLogger().info("Saving faction " + event.getTargetFaction().getName() + " as it removed a relation with " + event.getSenderFaction().getName() + ".");
    }


    private PlayerFaction getPlayerFaction(Player player){
        return HCF.getInstance().getFactionManager().getPlayerFaction(player.getUniqueId());
    }

}
