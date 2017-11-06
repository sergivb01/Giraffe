package me.sergivb01.giraffe.utils.report;

import me.sergivb01.giraffe.Giraffe;
import me.sergivb01.giraffe.utils.lag.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BukkitReportListener extends AbstractReportListener {

    private final Map<ChunkPosition, AtomicInteger> redstone = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> hoppers = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> hopperItems = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> pistons = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> physics = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> vehicles = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> blockGrowth = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> livingEntitySpawns = new HashMap<>();
    private final Map<ChunkPosition, AtomicInteger> itemSpawns = new HashMap<>();

    public BukkitReportListener(Giraffe plugin) {
        super(plugin);
    }

    @Override
    public void appendData(StringBuilder report) {
        appendReport(report, redstone, "Redstone", "This section shows the total number of redstone updates per chunk.", true);
        appendReport(report, hoppers, "Hoppers", "This section shows the total number of hopper item movements per chunk.", true);
        appendReport(report, hopperItems, "Hopper Item Pickup", "This section shows the total number of hopper item pickups per chunk.", true);
        appendReport(report, pistons, "Pistons", "This section shows the total number of piston extensions/retractions per chunk.", true);
        appendReport(report, physics, "Physics", "This section shows the total number of physics updates per chunk.", true);
        appendReport(report, vehicles, "Vehicles", "This section shows the total number of vehicle movements per chunk.", true);
        appendReport(report, blockGrowth, "Block Growth", "This section shows the total number of natural block 'growths' per chunk.", true);
        appendReport(report, livingEntitySpawns, "Living Entity Spawns", "This section shows the total number of living entity spawns per chunk.", true);
        appendReport(report, itemSpawns, "Item Spawns", "This section shows the total number of item spawns per chunk.", true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRedstone(BlockRedstoneEvent e) {
        redstone.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopper(InventoryMoveItemEvent e) {
        Location location = null;

        if (e.getInitiator().getType() == InventoryType.HOPPER) {
            location = ((Hopper)e.getInitiator().getHolder()).getLocation();
        } else if (e.getSource().getType() == InventoryType.HOPPER) {
            location = ((Hopper)e.getSource()).getLocation();
        } else if (e.getDestination().getType() == InventoryType.HOPPER) {
            location = ((Hopper)e.getDestination()).getLocation();
        }

        if (location != null) {
            hoppers.computeIfAbsent(ChunkPosition.of(location), ZERO_COUNTER).incrementAndGet();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperPickup(InventoryPickupItemEvent e) {
        if (e.getInventory().getType() == InventoryType.HOPPER) {
            Location location = ((Hopper)e.getInventory().getHolder()).getLocation();
            if (location != null) {
                hopperItems.computeIfAbsent(ChunkPosition.of(location), ZERO_COUNTER).incrementAndGet();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        pistons.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
        for (Block block : e.getBlocks()) {
            pistons.computeIfAbsent(ChunkPosition.of(block), ZERO_COUNTER).incrementAndGet();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        pistons.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
            pistons.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent e) {
        physics.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicle(VehicleMoveEvent e) {
        // ignore if the vehicle hasn't moved over a block boundary
        if (e.getFrom().getBlockX() == e.getTo().getBlockX() && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        vehicles.computeIfAbsent(ChunkPosition.of(e.getTo()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        blockGrowth.computeIfAbsent(ChunkPosition.of(e.getBlock()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        blockGrowth.computeIfAbsent(ChunkPosition.of(e.getLocation()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        livingEntitySpawns.computeIfAbsent(ChunkPosition.of(e.getLocation()), ZERO_COUNTER).incrementAndGet();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        itemSpawns.computeIfAbsent(ChunkPosition.of(e.getLocation()), ZERO_COUNTER).incrementAndGet();
    }

}