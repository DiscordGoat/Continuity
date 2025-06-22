package goat.minecraft.minecraftnew.subsystems.realms.utils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.*;

public class FrozenTerrainUtils {
    private static final int SEA_LEVEL = 64;
    private static final int ISLAND_RADIUS = 120;
    private static final int BEACH_IN = 15;
    private static final int BEACH_OUT = 16;

    private final SimplexNoiseGenerator boundaryNoise;
    private final SimplexNoiseGenerator floorNoise;
    private final SimplexNoiseGenerator ravineNoise;
    private final SimplexNoiseGenerator warpNoise;
    private final SimplexNoiseGenerator baseNoise;

    public FrozenTerrainUtils() {
        this.boundaryNoise = new SimplexNoiseGenerator(new Random().nextLong());
        this.floorNoise = new SimplexNoiseGenerator(new Random().nextLong());
        this.ravineNoise = new SimplexNoiseGenerator(new Random().nextLong());
        this.warpNoise = new SimplexNoiseGenerator(new Random().nextLong());
        this.baseNoise = new SimplexNoiseGenerator(new Random().nextLong());
    }

    /**
     * Generates ocean floor terrain for a chunk
     */
    public void genOceanFloor(World frozenWorld, int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) for (int dz = 0; dz < 16; dz++) {
            int wx = ox + dx, wz = oz + dz;
            double dist = Math.hypot(wx, wz);
            double br   = ISLAND_RADIUS + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;
            double t    = Math.min(dist / br, 1.0);

            double u = wx + warpNoise.noise(wx * 0.005, wz * 0.005) * 30;
            double v = wz + warpNoise.noise(wx * 0.005 + 100, wz * 0.005 + 100) * 30;

            double amp = 1, freq = 0.01, sum = 0;
            for (int o = 0; o < 6; o++) {
                sum += baseNoise.noise(u * freq, v * freq) * amp;
                amp *= 0.5;
                freq *= 2;
            }

            // Deepen ocean floor further and taper height more sharply
            double baseY = lerp(SEA_LEVEL - 25, 5, t); // Deeper ocean base
            double height = ((sum + 1) / 2.0) * 35 * Math.pow(t, 2.5); // Aggressive taper
            int floorY = clamp((int)(baseY + height), 5, SEA_LEVEL - 4); // Prevent surfacing

            double rv = ravineNoise.noise(wx * 0.1, wz * 0.1);
            boolean isRavine = rv > 0.6;
            if (isRavine) floorY -= (int) ((rv - 0.6) * 20);

            frozenWorld.getBlockAt(wx, 0, wz).setType(Material.BEDROCK);

            for (int y = 1; y <= floorY; y++) {
                Material mat;

                if (y > floorY - 3) {
                    mat = Material.GRAVEL;
                } else if (floorY >= 45 && y == floorY - 3 && Math.random() < 0.5) {
                    mat = Material.DEEPSLATE_DIAMOND_ORE;
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx2 = -1; dx2 <= 1; dx2++)
                            for (int dz2 = -1; dz2 <= 1; dz2++)
                                if (Math.abs(dx2) + Math.abs(dy) + Math.abs(dz2) <= 2)
                                    frozenWorld.getBlockAt(wx + dx2, y + dy, wz + dz2)
                                            .setType(Material.DEEPSLATE_DIAMOND_ORE);
                    continue;
                } else if (isRavine && y == floorY && Math.random() < 0.4) {
                    mat = Material.MAGMA_BLOCK;
                } else if (y < 30 && Math.random() < 0.01) {
                    mat = Material.MAGMA_BLOCK;
                } else {
                    mat = (y < 30 ? Material.DEEPSLATE : Material.STONE);
                }

                frozenWorld.getBlockAt(wx, y, wz).setType(mat);
            }
        }
    }

    /**
     * Generates island base terrain for a chunk
     */
    public void genIslandBase(World frozenWorld, int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;

                double dist = Math.hypot(wx, wz);
                double br   = ISLAND_RADIUS + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;

                // preserve original island "footprint"
                if (dist < br + BEACH_IN) {
                    // normalized [0=center → 1=edge]
                    double f = Math.min(dist / br, 1.0);
                    double slopeFactor = Math.pow(1.0 - f, 1.5);

                    int maxHeight = 20;
                    int topY = SEA_LEVEL + (int)(maxHeight * slopeFactor);
                    int baseY = SEA_LEVEL - 4;

                    // core: stone→dirt→grass as before
                    for (int y = baseY; y <= topY; y++) {
                        Material mat;
                        if (y == topY) {
                            mat = Material.SNOW_BLOCK;
                        } else if (y > SEA_LEVEL - 2) {
                            mat = Material.DIRT;
                        } else {
                            mat = Material.STONE;
                        }
                        frozenWorld.getBlockAt(wx, y, wz).setType(mat);
                    }

                    // now carve a staircase "slope" off the beach downward
                    // direction vector from island center
                    double dirX = wx / dist;
                    double dirZ = wz / dist;

                    for (int step = 0; ; step++) {
                        int sy = SEA_LEVEL - 1 - step;
                        if (sy <= 0) break;

                        int sx = (int)Math.round(wx + dirX * step);
                        int sz = (int)Math.round(wz + dirZ * step);

                        boolean placeGravel = (step % 2 == 1);
                        if (placeGravel) {
                            // ensure solid support
                            frozenWorld.getBlockAt(sx, sy - 1, sz).setType(Material.STONE);
                            frozenWorld.getBlockAt(sx,     sy,     sz).setType(Material.GRAVEL);
                        } else {
                            frozenWorld.getBlockAt(sx, sy, sz).setType(Material.STONE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Paints superior beach terrain for a chunk
     */
    public void paintSuperiorBeach(World frozenWorld, int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;

        // Beach height constants
        final int BEACH_MAX_HEIGHT = 67; // Maximum height for beach sand
        final int BEACH_SLOPE_DEPTH = 6; // How far inland the beach slope extends
        final int WATER_DEPTH = 3;      // How deep underwater to extend the sand

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;

                // Find the highest block at this position
                int highestY = frozenWorld.getHighestBlockYAt(wx, wz);
                Material surfaceMaterial = frozenWorld.getBlockAt(wx, highestY, wz).getType();

                // Check if block is at water level or if it's grass below our beach max height
                boolean isAtWaterLevel = highestY <= SEA_LEVEL;
                boolean isGrassNearWater = surfaceMaterial == Material.SNOW_BLOCK && highestY <= BEACH_MAX_HEIGHT;

                // Process blocks near water
                if (isAtWaterLevel || isGrassNearWater) {
                    // Check if this position is near water horizontally
                    boolean nearWater = false;
                    int searchRadius = BEACH_SLOPE_DEPTH;

                    // More efficient water detection - only search until we find water
                    searchLoop:
                    for (int sx = -searchRadius; sx <= searchRadius; sx++) {
                        for (int sz = -searchRadius; sz <= searchRadius; sz++) {
                            // Skip checking blocks that are too far away (use circular radius)
                            if (sx*sx + sz*sz > searchRadius*searchRadius) continue;

                            int checkX = wx + sx;
                            int checkZ = wz + sz;

                            // Find the top block at this position
                            int checkY = frozenWorld.getHighestBlockYAt(checkX, checkZ);

                            // If we find water at sea level, this is near water
                            if (frozenWorld.getBlockAt(checkX, checkY, checkZ).getType() == Material.WATER) {
                                nearWater = true;
                                break searchLoop;
                            }
                        }
                    }

                    // If we're near water or at water level, apply beach transformation
                    if (nearWater || isAtWaterLevel) {
                        // Apply a smooth slope down to the water
                        if (isGrassNearWater) {
                            // Calculate distance to nearest water (more accurate approach)
                            int distanceToWater = calculateDistanceToWater(frozenWorld, wx, wz, BEACH_SLOPE_DEPTH);

                            // Determine desired height based on distance to water
                            int desiredHeight = SEA_LEVEL +
                                    Math.min(3, (int)((float)distanceToWater / BEACH_SLOPE_DEPTH * (highestY - SEA_LEVEL)));

                            // Smooth the transition - cap max height
                            desiredHeight = Math.min(desiredHeight, BEACH_MAX_HEIGHT);

                            // Replace all blocks from desired height down to the original surface with sand
                            for (int y = highestY; y <= desiredHeight; y++) {
                                frozenWorld.getBlockAt(wx, y, wz).setType(Material.SNOW_BLOCK);
                            }

                            // Clear air above to make slope
                            for (int y = desiredHeight + 1; y <= highestY; y++) {
                                frozenWorld.getBlockAt(wx, y, wz).setType(Material.AIR);
                            }
                        }
                        // Handle underwater areas - replace with sand down to a certain depth
                        else if (isAtWaterLevel) {
                            // Replace underwater surface with sand
                            Block surfaceBlock = frozenWorld.getBlockAt(wx, highestY, wz);
                            if (surfaceBlock.getType() == Material.WATER) {
                                // Find actual ground under water
                                int groundY = highestY;
                                while (groundY > 0 && frozenWorld.getBlockAt(wx, groundY, wz).getType() == Material.WATER) {
                                    groundY--;
                                }
                            }
                        }
                    }

                    // Apply a direct y-height based replacement up to BEACH_MAX_HEIGHT
                    // This handles areas that aren't necessarily near water but are below our beach height
                    else if (highestY <= BEACH_MAX_HEIGHT && surfaceMaterial == Material.SNOW_BLOCK) {
                        frozenWorld.getBlockAt(wx, highestY, wz).setType(Material.SNOW_BLOCK);
                    }
                }
            }
        }
    }

    /**
     * Paints hills terrain for a chunk
     */
    public void paintHills(World frozenWorld, int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;
                double dist = Math.hypot(wx, wz);
                double br   = ISLAND_RADIUS + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;

                if (dist <= br + BEACH_IN) {
                    // warp + fractal noise
                    double u = wx + warpNoise.noise(wx * 0.005, wz * 0.005) * 30;
                    double v = wz + warpNoise.noise(wx * 0.005 + 100, wz * 0.005 + 100) * 30;
                    double amp = 1, freq = 0.01, sum = 0;
                    for (int o = 0; o < 5; o++) {
                        sum += baseNoise.noise(u * freq, v * freq) * amp;
                        amp  *= 0.5;
                        freq *= 2;
                    }

                    // scaled height + stronger fall-off exponent for gentler edges
                    double falloff = Math.pow(1 - (dist / br), 2.0);
                    double rawHgt  = (sum + 1) / 2 * 40 * falloff;
                    int   top      = SEA_LEVEL + (int)rawHgt;

                    // cap how many blocks we "grow" in one go to avoid sheer walls
                    int cur = frozenWorld.getHighestBlockYAt(wx, wz);
                    int maxRise = 4;
                    if (top > cur + maxRise) {
                        top = cur + maxRise;
                    }

                    // Check if we're too close to water before placing dirt/grass
                    boolean nearWater = isNearWater(frozenWorld, wx, wz, 2); // Check 2-block radius

                    // Only place hills if we're not too close to water and above sea level
                    if (!nearWater && top > SEA_LEVEL + 2) {
                        // Fill any air gaps with sand first (between current surface and new hill)
                        fillAirGapsWithSand(frozenWorld, wx, wz, cur, top);

                        // Then place the hill material on top
                        for (int y = cur + 1; y <= top; y++) {
                            Material m = (y == top ? Material.SNOW_BLOCK : Material.DIRT);
                            frozenWorld.getBlockAt(wx, y, wz).setType(m);
                        }

                        // occasional flower on the new grass
                        if (Math.random() < 0.02) {
                            Material flower = (Math.random() < 0.5
                                    ? Material.DANDELION
                                    : Material.POPPY);
                            frozenWorld.getBlockAt(wx, top + 1, wz).setType(flower);
                        }
                    }
                }
            }
        }
    }

    /**
     * Plants trees in a chunk
     */
    public void plantTree(World frozenWorld, int cx, int cz, Player player) {
        int ox = cx << 4, oz = cz << 4;
        Random R = new Random((long)cx * 3418731287L ^ cz * 1328979875L);
        for (int i = 0; i < 2; i++) {
            int x = ox + R.nextInt(16), z = oz + R.nextInt(16);
            int y = frozenWorld.getHighestBlockYAt(x, z);
            if (frozenWorld.getBlockAt(x, y - 1, z).getType() != Material.SNOW_BLOCK) continue;
            // trunk - make trees much taller
            int height = 10 + R.nextInt(6);
            for (int h = 0; h < height; h++) {
                frozenWorld.getBlockAt(x, y + h, z).setType(Material.SPRUCE_LOG);
            }
            // leaves - bigger canopy covered with snow
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = height - 4; dy <= height; dy++) {
                        if (Math.abs(dx) + Math.abs(dz) + (height - dy) <= 6) {
                            frozenWorld.getBlockAt(x + dx, y + dy, z + dz).setType(Material.SPRUCE_LEAVES);
                            // sprinkle snow layer
                            Block above = frozenWorld.getBlockAt(x + dx, y + dy + 1, z + dz);
                            if (above.getType() == Material.AIR) {
                                above.setType(Material.SNOW);
                            }
                        }
                    }
                }
            }
            if (player != null) {
                player.sendMessage("§7Tree planted at " + x + "," + y + "," + z);
            }
        }
    }

    /**
     * Levels the ground at the specified location.
     */
    public void levelGround(World frozenWorld, int startX, int targetY, int startZ, int width, int length, Material material) {
        // Set all blocks in the area to the target material at targetY
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                int x = startX + dx;
                int z = startZ + dz;

                // Replace block at targetY with desired material
                frozenWorld.getBlockAt(x, targetY, z).setType(material);

                // Clear any blocks above
                for (int y = targetY + 1; y <= targetY + 5; y++) {
                    frozenWorld.getBlockAt(x, y, z).setType(Material.AIR);
                }

                // Fill any gaps below to provide support
                for (int y = targetY - 1; y >= targetY - 3; y--) {
                    Block block = frozenWorld.getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR || block.getType() == Material.WATER) {
                        block.setType(Material.DIRT);
                    } else {
                        break; // Stop when we hit solid ground
                    }
                }
            }
        }
    }

    /**
     * Search all chunks for a size×size patch of contiguous sand,
     * and return the center location (at surface height) of the first one found.
     *
     * @param size Number of blocks along one edge of the square patch to look for.
     * @return Center Location of the patch, or null if none found.
     */
    public Location findSandPatchLocation(World frozenWorld, List<ChunkCoord> chunkCoords, int size) {
        for (ChunkCoord cc : chunkCoords) {
            int ox = cc.x << 4;
            int oz = cc.z << 4;

            // slide a size×size window across this chunk
            for (int dx = 0; dx <= 16 - size; dx++) {
                for (int dz = 0; dz <= 16 - size; dz++) {
                    // check that every block in the window is sand at surface
                    boolean isPatch = true;
                    for (int x = 0; x < size && isPatch; x++) {
                        for (int z = 0; z < size; z++) {
                            int wx = ox + dx + x;
                            int wz = oz + dz + z;
                            int sy = frozenWorld.getHighestBlockYAt(wx, wz);
                            if (frozenWorld.getBlockAt(wx, sy - 1, wz).getType() != Material.SNOW_BLOCK) {
                                isPatch = false;
                                break;
                            }
                        }
                    }
                    if (!isPatch) continue;

                    // compute center of the patch
                    int cx = ox + dx + size / 2;
                    int cz = oz + dz + size / 2;

                    // reject if within 10 blocks of any water (same as your original)
                    boolean nearWater = false;
                    outer:
                    for (int xOff = -10; xOff <= 10; xOff++) {
                        for (int zOff = -10; zOff <= 10; zOff++) {
                            if (xOff*xOff + zOff*zOff > 100) continue;
                            int nx = cx + xOff;
                            int nz = cz + zOff;
                            int ny = frozenWorld.getHighestBlockYAt(nx, nz);
                            if (frozenWorld.getBlockAt(nx, ny, nz).getType() == Material.WATER) {
                                nearWater = true;
                                break outer;
                            }
                        }
                    }
                    if (nearWater) continue;

                    int cy = frozenWorld.getHighestBlockYAt(cx, cz);
                    return new Location(frozenWorld, cx, cy, cz);
                }
            }
        }
        return null;
    }

    /**
     * Finds a sand patch location with a different algorithm for lighthouse placement
     */
    public Location findSandPatchLocation(World frozenWorld, List<ChunkCoord> chunkCoords) {
        for (ChunkCoord cc : chunkCoords) {
            int ox = cc.x << 4, oz = cc.z << 4;
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz <= 16 - 3; dz++) {
                    // check 1×3 patch along Z
                    boolean patch = true;
                    for (int k = 0; k < 3; k++) {
                        int wx = ox + dx, wz = oz + dz + k;
                        int sy = frozenWorld.getHighestBlockYAt(wx, wz);
                        if (frozenWorld.getBlockAt(wx, sy - 1, wz).getType() != Material.SNOW_BLOCK) {
                            patch = false;
                            break;
                        }
                    }
                    if (!patch) continue;

                    // ensure center is ≥10 blocks from any water
                    int cx = ox + dx, cz = oz + dz + 1;
                    boolean nearWater = false;
                    for (int dx2 = -10; dx2 <= 10 && !nearWater; dx2++) {
                        for (int dz2 = -10; dz2 <= 10; dz2++) {
                            if (dx2*dx2 + dz2*dz2 > 100) continue;
                            int nx = cx + dx2, nz = cz + dz2;
                            int ny = frozenWorld.getHighestBlockYAt(nx, nz);
                            if (frozenWorld.getBlockAt(nx, ny, nz).getType() == Material.WATER) {
                                nearWater = true;
                                break;
                            }
                        }
                    }
                    if (nearWater) continue;

                    int cy = frozenWorld.getHighestBlockYAt(cx, cz);
                    return new Location(frozenWorld, cx, cy, cz);
                }
            }
        }
        return null;
    }

    /**
     * Finds the nearest blackstone block to a given center location
     */
    public Location findNearestBlackstone(Location center, int radius) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cz = center.getBlockZ(), cy = center.getBlockY();
        double best = Double.MAX_VALUE;
        Location bestLoc = null;
        int rSq = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx*dx + dz*dz > rSq) continue;
                int x = cx + dx, z = cz + dz;
                for (int dy = -5; dy <= 5; dy++) {
                    int y = cy + dy;
                    if (y < world.getMinHeight() || y > world.getMaxHeight()) continue;
                    if (world.getBlockAt(x, y, z).getType() == Material.BLACKSTONE) {
                        double d2 = center.distanceSquared(new Location(world, x, y, z));
                        if (d2 < best) {
                            best = d2;
                            bestLoc = new Location(world, x, y, z);
                        }
                    }
                }
            }
        }
        return bestLoc;
    }

    /**
     * Finds the nearest obsidian block to a given center location
     */
    public Location findNearestObsidian(Location center, int radius) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cz = center.getBlockZ(), cy = center.getBlockY();
        double best = Double.MAX_VALUE;
        Location bestLoc = null;
        int rSq = radius * radius;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx*dx + dz*dz > rSq) continue;
                int x = cx + dx, z = cz + dz;
                for (int dy = -5; dy <= 5; dy++) {
                    int y = cy + dy;
                    if (y < world.getMinHeight() || y > world.getMaxHeight()) continue;
                    if (world.getBlockAt(x, y, z).getType() == Material.OBSIDIAN) {
                        double d2 = center.distanceSquared(new Location(world, x, y, z));
                        if (d2 < best) {
                            best = d2;
                            bestLoc = new Location(world, x, y, z);
                        }
                    }
                }
            }
        }
        return bestLoc;
    }

    /**
     * Helper to detect cardinal adjacency at same Y
     */
    public boolean isAdjacent(World frozenWorld, int x, int y, int z, Material target) {
        return frozenWorld.getBlockAt(x + 1, y, z).getType() == target
                || frozenWorld.getBlockAt(x - 1, y, z).getType() == target
                || frozenWorld.getBlockAt(x, y, z + 1).getType() == target
                || frozenWorld.getBlockAt(x, y, z - 1).getType() == target;
    }

    // ─── Helper Methods ─────────────────────────────────────────────────────

    /**
     * Helper method to calculate actual distance to nearest water
     */
    private int calculateDistanceToWater(World frozenWorld, int x, int z, int maxRadius) {
        for (int r = 1; r <= maxRadius; r++) {
            // Check in expanding squares
            for (int dx = -r; dx <= r; dx++) {
                // Check the perimeter only (top and bottom edges of the square)
                for (int dz : new int[]{-r, r}) {
                    if (isWaterAt(frozenWorld, x + dx, z + dz)) {
                        return r;
                    }
                }
            }

            // Check left and right edges (excluding corners which we already checked)
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                for (int dx : new int[]{-r, r}) {
                    if (isWaterAt(frozenWorld, x + dx, z + dz)) {
                        return r;
                    }
                }
            }
        }

        return maxRadius + 1; // If no water found within radius
    }

    /**
     * Helper method to check if a block is water
     */
    private boolean isWaterAt(World frozenWorld, int x, int z) {
        int y = frozenWorld.getHighestBlockYAt(x, z);
        return frozenWorld.getBlockAt(x, y, z).getType() == Material.WATER;
    }

    /**
     * Checks if a position is near water within the specified radius
     */
    private boolean isNearWater(World frozenWorld, int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                // Check at sea level and one block above/below
                for (int dy = -1; dy <= 1; dy++) {
                    int checkY = SEA_LEVEL + dy;
                    Block block = frozenWorld.getBlockAt(checkX, checkY, checkZ);
                    if (block.getType() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Fills air gaps with sand between current surface and new hill
     */
    private void fillAirGapsWithSand(World frozenWorld, int x, int z, int currentTop, int newTop) {
        // Fill any air blocks between the current surface and where we want to build
        for (int y = currentTop + 1; y < newTop; y++) {
            Block block = frozenWorld.getBlockAt(x, y, z);
            if (block.getType() == Material.AIR) {
                // Check what's below to determine fill material
                Block below = frozenWorld.getBlockAt(x, y - 1, z);
                if (below.getType() == Material.SNOW_BLOCK || below.getType() == Material.ICE) {
                    block.setType(Material.SNOW_BLOCK);
                } else {
                    // Default to snow for frozen theme
                    block.setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    // ─── Utility Methods ────────────────────────────────────────────────────

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    private double clampDouble(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    // ─── Getters for Constants ──────────────────────────────────────────────

    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    public int getIslandRadius() {
        return ISLAND_RADIUS;
    }

    public int getBeachIn() {
        return BEACH_IN;
    }

    public int getBeachOut() {
        return BEACH_OUT;
    }

    // ─── Helper Classes ─────────────────────────────────────────────────────

    public static class ChunkCoord {
        public final int x, z;
        public ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ChunkCoord that = (ChunkCoord) obj;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    public static class Point {
        public final int x, z;
        public Point(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }
}
