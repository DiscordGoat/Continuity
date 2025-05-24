// src/main/java/goat/minecraft/minecraftnew/regions/terrain/Beach.java
package goat.minecraft.minecraftnew.regions.terrain;

import org.bukkit.Material;
import org.bukkit.World;

public class Beach {
    private final World world;
    public Beach(World world) { this.world = world; }

    // existing whole‐chunk beach generator
    /**
     * Generates beach terrain for an entire chunk.
     * Sets sand for 5 blocks (y=60-64), sandstone for 3 blocks (y=57-59),
     * stone down to y=1, deepslate down to y=-63, and bedrock at y=-64.
     */
    public void generateBeachChunk(int cx, int cz) {
        int startX = cx << 4;
        int startZ = cz << 4;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = startX + dx;
                int wz = startZ + dz;
                genBeachColumn(wx, wz);
            }
        }
    }

    /**
     * Checks if a chunk is entirely beach (all blocks are YELLOW_CONCRETE)
     */
    public boolean isPureBeachChunk(int cx, int cz) {
        int startX = cx << 4;
        int startZ = cz << 4;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                Material surfaceMaterial = world.getBlockAt(startX + dx, 63, startZ + dz).getType();
                if (surfaceMaterial != Material.YELLOW_CONCRETE) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Generates a *single column* of beach at (wx,wz). */
    public void genBeachColumn(int wx, int wz) {
        // 5 sand layers
        for (int y = 63; y >= 60; y--) {
            world.getBlockAt(wx, y, wz).setType(Material.SAND);
        }
        // 3 sandstone
        for (int y = 59; y >= 57; y--) {
            world.getBlockAt(wx, y, wz).setType(Material.SANDSTONE);
        }
        // stone down to y=1
        for (int y = 56; y >= 1; y--) {
            world.getBlockAt(wx, y, wz).setType(Material.STONE);
        }
        // deepslate down to -63
        for (int y = 0; y >= -63; y--) {
            world.getBlockAt(wx, y, wz).setType(Material.DEEPSLATE);
        }
        world.getBlockAt(wx, -64, wz).setType(Material.BEDROCK);
    }
}
