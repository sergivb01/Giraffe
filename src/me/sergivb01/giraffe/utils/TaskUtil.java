package me.sergivb01.giraffe.utils;

import me.sergivb01.giraffe.Giraffe;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class TaskUtil {
    private static final Giraffe plugin;

    public static int runTaskNextTick(final Runnable run) {
        return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, run, 1L);
    }

    public static int runTaskAsyncNextTick(final Runnable run) {
        return Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, run, 1L);
    }

    public static BukkitTask runTaskTimerAsync(final Runnable run, final long delay, final long period) {
        return Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, run, delay, period);
    }

    public static int scheduleTask(final Runnable run, final long delay) {
        return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, run, delay);
    }
    
    static{
        plugin = Giraffe.getInstance();
    }
    
    
}
