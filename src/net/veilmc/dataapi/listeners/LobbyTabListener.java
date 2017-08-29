package net.veilmc.dataapi.listeners;


import me.joeleoli.construct.Construct;
import me.joeleoli.construct.api.ConstructVersion;
import me.joeleoli.construct.api.IConstructLibrary;
import me.joeleoli.construct.api.IConstructPlayer;
import me.joeleoli.construct.util.TaskUtil;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class LobbyTabListener implements Listener {
    //TODO: Update stuff
    private DataAPI plugin;
    private IConstructLibrary construct;

    public LobbyTabListener(DataAPI plugin) {
        // Define construct before registering event listeners
        this.plugin = plugin;
        this.construct = Construct.getLibrary();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!this.plugin.getToggleTab()){
            event.getPlayer().sendMessage(ChatColor.RED + "Your tab is currently disabled.");
            return;
        }

        TaskUtil.runTaskNextTick(() -> {
            if(this.construct.hasTabList(player)){
                if(!this.construct.hasTabList(player)) this.construct.createTabList(event.getPlayer());
                this.initialUpdate(event.getPlayer());
            }
        });


    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if(!this.plugin.getToggleTab()) return;

        TaskUtil.runTaskNextTick(() -> {
            this.construct.removeTabList(event.getPlayer());
        });

    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if(!this.plugin.getToggleTab()) return;
        this.construct.removeTabList(event.getPlayer());
    }

    private void initialUpdate(Player player){
        IConstructPlayer tabPlayer = this.construct.getPlayer(player);

        //Start first row
        tabPlayer.setPosition(1, "");
        tabPlayer.setPosition(2, "");
        tabPlayer.setPosition(3, "");
        tabPlayer.setPosition(4, "");
        tabPlayer.setPosition(5, "");
        tabPlayer.setPosition(6, "");
        tabPlayer.setPosition(7, "");
        tabPlayer.setPosition(8, "");
        tabPlayer.setPosition(9, "");
        tabPlayer.setPosition(10, "");
        tabPlayer.setPosition(11, "");
        tabPlayer.setPosition(12, "");
        tabPlayer.setPosition(13, "");
        tabPlayer.setPosition(14, "");
        tabPlayer.setPosition(15, "");
        tabPlayer.setPosition(16, "");
        tabPlayer.setPosition(17, "");
        tabPlayer.setPosition(18, "");
        tabPlayer.setPosition(19, "");
        tabPlayer.setPosition(20, "");
        //End first row

        //Start second row
        tabPlayer.setPosition(21, "");
        tabPlayer.setPosition(22, "");
        tabPlayer.setPosition(23, "");
        tabPlayer.setPosition(24, "");
        tabPlayer.setPosition(25, "");
        tabPlayer.setPosition(26, "");
        tabPlayer.setPosition(27, "");
        tabPlayer.setPosition(28, "");
        tabPlayer.setPosition(29, "");
        tabPlayer.setPosition(30, "");
        tabPlayer.setPosition(31, "");
        tabPlayer.setPosition(32, "");
        tabPlayer.setPosition(33, "");
        tabPlayer.setPosition(34, "");
        tabPlayer.setPosition(35, "");
        tabPlayer.setPosition(36, "");
        tabPlayer.setPosition(37, "");
        tabPlayer.setPosition(38, "");
        tabPlayer.setPosition(39, "");
        tabPlayer.setPosition(40, "");
        //End second row

        //Start third row
        tabPlayer.setPosition(41, "");
        tabPlayer.setPosition(42, "");
        tabPlayer.setPosition(43, "");
        tabPlayer.setPosition(44, "");
        tabPlayer.setPosition(45, "");
        tabPlayer.setPosition(46, "");
        tabPlayer.setPosition(47, "");
        tabPlayer.setPosition(48, "");
        tabPlayer.setPosition(49, "");
        tabPlayer.setPosition(50, "");
        tabPlayer.setPosition(51, "");
        tabPlayer.setPosition(52, "");
        tabPlayer.setPosition(53, "");
        tabPlayer.setPosition(54, "");
        tabPlayer.setPosition(55, "");
        tabPlayer.setPosition(56, "");
        tabPlayer.setPosition(57, "");
        tabPlayer.setPosition(58, "");
        tabPlayer.setPosition(59, "");
        tabPlayer.setPosition(60, "");

        if(!tabPlayer.getVersion().equals(ConstructVersion.V1_8)) return;


        tabPlayer.setPosition(61, " ");
        tabPlayer.setPosition(62, " ");
        tabPlayer.setPosition(63, " ");
        tabPlayer.setPosition(64, " ");
        tabPlayer.setPosition(65, " ");
        tabPlayer.setPosition(66, " ");
        tabPlayer.setPosition(67, " ");
        tabPlayer.setPosition(68, " ");
        tabPlayer.setPosition(69, " ");
        tabPlayer.setPosition(70, "&cFor optimal");
        tabPlayer.setPosition(71, "&cperformance");
        tabPlayer.setPosition(72, "&cplease use 1.7");
        tabPlayer.setPosition(73, " ");
        tabPlayer.setPosition(74, " ");
        tabPlayer.setPosition(75, " ");
        tabPlayer.setPosition(76, " ");
        tabPlayer.setPosition(77, " ");
        tabPlayer.setPosition(78, " ");
        tabPlayer.setPosition(79, " ");
        tabPlayer.setPosition(80, " ");


    }



}
