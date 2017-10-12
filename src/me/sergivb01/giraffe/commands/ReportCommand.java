package me.sergivb01.giraffe.commands;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import me.sergivb01.giraffe.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportCommand implements CommandExecutor{
    private Giraffe plugin;
    private static final Map<UUID, Long> COOLDOWNS;

    public ReportCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command comm, final String label, final String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players nigger.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /report <player> <reason...>");
            return false;
        }
        final Player reported = Bukkit.getPlayer(args[0]);
        if (reported == null) {
            player.sendMessage(ChatColor.RED + "No player named '" + args[0] + "' found online.");
            return false;
        }
        if (reported.equals(player)) {
            player.sendMessage(ChatColor.RED + "You can't report yourself!");
            return false;
        }
        if (ReportCommand.COOLDOWNS.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - ReportCommand.COOLDOWNS.get(player.getUniqueId()) < 100000L) {
                player.sendMessage(ChatColor.RED + "You must wait before attempting to reporting a player again.");
                return false;
            }
            ReportCommand.COOLDOWNS.remove(player.getUniqueId());
        }
        this.plugin.getPublisher().write("report;" + player.getDisplayName() + ";" + this.plugin.getServerName() + ";" + StringUtils.join(args, " ").replace(";", ":") + ";" + reported.getDisplayName());
        player.sendMessage(ChatColor.GREEN + "Staff have been notified of your player report.");
        ReportCommand.COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis());

        return true;
    }

    static {
        COOLDOWNS = new HashMap<>();
    }
}