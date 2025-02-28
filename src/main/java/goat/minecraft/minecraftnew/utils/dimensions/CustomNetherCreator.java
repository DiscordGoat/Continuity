package goat.minecraft.minecraftnew.utils.dimensions;

import org.bukkit.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.WorldCreator;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

public class CustomNetherCreator {

    /**
     * Initializes the custom nether world and arena if it doesn't already exist.
     * Call this method in your plugin's onEnable().
     *
     * @param plugin the main plugin instance.
     */
    public static void init(JavaPlugin plugin) {
        World customNether = Bukkit.getWorld("custom_nether");
        if (customNether == null) {
            // Create the custom nether using a flat world generator.
            WorldCreator wc = new WorldCreator("custom_nether");
            wc.environment(World.Environment.NETHER); // You can change this to NETHER if desired
            wc.type(WorldType.FLAT);
            wc.generator(new CustomNetherGenerator());
            customNether = wc.createWorld();
            plugin.getLogger().info("Created custom_nether world.");
            buildArena(customNether, plugin);
        } else {
            plugin.getLogger().info("custom_nether already exists. Arena creation skipped.");
        }
    }

    /**
     * Builds the arena in the custom nether.
     * The arena consists of:
     * - A circular bastion-esque platform with Nether Bricks and scattered Glowstone for lighting.
     * - A lava ocean beneath the arena.
     * - Bedrock walls (and a ceiling) extending from -200 to +200 blocks around the arena center.
     *
     * @param world  The custom nether world.
     * @param plugin The main plugin instance.
     */
    private static void buildArena(World world, JavaPlugin plugin) {
        // Arena parameters
        int arenaCenterX = 0;
        int arenaCenterZ = 0;
        int floorY = 100;        // Arena floor level
        int arenaRadius = 30;    // Arena platform radius
        int lavaDepth = 10;      // Lava ocean depth beneath arena
        // Walls will be built from -200 to +200 blocks around the center
        int wallBoundary = 200;
        int ceilingY = 130;      // Bedrock ceiling level

        Random rand = new Random();

        // Build the arena floor:
        // For each block within the circular arena, place Nether Bricks,
        // with a 15% chance to instead place Glowstone for lighting.
        for (int x = -arenaRadius; x <= arenaRadius; x++) {
            for (int z = -arenaRadius; z <= arenaRadius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= arenaRadius) {
                    Location loc = new Location(world, arenaCenterX + x, floorY, arenaCenterZ + z);
                    if (rand.nextDouble() < 0.15) {
                        loc.getBlock().setType(Material.GLOWSTONE);
                    } else {
                        loc.getBlock().setType(Material.NETHER_BRICKS);
                    }
                }
            }
        }

        // Create a lava ocean beneath the arena: fill from (floorY - lavaDepth) to (floorY - 1)
        for (int x = -arenaRadius; x <= arenaRadius; x++) {
            for (int z = -arenaRadius; z <= arenaRadius; z++) {
                double distance = Math.sqrt(x * x + z * z);
                if (distance <= arenaRadius) {
                    for (int y = floorY - lavaDepth; y < floorY; y++) {
                        Location loc = new Location(world, arenaCenterX + x, y, arenaCenterZ + z);
                        loc.getBlock().setType(Material.LAVA);
                    }
                }
            }
        }

        // Build bedrock walls around the arena.
        // Walls will be built along the perimeter defined by wallBoundary.
        int wallMin = -wallBoundary;
        int wallMax = wallBoundary;
        for (int x = wallMin; x <= wallMax; x++) {
            for (int y = floorY; y <= ceilingY; y++) {
                // North and South walls
                new Location(world, arenaCenterX + x, y, arenaCenterZ + wallMin).getBlock().setType(Material.BEDROCK);
                new Location(world, arenaCenterX + x, y, arenaCenterZ + wallMax).getBlock().setType(Material.BEDROCK);
            }
        }
        for (int z = wallMin; z <= wallMax; z++) {
            for (int y = floorY; y <= ceilingY; y++) {
                // West and East walls
                new Location(world, arenaCenterX + wallMin, y, arenaCenterZ + z).getBlock().setType(Material.BEDROCK);
                new Location(world, arenaCenterX + wallMax, y, arenaCenterZ + z).getBlock().setType(Material.BEDROCK);
            }
        }

        // Build a bedrock ceiling over the entire arena area (from wallMin to wallMax)
        for (int x = wallMin; x <= wallMax; x++) {
            for (int z = wallMin; z <= wallMax; z++) {
                new Location(world, arenaCenterX + x, ceilingY, arenaCenterZ + z).getBlock().setType(Material.BEDROCK);
            }
        }

        plugin.getLogger().info("Custom Nether arena built successfully.");
    }

    /**
     * A minimal custom ChunkGenerator that returns empty chunks.
     * (This generator ensures the world is flat; you can modify it as needed.)
     */
    public static class CustomNetherGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            // Create an empty chunk; our arena is built manually later.
            ChunkData chunkData = createChunkData(world);
            return chunkData;
        }
    }
}
