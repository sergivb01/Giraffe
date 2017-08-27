package net.veilmc.dataapi.commands;

import net.veilmc.dataapi.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SyncDataCommand implements CommandExecutor{
    private DataAPI plugin;

    public SyncDataCommand(final DataAPI plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        int players = Bukkit.getOnlinePlayers().size();
        this.plugin.savePlayerGlobalData();
        this.plugin.saveServerData();
        sender.sendMessage(ChatColor.RED + "Saved player data of " + ChatColor.WHITE + players + " players" + ChatColor.RED + ".");
        return true;
    }
}