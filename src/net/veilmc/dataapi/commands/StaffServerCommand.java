package net.veilmc.dataapi.commands;

import net.minecraft.util.com.google.common.io.ByteArrayDataOutput;
import net.minecraft.util.com.google.common.io.ByteStreams;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

public class StaffServerCommand implements CommandExecutor{
    private DataAPI plugin;

    public StaffServerCommand(final DataAPI plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(final CommandSender sender, final Command comm, final String label, final String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players nigger.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /staffserver <server>");
            return false;
        }

        Jedis jedis = plugin.getJedisPool().getResource();
        jedis.set("data:global:" + player.getUniqueId().toString(), "tp " + args[1]);
        jedis.expire("data:global:" + player.getUniqueId().toString(), 30);
        plugin.getJedisPool().returnResource(jedis);
        jedis.close();

        final ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("Connect");
        dataOutput.writeUTF(args[0]);
        ((Player)sender).sendPluginMessage(plugin, "BungeeCord", dataOutput.toByteArray());
        sender.sendMessage(ChatColor.YELLOW + "Sending you to " + ChatColor.GOLD + args[0]);

        return true;
    }
}