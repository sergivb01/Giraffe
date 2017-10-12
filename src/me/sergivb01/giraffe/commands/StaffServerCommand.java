package me.sergivb01.giraffe.commands;

import me.sergivb01.giraffe.Giraffe;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffServerCommand implements CommandExecutor{
    private Giraffe plugin;

    public StaffServerCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command comm, final String label, final String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players nigger.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length <= 1) {
            player.sendMessage(ChatColor.RED + "Usage: /staffserver <server> <player_to_teleport>");
            player.sendMessage(ChatColor.RED + "Example: " + ChatColor.GRAY + "/staffserver kits Hacker_ProGamer25");
            return false;
        }

        Bukkit.dispatchCommand(player, "tp " + args[1]);

        if(!args[0].equalsIgnoreCase(this.plugin.getServerName())) {
            final ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
            dataOutput.writeUTF("Connect");
            dataOutput.writeUTF(args[0]);
            ((Player) sender).sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
            sender.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.GOLD + args[0]);
        }

        return true;
    }
}