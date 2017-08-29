package net.veilmc.dataapi.commands;

import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RequestCommand implements CommandExecutor{
    private DataAPI plugin;
    private static final Map<UUID, Long> COOLDOWNS;

    public RequestCommand(final DataAPI plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command comm, final String label, final String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players nigger.");
            return false;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /request <reason...>");
            return false;
        }
        if (RequestCommand.COOLDOWNS.containsKey(player.getUniqueId())) {
            if (System.currentTimeMillis() - RequestCommand.COOLDOWNS.get(player.getUniqueId()) < 100000L) {
                player.sendMessage(ChatColor.RED + "You must wait before attempting to request staff assistance again.");
                return false;
            }
            RequestCommand.COOLDOWNS.remove(player.getUniqueId());
        }
        this.plugin.getPublisher().write("request;" + player.getDisplayName() + ";" + Bukkit.getServerName() + ";" + StringUtils.join(args, " ").replace(";", ":"));
        player.sendMessage(ChatColor.GREEN + "Staff have been notified of your request.");
        RequestCommand.COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis());

        return true;
    }

    static {
        COOLDOWNS = new HashMap<>();
    }
}