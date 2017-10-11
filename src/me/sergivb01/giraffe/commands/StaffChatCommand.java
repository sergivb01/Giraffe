package me.sergivb01.giraffe.commands;

import com.customhcf.base.BasePlugin;
import com.customhcf.base.command.BaseCommand;
import com.customhcf.base.user.ServerParticipator;
import me.sergivb01.giraffe.Giraffe;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffChatCommand implements CommandExecutor{
    private Giraffe plugin;

    public StaffChatCommand(final Giraffe plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command comm, final String label, final String[] args) {
        ServerParticipator target;
        ServerParticipator participator = BasePlugin.getPlugin().getUserManager().getParticipator(sender);
        if (participator == null) {
            sender.sendMessage(ChatColor.RED + "You are not allowed to do this.");
            return true;
        }
        if (args.length <= 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <message|playerName>");
                return true;
            }
            target = participator;
        } else {
            Player targetPlayer = Bukkit.getPlayerExact(args[0]);
            if (targetPlayer == null || !BaseCommand.canSee(sender, targetPlayer) || !sender.hasPermission("command.staffchat.others")) {
                plugin.getPublisher().write("staffchat;" + sender.getName() + ";" + Bukkit.getServerName() + ";" + StringUtils.join(args, " ").replace(";", ":"));
                return true;
            }
            target = BasePlugin.getPlugin().getUserManager().getUser(targetPlayer.getUniqueId());
        }
        boolean newStaffChat = !target.isInStaffChat() || args.length >= 2 && Boolean.parseBoolean(args[1]);
        target.setInStaffChat(newStaffChat);
        sender.sendMessage(ChatColor.YELLOW + "Staff chat mode of " + target.getName() + " set to " + newStaffChat + '.');
        //plugin.getPublisher().write("staffchat;" + player.getName() + ";" + Bukkit.getServerName() + ";" + StringUtils.join(args, " ").replace(";", ":"));
        return true;
    }
}