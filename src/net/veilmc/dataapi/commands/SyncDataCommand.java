package net.veilmc.dataapi.commands;

import net.veilmc.dataapi.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SyncDataCommand implements CommandExecutor{
    private Giraffe plugin;

    public SyncDataCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        int players = Bukkit.getOnlinePlayers().size();
        //this.plugin.savePlayerGlobalData();
        //this.plugin.saveServerData(true);
        sender.sendMessage(ChatColor.RED + "Saved player data of " + ChatColor.WHITE + players + " players" + ChatColor.RED + ".");
        return true;
    }
}