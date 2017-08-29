package net.veilmc.dataapi.listeners;


import com.customhcf.hcf.HCF;
import com.customhcf.hcf.faction.event.PlayerJoinedFactionEvent;
import com.customhcf.hcf.faction.event.PlayerLeftFactionEvent;
import com.google.common.base.Optional;
import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.ConstructVersion;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.DataAPI;
import net.veilmc.dataapi.utils.TabUtils;
import org.bukkit.Bukkit;
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
    private IConstructLibrary construct;

    public FactionsTabListener(DataAPI plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if(!this.plugin.getToggleTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }
        TaskUtil.runTaskNextTick(() -> {
           this.construct.createTabList(event.getPlayer());
           this.initialUpdate(event.getPlayer());
            for(Player p : Bukkit.getOnlinePlayers()){
                updateOnlinePlayers(p);
            }
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.getToggleTab()) return;
        this.construct.removeTabList(event.getPlayer());


        TaskUtil.runTaskNextTick(() -> {
            for(Player p : Bukkit.getOnlinePlayers()){
                updateOnlinePlayers(p);
            }
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.getToggleTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onDeathKill(PlayerDeathEvent event){
        if(!this.plugin.getToggleTab()) return;
        if(event.getEntity().getKiller() != null){
            Player player = event.getEntity().getKiller();
            if(!this.construct.hasTabList(player)) return;

            TaskUtil.runTaskNextTick(() -> {
                updatePlayerKills(player);
                updatePlayerVault(player);
            });

        }
    }

    @EventHandler
    public void factionLeft(PlayerLeftFactionEvent event){
        if(!this.plugin.getToggleTab()) return;

        Player player;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();

            if (!this.construct.hasTabList(player)) return;


            TaskUtil.runTaskNextTick(() -> {
                for(Player member : event.getFaction().getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                    }
                }
                if(this.construct.hasTabList(player)){
                    updateFactions(player);
                }
            });

            TaskUtil.runTaskNextTick(() -> {
                for(Player on : Bukkit.getOnlinePlayers()){
                    if(this.construct.hasTabList(on)){
                        updateFactionList(on);
                    }
                }
            });
        }

    }

    @EventHandler
    public void factionJoined(PlayerJoinedFactionEvent event){
        if(!this.plugin.getToggleTab()) return;

        Player player;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();

        if (!this.construct.hasTabList(player)) return;


            TaskUtil.runTaskNextTick(() -> {
                for(Player member : HCF.getPlugin().getFactionManager().getPlayerFaction(player.getUniqueId()).getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                    }
                }
            });

            TaskUtil.runTaskNextTick(() -> {
                for(Player on : Bukkit.getOnlinePlayers()){
                    if(this.construct.hasTabList(on)){
                        updateFactionList(on);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!this.plugin.getToggleTab()) return;
        Player player = event.getPlayer();

        if (!this.construct.hasTabList(player)) return;

        updatePlayerLocation(player);
    }

    public void initialUpdate(Player player) {
        if(!this.plugin.getToggleTab()) return;
        if (!this.construct.hasTabList(player)) {
            return;
        }

        //Start first row
        this.construct.getPlayer(player).setPosition(1, TabUtils.translate(player, "&ePlayer Info"));
        updatePlayerKills(player);
        this.construct.getPlayer(player).setPosition(4, TabUtils.translate(player, " "));
        this.construct.getPlayer(player).setPosition(5, TabUtils.translate(player, "&eYour location"));
        updatePlayerLocation(player);
        this.construct.getPlayer(player).setPosition(8, TabUtils.translate(player, " "));
        updateFactionsDetails(player);
        this.construct.getPlayer(player).setPosition(14, TabUtils.translate(player, ""));
        this.construct.getPlayer(player).setPosition(15, TabUtils.translate(player, "&ePlayer Vault"));
        updatePlayerVault(player);
        this.construct.getPlayer(player).setPosition(18, TabUtils.translate(player, " "));
        this.construct.getPlayer(player).setPosition(19, TabUtils.translate(player, " "));
        this.construct.getPlayer(player).setPosition(20, TabUtils.translate(player, " "));
        //End first row

        //Start second row
        this.construct.getPlayer(player).setPosition(21, TabUtils.translate(player, ChatColor.GOLD + "" + ChatColor.BOLD + "VeilMC.net"));
        this.construct.getPlayer(player).setPosition(22, TabUtils.translate(player, "&ePlayers Online"));
        updateOnlinePlayers(player);
        this.construct.getPlayer(player).setPosition(24, TabUtils.translate(player, " "));
        updateFactions(player);
        updateFactionList(player);
        this.construct.getPlayer(player).setPosition(41, TabUtils.translate(player, "&eFaction List"));
        //End third row


        //^^ 1.7 stuff
        //vv 1.8 stuff

        if(!this.construct.getPlayer(player).getVersion().equals(ConstructVersion.V1_8)) return;


        this.construct.getPlayer(player).setPosition(61, " ");
        this.construct.getPlayer(player).setPosition(62, " ");
        this.construct.getPlayer(player).setPosition(63, " ");
        this.construct.getPlayer(player).setPosition(64, " ");
        this.construct.getPlayer(player).setPosition(65, " ");
        this.construct.getPlayer(player).setPosition(66, " ");
        this.construct.getPlayer(player).setPosition(67, " ");
        this.construct.getPlayer(player).setPosition(68, " ");
        this.construct.getPlayer(player).setPosition(69, " ");
        this.construct.getPlayer(player).setPosition(70, ChatColor.RED + "For optimal");
        this.construct.getPlayer(player).setPosition(71, ChatColor.RED + "performance");
        this.construct.getPlayer(player).setPosition(72, ChatColor.RED + "please use 1.7");
        this.construct.getPlayer(player).setPosition(73, " ");
        this.construct.getPlayer(player).setPosition(74, " ");
        this.construct.getPlayer(player).setPosition(75, " ");
        this.construct.getPlayer(player).setPosition(76, " ");
        this.construct.getPlayer(player).setPosition(77, " ");
        this.construct.getPlayer(player).setPosition(78, " ");
        this.construct.getPlayer(player).setPosition(79, " ");
        this.construct.getPlayer(player).setPosition(80, " ");

    }
    public void updatePlayerKills(Player player){
        this.construct.getPlayer(player).setPosition(2, TabUtils.translate(player, "&7Kills:&a %player_kills%"));
    }

    public void updatePlayerLocation(Player player){
        this.construct.getPlayer(player).setPosition(6, TabUtils.translate(player, "%faction_location%"));
        this.construct.getPlayer(player).setPosition(7, TabUtils.translate(player, "&7%player_location%"));
    }

    public void updatePlayerVault(Player player){
        this.construct.getPlayer(player).setPosition(16, TabUtils.translate(player, "&7Lives:&a %player_lives%"));
        this.construct.getPlayer(player).setPosition(17, TabUtils.translate(player, "&7Balance:&a %player_bal%"));
    }

    public void updateOnlinePlayers(Player player){
        this.construct.getPlayer(player).setPosition(23, TabUtils.translate(player, "&7" + "%online_players%"));
    }

    public void updateFactionsDetails(Player player){
        this.construct.getPlayer(player).setPosition(9, TabUtils.translate(player, "&e%f_title%"));
        this.construct.getPlayer(player).setPosition(10, TabUtils.translate(player, "%fhome%"));
        this.construct.getPlayer(player).setPosition(11, TabUtils.translate(player, "%fonline%"));
        this.construct.getPlayer(player).setPosition(12, TabUtils.translate(player, "%fbal%"));
        this.construct.getPlayer(player).setPosition(13, TabUtils.translate(player, "%fdtr%"));
    }

    public void updateFactions(Player player){
        this.construct.getPlayer(player).setPosition(25, TabUtils.translate(player, "&e" + "%ftag%"));
        this.construct.getPlayer(player).setPosition(26, TabUtils.translate(player, "%f_member_1%"));
        this.construct.getPlayer(player).setPosition(27, TabUtils.translate(player, "%f_member_2%"));
        this.construct.getPlayer(player).setPosition(28, TabUtils.translate(player, "%f_member_3%"));
        this.construct.getPlayer(player).setPosition(29, TabUtils.translate(player, "%f_member_4%"));
        this.construct.getPlayer(player).setPosition(30, TabUtils.translate(player, "%f_member_5%"));
        this.construct.getPlayer(player).setPosition(31, TabUtils.translate(player, "%f_member_6%"));
        this.construct.getPlayer(player).setPosition(32, TabUtils.translate(player, "%f_member_7%"));
        this.construct.getPlayer(player).setPosition(33, TabUtils.translate(player, "%f_member_8%"));
        this.construct.getPlayer(player).setPosition(34, TabUtils.translate(player, "%f_member_9%"));
        this.construct.getPlayer(player).setPosition(35, TabUtils.translate(player, "%f_member_10%"));
        this.construct.getPlayer(player).setPosition(36, TabUtils.translate(player, "%f_member_11%"));
        this.construct.getPlayer(player).setPosition(37, TabUtils.translate(player, "%f_member_12%"));
        this.construct.getPlayer(player).setPosition(38, TabUtils.translate(player, "%f_member_13%"));
        this.construct.getPlayer(player).setPosition(39, TabUtils.translate(player, "%f_member_14%"));
        this.construct.getPlayer(player).setPosition(40, TabUtils.translate(player, "%f_member_15%"));
        //End second row

    }

    public void updateFactionList(Player player){
        this.construct.getPlayer(player).setPosition(42, TabUtils.translate(player, "%f_list_1%"));
        this.construct.getPlayer(player).setPosition(43, TabUtils.translate(player, "%f_list_2%"));
        this.construct.getPlayer(player).setPosition(44, TabUtils.translate(player, "%f_list_3%"));
        this.construct.getPlayer(player).setPosition(45, TabUtils.translate(player, "%f_list_4%"));
        this.construct.getPlayer(player).setPosition(46, TabUtils.translate(player, "%f_list_5%"));
        this.construct.getPlayer(player).setPosition(47, TabUtils.translate(player, "%f_list_6%"));
        this.construct.getPlayer(player).setPosition(48, TabUtils.translate(player, "%f_list_7%"));
        this.construct.getPlayer(player).setPosition(49, TabUtils.translate(player, "%f_list_8%"));
        this.construct.getPlayer(player).setPosition(50, TabUtils.translate(player, "%f_list_9%"));
        this.construct.getPlayer(player).setPosition(51, TabUtils.translate(player, "%f_list_10%"));
        this.construct.getPlayer(player).setPosition(52, TabUtils.translate(player, "%f_list_11%"));
        this.construct.getPlayer(player).setPosition(53, TabUtils.translate(player, "%f_list_12%"));
        this.construct.getPlayer(player).setPosition(54, TabUtils.translate(player, "%f_list_13%"));
        this.construct.getPlayer(player).setPosition(55, TabUtils.translate(player, "%f_list_14%"));
        this.construct.getPlayer(player).setPosition(56, TabUtils.translate(player, "%f_list_15%"));
        this.construct.getPlayer(player).setPosition(57, TabUtils.translate(player, "%f_list_16%"));
        this.construct.getPlayer(player).setPosition(58, TabUtils.translate(player, "%f_list_17%"));
        this.construct.getPlayer(player).setPosition(59, TabUtils.translate(player, "%f_list_18%"));
        this.construct.getPlayer(player).setPosition(60, TabUtils.translate(player, "%f_list_19%"));
    }



}
