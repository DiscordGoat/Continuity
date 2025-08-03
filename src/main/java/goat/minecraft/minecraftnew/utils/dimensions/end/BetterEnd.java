package goat.minecraft.minecraftnew.utils.dimensions.end;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * CustomEndArchipelago creates a separate End world (named "custom_end")
 * with a custom chunk generator that produces natural islands with irregular boundaries,
 * a naturally curving underside that slopes inward, rectangular obsidian pillars,
 * and floating parkour platforms.
 *
 * Players entering an End portal are redirected to this custom End.
 *
 * Usage: call CustomEndArchipelago.init(plugin) in your main onEnable().
 */
public class BetterEnd implements Listener {

    // ---------------- Configuration ----------------

    /** Name of the custom End world. */
    private static final String CUSTOM_END_WORLD_NAME = "custom_end";

    // Island generation parameters
    private static final int ISLAND_COUNT = 30;          // Number of islands
    private static final int MAX_DISTRIBUTION_RADIUS = 250; // Islands are clustered closer together
    private static final int MIN_ISLAND_RADIUS = 30;
    private static final int MAX_ISLAND_RADIUS = 50;

    // Base height for islands
    private static final int ISLAND_BASE_Y = 30;
    // Plateau top height offset (flat top)
    private static final int MIN_ISLAND_HEIGHT_OFFSET = 2;
    private static final int MAX_ISLAND_HEIGHT_OFFSET = 8;

    // Pillar parameters
    private static final int MIN_PILLAR_HEIGHT = 10;
    private static final int MAX_PILLAR_HEIGHT = 100;

    // Noise parameters for island boundary and vertical variation
    private static final double NOISE_AMPLITUDE = 0.3; // increased for jagged, irregular edges
    private static final double NOISE_VERTICAL_AMPLITUDE = 1.2; // vertical variation
    private static final double NOISE_FREQUENCY = 0.1;

    // Parkour (platform) parameters
    private static final int MIN_PLATFORMS = 30;
    private static final int MAX_PLATFORMS = 30;
    private static final int PLATFORM_SIZE = 2; // 2x2 block platform

    // ---------------- Fields ----------------

    private static World customEndWorld = null;
    private static boolean initialized = false;

    // ---------------- Public Init Method ----------------

    public static void init(Plugin plugin) {
        if (initialized) return;
        initialized = true;

        // Create/load the custom End world with our generator.
        WorldCreator creator = new WorldCreator(CUSTOM_END_WORLD_NAME);
        creator.environment(World.Environment.THE_END);
        creator.type(WorldType.NORMAL);
        creator.generator(new ArchipelagoChunkGenerator());
        customEndWorld = Bukkit.createWorld(creator);

        // Register event listeners for redirecting players.
        Bukkit.getPluginManager().registerEvents(new BetterEnd(), plugin);

        // Optionally ensure the Ender Dragon is spawned in this custom world.
        spawnDragonIfNone(plugin);
    }

    private static void spawnDragonIfNone(Plugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (customEndWorld == null) return;
                if (customEndWorld.getEntitiesByClass(org.bukkit.entity.EnderDragon.class).isEmpty()) {
                    customEndWorld.spawnEntity(new Location(customEndWorld, 0, 100, 0), EntityType.ENDER_DRAGON);
                }
            }
        }.runTaskLater(plugin, 100L);
    }

    // ---------------- Event Listeners ----------------

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
        if (event.getWorld().getName().equals(CUSTOM_END_WORLD_NAME)) {
            // Additional adjustments can be added here if needed.
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getCause() == PlayerPortalEvent.TeleportCause.END_PORTAL) {
            if (customEndWorld != null) {
                event.setCancelled(true);
                Location target = new Location(customEndWorld, 0.5, 80, 0.5);
                event.getPlayer().teleport(target);

                // Begin the dragon fight when the first player arrives.
                MinecraftNew.getInstance().getDragonFightManager().startFight(customEndWorld);
            }
        }
    }


    // ---------------- Inner Classes ----------------


    /**
     * The custom chunk generator that produces islands with irregular boundaries,
     * a naturally sloping underside, rectangular obsidian pillars, and floating parkour platforms.
     */
    private static class ArchipelagoChunkGenerator extends ChunkGenerator {

        private final List<IslandInfo> islands;
        private final Random rand;

        /**
         * Represents one island's settings.
         */
        private static class IslandInfo {
            final int centerX, centerZ;
            final int radius;            // guideline radius
            final int heightOffset;      // plateau top height offset
            final boolean hasPillar;
            final int pillarHeight;
            final int pillarWidthX;      // rectangular pillar width in X
            final int pillarWidthZ;      // rectangular pillar width in Z
            final long noiseSeed;        // seed for noise variation

            IslandInfo(int cx, int cz, int r, int heightOffset, boolean p, int ph, int pwX, int pwZ, long noiseSeed) {
                centerX = cx;
                centerZ = cz;
                radius = r;
                this.heightOffset = heightOffset;
                hasPillar = p;
                pillarHeight = ph;
                pillarWidthX = pwX;
                pillarWidthZ = pwZ;
                this.noiseSeed = noiseSeed;
            }
        }

        public ArchipelagoChunkGenerator() {
            rand = new Random();
            islands = new ArrayList<>();

            // Generate island data.
            for (int i = 0; i < ISLAND_COUNT; i++) {
                double angle = rand.nextDouble() * 2 * Math.PI;
                double distance = rand.nextDouble() * MAX_DISTRIBUTION_RADIUS;
                int cx = (int) (Math.cos(angle) * distance);
                int cz = (int) (Math.sin(angle) * distance);

                int islandRadius = MIN_ISLAND_RADIUS + rand.nextInt(MAX_ISLAND_RADIUS - MIN_ISLAND_RADIUS + 1);
                int heightOffset = MIN_ISLAND_HEIGHT_OFFSET + rand.nextInt(MAX_ISLAND_HEIGHT_OFFSET - MIN_ISLAND_HEIGHT_OFFSET + 1);

                boolean hasPillar = rand.nextInt(100) < 60; // 60% chance for a pillar
                int pillarHeight = 0;
                int pillarWidthX = 0, pillarWidthZ = 0;
                if (hasPillar) {
                    pillarHeight = MIN_PILLAR_HEIGHT + rand.nextInt(MAX_PILLAR_HEIGHT - MIN_PILLAR_HEIGHT + 1);
                    // Pillar dimensions: width X between 2 and 4, width Z between 2 and 4.
                    pillarWidthX = 2 + rand.nextInt(3);
                    pillarWidthZ = 2 + rand.nextInt(3);
                }
                long noiseSeed = rand.nextLong();
                islands.add(new IslandInfo(cx, cz, islandRadius, heightOffset, hasPillar, pillarHeight, pillarWidthX, pillarWidthZ, noiseSeed));
            }
        }

        @Override
        public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
            ChunkData data = createChunkData(world);

            // Set biome for the entire chunk.
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biome.setBiome(x, z, Biome.THE_END);
                }
            }

            int startX = chunkX << 4;
            int startZ = chunkZ << 4;

            // Generate islands.
            for (IslandInfo island : islands) {
                int minX = island.centerX - island.radius;
                int maxX = island.centerX + island.radius;
                int minZ = island.centerZ - island.radius;
                int maxZ = island.centerZ + island.radius;
                if (maxX < startX || minX > startX + 15 || maxZ < startZ || minZ > startZ + 15) {
                    continue;
                }
                generateIsland(data, island, startX, startZ);
                if (island.hasPillar) {
                    generatePillarAndPlatforms(data, island, startX, startZ);
                }
            }

            return data;
        }

        /**
         * Generate an island with an irregular boundary and a curved underside.
         *
         * The island uses a noise offset added to the Euclidean distance so its boundary is jagged.
         * The top is calculated similarly to before (a plateau in the center sloping off toward the edges),
         * while the bottom now slopes downward as you move outward.
         */
        private void generateIsland(ChunkData data, IslandInfo island, int chunkStartX, int chunkStartZ) {
            int chunkEndX = chunkStartX + 15;
            int chunkEndZ = chunkStartZ + 15;
            double plateauFraction = 0.6;
            double plateauRadius = island.radius * plateauFraction;

            for (int x = Math.max(island.centerX - island.radius, chunkStartX); x <= Math.min(island.centerX + island.radius, chunkEndX); x++) {
                for (int z = Math.max(island.centerZ - island.radius, chunkStartZ); z <= Math.min(island.centerZ + island.radius, chunkEndZ); z++) {
                    double dx = x - island.centerX;
                    double dz = z - island.centerZ;
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    // Compute noise offset to perturb the distance.
                    double noise = (Math.sin(x * NOISE_FREQUENCY + (island.noiseSeed % 1000))
                            + Math.cos(z * NOISE_FREQUENCY + (island.noiseSeed % 1000))) / 2.0;
                    // Add noise scaled by island radius.
                    double effectiveDistance = distance + noise * (island.radius * NOISE_AMPLITUDE);
                    if (effectiveDistance <= island.radius) {
                        int relX = x - chunkStartX;
                        int relZ = z - chunkStartZ;
                        double t = effectiveDistance / island.radius; // 0 at center, 1 at edge

                        // Compute top: flat in center, then slopes off quadratically.
                        int topY;
                        if (effectiveDistance <= plateauRadius) {
                            topY = ISLAND_BASE_Y + island.heightOffset;
                        } else {
                            double u = (effectiveDistance - plateauRadius) / (island.radius - plateauRadius);
                            u = u * u;
                            topY = ISLAND_BASE_Y + (int)(island.heightOffset * (1 - u));
                        }
                        // Compute bottom: slopes downward as t increases.
                        int bottomY = ISLAND_BASE_Y - (int)(island.heightOffset * (t * t));

                        // Optionally, add some vertical noise to top.
                        double vNoise = Math.sin(x * NOISE_FREQUENCY * 2 + (island.noiseSeed % 500))
                                * Math.cos(z * NOISE_FREQUENCY * 2 + (island.noiseSeed % 500));
                        int noiseOffset = (int)(vNoise * NOISE_VERTICAL_AMPLITUDE);
                        topY += noiseOffset;
                        // Ensure bottom is below top.
                        if (bottomY >= topY) bottomY = topY - 1;
                        // Fill the column from bottomY to topY.
                        for (int y = bottomY; y <= topY && y < data.getMaxHeight(); y++) {
                            data.setBlock(relX, y, relZ, Material.END_STONE);
                        }
                    }
                }
            }
        }

        /**
         * Generate a rectangular obsidian pillar and floating parkour platforms.
         */
        private void generatePillarAndPlatforms(ChunkData data, IslandInfo island, int chunkStartX, int chunkStartZ) {
            int pillarCenterX = island.centerX;
            int pillarCenterZ = island.centerZ;
            int pillarBaseY = ISLAND_BASE_Y + 5; // Start a few blocks above island surface.
            int pillarHeight = island.pillarHeight;
            int halfWidthX = island.pillarWidthX/2;
            int halfWidthZ = island.pillarWidthZ/2;

            // Build the rectangular pillar.
            for (int x = pillarCenterX - halfWidthX; x <= pillarCenterX - halfWidthX + island.pillarWidthX - 1; x++) {
                for (int z = pillarCenterZ - halfWidthZ; z <= pillarCenterZ - halfWidthZ + island.pillarWidthZ - 1; z++) {
                    if (x >= chunkStartX && x <= chunkStartX + 15 && z >= chunkStartZ && z <= chunkStartZ + 15) {
                        int relX = x - chunkStartX;
                        int relZ = z - chunkStartZ;
                        for (int y = 0; y < pillarHeight; y++) {
                            int actualY = pillarBaseY + y;
                            if (actualY < data.getMaxHeight()) {
                                data.setBlock(relX, actualY, relZ, Material.OBSIDIAN);
                            }
                        }
                    }
                }
            }
            // Place a marker block (GLOWSTONE) on top for End Crystal spawning.
            if (pillarCenterX >= chunkStartX && pillarCenterX <= chunkStartX + 15 &&
                    pillarCenterZ >= chunkStartZ && pillarCenterZ <= chunkStartZ + 15) {
                int relX = pillarCenterX - chunkStartX;
                int relZ = pillarCenterZ - chunkStartZ;
                if (pillarBaseY + pillarHeight < data.getMaxHeight()) {
                    data.setBlock(relX, pillarBaseY + pillarHeight, relZ, Material.GLOWSTONE);
                }
            }

            // Generate floating platforms around the pillar.
            int platformCount = MIN_PLATFORMS + rand.nextInt(MAX_PLATFORMS - MIN_PLATFORMS + 1);
            for (int i = 0; i < platformCount; i++) {
                int offsetY = pillarBaseY + 2 + rand.nextInt(pillarHeight - 4);
                double angle = rand.nextDouble() * 2 * Math.PI;
                // Random distance from pillar edge (platforms float a few blocks away)
                int offsetDistance = (int) ((Math.max(island.pillarWidthX, island.pillarWidthZ) / 2.0) + 2 + rand.nextDouble() * 3);
                int platformCenterX = pillarCenterX + (int)Math.round(offsetDistance * Math.cos(angle));
                int platformCenterZ = pillarCenterZ + (int)Math.round(offsetDistance * Math.sin(angle));
                // Build a small PLATFORM_SIZE x PLATFORM_SIZE platform centered at that point.
                for (int dx = -PLATFORM_SIZE/2; dx <= PLATFORM_SIZE/2; dx++) {
                    for (int dz = -PLATFORM_SIZE/2; dz <= PLATFORM_SIZE/2; dz++) {
                        int blockX = platformCenterX + dx;
                        int blockZ = platformCenterZ + dz;
                        if (blockX >= chunkStartX && blockX <= chunkStartX + 15 && blockZ >= chunkStartZ && blockZ <= chunkStartZ + 15) {
                            int relX = blockX - chunkStartX;
                            int relZ = blockZ - chunkStartZ;
                            if (offsetY < data.getMaxHeight()) {
                                data.setBlock(relX, offsetY, relZ, Material.OBSIDIAN);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public List<BlockPopulator> getDefaultPopulators(World world) {
            // Replace GLOWSTONE markers with obsidian and spawn an End Crystal.
            return Collections.singletonList(new BlockPopulator() {
                @Override
                public void populate(World world, Random random, org.bukkit.Chunk source) {
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < world.getMaxHeight(); y++) {
                            for (int z = 0; z < 16; z++) {
                                if (source.getBlock(x, y, z).getType() == Material.GLOWSTONE) {
                                    source.getBlock(x, y, z).setType(Material.OBSIDIAN);
                                    Location spawnLoc = new Location(world,
                                            (source.getX() << 4) + x + 0.5,
                                            y + 1.0,
                                            (source.getZ() << 4) + z + 0.5);
                                    world.spawnEntity(spawnLoc, EntityType.END_CRYSTAL);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
