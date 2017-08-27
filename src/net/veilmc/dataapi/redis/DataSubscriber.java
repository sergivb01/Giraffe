package net.veilmc.dataapi.redis;

import com.customhcf.base.BasePlugin;
import com.customhcf.util.chat.ClickAction;
import com.customhcf.util.chat.Text;
import net.veilmc.dataapi.DataAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class DataSubscriber{
    private JedisPubSub jedisPubSub;
    private Jedis jedis;
    private DataAPI main;

    public DataSubscriber(final DataAPI main) {
        this.main = main;
        this.jedis = new Jedis(main.getConfig().getString("redis-server"));
        this.subscribe();
    }

    public void subscribe() {
        this.jedisPubSub = this.get();
        new Thread() {
            @Override
            public void run() {
                DataSubscriber.this.jedis.subscribe(DataSubscriber.this.jedisPubSub, "ares");
            }
        }.start();
    }

    private JedisPubSub get() {
        return new JedisPubSub() {
            @Override
            public void onMessage(final String channel, final String message) {
                if (channel.equalsIgnoreCase("ares")) {
                    final String[] args = message.split(";");
                    if (args.length > 2) {
                        final String command = args[0].toLowerCase();
                        final String sender = args[1];
                        final String server = args[2];
                        final String msg = args[3];
                        switch(command){
                            case "staffchat":
                                for(Player staff : Bukkit.getOnlinePlayers()){
                                    if(staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        staff.sendMessage(ChatColor.DARK_AQUA + "[" + server + "] " + ChatColor.AQUA + sender + ": " + msg);
                                    }
                                }
                                break;
                            case "request":
                                for(Player staff : Bukkit.getOnlinePlayers()){
                                    if(staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        //staff.sendMessage(ChatColor.BLUE + "[Request] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " requested assistance.");
                                        new Text(ChatColor.BLUE + "[Request] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " requested assistance.").setHoverText(ChatColor.BLUE + "Click here to teleport to " + ChatColor.GRAY + sender).setClick(ClickAction.RUN_COMMAND, "/staffserver " + server + " " + sender).send(staff);
                                        staff.sendMessage(ChatColor.BLUE + "    Reason: " + ChatColor.GRAY + msg);
                                    }
                                }
                                break;
                            case "report":
                                final String target = args[4];
                                for(Player staff : Bukkit.getOnlinePlayers()){
                                    if(staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        //staff.sendMessage(ChatColor.RED + "[Report] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " has reported " + ChatColor.RESET + target + ChatColor.GRAY + ".");
                                        new Text(ChatColor.RED + "[Report] " + ChatColor.GRAY + "[" + server + "] " + ChatColor.RESET + sender + ChatColor.GRAY + " has reported " + ChatColor.RESET + target + ChatColor.GRAY + ".").setHoverText(ChatColor.RED + "Click here to teleport to " + ChatColor.GRAY + target).setClick(ClickAction.RUN_COMMAND, "/staffserver " + server + " " + target).send(staff);
                                        staff.sendMessage(ChatColor.RED + "    Reason:" + ChatColor.GRAY + msg.replace(target, ""));
                                    }
                                }
                                break;
                            case "staffswitch":
                                for(Player staff : Bukkit.getOnlinePlayers()){
                                    if(staff.hasPermission("rank.staff") && BasePlugin.getPlugin().getUserManager().getUser(staff.getUniqueId()).isStaffChatVisible()) {
                                        staff.sendMessage(ChatColor.DARK_AQUA + "[" + server + "] " + ChatColor.AQUA + sender + " " + msg);
                                    }
                                }
                                break;
                        }

                    }
                }
            }

            @Override
            public void onPMessage(String s, String s1, String s2) {

            }

            @Override
            public void onSubscribe(String s, int i) {

            }

            @Override
            public void onUnsubscribe(String s, int i) {

            }

            @Override
            public void onPUnsubscribe(String s, int i) {

            }

            @Override
            public void onPSubscribe(String s, int i) {

            }
        };
    }

    public JedisPubSub getJedisPubSub() {
        return this.jedisPubSub;
    }

    public Jedis getJedis() {
        return this.jedis;
    }
}
