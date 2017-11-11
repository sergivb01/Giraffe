package me.sergivb01.giraffe.utils.report;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.commands.FindLagCommand;
import me.sergivb01.giraffe.utils.TaskUtil;
import me.sergivb01.giraffe.utils.lag.ChunkPosition;
import me.sergivb01.giraffe.utils.lag.PasteUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractReportListener implements ReportListener {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    // functions to create zero'd counters
    static final Function<ChunkPosition, AtomicInteger> ZERO_COUNTER = o -> new AtomicInteger(0);
    private static final Function<EntityType, AtomicInteger> ZERO_COUNTER_ENTITY = o -> new AtomicInteger(0);

    private final Giraffe plugin;

    // the time when the listener started
    private long startSeconds = -1;

    // the ticks when the listener started & stopped.
    private long startTick = -1;
    private long endTick = -1;

    AbstractReportListener(Giraffe plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        startSeconds = System.currentTimeMillis() / 1000L;
        startTick = plugin.getTickCounter().getTick();
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(this);
        endTick = plugin.getTickCounter().getTick();
    }

    public abstract void appendData(StringBuilder sb);

    public void sendReport(CommandSender sender) {
        if (endTick == -1) {
            throw new IllegalStateException("Listener not yet stopped");
        }

        Map<EntityType, AtomicInteger> entityStats = gatherEntityStats();
        List<Map<ChunkPosition, AtomicInteger>> chunkEntityStats = gatherChunkEntityStats();

        int ticks = (int) (endTick - startTick);
        long time = (System.currentTimeMillis() / 1000L) - startSeconds;
        String startDate = DATE_FORMAT.format(new Date(startSeconds * 1000L));

        TaskUtil.runTaskAsyncNextTick(() -> {
            FindLagCommand.msgPrefix(sender, "&eBuilding report. Please wait...");

            StringBuilder report = new StringBuilder();
            report.append("# FindLag Report\n");
            report.append("**This report was automatically generated by [Giraffe](https://twitter.com/sergivb01).**\n\n");
            report.append("**Report issues at:** @sergivb01 on Slack\n");
            report.append("**Report duration:** ").append(time).append(" seconds   \n");
            report.append("**Report duration (ticks):** ").append(ticks).append("   \n");
            report.append("**Ran ticks (%):** ").append((ticks * (time * 20)) / time).append("% of ticks were ran").append("   \n");
            report.append("**Report time:** ").append(startDate).append("   \n\n");

            appendData(report);

            appendReport(report, chunkEntityStats.get(0), "Total Entities", "This section shows the total number of entities per chunk, at the time when the report was generated.", false);
            appendReport(report, chunkEntityStats.get(1), "Total Items", "This section shows the total number of item entities per chunk, at the time when the report was generated.", false);
            appendReport(report, chunkEntityStats.get(2), "Total Tile Entities", "This section shows the total number of tile entities per chunk, at the time when the report was generated.", false);

            appendEnumReport(report, entityStats, "Entity Distribution", "This section shows the overall entity distribution across all worlds.");

            String url = PasteUtils.paste("FindLag Report", ImmutableList.of(Maps.immutableEntry("findlag-report.md", report.toString())));

            FindLagCommand.msgPrefix(sender, "&eReport URL:");
            FindLagCommand.msgPrefix(sender, "&7" + url);
        });
    }

    private List<Map<ChunkPosition, AtomicInteger>> gatherChunkEntityStats() {
        Map<ChunkPosition, AtomicInteger> entities = new HashMap<>();
        Map<ChunkPosition, AtomicInteger> items = new HashMap<>();
        Map<ChunkPosition, AtomicInteger> tiles = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                ChunkPosition chunkPosition = ChunkPosition.of(chunk);

                Entity[] ents = chunk.getEntities();
                entities.computeIfAbsent(chunkPosition, ZERO_COUNTER).addAndGet(ents.length);

                for (Entity entity : ents) {
                    if (entity.getType() == EntityType.DROPPED_ITEM) {
                        items.computeIfAbsent(chunkPosition, ZERO_COUNTER).incrementAndGet();
                    }
                }

                tiles.computeIfAbsent(chunkPosition, ZERO_COUNTER).addAndGet(chunk.getTileEntities().length);
            }
        }

        return ImmutableList.of(entities, items, tiles);
    }

    private Map<EntityType, AtomicInteger> gatherEntityStats() {
        Map<EntityType, AtomicInteger> entities = new HashMap<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                entities.computeIfAbsent(entity.getType(), ZERO_COUNTER_ENTITY).incrementAndGet();
            }
        }

        return entities;
    }

    void appendReport(StringBuilder report, Map<ChunkPosition, AtomicInteger> data, String name, String description, boolean avg) {
        report.append("## ").append(name).append("\n");
        report.append("__").append(description).append("__\n\n");

        if (data.isEmpty()) {
            report.append("Nothing here. :(\n\n");
            return;
        }

        if (avg) {
            report.append("| # | Chunk | Count | Avg count per tick |\n");
            report.append("|---|-------|-------|--------------------|\n");
        } else {
            report.append("| # | Chunk | Count |\n");
            report.append("|---|-------|-------|\n");
        }


        List<Map.Entry<ChunkPosition, Integer>> sorted = data.entrySet().stream()
                .filter(e -> e.getValue().get() > 0)
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().get()))
                .sorted(Collections.reverseOrder((o1, o2) -> {
                    if (o1.equals(o2)) {
                        return 0;
                    }

                    int i = Integer.compare(o1.getValue(), o2.getValue());
                    if (i != 0) {
                        return i;
                    }

                    ChunkPosition chunk1 = o1.getKey();
                    ChunkPosition chunk2 = o2.getKey();

                    i = Integer.compare(chunk1.getX(), chunk2.getX());
                    if (i != 0) {
                        return i;
                    }

                    i = Integer.compare(chunk1.getZ(), chunk2.getZ());
                    if (i != 0) {
                        return i;
                    }

                    return chunk1.getWorld().compareTo(chunk2.getWorld());
                }))
                .limit(25)
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<ChunkPosition, Integer> e = sorted.get(i);

            report.append("| ")
                    .append(i + 1)
                    .append(" | `")
                    .append(e.getKey().getX())
                    .append(" ")
                    .append(e.getKey().getZ())
                    .append(" ")
                    .append(e.getKey().getWorld())
                    .append("` | ")
                    .append(e.getValue());

            if (avg) {
                double count = e.getValue();
                double ticks = endTick - startTick;
                double average = count / ticks;

                report.append(" | ").append(String.format("%.2f", average));
            }

            report.append(" |\n");
        }

        report.append("\n\n");
        data.clear();
    }

    private <T extends Enum<T>> void appendEnumReport(StringBuilder report, Map<T, AtomicInteger> data, String name, String description) {
        report.append("## ").append(name).append("\n");
        report.append("__").append(description).append("__\n\n");

        if (data.isEmpty()) {
            report.append("Nothing here. :(\n\n");
            return;
        }

        report.append("| # | Type | Count |\n");
        report.append("|---|------|-------|\n");

        List<Map.Entry<T, Integer>> sorted = data.entrySet().stream()
                .filter(e -> e.getValue().get() > 0)
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().get()))
                .sorted(Collections.reverseOrder((o1, o2) -> {
                    if (o1.equals(o2)) {
                        return 0;
                    }

                    int i = Integer.compare(o1.getValue(), o2.getValue());
                    if (i != 0) {
                        return i;
                    }

                    T et1 = o1.getKey();
                    T et2 = o2.getKey();

                    return et1.compareTo(et2);
                }))
                .limit(25)
                .collect(Collectors.toList());

        for (int i = 0; i < sorted.size(); i++) {
            Map.Entry<T, Integer> e = sorted.get(i);

            report.append("| ")
                    .append(i + 1)
                    .append(" | `")
                    .append(e.getKey().name())
                    .append("` | ")
                    .append(e.getValue())
                    .append(" |\n");
        }

        report.append("\n\n");
        data.clear();
    }

}