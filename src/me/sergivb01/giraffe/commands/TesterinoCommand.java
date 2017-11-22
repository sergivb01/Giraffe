package me.sergivb01.giraffe.commands;

import me.sergivb01.giraffe.Giraffe;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TesterinoCommand implements CommandExecutor{
    private Giraffe plugin;

    public TesterinoCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if(args.length < 1){
            sender.sendMessage(ChatColor.RED + "Choose your testerino dude :(");
            return false;
        }
        String choice = args[0].toLowerCase();
        sender.sendMessage(choice);

        return true;
    }
}