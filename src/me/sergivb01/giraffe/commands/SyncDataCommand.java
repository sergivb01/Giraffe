package me.sergivb01.giraffe.commands;

import com.customhcf.hcf.HCF;
import com.customhcf.hcf.faction.FactionManager;
import me.sergivb01.giraffe.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SyncDataCommand implements CommandExecutor{
    private Giraffe plugin;

    public SyncDataCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        int players = Bukkit.getOnlinePlayers().size();
        this.plugin.saveServerData(true);
        final FactionManager factionManager = HCF.getPlugin().getFactionManager();

        for(Player target : Bukkit.getOnlinePlayers()){
            this.plugin.saveSinglePlayerData(target, true, true);
            this.plugin.saveFaction(factionManager.getPlayerFaction(target.getUniqueId()));
            target.sendMessage(ChatColor.RED + "Your faction has been saved.");
        }

        sender.sendMessage(ChatColor.RED + "Saved player data of " + ChatColor.WHITE + players + " players" + ChatColor.RED + ".");
        return true;
    }
}