package goat.minecraft.minecraftnew.regions;

import goat.minecraft.minecraftnew.regions.terrain.Beach;
import goat.minecraft.minecraftnew.regions.terrain.Ocean;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.noise.SimplexNoiseGenerator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class RegenerateCommand implements CommandExecutor {
    private final JavaPlugin plugin;

    /** One merged landmass: */
    private static class Mass {
        int cx, cz;
        final List<RegionType> types;
        final int radius, beach;
        Mass(List<RegionType> types, int radius, int beach) {
            this.types = types;
            this.radius = radius;
            this.beach = beach;
        }
    }

    /** A sub‐seed for Voronoi subdivision inside each core */
    private record SubSeed(double wx, double wz, RegionType type) {}

    // Configuration
    private static final int CHUNKS = 36;       // map is 36×36 chunks
    private static final int ISLAND_GAP = 5;    // min ocean gap for island
    private static final double NOISE_FREQ = 0.03;  // noise frequency
    private static final double NOISE_AMP = 0.5;    // noise amplitude
    private static final int BATCH = 16;        // chunks painted per tick

    // State tracking
    private AtomicInteger paintedChunks = new AtomicInteger();
    private AtomicInteger oceanChunks = new AtomicInteger();
    private AtomicReference<BukkitTask> rateTaskRef = new AtomicReference<>();

    public RegenerateCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can run this command.");
            return true;
        }

        World world = Bukkit.getWorld("tropic");
        if (world == null) {
            player.sendMessage("§cNo tropic world loaded.");
            return true;
        }

        // Preview teleport
        teleportPlayerToPreviewLocation(player, world);
        player.sendMessage("§eRegenerating with hard-coded region sizes…");

        // Define masses
        List<Mass> masses = defineRegionMasses();

        // Start rate logger
        startRateLogger(player);

        // Begin phase 1
        startTerrainGeneration(player, world, masses);

        return true;
    }

    private void teleportPlayerToPreviewLocation(Player player, World world) {
        player.teleport(new Location(
                world, 276.651, 269.49881, 121.108, -1.3f, 75.5f
        ));
    }

    private List<Mass> defineRegionMasses() {
        return List.of(
                // mountain/safari/fields + forest
                new Mass(
                        List.of(
                                RegionType.MOUNTAIN,
                                RegionType.SAFARI,
                                RegionType.FIELDS,
                                RegionType.FOREST
                        ),
                        1,   // core radius: 1 chunk
                        2    // beach band: 2 chunks
                ),
                // jungle/swamp + forest
                new Mass(
                        List.of(
                                RegionType.JUNGLE,
                                RegionType.SWAMP,
                                RegionType.FOREST
                        ),
                        1,   // core radius: 1 chunk
                        2    // beach band: 2 chunks
                ),
                // mesa/desert
                new Mass(
                        List.of(
                                RegionType.MESA,
                                RegionType.DESERT
                        ),
                        1,   // core radius: 1 chunk
                        2    // beach band: 2 chunks
                ),
                // isolated island
                new Mass(
                        List.of(RegionType.ISLAND),
                        1,   // core radius: 1 chunk
                        2    // beach band: 2 chunks
                )
        );
    }

    private void startRateLogger(Player player) {
        paintedChunks.set(0);
        oceanChunks.set(0);

        BukkitTask rateLogger = new BukkitRunnable() {
            @Override
            public void run() {
                int count = paintedChunks.getAndSet(0) + oceanChunks.getAndSet(0);
                player.sendMessage("§eChunk rate: " + count + " chunks/sec");
            }
        }.runTaskTimer(plugin, 20L, 20L);

        rateTaskRef.set(rateLogger);
    }

    private void stopRateLogger() {
        BukkitTask rt = rateTaskRef.get();
        if (rt != null) rt.cancel();
    }

    private void startTerrainGeneration(Player player, World world, List<Mass> masses) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // Setup random and noise generator
            Random rnd = new Random();
            SimplexNoiseGenerator noise = new SimplexNoiseGenerator(rnd.nextLong());

            // Place seeds for masses
            placeMassSeeds(masses, rnd, player);

            // Create sub-seeds for Voronoi regions
            Map<Mass, List<SubSeed>> subSeeds = createSubSeeds(masses, rnd);

            player.sendMessage("§eSub-seeds placed, painting…");

            // Begin painting phase
            startPaintingPhase(player, world, masses, subSeeds, noise);
        });
    }

    private void placeMassSeeds(List<Mass> masses, Random rnd, Player player) {
        // Compute buffer/spacing using the largest radius+beach
        int maxR = masses.stream().mapToInt(m -> m.radius).max().orElse(0);
        int maxB = masses.stream().mapToInt(m -> m.beach).max().orElse(0);
        int buffer = maxR + maxB + ISLAND_GAP;
        int minSpace = 2*(maxR + maxB) + ISLAND_GAP;

        // Pick one seed-chunk for each mass
        for (Mass m : masses) {
            while (true) {
                int cx = rnd.nextInt(CHUNKS - 2*buffer) + buffer;
                int cz = rnd.nextInt(CHUNKS - 2*buffer) + buffer;
                boolean ok = masses.stream()
                        .filter(o -> o != m)
                        .noneMatch(o ->
                                Math.hypot(cx - o.cx, cz - o.cz) < minSpace
                        );
                if (ok) {
                    m.cx = cx;
                    m.cz = cz;
                    break;
                }
            }
            player.sendMessage(
                    "§aSeeded mass " + m.types +
                            " at ["+m.cx+","+m.cz+"] radius="+m.radius
            );
        }
    }

    private Map<Mass, List<SubSeed>> createSubSeeds(List<Mass> masses, Random rnd) {
        Map<Mass, List<SubSeed>> subSeeds = new HashMap<>();

        for (Mass m : masses) {
            List<SubSeed> list = new ArrayList<>();
            double cx = m.cx*16 + 8;
            double cz = m.cz*16 + 8;

            // Mountain at center if present
            if (m.types.contains(RegionType.MOUNTAIN)) {
                list.add(new SubSeed(cx, cz, RegionType.MOUNTAIN));
            }

            // Scatter others on a 70% ring of the core radius
            double ring = m.radius*16 * 0.7;
            for (RegionType rt : m.types) {
                if (rt == RegionType.MOUNTAIN) continue;
                double ang = rnd.nextDouble()*2*Math.PI;
                double dist = ring*(0.5 + rnd.nextDouble()*0.5);
                double wx = cx + Math.cos(ang)*dist;
                double wz = cz + Math.sin(ang)*dist;
                list.add(new SubSeed(wx, wz, rt));
            }

            subSeeds.put(m, list);
        }

        return subSeeds;
    }

    private void startPaintingPhase(Player player, World world, List<Mass> masses,
                                    Map<Mass, List<SubSeed>> subSeeds, SimplexNoiseGenerator noise) {
        new BukkitRunnable() {
            int idx = 0, total = CHUNKS*CHUNKS;

            @Override
            public void run() {
                int start = idx*BATCH;
                if (start >= total) {
                    player.sendMessage("§aPhase 1 complete.");
                    cancel();

                    // Once done, start Phase 2
                    startOceanFloorPhase(player, world);
                    return;
                }

                int end = Math.min(total, start+BATCH);
                paintTerrainBatch(start, end, world, masses, subSeeds, noise);

                paintedChunks.addAndGet(end - start);
                double pct = end/(double)total * 100;
                player.sendMessage(String.format("§ePainted chunks %d–%d (%.1f%%)", start, end-1, pct));
                idx++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void paintTerrainBatch(int start, int end, World world, List<Mass> masses,
                                   Map<Mass, List<SubSeed>> subSeeds, SimplexNoiseGenerator noise) {
        for (int i = start; i < end; i++) {
            int gx = i/CHUNKS, gz = i%CHUNKS, ox = gx << 4, oz = gz << 4;

            for (int dx = 0; dx < 16; dx++) {
                for (int dz = 0; dz < 16; dz++) {
                    int wx = ox+dx, wz = oz+dz;
                    double best = Double.MAX_VALUE;
                    Mass win = masses.get(0);

                    // Find closest mass
                    for (Mass m : masses) {
                        double cx = m.cx*16+8, cz = m.cz*16+8;
                        double d = Math.hypot(wx-cx, wz-cz);
                        double w = d + noise.noise(wx*NOISE_FREQ, wz*NOISE_FREQ)*NOISE_AMP*(m.radius*16);
                        if (w < best) {
                            best = w;
                            win = m;
                        }
                    }

                    // Determine region type
                    RegionType pick = RegionType.OCEAN;
                    double ld = win.radius*16, bd = (win.radius+win.beach)*16;

                    if (best <= bd && best > ld) {
                        pick = RegionType.BEACH;
                    } else if (best <= ld) {
                        double bs = Double.MAX_VALUE;
                        for (SubSeed ss : subSeeds.get(win)) {
                            double d2 = Math.hypot(wx-ss.wx, wz-ss.wz);
                            if (d2 < bs) {
                                bs = d2;
                                pick = ss.type;
                            }
                        }
                    }

                    // Set blocks
                    for (int y = -63; y <= 63; y++) {
                        world.getBlockAt(wx, y, wz).setType(pick.getMarker());
                    }
                }
            }
        }
    }

    private void startOceanFloorPhase(Player player, World world) {
        player.sendMessage("§eStarting Phase 2: ocean floors and beaches...");
        Ocean ocean = new Ocean(world, 12345L);
        Beach beach = new Beach(world); // Create a Beach instance

        new BukkitRunnable() {
            int oidx = 0, ototal = CHUNKS*CHUNKS;

            @Override
            public void run() {
                int startO = oidx*BATCH;
                if (startO >= ototal) {
                    player.sendMessage("§aPhase 2 complete.");
                    cancel();

                    // Cleanup
                    completeGeneration(player, world);
                    return;
                }

                int endO = Math.min(ototal, startO+BATCH);
                processOceanAndBeachBatch(startO, endO, world, ocean, beach); // Updated method

                oceanChunks.addAndGet(endO - startO);
                double pct = endO/(double)ototal * 100;
                player.sendMessage(String.format("§eOcean & beach phase: chunks %d–%d (%.1f%%)", startO, endO-1, pct));
                oidx++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // Updated method to process both ocean and beach chunks
    private void processOceanAndBeachBatch(int start, int end, World world, Ocean ocean, Beach beach) {
        for (int i = start; i < end; i++) {
            int cx = i/CHUNKS, cz = i%CHUNKS;

            if (isFullOceanChunk(cx, cz, world)) {
                ocean.genOceanFloor(cx, cz);
            } else if (beach.isPureBeachChunk(cx, cz)) {
                beach.generateBeachChunk(cx, cz);
            } else {
                fillWaterPillars(cx, cz, world);
                fillBeachColumns(cx, cz, world, beach);
            }
        }
    }

    // New method to fill beach columns in mixed chunks
    private void fillBeachColumns(int cx, int cz, World world, Beach beach) {
        int ox = cx << 4;
        int oz = cz << 4;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx;
                int wz = oz + dz;

                // Look at the "surface" block of this column
                Block surf = world.getBlockAt(wx, 63, wz);
                if (surf.getType() == Material.YELLOW_CONCRETE) {
                    // This is a beach column
                    beach.genBeachColumn(wx, wz);
                }
            }
        }
    }


    private boolean isFullOceanChunk(int cx, int cz, World world) {
        int ox = cx*16, oz = cz*16;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                if (world.getBlockAt(ox+dx, 63, oz+dz).getType() != Material.BLUE_CONCRETE) {
                    return false;
                }
            }
        }

        return true;
    }

    private void completeGeneration(Player player, World world) {
        // Teardown
        stopRateLogger();
        unloadGeneratedChunks(world);
        player.sendMessage("§eUnloaded generated chunks.");
    }

    /**
     * Only for those x,z within the chunk whose block at y=64 is BLUE_CONCRETE,
     * replace everything from y=-64 up through y=64 with:
     *   y=-64      → BEDROCK
     *   y=-63..0   → DEEPSLATE
     *   y=1..20    → STONE
     *   y>20..64   → WATER
     */
    private void fillWaterPillars(int cx, int cz, World world) {
        int ox = cx << 4;
        int oz = cz << 4;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx;
                int wz = oz + dz;

                // Look at the "surface" block of this column
                Block surf = world.getBlockAt(wx, 63, wz);
                if (surf.getType() != Material.BLUE_CONCRETE) {
                    // Not a pure-ocean column → skip it entirely
                    continue;
                }

                // We have an ocean column → overwrite its entire pillar
                for (int y = -64; y <= 63; y++) {
                    Material m;
                    if (y == -64) {
                        m = Material.BEDROCK;
                    } else if (y <= 0) {
                        m = Material.DEEPSLATE;
                    } else if (y <= 20) {
                        m = Material.STONE;
                    } else {
                        m = Material.WATER;
                    }
                    world.getBlockAt(wx, y, wz).setType(m);
                }
            }
        }
    }

    /** Unloads the CHUNKS×CHUNKS region once both phases finish */
    private void unloadGeneratedChunks(World world) {
        for (int cx = 0; cx < CHUNKS; cx++) {
            for (int cz = 0; cz < CHUNKS; cz++) {
                world.unloadChunk(cx, cz, false);
            }
        }
    }
}
