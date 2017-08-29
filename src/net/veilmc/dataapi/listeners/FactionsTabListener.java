package net.veilmc.dataapi.listeners;


import com.customhcf.hcf.faction.event.PlayerJoinedFactionEvent;
import com.customhcf.hcf.faction.event.PlayerLeftFactionEvent;
import com.google.common.base.Optional;
import me.joeleoli.construct.ConstructLibrary;
import me.joeleoli.construct.api.ConstructVersion;
import me.joeleoli.construct.api.IConstruct;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.DataAPI;
import net.veilmc.dataapi.utils.TabUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class FactionsTabListener implements Listener {
    //TODO: Update stuff
    private DataAPI plugin;
    private IConstruct construct;

    public FactionsTabListener(DataAPI plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = ConstructLibrary.getApi();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!this.plugin.getToggleTab()) return;

        TaskUtil.runTaskNextTick(() -> {
           this.construct.createTabList(event.getPlayer());
           this.initialUpdate(event.getPlayer());
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.getToggleTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.getToggleTab()) return;
        ConstructLibrary.getApi().removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onDeathKill(PlayerDeathEvent event){
        if(!this.plugin.getToggleTab()) return;
        if(event.getEntity().getKiller() instanceof Player){
            Player player = event.getEntity().getKiller();
            if(!this.construct.hasTabList(player)) return;

            this.construct.setPosition(player, 2, TabUtils.translate(player, "&7Kills:&a %player_kills%"));
            this.construct.setPosition(player, 17, TabUtils.translate(player, "&7Balance:&a %player_bal%"));
        }
    }

    @EventHandler
    public void factionLeft(PlayerLeftFactionEvent event){
        if(!this.plugin.getToggleTab()) return;

        Player player = null;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();
        }

        if (!this.construct.hasTabList(player)) return;

        this.construct.setPosition(player, 9, TabUtils.translate(player, "&e%f_title%"));
        this.construct.setPosition(player, 10, TabUtils.translate(player, "%fhome%"));
        this.construct.setPosition(player, 11, TabUtils.translate(player, "%fonline%"));
        this.construct.setPosition(player, 12, TabUtils.translate(player, "%fbal%"));
        this.construct.setPosition(player, 13, TabUtils.translate(player, "%fdtr%"));

        this.construct.setPosition(player, 25, TabUtils.translate(player, "&e" + "%ftag%"));
        this.construct.setPosition(player, 26, TabUtils.translate(player, "%f_member_1%"));
        this.construct.setPosition(player, 27, TabUtils.translate(player, "%f_member_2%"));
        this.construct.setPosition(player, 28, TabUtils.translate(player, "%f_member_3%"));
        this.construct.setPosition(player, 29, TabUtils.translate(player, "%f_member_4%"));
        this.construct.setPosition(player, 30, TabUtils.translate(player, "%f_member_5%"));
        this.construct.setPosition(player, 31, TabUtils.translate(player, "%f_member_6%"));
        this.construct.setPosition(player, 32, TabUtils.translate(player, "%f_member_7%"));
        this.construct.setPosition(player, 33, TabUtils.translate(player, "%f_member_8%"));
        this.construct.setPosition(player, 34, TabUtils.translate(player, "%f_member_9%"));
        this.construct.setPosition(player, 35, TabUtils.translate(player, "%f_member_10%"));
        this.construct.setPosition(player, 36, TabUtils.translate(player, "%f_member_11%"));
        this.construct.setPosition(player, 37, TabUtils.translate(player, "%f_member_12%"));
        this.construct.setPosition(player, 38, TabUtils.translate(player, "%f_member_13%"));
        this.construct.setPosition(player, 39, TabUtils.translate(player, "%f_member_14%"));
        this.construct.setPosition(player, 40, TabUtils.translate(player, "%f_member_15%"));
        //End second row

        //Start third row
        this.construct.setPosition(player, 41, TabUtils.translate(player, "&eFaction List"));
        this.construct.setPosition(player, 42, TabUtils.translate(player, "%f_list_1%"));
        this.construct.setPosition(player, 43, TabUtils.translate(player, "%f_list_2%"));
        this.construct.setPosition(player, 44, TabUtils.translate(player, "%f_list_3%"));
        this.construct.setPosition(player, 45, TabUtils.translate(player, "%f_list_4%"));
        this.construct.setPosition(player, 46, TabUtils.translate(player, "%f_list_5%"));
        this.construct.setPosition(player, 47, TabUtils.translate(player, "%f_list_6%"));
        this.construct.setPosition(player, 48, TabUtils.translate(player, "%f_list_7%"));
        this.construct.setPosition(player, 49, TabUtils.translate(player, "%f_list_8%"));
        this.construct.setPosition(player, 50, TabUtils.translate(player, "%f_list_9%"));
        this.construct.setPosition(player, 51, TabUtils.translate(player, "%f_list_10%"));
        this.construct.setPosition(player, 52, TabUtils.translate(player, "%f_list_11%"));
        this.construct.setPosition(player, 53, TabUtils.translate(player, "%f_list_12%"));
        this.construct.setPosition(player, 54, TabUtils.translate(player, "%f_list_13%"));
        this.construct.setPosition(player, 55, TabUtils.translate(player, "%f_list_14%"));
        this.construct.setPosition(player, 56, TabUtils.translate(player, "%f_list_15%"));
        this.construct.setPosition(player, 57, TabUtils.translate(player, "%f_list_16%"));
        this.construct.setPosition(player, 58, TabUtils.translate(player, "%f_list_17%"));
        this.construct.setPosition(player, 59, TabUtils.translate(player, "%f_list_18%"));
        this.construct.setPosition(player, 60, TabUtils.translate(player, "%f_list_19%"));
    }

    @EventHandler
    public void factionJoined(PlayerJoinedFactionEvent event){
        if(!this.plugin.getToggleTab()) return;

        Player player;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();

        if (!this.construct.hasTabList(player)) return;


        this.construct.setPosition(player, 9, TabUtils.translate(player, "&e%f_title%"));
        this.construct.setPosition(player, 10, TabUtils.translate(player, "%fhome%"));
        this.construct.setPosition(player, 11, TabUtils.translate(player, "%fonline%"));
        this.construct.setPosition(player, 12, TabUtils.translate(player, "%fbal%"));
        this.construct.setPosition(player, 13, TabUtils.translate(player, "%fdtr%"));

        this.construct.setPosition(player, 25, TabUtils.translate(player, "&e" + "%ftag%"));
        this.construct.setPosition(player, 26, TabUtils.translate(player, "%f_member_1%"));
        this.construct.setPosition(player, 27, TabUtils.translate(player, "%f_member_2%"));
        this.construct.setPosition(player, 28, TabUtils.translate(player, "%f_member_3%"));
        this.construct.setPosition(player, 29, TabUtils.translate(player, "%f_member_4%"));
        this.construct.setPosition(player, 30, TabUtils.translate(player, "%f_member_5%"));
        this.construct.setPosition(player, 31, TabUtils.translate(player, "%f_member_6%"));
        this.construct.setPosition(player, 32, TabUtils.translate(player, "%f_member_7%"));
        this.construct.setPosition(player, 33, TabUtils.translate(player, "%f_member_8%"));
        this.construct.setPosition(player, 34, TabUtils.translate(player, "%f_member_9%"));
        this.construct.setPosition(player, 35, TabUtils.translate(player, "%f_member_10%"));
        this.construct.setPosition(player, 36, TabUtils.translate(player, "%f_member_11%"));
        this.construct.setPosition(player, 37, TabUtils.translate(player, "%f_member_12%"));
        this.construct.setPosition(player, 38, TabUtils.translate(player, "%f_member_13%"));
        this.construct.setPosition(player, 39, TabUtils.translate(player, "%f_member_14%"));
        this.construct.setPosition(player, 40, TabUtils.translate(player, "%f_member_15%"));
        //End second row

        //Start third row
        this.construct.setPosition(player, 41, TabUtils.translate(player, "&eFaction List"));
        this.construct.setPosition(player, 42, TabUtils.translate(player, "%f_list_1%"));
        this.construct.setPosition(player, 43, TabUtils.translate(player, "%f_list_2%"));
        this.construct.setPosition(player, 44, TabUtils.translate(player, "%f_list_3%"));
        this.construct.setPosition(player, 45, TabUtils.translate(player, "%f_list_4%"));
        this.construct.setPosition(player, 46, TabUtils.translate(player, "%f_list_5%"));
        this.construct.setPosition(player, 47, TabUtils.translate(player, "%f_list_6%"));
        this.construct.setPosition(player, 48, TabUtils.translate(player, "%f_list_7%"));
        this.construct.setPosition(player, 49, TabUtils.translate(player, "%f_list_8%"));
        this.construct.setPosition(player, 50, TabUtils.translate(player, "%f_list_9%"));
        this.construct.setPosition(player, 51, TabUtils.translate(player, "%f_list_10%"));
        this.construct.setPosition(player, 52, TabUtils.translate(player, "%f_list_11%"));
        this.construct.setPosition(player, 53, TabUtils.translate(player, "%f_list_12%"));
        this.construct.setPosition(player, 54, TabUtils.translate(player, "%f_list_13%"));
        this.construct.setPosition(player, 55, TabUtils.translate(player, "%f_list_14%"));
        this.construct.setPosition(player, 56, TabUtils.translate(player, "%f_list_15%"));
        this.construct.setPosition(player, 57, TabUtils.translate(player, "%f_list_16%"));
        this.construct.setPosition(player, 58, TabUtils.translate(player, "%f_list_17%"));
        this.construct.setPosition(player, 59, TabUtils.translate(player, "%f_list_18%"));
        this.construct.setPosition(player, 60, TabUtils.translate(player, "%f_list_19%"));

        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!this.plugin.getToggleTab()) return;
        Player player = event.getPlayer();

        if (!this.construct.hasTabList(player)) return;

        this.construct.setPosition(player, 6, TabUtils.translate(player, "%faction_location%"));
        this.construct.setPosition(player, 7, TabUtils.translate(player, "&7%player_location%"));
    }

    public void initialUpdate(Player player) {
        if(!this.plugin.getToggleTab()) return;
        if (!this.construct.hasTabList(player)) {
            return;
        }

        //Start first row
        this.construct.setPosition(player, 1, TabUtils.translate(player, "&ePlayer Info"));
        this.construct.setPosition(player, 2, TabUtils.translate(player, "&7Kills:&a %player_kills%"));
        this.construct.setPosition(player, 3, TabUtils.translate(player, "&7Deaths:&a %player_deaths%"));
        this.construct.setPosition(player, 4, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 5, TabUtils.translate(player, "&eYour location"));
        this.construct.setPosition(player, 6, TabUtils.translate(player, "%faction_location%"));
        this.construct.setPosition(player, 7, TabUtils.translate(player, "&7%player_location%"));
        this.construct.setPosition(player, 8, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 9, TabUtils.translate(player, "&e%f_title%"));
        this.construct.setPosition(player, 10, TabUtils.translate(player, "%fhome%"));
        this.construct.setPosition(player, 11, TabUtils.translate(player, "%fonline%"));
        this.construct.setPosition(player, 12, TabUtils.translate(player, "%fbal%"));
        this.construct.setPosition(player, 13, TabUtils.translate(player, "%fdtr%"));
        this.construct.setPosition(player, 14, TabUtils.translate(player, ""));
        this.construct.setPosition(player, 15, TabUtils.translate(player, "&ePlayer Vault"));
        this.construct.setPosition(player, 16, TabUtils.translate(player, "&7Lives:&a %player_lives%"));
        this.construct.setPosition(player, 17, TabUtils.translate(player, "&7Balance:&a %player_bal%"));
        this.construct.setPosition(player, 18, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 19, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 20, TabUtils.translate(player, " "));
        //End first row

        //Start second row
        this.construct.setPosition(player, 21, TabUtils.translate(player, ChatColor.RED + "" + ChatColor.BOLD + "veilmc.net"));
        this.construct.setPosition(player, 22, TabUtils.translate(player, "&ePlayers Online"));
        this.construct.setPosition(player, 23, TabUtils.translate(player, "&7" + "%online_players%"));
        this.construct.setPosition(player, 24, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 25, TabUtils.translate(player, "&e" + "%ftag%"));
        this.construct.setPosition(player, 26, TabUtils.translate(player, "%f_member_1%"));
        this.construct.setPosition(player, 27, TabUtils.translate(player, "%f_member_2%"));
        this.construct.setPosition(player, 28, TabUtils.translate(player, "%f_member_3%"));
        this.construct.setPosition(player, 29, TabUtils.translate(player, "%f_member_4%"));
        this.construct.setPosition(player, 30, TabUtils.translate(player, "%f_member_5%"));
        this.construct.setPosition(player, 31, TabUtils.translate(player, "%f_member_6%"));
        this.construct.setPosition(player, 32, TabUtils.translate(player, "%f_member_7%"));
        this.construct.setPosition(player, 33, TabUtils.translate(player, "%f_member_8%"));
        this.construct.setPosition(player, 34, TabUtils.translate(player, "%f_member_9%"));
        this.construct.setPosition(player, 35, TabUtils.translate(player, "%f_member_10%"));
        this.construct.setPosition(player, 36, TabUtils.translate(player, "%f_member_11%"));
        this.construct.setPosition(player, 37, TabUtils.translate(player, "%f_member_12%"));
        this.construct.setPosition(player, 38, TabUtils.translate(player, "%f_member_13%"));
        this.construct.setPosition(player, 39, TabUtils.translate(player, "%f_member_14%"));
        this.construct.setPosition(player, 40, TabUtils.translate(player, "%f_member_15%"));
        //End second row

        //Start third row
        this.construct.setPosition(player, 41, TabUtils.translate(player, "&eFaction List"));
        this.construct.setPosition(player, 42, TabUtils.translate(player, "%f_list_1%"));
        this.construct.setPosition(player, 43, TabUtils.translate(player, "%f_list_2%"));
        this.construct.setPosition(player, 44, TabUtils.translate(player, "%f_list_3%"));
        this.construct.setPosition(player, 45, TabUtils.translate(player, "%f_list_4%"));
        this.construct.setPosition(player, 46, TabUtils.translate(player, "%f_list_5%"));
        this.construct.setPosition(player, 47, TabUtils.translate(player, "%f_list_6%"));
        this.construct.setPosition(player, 48, TabUtils.translate(player, "%f_list_7%"));
        this.construct.setPosition(player, 49, TabUtils.translate(player, "%f_list_8%"));
        this.construct.setPosition(player, 50, TabUtils.translate(player, "%f_list_9%"));
        this.construct.setPosition(player, 51, TabUtils.translate(player, "%f_list_10%"));
        this.construct.setPosition(player, 52, TabUtils.translate(player, "%f_list_11%"));
        this.construct.setPosition(player, 53, TabUtils.translate(player, "%f_list_12%"));
        this.construct.setPosition(player, 54, TabUtils.translate(player, "%f_list_13%"));
        this.construct.setPosition(player, 55, TabUtils.translate(player, "%f_list_14%"));
        this.construct.setPosition(player, 56, TabUtils.translate(player, "%f_list_15%"));
        this.construct.setPosition(player, 57, TabUtils.translate(player, "%f_list_16%"));
        this.construct.setPosition(player, 58, TabUtils.translate(player, "%f_list_17%"));
        this.construct.setPosition(player, 59, TabUtils.translate(player, "%f_list_18%"));
        this.construct.setPosition(player, 60, TabUtils.translate(player, "%f_list_19%"));
        //End third row


        //^^ 1.7 stuff
        //vv 1.8 stuff

        if(!this.construct.getVersion(player).equals(ConstructVersion.V1_8)) return;


        this.construct.setPosition(player, 61, " ");
        this.construct.setPosition(player, 62, " ");
        this.construct.setPosition(player, 63, " ");
        this.construct.setPosition(player, 64, " ");
        this.construct.setPosition(player, 65, " ");
        this.construct.setPosition(player, 66, " ");
        this.construct.setPosition(player, 67, " ");
        this.construct.setPosition(player, 68, " ");
        this.construct.setPosition(player, 69, " ");
        //this.construct.setPosition(player, 70, ChatColor.RED + "For a better experience");
        //this.construct.setPosition(player, 71, ChatColor.RED + "We recommend playing in 1.7");
        this.construct.setPosition(player, 72, " ");
        this.construct.setPosition(player, 73, " ");
        this.construct.setPosition(player, 74, " ");
        this.construct.setPosition(player, 75, " ");
        this.construct.setPosition(player, 76, " ");
        this.construct.setPosition(player, 77, " ");
        this.construct.setPosition(player, 78, " ");
        this.construct.setPosition(player, 79, " ");
        this.construct.setPosition(player, 80, " ");

    }



}
