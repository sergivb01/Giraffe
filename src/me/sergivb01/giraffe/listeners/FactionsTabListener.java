package me.sergivb01.giraffe.listeners;

import com.google.common.base.Optional;
import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.TaskUtil;
import me.sergivb01.giraffe.utils.tab.TabUtils;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.veilmc.hcf.HCF;
import net.veilmc.hcf.faction.event.FactionDtrChangeEvent;
import net.veilmc.hcf.faction.event.FactionRenameEvent;
import net.veilmc.hcf.faction.event.PlayerJoinedFactionEvent;
import net.veilmc.hcf.faction.event.PlayerLeftFactionEvent;
import net.veilmc.hcf.faction.type.PlayerFaction;
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
    private Giraffe plugin;
    private IConstructLibrary construct;
    private HCF hcf;

    public FactionsTabListener(Giraffe plugin) {
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        this.hcf = HCF.getInstance();
        new Thread(()-> TaskUtil.runTaskTimerAsync(()->{
            if(!this.plugin.isUseTab()){
                return;
            }
            for(Player player : Bukkit.getOnlinePlayers()) {
                if (construct.hasTabList(player)) {
                    updateKoth(player);
                }
            }
            MinecraftServer.getServer().setMotd(ChatColor.YELLOW + "Next Koth:" + "\n" +
                    ChatColor.translateAlternateColorCodes('&', ((hcf.NEXT_KOTH > 0) ? "&9&l" + hcf.getNextGame() + " &7(" + hcf.getKothRemaining() + ")" : "&7None Scheduled")));
        }, 20L, 5 * 20L)).start();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.isUseTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }

        TaskUtil.runTaskNextTick(() -> {
            if(!this.construct.hasTabList(player)) {
                this.construct.createTabList(player);
            }
            this.initialUpdate(player);
        });

        if(HCF.getPlugin().getFactionManager().getPlayerFaction(player.getUniqueId()) != null){
            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player member : HCF.getPlugin().getFactionManager().getPlayerFaction(player.getUniqueId()).getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                        updateFactionsDetails(member);
                    }
                }
            });
        }

        TaskUtil.runTaskAsyncNextTick(() -> {
            for(Player on : Bukkit.getOnlinePlayers()){
                if(this.construct.hasTabList(on)){
                    updateFactionList(on);
                }
            }
        });



    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());


        TaskUtil.runTaskAsyncNextTick(() -> {
            for(Player p : Bukkit.getOnlinePlayers()){
                updateOnlinePlayers(p);
            }
        });

        if(HCF.getPlugin().getFactionManager().getPlayerFaction(event.getPlayer().getUniqueId()) != null) {
            TaskUtil.runTaskAsyncNextTick(() -> {
                for (Player member : HCF.getPlugin().getFactionManager().getPlayerFaction(event.getPlayer().getUniqueId()).getOnlinePlayers()) {
                    if (this.construct.hasTabList(member)) {
                        updateFactions(member);
                        updateFactionsDetails(member);
                    }
                }
            });
        }

        TaskUtil.runTaskAsyncNextTick(() -> {
            for(Player on : Bukkit.getOnlinePlayers()){
                if(this.construct.hasTabList(on)){
                    updateFactionList(on);
                }
            }
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.isUseTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    @EventHandler
    public void onDeathKill(PlayerDeathEvent event){
        if(!this.plugin.isUseTab()) return;
        if(event.getEntity().getKiller() != null){
            Player player = event.getEntity().getKiller();
            Player death = event.getEntity();
            if(!this.construct.hasTabList(player)) return;

            TaskUtil.runTaskAsyncNextTick(() -> {
                updatePlayerKills(player);
                updatePlayerVault(player);
                updatePlayerKills(death);
                updatePlayerVault(death);
            });

            if(HCF.getPlugin().getFactionManager().getPlayerFaction(death.getUniqueId()) != null) {
                TaskUtil.runTaskAsyncNextTick(() -> {
                    for (Player member : HCF.getPlugin().getFactionManager().getPlayerFaction(death.getUniqueId()).getOnlinePlayers()) {
                        if (this.construct.hasTabList(member)) {
                            updateFactions(member);
                            updateFactionsDetails(member);
                        }
                    }
                });
            }

            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player on : Bukkit.getOnlinePlayers()){
                    if(this.construct.hasTabList(on)){
                        updateFactionList(on);
                    }
                }
            });

        }
    }

    @EventHandler
    public void onDtrChange(FactionDtrChangeEvent event){
        if(!this.plugin.isUseTab()) return;

        if(event.getRaidable() instanceof PlayerFaction){
            PlayerFaction playerFaction = (PlayerFaction) event.getRaidable();
            for(Player player : playerFaction.getOnlinePlayers()){
                TaskUtil.runTaskAsyncNextTick(() -> {
                    if(this.construct.hasTabList(player)) updateFactionsDetails(player);
                });
            }
        }
    }

    @EventHandler
    public void factionLeft(PlayerLeftFactionEvent event){
        if(!this.plugin.isUseTab()) return;

        Player player;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();

            if (!this.construct.hasTabList(player)) return;


            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player member : event.getFaction().getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                        updateFactionsDetails(member);
                    }
                }
                if(this.construct.hasTabList(player)){
                    updateFactions(player);
                    updateFactionsDetails(player);
                }
            });

            TaskUtil.runTaskAsyncNextTick(() -> {
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
        if(!this.plugin.isUseTab()) return;

        Player player;
        final Optional<Player> optional = event.getPlayer();
        if (optional.isPresent()) {
            player = optional.get();

            if (!this.construct.hasTabList(player)) return;


            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player member : HCF.getPlugin().getFactionManager().getPlayerFaction(player.getUniqueId()).getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                        updateFactionsDetails(member);
                    }
                }
            });

            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player on : Bukkit.getOnlinePlayers()){
                    if(this.construct.hasTabList(on)){
                        updateFactionList(on);
                    }
                }
            });
        }
    }

    @EventHandler
    public void factionRename(FactionRenameEvent event){
            if(!(event.getFaction() instanceof PlayerFaction)) return;
            if(!this.plugin.isUseTab()) return;

            PlayerFaction playerFaction = (PlayerFaction) event.getFaction();

            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player member : playerFaction.getOnlinePlayers()){
                    if(this.construct.hasTabList(member)){
                        updateFactions(member);
                        updateFactionList(member);
                    }
                }
            });

            TaskUtil.runTaskAsyncNextTick(() -> {
                for(Player on : Bukkit.getOnlinePlayers()){
                    if(this.construct.hasTabList(on)){
                        updateFactionList(on);
                    }
                }
            });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if(!this.plugin.isUseTab()) return;
        if(event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ()) {
            Player player = event.getPlayer();

            if (!this.construct.hasTabList(player)) return;

            TaskUtil.runTaskAsyncNextTick(()-> updatePlayerLocation(player));
        }
    }

    private void initialUpdate(Player player) {
        if (!this.construct.hasTabList(player)) {
            return;
        }
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);

        //Start first row
        tabPlayer.setPosition(1, TabUtils.translate(player, "&ePlayer Info"));
        updatePlayerKills(player);

        tabPlayer.setPosition(5, TabUtils.translate(player, "&eYour location"));
        updatePlayerLocation(player);

        updateFactionsDetails(player);

        tabPlayer.setPosition(9, TabUtils.translate(player, "&ePlayer Vault"));
        updatePlayerVault(player);

        tabPlayer.setPosition(19, TabUtils.translate(player, "&eNext Koth"));
        updateKoth(player);
        //End first row

        //Start second row
        tabPlayer.setPosition(21, TabUtils.translate(player, "&6&lVeilMC.net"));
        tabPlayer.setPosition(22, TabUtils.translate(player, "&ePlayers Online"));
        updateOnlinePlayers(player);
        updateFactions(player);
        updateFactionList(player);
        tabPlayer.setPosition(41, TabUtils.translate(player, "&eFaction List"));
        //End third row

    }

    private void updateKoth(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        tabPlayer.setPosition(20, ChatColor.translateAlternateColorCodes('&', ((hcf.NEXT_KOTH > 0) ? "&9&l" + hcf.getNextGame() + " &7(" + hcf.getKothRemaining() + ")" : "&7None Scheduled")));
    }

    private void updatePlayerKills(Player player){
        this.construct.getPlayer(player).setPosition(2, TabUtils.translate(player, "&7Kills:&a %player_kills%"));
        this.construct.getPlayer(player).setPosition(3, TabUtils.translate(player, "&7Deaths:&a %player_deaths%"));
    }

    private void updatePlayerLocation(Player player){
        this.construct.getPlayer(player).setPosition(6, TabUtils.translate(player, "%faction_location%"));
        this.construct.getPlayer(player).setPosition(7, TabUtils.translate(player, "&7%player_location%"));
    }

    private void updateOnlinePlayers(Player player){
        this.construct.getPlayer(player).setPosition(23, TabUtils.translate(player, "&7" + "%online_players%"));
    }

    private void updatePlayerVault(Player player){
        this.construct.getPlayer(player).setPosition(10, TabUtils.translate(player, "&7Lives:&a %player_lives%"));
        this.construct.getPlayer(player).setPosition(11, TabUtils.translate(player, "&7Balance:&a %player_bal%"));
    }

    private void updateFactionsDetails(Player player){
        this.construct.getPlayer(player).setPosition(13, TabUtils.translate(player, "&e%f_title%"));
        this.construct.getPlayer(player).setPosition(14, TabUtils.translate(player, "%fhome%"));
        this.construct.getPlayer(player).setPosition(15, TabUtils.translate(player, "%fonline%"));
        this.construct.getPlayer(player).setPosition(16, TabUtils.translate(player, "%fbal%"));
        this.construct.getPlayer(player).setPosition(17, TabUtils.translate(player, "%fdtr%"));
    }

    private void updateFactions(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        tabPlayer.setPosition(25, TabUtils.translate(player, "&e" + "%ftag%"));
        tabPlayer.setPosition(26, TabUtils.translate(player, "%f_member_1%"));
        tabPlayer.setPosition(27, TabUtils.translate(player, "%f_member_2%"));
        tabPlayer.setPosition(28, TabUtils.translate(player, "%f_member_3%"));
        tabPlayer.setPosition(29, TabUtils.translate(player, "%f_member_4%"));
        tabPlayer.setPosition(30, TabUtils.translate(player, "%f_member_5%"));
        tabPlayer.setPosition(31, TabUtils.translate(player, "%f_member_6%"));
        tabPlayer.setPosition(32, TabUtils.translate(player, "%f_member_7%"));
        tabPlayer.setPosition(33, TabUtils.translate(player, "%f_member_8%"));
        tabPlayer.setPosition(34, TabUtils.translate(player, "%f_member_9%"));
        tabPlayer.setPosition(35, TabUtils.translate(player, "%f_member_10%"));
        tabPlayer.setPosition(36, TabUtils.translate(player, "%f_member_11%"));
        tabPlayer.setPosition(37, TabUtils.translate(player, "%f_member_12%"));
        tabPlayer.setPosition(38, TabUtils.translate(player, "%f_member_13%"));
        tabPlayer.setPosition(39, TabUtils.translate(player, "%f_member_14%"));
        tabPlayer.setPosition(40, TabUtils.translate(player, "%f_member_15%"));

    }

    private void updateFactionList(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);
        tabPlayer.setPosition(42, TabUtils.translate(player, "%f_list_1%"));
        tabPlayer.setPosition(43, TabUtils.translate(player, "%f_list_2%"));
        tabPlayer.setPosition(44, TabUtils.translate(player, "%f_list_3%"));
        tabPlayer.setPosition(45, TabUtils.translate(player, "%f_list_4%"));
        tabPlayer.setPosition(46, TabUtils.translate(player, "%f_list_5%"));
        tabPlayer.setPosition(47, TabUtils.translate(player, "%f_list_6%"));
        tabPlayer.setPosition(48, TabUtils.translate(player, "%f_list_7%"));
        tabPlayer.setPosition(49, TabUtils.translate(player, "%f_list_8%"));
        tabPlayer.setPosition(50, TabUtils.translate(player, "%f_list_9%"));
        tabPlayer.setPosition(51, TabUtils.translate(player, "%f_list_10%"));
        tabPlayer.setPosition(52, TabUtils.translate(player, "%f_list_11%"));
        tabPlayer.setPosition(53, TabUtils.translate(player, "%f_list_12%"));
        tabPlayer.setPosition(54, TabUtils.translate(player, "%f_list_13%"));
        tabPlayer.setPosition(55, TabUtils.translate(player, "%f_list_14%"));
        tabPlayer.setPosition(56, TabUtils.translate(player, "%f_list_15%"));
        tabPlayer.setPosition(57, TabUtils.translate(player, "%f_list_16%"));
        tabPlayer.setPosition(58, TabUtils.translate(player, "%f_list_17%"));
        tabPlayer.setPosition(59, TabUtils.translate(player, "%f_list_18%"));
        tabPlayer.setPosition(60, TabUtils.translate(player, "%f_list_19%"));
    }



}
