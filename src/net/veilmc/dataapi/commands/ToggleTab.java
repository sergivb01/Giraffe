package net.veilmc.dataapi.commands;

import me.joeleoli.construct.Construct;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class ToggleTab implements CommandExecutor{
    private DataAPI plugin;

    public ToggleTab(final DataAPI plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        this.plugin.toggleTab();
        sender.sendMessage(ChatColor.YELLOW + "Custom tab is now " + (this.plugin.getToggleTab() ? (ChatColor.GREEN + "enabled") : (ChatColor.RED + "disabled")) + ChatColor.YELLOW + "!");

        for(Player player : Bukkit.getOnlinePlayers()){
            if(this.plugin.getToggleTab()){
                if(!Construct.getLibrary().hasTabList(player)){
                    Bukkit.getPluginManager().callEvent(new PlayerJoinEvent(player, "hello"));
                    player.sendMessage(ChatColor.GREEN + "You tab has been enabled.");
                }
            }else{
                Construct.getLibrary().removeTabList(player);
                player.sendMessage(ChatColor.RED + "Your tab has been disabled! (Will not update)");
            }
        }

        return true;
    }
}