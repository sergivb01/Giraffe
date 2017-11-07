package me.sergivb01.giraffe.commands;

import me.sergivb01.giraffe.Giraffe;
import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
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


        final ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("Connect");
        dataOutput.writeUTF(args[0]);
        player.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.GOLD + args[0]);
        player.sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());

        return true;
    }
}