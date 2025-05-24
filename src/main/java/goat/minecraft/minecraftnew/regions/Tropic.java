package goat.minecraft.minecraftnew.regions;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import goat.minecraft.minecraftnew.utils.devtools.SchemManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.noise.SimplexNoiseGenerator;


import java.io.File;
import java.util.*;

public class Tropic implements CommandExecutor {
    private static final String WORLD_NAME    = "tropic";
    private static final int    SEA_LEVEL     = 64;
    private static final int    ISLAND_RADIUS = 120;

    private static final int BEACH_IN  = 15;
    private static final int BEACH_OUT = 16;

    private final SimplexNoiseGenerator boundaryNoise = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator floorNoise    = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator ravineNoise   = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator warpNoise     = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator baseNoise     = new SimplexNoiseGenerator(new Random().nextLong());
    // at class level, alongside chunkCoords:
    private final Set<ChunkCoord> landChunks = new HashSet<>();

    // somewhere up at the top of your class:
    private Location barLocation = null;
    private Location lighthouseLocation = null;



    private final JavaPlugin plugin;
    private World tropicWorld;
    private Player player;                    // store who ran /tropic
    private final List<ChunkCoord> chunkCoords = new ArrayList<>();
    private final List<Point> pathPoints    = new ArrayList<>();

    public Tropic(JavaPlugin plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player)) return false;
        player = (Player)s;

        if (cmd.getName().equalsIgnoreCase("tropic")) {
            if (Bukkit.getWorld(WORLD_NAME) != null) unloadAndDelete();

            tropicWorld = Bukkit.createWorld(
                    new WorldCreator(WORLD_NAME)
                            .environment(World.Environment.NORMAL)
                            .type(WorldType.NORMAL)
                            .generator(new EmptyWaterGenerator())
            );

            player.sendMessage("§aTropic world created—teleporting in 1s...");
            new BukkitRunnable() {
                @Override public void run() {
                    player.teleport(new Location(tropicWorld, 50, 180, 50));
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("§aWatch each build phase unfold!");

                    prepareChunks();
                    pickPathPoints();
                    startPhase0();
                }
            }.runTaskLater(plugin, 20);

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("decomission")) {
            if(player.getWorld().getName().equalsIgnoreCase("Tropic")){
                tropicWorld = player.getWorld();
            }
            if (tropicWorld != null) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                unloadAndDelete();
                player.sendMessage("§cTropic world removed.");
            } else {
                player.sendMessage("§cNo tropic world to remove.");
            }
            return true;
        }

        return false;
    }

    private void unloadAndDelete() {
        Bukkit.unloadWorld(WORLD_NAME, false);
        deleteDir(new File(Bukkit.getWorldContainer(), WORLD_NAME));
    }
    private boolean deleteDir(File f) {
        if (!f.exists()) return true;
        for (File c : f.listFiles()) {
            if (c.isDirectory()) deleteDir(c);
            else c.delete();
        }
        return f.delete();
    }

    private void prepareChunks() {
        chunkCoords.clear();
        int r = ((ISLAND_RADIUS * 2) + 15) >> 4;
        for (int cx = -r; cx <= r; cx++)
            for (int cz = -r; cz <= r; cz++)
                chunkCoords.add(new ChunkCoord(cx, cz));
    }

    private void pickPathPoints() {
        pathPoints.clear();
        Random R = new Random();
        for (int i = 0; i < 6; i++) {
            double ang = R.nextDouble() * 2 * Math.PI;
            double rad = R.nextDouble() * ISLAND_RADIUS * 0.6;
            int x = (int)(Math.cos(ang) * rad);
            int z = (int)(Math.sin(ang) * rad);
            pathPoints.add(new Point(x, z));
        }
    }

    // ─── Phase 0: Ocean Floor ───────────────────────────────────────────────
    private void startPhase0() {
        player.sendMessage("§ePhase 0: Generating ocean floor…");
        new BukkitRunnable() {
            int idx = 0;
            final int total0 = chunkCoords.size();

            @Override public void run() {
                int pct0 = (int)((idx / (double) total0) * 100);
                player.sendMessage("§ePhase 0 progress: " + pct0 + "%");
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase1();
                    return;
                }
                ChunkCoord cc = chunkCoords.get(idx++);
                genOceanFloor(cc.x, cc.z);
                player.sendMessage("§7Ocean floor chunk at " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }



    // ─── Phase 1: Island Base ───────────────────────────────────────────────
    private void startPhase1() {
        player.sendMessage("§ePhase 1: Building island core…");
        new BukkitRunnable(){
            int idx = 0;
            final int total1 = chunkCoords.size();
            @Override public void run(){
                int pct1 = (int)((idx / (double) total1) * 100);
                player.sendMessage("§ePhase 1 progress: " + pct1 + "%");


                if (idx >= chunkCoords.size()) {
                    cancel();
                    // now landChunks contains every chunk we just carved out
                    startPhase2();
                    return;
                }
                ChunkCoord cc = chunkCoords.get(idx++);
                genIslandBase(cc.x, cc.z);

                // *** cache it as “land” ***
                landChunks.add(cc);

                player.sendMessage("§7Island base at " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }



    private void startPhase2() {
        player.sendMessage("§ePhase 2: Shaping beaches…");
        new BukkitRunnable() {
            int idx = 0;
            final int total2 = landChunks.size();
            int done2 = 0;

            @Override public void run() {
                // skip non‐land chunks
                while (idx < chunkCoords.size() && !landChunks.contains(chunkCoords.get(idx))) {
                    idx++;
                }
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase3();
                    return;
                }

                ChunkCoord cc = chunkCoords.get(idx++);
                paintSuperiorBeach(cc.x, cc.z);
                done2++;
                int pct2 = done2 * 100 / total2;
                player.sendMessage("§ePhase 2 progress: " + pct2 + "%");

                int ox = cc.x << 4, oz = cc.z << 4;

                // — once‐only bar detection —
                if (barLocation == null) {
                    outerBar:
                    for (int dx = 0; dx <= 6; dx++) {
                        for (int dz = 0; dz < 16; dz++) {
                            // Check for flat 10×10 sand area at y=65
                            boolean ok = true;
                            for (int sx = 0; sx < 10; sx++) {
                                for (int sz = 0; sz < 10; sz++) {
                                    int wx = ox + dx + sx, wz = oz + dz + sz;

                                    // Check if highest block is at y=65 and is sand
                                    int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                                    if (sy != 65 || tropicWorld.getBlockAt(wx, sy, wz).getType() != Material.SAND) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (!ok) break;
                            }
                            if (!ok) continue;

                            // Center of that 10x10 area
                            int cx = ox + dx + 5, cz = oz + dz + 5;
                            int cy = 65; // Fixed height

                            // Check no grass within 5 blocks
                            boolean noGrass = true;
                            for (int xOff = -5; xOff <= 5 && noGrass; xOff++) {
                                for (int zOff = -5; zOff <= 5 && noGrass; zOff++) {
                                    Block check = tropicWorld.getBlockAt(cx + xOff, cy, cz + zOff);
                                    if (check.getType() == Material.GRASS_BLOCK) {
                                        noGrass = false;
                                        break;
                                    }
                                }
                            }
                            if (!noGrass) continue;

                            // No water within 7 blocks horizontally & down 3
                            boolean safe = true;
                            for (int xOff = -7; xOff <= 7 && safe; xOff++) {
                                for (int zOff = -7; zOff <= 7 && safe; zOff++) {
                                    for (int yOff = 0; yOff >= -3; yOff--) {
                                        Block check = tropicWorld.getBlockAt(cx + xOff, cy + yOff, cz + zOff);
                                        if (check.getType() == Material.WATER) {
                                            safe = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (!safe) continue;

                            // Must be 40+ blocks from lighthouse if that's already set
                            if (lighthouseLocation != null
                                    && lighthouseLocation.distance(new Location(tropicWorld, cx, cy, cz)) < 40) {
                                continue;
                            }

                            // All checks passed → set bar
                            barLocation = new Location(tropicWorld, cx, cy, cz);
                            player.sendMessage("§aBar location set at "
                                    + cx + "," + cy + "," + cz);
                            break outerBar;
                        }
                    }
                }

                if (lighthouseLocation == null) {
                    Location foundLocation = findBestLighthouseLocation();
                    if (foundLocation != null) {
                        lighthouseLocation = foundLocation;
                        player.sendMessage("§aLighthouse location set at "
                                + lighthouseLocation.getBlockX() + "," + lighthouseLocation.getBlockY() + "," + lighthouseLocation.getBlockZ());
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }



    // ─── Phase 3: Hills (land‐only, in chunkCoords order) ─────────────────────
    private void startPhase3() {
        player.sendMessage("§ePhase 3: Sculpting hills…");
        new BukkitRunnable() {
            int idx = 0;
            final int total3 = landChunks.size();
            int done3 = 0;

            final int total0 = chunkCoords.size();
            @Override public void run() {
                // skip non‐land
                while (idx < chunkCoords.size() && !landChunks.contains(chunkCoords.get(idx))) {
                    idx++;
                }
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase4();
                    return;
                }

                ChunkCoord cc = chunkCoords.get(idx++);
                paintHills(cc.x, cc.z);
                done3++;
                int pct3 = done3 * 100 / total3;
                player.sendMessage("§ePhase 3 progress: " + pct3 + "%");

                player.sendMessage("§7Hills painted in chunk " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }




    // ─── Phase 4: Trees ─────────────────────────────────────────────────────
    private void startPhase4() {
        player.sendMessage("§ePhase 4: Planting trees, placing sea‐mines, spawning bar & entities…");
        SchemManager schemManager = new SchemManager(plugin);

        new BukkitRunnable() {
            int idx = 0;
            final int total4 = chunkCoords.size();

            boolean barPlaced = false;
            boolean lighthousePlaced = false;
            Random R = new Random();

            @Override
            public void run() {
                int pct4 = (int)((idx / (double) total4) * 100);
                player.sendMessage("§ePhase 4 progress: " + pct4 + "%");

                if (idx < chunkCoords.size()) {
                    // 1) Plant trees
                    ChunkCoord cc = chunkCoords.get(idx++);
                    plantTree(cc.x, cc.z);

                    // 2) Place seamines + spawn minecart‐ridden Elder Guardians
                    placeSeaMineAndGuardian(cc.x, cc.z, schemManager, R);

                } else {
                    // 3) Once all chunks done: spawn bar and Bartender
                    if (!barPlaced) {
                        if (barLocation == null) {
                            barLocation = findSandPatchLocation();
                        }
                        if (barLocation != null) {
                            Location barY66 = new Location(
                                    barLocation.getWorld(),
                                    barLocation.getBlockX(),
                                    65,
                                    barLocation.getBlockZ()
                            );
                            schemManager.placeStructure("bar", barY66);

                            Location stoneForBar = findNearestObsidian(barY66, 10);
                            if (stoneForBar != null) {
                                Location villLoc = stoneForBar.clone().add(0.5, 1, 0.5);
                                Villager bartender = tropicWorld.spawn(villLoc, Villager.class);
                                bartender.setCustomName(ChatColor.GOLD + "Bartender");
                                bartender.setCustomNameVisible(true);
                            }
                        }
                        barPlaced = true;
                    }

                    // 4) Place lighthouse on a 9x9 sand patch
                    if (!lighthousePlaced) {
                        if (lighthouseLocation != null) {
                            // elevate to y=65 (same as bar)
                            Location lightY65 = new Location(
                                    lighthouseLocation.getWorld(),
                                    lighthouseLocation.getBlockX(),
                                    65,
                                    lighthouseLocation.getBlockZ()
                            );
                            schemManager.placeStructure("lighthouse", lightY65);
                            plugin.getLogger().info("Lighthouse placed at: " + lightY65);
                        } else {
                            plugin.getLogger().warning("Could not place lighthouse: location was never set during Phase 2.");
                        }
                        lighthousePlaced = true;
                    }

                    // 5) Check if both structures are placed before completing
                    if (barPlaced && lighthousePlaced) {
                        plugin.getLogger().info("Phase 4 complete.");
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }
// ─── Helpers ────────────────────────────────────────────────────────────────


    /**
     * Search all chunks for a size×size patch of contiguous sand,
     * and return the center location (at surface height) of the first one found.
     *
     * @param size Number of blocks along one edge of the square patch to look for.
     * @return Center Location of the patch, or null if none found.
     */
    private Location findSandPatchLocation(int size) {
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
                            int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                            if (tropicWorld.getBlockAt(wx, sy - 1, wz).getType() != Material.SAND) {
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
                            int ny = tropicWorld.getHighestBlockYAt(nx, nz);
                            if (tropicWorld.getBlockAt(nx, ny, nz).getType() == Material.WATER) {
                                nearWater = true;
                                break outer;
                            }
                        }
                    }
                    if (nearWater) continue;

                    int cy = tropicWorld.getHighestBlockYAt(cx, cz);
                    return new Location(tropicWorld, cx, cy, cz);
                }
            }
        }
        return null;
    }

    private void genOceanFloor(int cx, int cz) {
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

            tropicWorld.getBlockAt(wx, 0, wz).setType(Material.BEDROCK);

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
                                    tropicWorld.getBlockAt(wx + dx2, y + dy, wz + dz2)
                                            .setType(Material.DEEPSLATE_DIAMOND_ORE);
                    continue;
                } else if (isRavine && y == floorY && Math.random() < 0.4) {
                    mat = Material.MAGMA_BLOCK;
                } else if (y < 30 && Math.random() < 0.01) {
                    mat = Material.MAGMA_BLOCK;
                } else {
                    mat = (y < 30 ? Material.DEEPSLATE : Material.STONE);
                }

                tropicWorld.getBlockAt(wx, y, wz).setType(mat);
            }
        }
    }

    private void genIslandBase(int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;

                double dist = Math.hypot(wx, wz);
                double br   = ISLAND_RADIUS + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;

                // preserve original island “footprint”
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
                            mat = Material.GRASS_BLOCK;
                        } else if (y > SEA_LEVEL - 2) {
                            mat = Material.DIRT;
                        } else {
                            mat = Material.STONE;
                        }
                        tropicWorld.getBlockAt(wx, y, wz).setType(mat);
                    }

                    // now carve a staircase “slope” off the beach downward
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
                            tropicWorld.getBlockAt(sx, sy - 1, sz).setType(Material.STONE);
                            tropicWorld.getBlockAt(sx,     sy,     sz).setType(Material.GRAVEL);
                        } else {
                            tropicWorld.getBlockAt(sx, sy, sz).setType(Material.STONE);
                        }
                    }
                }
            }
        }
    }

    private Location findBestLighthouseLocation() {
        List<Location> candidates = new ArrayList<>();

        // First pass: Find all potential locations with relaxed criteria
        for (ChunkCoord cc : landChunks) { // Only search land chunks
            int ox = cc.x << 4, oz = cc.z << 4;

            for (int dx = 0; dx <= 10; dx++) { // Reduced area check for better coverage
                for (int dz = 0; dz <= 10; dz++) {
                    // Check for flat 6×6 area instead of 10x10 (more lenient)
                    boolean ok = true;
                    int minHeight = Integer.MAX_VALUE;
                    int maxHeight = Integer.MIN_VALUE;

                    for (int sx = 0; sx < 6 && ok; sx++) {
                        for (int sz = 0; sz < 6 && ok; sz++) {
                            int wx = ox + dx + sx, wz = oz + dz + sz;
                            int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                            Material topBlock = tropicWorld.getBlockAt(wx, sy, wz).getType();

                            // Allow sand OR grass (more flexible)
                            if (topBlock != Material.SAND && topBlock != Material.GRASS_BLOCK) {
                                ok = false;
                                break;
                            }

                            minHeight = Math.min(minHeight, sy);
                            maxHeight = Math.max(maxHeight, sy);
                        }
                    }

                    // Allow up to 3 block height variation (more tolerant of terrain)
                    if (!ok || (maxHeight - minHeight) > 3) continue;

                    // Use average height for more stable placement
                    int avgHeight = (minHeight + maxHeight) / 2;
                    int cx = ox + dx + 3, cz = oz + dz + 3; // Center of 6x6 area

                    // Reduced grass check radius (3 blocks instead of 5)
                    boolean noGrassNearby = true;
                    if (avgHeight >= 65) { // Only check if we're at beach level or higher
                        for (int xOff = -3; xOff <= 3 && noGrassNearby; xOff++) {
                            for (int zOff = -3; zOff <= 3 && noGrassNearby; zOff++) {
                                Block check = tropicWorld.getBlockAt(cx + xOff, avgHeight, cz + zOff);
                                if (check.getType() == Material.GRASS_BLOCK) {
                                    noGrassNearby = false;
                                }
                            }
                        }
                    }

                    // More lenient water check (5 blocks instead of 7, and only 2 down instead of 3)
                    boolean safeFromWater = true;
                    for (int xOff = -5; xOff <= 5 && safeFromWater; xOff++) {
                        for (int zOff = -5; zOff <= 5 && safeFromWater; zOff++) {
                            for (int yOff = 0; yOff >= -2; yOff--) {
                                Block check = tropicWorld.getBlockAt(cx + xOff, avgHeight + yOff, cz + zOff);
                                if (check.getType() == Material.WATER) {
                                    safeFromWater = false;
                                    break;
                                }
                            }
                        }
                    }

                    // Reduced minimum distance from bar (25 blocks instead of 40)
                    boolean farFromBar = true;
                    if (barLocation != null) {
                        double distance = barLocation.distance(new Location(tropicWorld, cx, avgHeight, cz));
                        if (distance < 25) {
                            farFromBar = false;
                        }
                    }

                    // If all checks pass, add to candidates
                    if (noGrassNearby && safeFromWater && farFromBar) {
                        // Score based on height (prefer higher locations) and distance from center
                        double distFromCenter = Math.hypot(cx, cz);
                        double score = avgHeight * 2 - distFromCenter * 0.1; // Favor height over distance

                        Location candidate = new Location(tropicWorld, cx, avgHeight, cz);
                        candidate.setYaw((float) score); // Store score in yaw for sorting
                        candidates.add(candidate);
                    }
                }
            }
        }

        // If no candidates found with strict criteria, try again with very relaxed criteria
        if (candidates.isEmpty()) {
            for (ChunkCoord cc : landChunks) {
                int ox = cc.x << 4, oz = cc.z << 4;

                for (int dx = 0; dx <= 12; dx++) {
                    for (int dz = 0; dz <= 12; dz++) {
                        int wx = ox + dx, wz = oz + dz;
                        int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                        Material topBlock = tropicWorld.getBlockAt(wx, sy, wz).getType();

                        // Accept any solid surface above sea level
                        if ((topBlock == Material.SAND || topBlock == Material.GRASS_BLOCK) && sy >= SEA_LEVEL + 1) {
                            // Minimal water check
                            boolean hasNearbyWater = false;
                            for (int xOff = -3; xOff <= 3 && !hasNearbyWater; xOff++) {
                                for (int zOff = -3; zOff <= 3; zOff++) {
                                    Block check = tropicWorld.getBlockAt(wx + xOff, sy, wz + zOff);
                                    if (check.getType() == Material.WATER) {
                                        hasNearbyWater = true;
                                        break;
                                    }
                                }
                            }

                            if (!hasNearbyWater) {
                                double score = sy * 2; // Just prioritize height
                                Location candidate = new Location(tropicWorld, wx, sy, wz);
                                candidate.setYaw((float) score);
                                candidates.add(candidate);
                            }
                        }
                    }
                }
            }
        }

        // Sort candidates by score (stored in yaw) and return the best one
        if (!candidates.isEmpty()) {
            candidates.sort((a, b) -> Float.compare(b.getYaw(), a.getYaw())); // Descending order
            Location best = candidates.get(0);

            // Clear the yaw value before returning
            best.setYaw(0);

            // Ensure the location is properly leveled for structure placement
            int x = best.getBlockX();
            int z = best.getBlockZ();
            int y = best.getBlockY();

            // Level a 5x5 area around the lighthouse location
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    // Set to sand for consistent placement
                    tropicWorld.getBlockAt(x + dx, y, z + dz).setType(Material.SAND);
                    // Clear space above
                    for (int dy = 1; dy <= 3; dy++) {
                        tropicWorld.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                    }
                }
            }

            player.sendMessage("§aLighthouse site prepared at " + x + "," + y + "," + z);
            return best;
        }

        // Last resort: force place on any land chunk
        if (!landChunks.isEmpty()) {
            ChunkCoord cc = landChunks.iterator().next();
            int x = (cc.x << 4) + 8; // Center of chunk
            int z = (cc.z << 4) + 8;
            int y = Math.max(SEA_LEVEL + 2, tropicWorld.getHighestBlockYAt(x, z));

            // Force create a platform
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    tropicWorld.getBlockAt(x + dx, y, z + dz).setType(Material.SAND);
                    for (int dy = 1; dy <= 5; dy++) {
                        tropicWorld.getBlockAt(x + dx, y + dy, z + dz).setType(Material.AIR);
                    }
                }
            }

            player.sendMessage("§eForced lighthouse placement at " + x + "," + y + "," + z);
            return new Location(tropicWorld, x, y, z);
        }

        return null; // This should never happen if landChunks isn't empty
    }

    private void paintSuperiorBeach(int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;

        // Beach height constants
        final int BEACH_MAX_HEIGHT = 67; // Maximum height for beach sand
        final int BEACH_SLOPE_DEPTH = 6; // How far inland the beach slope extends
        final int WATER_DEPTH = 3;      // How deep underwater to extend the sand

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;

                // Find the highest block at this position
                int highestY = tropicWorld.getHighestBlockYAt(wx, wz);
                Material surfaceMaterial = tropicWorld.getBlockAt(wx, highestY, wz).getType();

                // Check if block is at water level or if it's grass below our beach max height
                boolean isAtWaterLevel = highestY <= SEA_LEVEL;
                boolean isGrassNearWater = surfaceMaterial == Material.GRASS_BLOCK && highestY <= BEACH_MAX_HEIGHT;

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
                            int checkY = tropicWorld.getHighestBlockYAt(checkX, checkZ);

                            // If we find water at sea level, this is near water
                            if (tropicWorld.getBlockAt(checkX, checkY, checkZ).getType() == Material.WATER) {
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
                            int distanceToWater = calculateDistanceToWater(wx, wz, BEACH_SLOPE_DEPTH);

                            // Determine desired height based on distance to water
                            int desiredHeight = SEA_LEVEL +
                                    Math.min(3, (int)((float)distanceToWater / BEACH_SLOPE_DEPTH * (highestY - SEA_LEVEL)));

                            // Smooth the transition - cap max height
                            desiredHeight = Math.min(desiredHeight, BEACH_MAX_HEIGHT);

                            // Replace all blocks from desired height down to the original surface with sand
                            for (int y = highestY; y <= desiredHeight; y++) {
                                tropicWorld.getBlockAt(wx, y, wz).setType(Material.SAND);
                            }

                            // Clear air above to make slope
                            for (int y = desiredHeight + 1; y <= highestY; y++) {
                                tropicWorld.getBlockAt(wx, y, wz).setType(Material.AIR);
                            }
                        }
                        // Handle underwater areas - replace with sand down to a certain depth
                        else if (isAtWaterLevel) {
                            // Replace underwater surface with sand
                            Block surfaceBlock = tropicWorld.getBlockAt(wx, highestY, wz);
                            if (surfaceBlock.getType() == Material.WATER) {
                                // Find actual ground under water
                                int groundY = highestY;
                                while (groundY > 0 && tropicWorld.getBlockAt(wx, groundY, wz).getType() == Material.WATER) {
                                    groundY--;
                                }
                            }
                        }
                    }

                    // Apply a direct y-height based replacement up to BEACH_MAX_HEIGHT
                    // This handles areas that aren't necessarily near water but are below our beach height
                    else if (highestY <= BEACH_MAX_HEIGHT && surfaceMaterial == Material.GRASS_BLOCK) {
                        tropicWorld.getBlockAt(wx, highestY, wz).setType(Material.SAND);
                    }
                }
            }
        }
    }

    // Helper method to calculate actual distance to nearest water
    private int calculateDistanceToWater(int x, int z, int maxRadius) {
        for (int r = 1; r <= maxRadius; r++) {
            // Check in expanding squares
            for (int dx = -r; dx <= r; dx++) {
                // Check the perimeter only (top and bottom edges of the square)
                for (int dz : new int[]{-r, r}) {
                    if (isWaterAt(x + dx, z + dz)) {
                        return r;
                    }
                }
            }

            // Check left and right edges (excluding corners which we already checked)
            for (int dz = -r + 1; dz <= r - 1; dz++) {
                for (int dx : new int[]{-r, r}) {
                    if (isWaterAt(x + dx, z + dz)) {
                        return r;
                    }
                }
            }
        }

        return maxRadius + 1; // If no water found within radius
    }

    // Helper method to check if a block is water
    private boolean isWaterAt(int x, int z) {
        int y = tropicWorld.getHighestBlockYAt(x, z);
        return tropicWorld.getBlockAt(x, y, z).getType() == Material.WATER;
    }

    private void paintHills(int cx, int cz) {
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
                    int cur = tropicWorld.getHighestBlockYAt(wx, wz);
                    int maxRise = 4;
                    if (top > cur + maxRise) {
                        top = cur + maxRise;
                    }

                    // Check if we're too close to water before placing dirt/grass
                    boolean nearWater = isNearWater(wx, wz, 2); // Check 2-block radius

                    // Only place hills if we're not too close to water and above sea level
                    if (!nearWater && top > SEA_LEVEL + 2) {
                        // Fill any air gaps with sand first (between current surface and new hill)
                        fillAirGapsWithSand(wx, wz, cur, top);

                        // Then place the hill material on top
                        for (int y = cur + 1; y <= top; y++) {
                            Material m = (y == top ? Material.GRASS_BLOCK : Material.DIRT);
                            tropicWorld.getBlockAt(wx, y, wz).setType(m);
                        }

                        // occasional flower on the new grass
                        if (Math.random() < 0.02) {
                            Material flower = (Math.random() < 0.5
                                    ? Material.DANDELION
                                    : Material.POPPY);
                            tropicWorld.getBlockAt(wx, top + 1, wz).setType(flower);
                        }
                    }
                }
            }
        }
    }

    private boolean isNearWater(int x, int z, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int checkX = x + dx;
                int checkZ = z + dz;

                // Check at sea level and one block above/below
                for (int dy = -1; dy <= 1; dy++) {
                    int checkY = SEA_LEVEL + dy;
                    Block block = tropicWorld.getBlockAt(checkX, checkY, checkZ);
                    if (block.getType() == Material.WATER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void fillAirGapsWithSand(int x, int z, int currentTop, int newTop) {
        // Fill any air blocks between the current surface and where we want to build
        for (int y = currentTop + 1; y < newTop; y++) {
            Block block = tropicWorld.getBlockAt(x, y, z);
            if (block.getType() == Material.AIR) {
                // Check what's below to determine fill material
                Block below = tropicWorld.getBlockAt(x, y - 1, z);
                if (below.getType() == Material.SAND || below.getType() == Material.SANDSTONE) {
                    block.setType(Material.SAND);
                } else {
                    // Default to sand for tropical theme
                    block.setType(Material.SAND);
                }
            }
        }
    }

    private void plantTree(int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        Random R = new Random((long)cx * 3418731287L ^ cz * 1328979875L);
        for (int i = 0; i < 2; i++) {
            int x = ox + R.nextInt(16), z = oz + R.nextInt(16);
            int y = tropicWorld.getHighestBlockYAt(x, z);
            if (tropicWorld.getBlockAt(x, y - 1, z).getType() != Material.GRASS_BLOCK) continue;
            // trunk
            for (int h = 0; h < 5; h++) {
                tropicWorld.getBlockAt(x, y + h, z).setType(Material.OAK_LOG);
            }
            // leaves
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    for (int dy = 3; dy <= 6; dy++) {
                        tropicWorld.getBlockAt(x + dx, y + dy, z + dz).setType(Material.OAK_LEAVES);
                    }
                }
            }
            player.sendMessage("§7Tree planted at " + x + "," + y + "," + z);
        }
    }

    private void placeSeaMineAndGuardian(int cx, int cz, SchemManager schemManager, Random R) {
        int ox = cx << 4, oz = cz << 4;
        for (int i = 0; i < 2; i++) {
            if (R.nextInt(5) > 1) continue;  // ~40% chance

            int x = ox + R.nextInt(16), z = oz + R.nextInt(16);
            int y = tropicWorld.getHighestBlockYAt(x, z);
            while (y > tropicWorld.getMinHeight()
                    && tropicWorld.getBlockAt(x, y, z).getType() == Material.WATER) {
                y--;
            }
            int floorY = y + 1;

            if (floorY == 35
                    && tropicWorld.getBlockAt(x, y, z).getType() == Material.GRAVEL
                    && tropicWorld.getBlockAt(x, floorY, z).getType() == Material.WATER) {

                // place the mine
                Location mineLoc = new Location(tropicWorld, x, floorY, z);
                schemManager.placeStructure("seamine", mineLoc);

                // look in a 3×20×3 area above for BLACKSTONE
                outer:
                for (int dx2 = -1; dx2 <= 1; dx2++) {
                    for (int dz2 = -1; dz2 <= 1; dz2++) {
                        for (int dy2 = 1; dy2 <= 20; dy2++) {
                            if (tropicWorld.getBlockAt(x + dx2, floorY + dy2, z + dz2)
                                    .getType() == Material.BLACKSTONE) {
                                // spawn a minecart there
                                Location cartLoc = new Location(
                                        tropicWorld,
                                        x + dx2 + 0.5,
                                        floorY + dy2 + 1.5,
                                        z + dz2 + 0.5
                                );
                                Minecart cart = tropicWorld.spawn(cartLoc, Minecart.class);

                                // spawn + mount the guardian
                                Entity eg = tropicWorld.spawnEntity(cartLoc, EntityType.ELDER_GUARDIAN);
                                cart.addPassenger(eg);

                                // anti‐despawn
                                if (eg instanceof Mob) {
                                    ((Mob)eg).setRemoveWhenFarAway(false);
                                }
                                eg.setPersistent(true);

                                break outer;
                            }
                        }
                    }
                }
            }
        }
    }

    private Location findSandPatchLocation() {
        for (ChunkCoord cc : chunkCoords) {
            int ox = cc.x << 4, oz = cc.z << 4;
            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz <= 16 - 3; dz++) {
                    // check 1×3 patch along Z
                    boolean patch = true;
                    for (int k = 0; k < 3; k++) {
                        int wx = ox + dx, wz = oz + dz + k;
                        int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                        if (tropicWorld.getBlockAt(wx, sy - 1, wz).getType() != Material.SAND) {
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
                            int ny = tropicWorld.getHighestBlockYAt(nx, nz);
                            if (tropicWorld.getBlockAt(nx, ny, nz).getType() == Material.WATER) {
                                nearWater = true;
                                break;
                            }
                        }
                    }
                    if (nearWater) continue;

                    int cy = tropicWorld.getHighestBlockYAt(cx, cz);
                    return new Location(tropicWorld, cx, cy, cz);
                }
            }
        }
        return null;
    }

    private Location findNearestBlackstone(Location center, int radius) {
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
    private Location findNearestObsidian(Location center, int radius) {
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





    // helper to detect cardinal adjacency at same Y
    private boolean isAdjacent(int x, int y, int z, Material target) {
        return tropicWorld.getBlockAt(x + 1, y, z).getType() == target
                || tropicWorld.getBlockAt(x - 1, y, z).getType() == target
                || tropicWorld.getBlockAt(x, y, z + 1).getType() == target
                || tropicWorld.getBlockAt(x, y, z - 1).getType() == target;
    }



    private double lerp(double a,double b,double t){ return a+(b-a)*t; }
    private int    clamp(int v,int lo,int hi){ return v<lo?lo:(v>hi?hi:v); }
    private double clampDouble(double v,double lo,double hi){ return v<lo?lo:(v>hi?hi:v); }

    private static class EmptyWaterGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World w, Random R,
                                           int cx, int cz,
                                           BiomeGrid bio) {
            ChunkData d = createChunkData(w);

            // 1) Place a bedrock floor at y=0
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    d.setBlock(x, 0, z, Material.BEDROCK);
                }
            }

            // 2) Fill water above it
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 1; y < SEA_LEVEL; y++) {
                        d.setBlock(x, y, z, Material.WATER);
                    }
                }
            }

            return d;
        }
    }

    private static class ChunkCoord { final int x,z; ChunkCoord(int x,int z){this.x=x;this.z=z;} }
    private static class Point      { final int x,z; Point(int x,int z){this.x=x;this.z=z;} }
}
