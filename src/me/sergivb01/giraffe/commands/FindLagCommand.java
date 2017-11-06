package me.sergivb01.giraffe.commands;

import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.report.ReportListener;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FindLagCommand implements CommandExecutor {
    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Giraffe plugin;

    public FindLagCommand(Giraffe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
            case "record":
                handleStart(sender, label);
                break;
            case "cancel":
                handleCancel(sender, label);
                break;
            case "report":
            case "paste":
                handleReport(sender, label);
                break;
            case "tpchunk":
            case "tp":
                handleTp(sender, label, args);
                break;
            default:
                sendUsage(sender, label);
                break;
        }
        return true;
    }

    private void handleStart(CommandSender sender, String label) {
        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE_UUID;

        plugin.startListening(uuid);
        msgPrefix(sender, "&eStarted gathering data. Run '/" + label + " report' to stop recording and view the results.");
    }

    private void handleCancel(CommandSender sender, String label) {
        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE_UUID;

        ReportListener listener = plugin.stopListening(uuid);
        if (listener != null) {
            msgPrefix(sender, "&eYour existing report has been cancelled.");
        } else {
            msgPrefix(sender, "&eUnable to find any active reports for your user. Run '/" + label + " start' to start gathering data.");
        }
    }

    private void handleReport(CommandSender sender, String label) {
        UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : CONSOLE_UUID;

        ReportListener listener = plugin.stopListening(uuid);
        if (listener != null) {
            listener.sendReport(sender);
        } else {
            msgPrefix(sender, "&eUnable to find any active reports for your user. Run '/" + label + " start' to start gathering data.");
        }
    }

    private void handleTp(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player)) {
            msgPrefix(sender, "&eYou must be a player to use this command!");
            return;
        }

        // tp x z world
        if (args.length < 4) {
            sendUsage(sender, label);
            return;
        }

        int x, z;
        World world;

        try {
            x = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            msgPrefix(sender, "&eExpected an integer for <x>, but got '" + args[1] + "' instead.");
            return;
        }

        try {
            z = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            msgPrefix(sender, "&eExpected an integer for <z>, but got '" + args[1] + "' instead.");
            return;
        }

        world = plugin.getServer().getWorld(args[3]);
        if (world == null) {
            msgPrefix(sender, "&eWorld '" + args[3] + "' does not exist.");
            return;
        }

        ((Player) sender).teleport(world.getHighestBlockAt(world.getChunkAt(x, z).getBlock(8, 0, 8).getLocation()).getLocation());
        msgPrefix(sender, "&eTeleported you to chunk x=" + x + ", z=" + z + ", world=" + world.getName());
    }

    private void sendUsage(CommandSender sender, String label) {
        msgPrefix(sender, "Running &e" + plugin.getDescription().getFullName() + "&7.");
        if (sender.hasPermission("findlag.use")) {
            msg(sender, "&8> &7/" + label + " start");
            msg(sender, "&8> &7/" + label + " cancel");
            msg(sender, "&8> &7/" + label + " report");
        }
        if (sender.hasPermission("findlag.tpchunk")) {
            msg(sender, "&8> &7/" + label + " tpchunk <x> <z> <world>");
        }
    }

    public static void msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static void msgPrefix(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&lFindLag &e" + msg));
    }
}