package net.veilmc.dataapi.listeners;

import com.customhcf.hcf.HCF;
import com.customhcf.hcf.Utils.DurationFormatter;
import com.customhcf.hcf.kothgame.CaptureZone;
import com.customhcf.hcf.kothgame.EventTimer;
import com.customhcf.hcf.kothgame.faction.ConquestFaction;
import com.customhcf.hcf.kothgame.faction.EventFaction;
import com.customhcf.hcf.kothgame.faction.KothFaction;
import me.joeleoli.construct.ConstructLibrary;
import me.joeleoli.construct.api.ConstructVersion;
import me.joeleoli.construct.api.IConstruct;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.utils.TabUtils;
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
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!this.construct.hasTabList(player)) {
            return;
        }

        this.construct.setPosition(player, 2, player.getLocation().getX() + ", " + player.getLocation().getZ());
    }


    private void initialUpdate(Player player) {
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
        this.construct.setPosition(player, 12, TabUtils.translate(player, "%fdtr%"));
        this.construct.setPosition(player, 13, TabUtils.translate(player, "%fregen%"));
        this.construct.setPosition(player, 14, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 15, TabUtils.translate(player, "&eEvent"));
        EventTimer eventTimer = HCF.getPlugin().getTimerManager().eventTimer;
        EventFaction eventFaction = eventTimer.getEventFaction();
        if (eventFaction instanceof KothFaction) {
            this.construct.setPosition(player, 16, TabUtils.translate(player, eventTimer.getScoreboardPrefix() + "" + eventFaction.getName() + ChatColor.GRAY + ": " + ChatColor.GOLD + DurationFormatter.getRemaining(eventTimer.getRemaining(), true)));
            this.construct.setPosition(player, 17, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 18, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 19, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 20, TabUtils.translate(player, " "));
        }else if (eventFaction instanceof ConquestFaction) {
            final ConquestFaction conquestFaction = (ConquestFaction) eventFaction;
            int a = 16;
            for (final CaptureZone captureZone : conquestFaction.getCaptureZones()) {
                final ConquestFaction.ConquestZone conquestZone = conquestFaction.getZone(captureZone);
                this.construct.setPosition(player, a, TabUtils.translate(player, conquestZone.getColor() + "" + ChatColor.BOLD + conquestZone.getName() + ChatColor.GRAY + ": " + DurationFormatter.getRemaining(captureZone.getRemainingCaptureMillis(), true)));
                a++;
            }
        }else{
            this.construct.setPosition(player, 16, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 17, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 18, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 19, TabUtils.translate(player, " "));
            this.construct.setPosition(player, 20, TabUtils.translate(player, " "));
        }
        //End first row

        //Start second row
        this.construct.setPosition(player, 21, TabUtils.translate(player, ChatColor.RED + "" + ChatColor.BOLD + "veilmc.net"));
        this.construct.setPosition(player, 22, TabUtils.translate(player, "&ePlayers Online"));
        this.construct.setPosition(player, 23, TabUtils.translate(player, "&7" + TabUtils.translate(player, "%online_players%")));
        this.construct.setPosition(player, 24, TabUtils.translate(player, " "));
        this.construct.setPosition(player, 25, TabUtils.translate(player, "&e" + "&e%ftag%"));
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
        this.construct.setPosition(player, 70, ChatColor.RED + "For a better experience");
        this.construct.setPosition(player, 71, ChatColor.RED + "We recommend playing in 1.7");
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
