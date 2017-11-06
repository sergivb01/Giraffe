package me.sergivb01.giraffe.utils.lag;


import com.google.common.base.Preconditions;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * An immutable and serializable chuck location object
 */
public final class ChunkPosition {

    public static ChunkPosition of(int x, int z, String world) {
        Preconditions.checkNotNull(world, "world");
        return new ChunkPosition(x, z, world);
    }

    public static ChunkPosition of(int x, int z, World world) {
        Preconditions.checkNotNull(world, "world");
        return of(x, z, world.getName());
    }

    public static ChunkPosition of(Chunk location) {
        Preconditions.checkNotNull(location, "location");
        return of(location.getX(), location.getZ(), location.getWorld().getName());
    }

    public static ChunkPosition of(Location location) {
        Preconditions.checkNotNull(location, "location");
        return of(location.getBlockX() >> 4, location.getBlockZ() >> 4, location.getWorld().getName());
    }

    public static ChunkPosition of(Block block) {
        Preconditions.checkNotNull(block, "block");
        return of(block.getX() >> 4, block.getZ() >> 4, block.getWorld().getName());
    }

    private final int x;
    private final int z;
    private final String world;

    private ChunkPosition(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public String getWorld() {
        return this.world;
    }

    public ChunkPosition getRelative(BlockFace face) {
        Preconditions.checkArgument(face != BlockFace.UP && face != BlockFace.DOWN, "invalid face");
        return ChunkPosition.of(x + face.getModX(), z + face.getModZ(), world);
    }

    public ChunkPosition getRelative(BlockFace face, int distance) {
        Preconditions.checkArgument(face != BlockFace.UP && face != BlockFace.DOWN, "invalid face");
        return ChunkPosition.of(x + (face.getModX() * distance), z + (face.getModZ() * distance), world);
    }

    public ChunkPosition add(int x, int z) {
        return ChunkPosition.of(this.x + x, this.z + z, world);
    }

    public ChunkPosition subtract(int x, int z) {
        return add(-x, -z);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ChunkPosition)) return false;
        final ChunkPosition other = (ChunkPosition) o;
        return this.getX() == other.getX() &&
                this.getZ() == other.getZ() &&
                (this.getWorld() == null ? other.getWorld() == null : this.getWorld().equals(other.getWorld()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getZ();
        result = result * PRIME + (this.getWorld() == null ? 43 : this.getWorld().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ChunkPosition(x=" + this.getX() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }

}