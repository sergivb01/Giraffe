package me.sergivb01.giraffe.utils.report;

import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.lag.ChunkPosition;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.SpawnerSpawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SpigotReportListener extends BukkitReportListener {

    private final Map<ChunkPosition, AtomicInteger> mobSpawners = new HashMap<>();

    public SpigotReportListener(Giraffe plugin) {
        super(plugin);
    }

    @Override
    public void appendData(StringBuilder report) {
        super.appendData(report);
        appendReport(report, mobSpawners, "Mob Spawners", "This section shows the total number of mob spawner spawns per chunk.", true);
    }

    @EventHandler(priority = EventPriority.LOWEST) // log all attempted spawns, even if they were cancelled.
    public void onMobSpawnerSpawn(SpawnerSpawnEvent e) {
        mobSpawners.computeIfAbsent(ChunkPosition.of(e.getLocation()), ZERO_COUNTER).incrementAndGet();
    }

}