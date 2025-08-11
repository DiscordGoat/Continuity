package goat.minecraft.minecraftnew.other.realms;

import goat.minecraft.minecraftnew.other.realms.utils.FrozenTerrainUtils;
import goat.minecraft.minecraftnew.utils.devtools.SchemManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class Frozen implements CommandExecutor, TabCompleter {
    private static final String WORLD_NAME = "frozen";
    private static final int SEA_LEVEL = 64;
    private static final int ISLAND_RADIUS = 120;
    private static final int BEACH_IN = 15;
    private static final int BEACH_OUT = 16;

    // Generation modes
    private enum GenerationMode {
        PREVIEW,    // Step-by-step with progress updates
        PERFORMANCE // Fast generation without step updates
    }

    // Terrain utility instance
    private final FrozenTerrainUtils terrainUtils;

    // Chunk tracking
    private final Set<FrozenTerrainUtils.ChunkCoord> landChunks = new HashSet<>();
    private final List<FrozenTerrainUtils.ChunkCoord> chunkCoords = new ArrayList<>();
    private final List<Point> pathPoints = new ArrayList<>();

    // Structure locations
    private Location barLocation = null;
    private Location lighthouseLocation = null;
    private final Map<String, Location> structureLocations = new HashMap<>();

    // Plugin and world references
    private final JavaPlugin plugin;
    private World frozenWorld;
    private Player player; // store who ran /frozen
    private GenerationMode currentMode = GenerationMode.PREVIEW;

    public Frozen(JavaPlugin plugin) {
        this.plugin = plugin;
        this.terrainUtils = new FrozenTerrainUtils();
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player)) return false;
        player = (Player)s;

        if (cmd.getName().equalsIgnoreCase("frozen")) {
            // Check for mode argument
            if (args.length > 0) {
                String mode = args[0].toLowerCase();
                if (mode.equals("preview")) {
                    currentMode = GenerationMode.PREVIEW;
                    player.sendMessage("§eUsing preview mode (step-by-step generation)");
                } else if (mode.equals("performance")) {
                    currentMode = GenerationMode.PERFORMANCE;
                    player.sendMessage("§eUsing performance mode (fast generation)");
                } else {
                    player.sendMessage("§cUnknown mode. Use '/frozen preview' or '/frozen performance'");
                    return true;
                }
            } else {
                // Default to preview mode
                currentMode = GenerationMode.PREVIEW;
                player.sendMessage("§eUsing default preview mode. Use '/frozen performance' for fast generation.");
            }

            if (Bukkit.getWorld(WORLD_NAME) != null) unloadAndDelete();

            frozenWorld = Bukkit.createWorld(
                    new WorldCreator(WORLD_NAME)
                            .environment(World.Environment.NORMAL)
                            .type(WorldType.NORMAL)
                            .generator(new EmptyWaterGenerator())
            );

            player.sendMessage("§aFrozen world created—teleporting in 1s...");
            new BukkitRunnable() {
                @Override public void run() {
                    player.teleport(new Location(frozenWorld, 50, 180, 50));
                    player.setGameMode(GameMode.SPECTATOR);

                    if (currentMode == GenerationMode.PREVIEW) {
                        player.sendMessage("§aWatch each build phase unfold!");
                    } else {
                        player.sendMessage("§aGenerating world at maximum speed...");
                    }

                    prepareChunks();
                    pickPathPoints();

                    if (currentMode == GenerationMode.PREVIEW) {
                        startPhase0();
                    } else {
                        startPerformanceGeneration();
                    }
                }
            }.runTaskLater(plugin, 1);

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("defrost")) {
            if(player.getWorld().getName().equalsIgnoreCase("Frozen")){
                frozenWorld = player.getWorld();
            }
            if (frozenWorld != null) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.setGameMode(GameMode.SURVIVAL);
                unloadAndDelete();
                player.sendMessage("§cFrozen world removed.");
            } else {
                player.sendMessage("§cNo frozen world to remove.");
            }
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("frozen")) {
            if (args.length == 1) {
                // First argument - show available modes
                List<String> modes = Arrays.asList("preview", "performance");
                String currentArg = args[0].toLowerCase();

                // Filter modes that start with what the player has typed
                for (String mode : modes) {
                    if (mode.startsWith(currentArg)) {
                        completions.add(mode);
                    }
                }
            }
            // No completions for additional arguments
        }

        return completions;
    }

    // ─── World Management ───────────────────────────────────────────────────

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

    // ─── Preparation Methods ────────────────────────────────────────────────

    private void prepareChunks() {
        World world = Bukkit.getWorld("frozen");
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setTime(11000);

        chunkCoords.clear();
        int r = ((ISLAND_RADIUS * 3) + 15) >> 4;
        for (int cx = -r; cx <= r; cx++)
            for (int cz = -r; cz <= r; cz++)
                chunkCoords.add(new FrozenTerrainUtils.ChunkCoord(cx, cz));
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

    // ─── Performance Mode Generation ────────────────────────────────────────

    private void startPerformanceGeneration() {
        player.sendMessage("§eStarting performance generation...");

        new BukkitRunnable() {
            private int currentPhase = 0;
            private int chunkIndex = 0;
            private long startTime = System.currentTimeMillis();
            private boolean barPlaced = false;
            private boolean lighthousePlaced = false;
            private SchemManager schemManager = new SchemManager(plugin);
            private Random R = new Random();

            @Override
            public void run() {
                // Process multiple chunks per tick to speed up generation
                int chunksPerTick = 16; // Adjust this value to balance speed vs server performance

                for (int i = 0; i < chunksPerTick && chunkIndex < chunkCoords.size(); i++) {
                    FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(chunkIndex);

                    switch (currentPhase) {
                        case 0: // Ocean Floor
                            terrainUtils.genOceanFloor(frozenWorld, cc.x, cc.z);
                            break;
                        case 1: // Island Base
                            terrainUtils.genIslandBase(frozenWorld, cc.x, cc.z);
                            landChunks.add(cc);
                            break;
                        case 2: // Beaches
                            if (landChunks.contains(cc)) {
                                terrainUtils.paintSuperiorBeach(frozenWorld, cc.x, cc.z);
                            }
                            break;
                        case 3: // Hills
                            if (landChunks.contains(cc)) {
                                terrainUtils.paintHills(frozenWorld, cc.x, cc.z);
                            }
                            break;
                        case 4: // Trees and Snow Golems
                            terrainUtils.plantTree(frozenWorld, cc.x, cc.z, null);
                            spawnSnowGolem(cc.x, cc.z, R);
                            break;
                    }

                    chunkIndex++;
                }

                // Check if current phase is complete
                if (chunkIndex >= chunkCoords.size()) {
                    currentPhase++;
                    chunkIndex = 0;

                    // Special handling between phases
                    if (currentPhase == 3 && lighthouseLocation == null) {
                        // Find lighthouse location after beaches
                        lighthouseLocation = findBestStructureLocation(
                                "Lighthouse", 11, 11, Material.SNOW_BLOCK,
                                Arrays.asList(Material.SNOW_BLOCK), 7,
                                structureLocations, 40, HeightPreference.LOWEST,
                                SEA_LEVEL, true
                        );
                        if (lighthouseLocation != null) {
                            structureLocations.put("lighthouse", lighthouseLocation);
                        }
                    }

                    // Place structures after all terrain is done
                    if (currentPhase == 5) {
                        // Place bar
                        if (!barPlaced) {
                            if (barLocation == null) {
                                barLocation = findBestStructureLocation(
                                        "Bar", 14, 14, Material.SNOW_BLOCK,
                                        Arrays.asList(Material.SNOW_BLOCK), 10,
                                        structureLocations, 40, HeightPreference.AVERAGE,
                                        SEA_LEVEL, true
                                );
                                if (barLocation != null) {
                                    structureLocations.put("bar", barLocation);
                                }
                            }

                            if (barLocation != null) {
                                Location barY66 = new Location(
                                        barLocation.getWorld(),
                                        barLocation.getBlockX(),
                                        barLocation.getBlockY() + 1,
                                        barLocation.getBlockZ()
                                );
                                schemManager.placeStructure("test", barY66);

                                Location stoneForBar = findNearestSolidBlock(barY66, 10);
                                if (stoneForBar != null) {
                                    Location villLoc = stoneForBar.clone().add(0.5, 1, 0.5);
                                    Villager bartender = frozenWorld.spawn(villLoc, Villager.class);
                                    bartender.setCustomName(ChatColor.GOLD + "Bartender");
                                    bartender.setCustomNameVisible(true);
                                }
                            }
                            barPlaced = true;
                        }

                        // Place lighthouse
                        if (!lighthousePlaced) {
                            if (lighthouseLocation != null) {
                                Location lightLocation = new Location(
                                        lighthouseLocation.getWorld(),
                                        lighthouseLocation.getBlockX(),
                                        lighthouseLocation.getBlockY()+1,
                                        lighthouseLocation.getBlockZ()
                                );

                                terrainUtils.levelGround(
                                        frozenWorld,
                                        lightLocation.getBlockX() - 5,
                                        lightLocation.getBlockY(),
                                        lightLocation.getBlockZ() - 5,
                                        11, 11, Material.SNOW_BLOCK
                                );

                                schemManager.placeStructure("lighthouse", lightLocation);
                            }
                            lighthousePlaced = true;
                        }

                        // Generation complete
                        if (barPlaced && lighthousePlaced) {
                            long endTime = System.currentTimeMillis();
                            double seconds = (endTime - startTime) / 1000.0;

                            player.sendMessage("§a§lFrozen world generation complete!");
                            player.sendMessage("§eGeneration time: " + String.format("%.2f", seconds) + " seconds");
                            player.sendMessage("§aYou can now explore the island!");
                            player.setGameMode(GameMode.SPECTATOR);
                            cancel();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Run on main thread with 1 tick intervals
    }

    // ─── Preview Mode Phases (Original Implementation) ──────────────────────

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
                FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(idx++);
                terrainUtils.genOceanFloor(frozenWorld, cc.x, cc.z);
                player.sendMessage("§7Ocean floor chunk at " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

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
                    startPhase2();
                    return;
                }
                FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(idx++);
                terrainUtils.genIslandBase(frozenWorld, cc.x, cc.z);

                // Cache it as "land"
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
                // Skip non-land chunks
                while (idx < chunkCoords.size() && !landChunks.contains(chunkCoords.get(idx))) {
                    idx++;
                }
                if (idx >= chunkCoords.size()) {
                    cancel();

                    // Find lighthouse location once all beaches are shaped
                    if (lighthouseLocation == null) {
                        lighthouseLocation = findBestStructureLocation(
                                "Lighthouse",
                                11, 11,
                                Material.SNOW_BLOCK,
                                Arrays.asList(Material.SNOW_BLOCK),
                                7,
                                structureLocations,
                                40,
                                HeightPreference.HIGHEST,
                                SEA_LEVEL,
                                true
                        );

                        if (lighthouseLocation != null) {
                            structureLocations.put("lighthouse", lighthouseLocation);
                        }
                    }

                    startPhase3();
                    return;
                }

                FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(idx++);
                terrainUtils.paintSuperiorBeach(frozenWorld, cc.x, cc.z);
                done2++;
                int pct2 = done2 * 100 / total2;
                player.sendMessage("§ePhase 2 progress: " + pct2 + "%");
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void startPhase3() {
        player.sendMessage("§ePhase 3: Sculpting hills…");
        new BukkitRunnable() {
            int idx = 0;
            final int total3 = landChunks.size();
            int done3 = 0;

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

                FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(idx++);
                terrainUtils.paintHills(frozenWorld, cc.x, cc.z);
                done3++;
                int pct3 = done3 * 100 / total3;
                player.sendMessage("§ePhase 3 progress: " + pct3 + "%");

                player.sendMessage("§7Hills painted in chunk " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

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
                    FrozenTerrainUtils.ChunkCoord cc = chunkCoords.get(idx++);
                    terrainUtils.plantTree(frozenWorld, cc.x, cc.z, player);

                    // 2) Occasionally spawn snow golems
                    spawnSnowGolem(cc.x, cc.z, R);

                } else {
                    // 3) Once all chunks done: spawn bar and Bartender
                    if (!barPlaced) {
                        if (barLocation == null) {
                            barLocation = findBestStructureLocation(
                                    "Bar",
                                    14, 14,
                                    Material.SNOW_BLOCK,
                                    Arrays.asList(Material.SNOW_BLOCK),
                                    10,
                                    structureLocations,
                                    40,
                                    HeightPreference.AVERAGE,
                                    SEA_LEVEL,
                                    true
                            );

                            if (barLocation != null) {
                                structureLocations.put("bar", barLocation);
                            }
                        }

                        if (barLocation != null) {
                            Location barY66 = new Location(
                                    barLocation.getWorld(),
                                    barLocation.getBlockX(),
                                    barLocation.getBlockY() +1,
                                    barLocation.getBlockZ()
                            );
                            schemManager.placeStructure("bar", barY66);

                            // Find a solid block near the bar for the bartender
                            Location stoneForBar = findNearestSolidBlock(barY66, 10);
                            if (stoneForBar != null) {
                                Location villLoc = stoneForBar.clone().add(0.5, 1, 0.5);
                                Villager bartender = frozenWorld.spawn(villLoc, Villager.class);
                                bartender.setCustomName(ChatColor.GOLD + "Bartender");
                                bartender.setCustomNameVisible(true);
                            }
                        }
                        barPlaced = true;
                    }

                    // 4) Place lighthouse
                    if (!lighthousePlaced) {
                        if (lighthouseLocation != null) {
                            Location lightLocation = new Location(
                                    lighthouseLocation.getWorld(),
                                    lighthouseLocation.getBlockX(),
                                    lighthouseLocation.getBlockY(),
                                    lighthouseLocation.getBlockZ()
                            );

                            // Level the ground using FrozenTerrainUtils
                            terrainUtils.levelGround(
                                    frozenWorld,
                                    lightLocation.getBlockX() - 5,
                                    lightLocation.getBlockY(),
                                    lightLocation.getBlockZ() - 5,
                                    11,
                                    11,
                                    Material.SNOW_BLOCK
                            );

                            schemManager.placeStructure("lighthouse", lightLocation);

                            plugin.getLogger().info("Lighthouse placed at: " +
                                    lightLocation.getBlockX() + "," +
                                    lightLocation.getBlockY() + "," +
                                    lightLocation.getBlockZ());
                        } else {
                            // Emergency placement fallback
                            plugin.getLogger().warning("Lighthouse location was not set. Attempting emergency placement...");
                            lighthouseLocation = findBestStructureLocation(
                                    "Lighthouse",
                                    11, 11,
                                    Material.SNOW_BLOCK,
                                    Arrays.asList(Material.SNOW_BLOCK, Material.DIRT),
                                    5, structureLocations, 25,
                                    HeightPreference.HIGHEST,
                                    SEA_LEVEL, true
                            );

                            if (lighthouseLocation != null) {
                                Location lightLocation = new Location(
                                        lighthouseLocation.getWorld(),
                                        lighthouseLocation.getBlockX(),
                                        lighthouseLocation.getBlockY() + 2,
                                        lighthouseLocation.getBlockZ()
                                );

                                terrainUtils.levelGround(
                                        frozenWorld,
                                        lightLocation.getBlockX() - 5,
                                        lightLocation.getBlockY(),
                                        lightLocation.getBlockZ() - 5,
                                        11, 11, Material.SNOW_BLOCK
                                );

                                schemManager.placeStructure("lighthouse", lightLocation);
                                structureLocations.put("lighthouse", lighthouseLocation);

                                plugin.getLogger().info("Emergency lighthouse placed at: " + lightLocation);
                            } else {
                                plugin.getLogger().severe("Failed to place lighthouse even with emergency placement!");
                            }
                        }
                        lighthousePlaced = true;
                    }

                    // 5) Check if both structures are placed before completing
                    if (barPlaced && lighthousePlaced) {
                        plugin.getLogger().info("Phase 4 complete.");
                        player.sendMessage("§a§lFrozen world generation complete! You can now explore the island!");
                        player.setGameMode(GameMode.SURVIVAL);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    // ─── Structure & Entity Placement ───────────────────────────────────────

    private void spawnSnowGolem(int cx, int cz, Random R) {
        if (R.nextInt(5) > 1) return; // ~40% chance
        int ox = cx << 4, oz = cz << 4;
        int x = ox + R.nextInt(16);
        int z = oz + R.nextInt(16);
        int y = frozenWorld.getHighestBlockYAt(x, z) + 1;
        Location loc = new Location(frozenWorld, x + 0.5, y, z + 0.5);
        if (frozenWorld.getBlockAt(x, y - 1, z).getType() != Material.AIR) {
            frozenWorld.spawnEntity(loc, EntityType.SNOW_GOLEM);
        }
    }

    // ─── Structure Location Finding ─────────────────────────────────────────

    private Location findBestStructureLocation(
            String structureType,
            int width,
            int length,
            Material preferredMaterial,
            List<Material> fallbackMaterials,
            int minDistanceFromWater,
            Map<String, Location> existingStructures,
            int minDistanceFromOtherStructures,
            HeightPreference heightPreference,
            int minElevation,
            boolean levelGround) {

        List<ScoredLocation> candidates = new ArrayList<>();

        // Search all land chunks for potential locations
        for (FrozenTerrainUtils.ChunkCoord cc : landChunks) {
            int ox = cc.x << 4, oz = cc.z << 4;

            for (int dx = 0; dx <= 16 - Math.min(width, 16); dx++) {
                for (int dz = 0; dz <= 16 - Math.min(length, 16); dz++) {
                    LocationAssessment assessment = assessLocation(
                            ox + dx, oz + dz, width, length,
                            preferredMaterial, fallbackMaterials,
                            minDistanceFromWater, existingStructures,
                            minDistanceFromOtherStructures, heightPreference, minElevation
                    );

                    if (assessment.suitable) {
                        candidates.add(new ScoredLocation(
                                new Location(frozenWorld,
                                        ox + dx + width/2.0,
                                        assessment.averageHeight,
                                        oz + dz + length/2.0),
                                assessment.score
                        ));
                    }
                }
            }
        }

        // Try with relaxed criteria if no candidates found
        if (candidates.isEmpty()) {
            if (currentMode == GenerationMode.PREVIEW) {
                player.sendMessage("§eNo ideal location found for " + structureType + ". Trying with relaxed criteria...");
            }

            for (FrozenTerrainUtils.ChunkCoord cc : landChunks) {
                int ox = cc.x << 4, oz = cc.z << 4;

                for (int dx = 0; dx <= 16 - Math.min(width/2, 16); dx++) {
                    for (int dz = 0; dz <= 16 - Math.min(length/2, 16); dz++) {
                        LocationAssessment assessment = assessLocation(
                                ox + dx, oz + dz, width, length,
                                preferredMaterial,
                                Arrays.asList(Material.SNOW_BLOCK, Material.DIRT, Material.SNOW_BLOCK, Material.GRAVEL),
                                Math.max(1, minDistanceFromWater/2), existingStructures,
                                Math.max(5, minDistanceFromOtherStructures/2),
                                heightPreference, minElevation - 5
                        );

                        if (assessment.suitable) {
                            candidates.add(new ScoredLocation(
                                    new Location(frozenWorld,
                                            ox + dx + width/2.0,
                                            assessment.averageHeight,
                                            oz + dz + length/2.0),
                                    assessment.score * 0.5
                            ));
                        }
                    }
                }
            }
        }

        // Last resort - create artificial platform
        if (candidates.isEmpty()) {
            if (currentMode == GenerationMode.PREVIEW) {
                player.sendMessage("§cNo suitable location found for " + structureType + ". Creating artificial platform.");
            }

            if (!landChunks.isEmpty()) {
                FrozenTerrainUtils.ChunkCoord cc = landChunks.iterator().next();
                int x = (cc.x << 4) + 8;
                int z = (cc.z << 4) + 8;
                int y = Math.max(SEA_LEVEL + 2, frozenWorld.getHighestBlockYAt(x, z));

                terrainUtils.levelGround(frozenWorld, x - (width+4)/2, y, z - (length+4)/2, width+4, length+4, preferredMaterial);
                return new Location(frozenWorld, x, y, z);
            }
        }

        // Sort and return best candidate
        if (!candidates.isEmpty()) {
            candidates.sort((a, b) -> Double.compare(b.score, a.score));
            ScoredLocation best = candidates.get(0);

            if (levelGround) {
                terrainUtils.levelGround(
                        frozenWorld,
                        best.location.getBlockX() - width/2,
                        best.location.getBlockY(),
                        best.location.getBlockZ() - length/2,
                        width, length, preferredMaterial
                );
            }

            if (currentMode == GenerationMode.PREVIEW) {
                player.sendMessage("§a" + structureType + " location set at " +
                        best.location.getBlockX() + "," + best.location.getBlockY() + "," + best.location.getBlockZ() +
                        " (score: " + String.format("%.2f", best.score) + ")");
            }

            return best.location;
        }

        return null;
    }

    private LocationAssessment assessLocation(
            int startX, int startZ, int width, int length,
            Material preferredMaterial, List<Material> fallbackMaterials,
            int minDistanceFromWater, Map<String, Location> existingStructures,
            int minDistanceFromOtherStructures, HeightPreference heightPreference, int minElevation) {

        LocationAssessment result = new LocationAssessment();
        result.suitable = false;

        int preferredCount = 0;
        int fallbackCount = 0;
        int totalBlocks = width * length;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        int sumHeight = 0;
        int blockCount = 0;

        // Gather height data and material counts
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < length; dz++) {
                int x = startX + dx;
                int z = startZ + dz;
                int y = frozenWorld.getHighestBlockYAt(x, z);

                Material topMaterial = frozenWorld.getBlockAt(x, y, z).getType();
                if (topMaterial == Material.WATER) {
                    return result; // Not suitable
                }

                Material baseMaterial = frozenWorld.getBlockAt(x, y, z).getType();
                if (baseMaterial == preferredMaterial) {
                    preferredCount++;
                } else if (fallbackMaterials.contains(baseMaterial)) {
                    fallbackCount++;
                }

                minHeight = Math.min(minHeight, y);
                maxHeight = Math.max(maxHeight, y);
                sumHeight += y;
                blockCount++;
            }
        }

        if (blockCount < totalBlocks) return result;

        result.averageHeight = sumHeight / blockCount;

        if (maxHeight - minHeight > 3) return result; // Too uneven
        if (result.averageHeight < minElevation) return result; // Too low

        // Choose target height based on preference
        int targetHeight;
        switch (heightPreference) {
            case HIGHEST: targetHeight = maxHeight; break;
            case LOWEST: targetHeight = minHeight; break;
            case AVERAGE:
            default: targetHeight = result.averageHeight; break;
        }

        // Check water proximity
        for (int xOffset = -minDistanceFromWater; xOffset <= width + minDistanceFromWater; xOffset++) {
            for (int zOffset = -minDistanceFromWater; zOffset <= length + minDistanceFromWater; zOffset++) {
                if (xOffset >= 0 && xOffset < width && zOffset >= 0 && zOffset < length) continue;

                int x = startX + xOffset;
                int z = startZ + zOffset;

                for (int dy = -2; dy <= 2; dy++) {
                    int checkY = Math.min(Math.max(targetHeight + dy, 0), frozenWorld.getMaxHeight());
                    if (frozenWorld.getBlockAt(x, checkY, z).getType() == Material.WATER) {
                        return result; // Too close to water
                    }
                }
            }
        }

        // Check distance from other structures
        if (existingStructures != null) {
            Location centerLocation = new Location(frozenWorld, startX + width/2.0, targetHeight, startZ + length/2.0);

            for (Map.Entry<String, Location> entry : existingStructures.entrySet()) {
                if (entry.getValue() != null &&
                        entry.getValue().distance(centerLocation) < minDistanceFromOtherStructures) {
                    return result; // Too close to another structure
                }
            }
        }

        // Calculate score
        double materialScore = (preferredCount * 50.0) / totalBlocks;
        double fallbackScore = (fallbackCount * 25.0) / totalBlocks;
        double flatnessScore = 20.0 * (1.0 - ((maxHeight - minHeight) / 3.0));

        double optimalHeight = SEA_LEVEL + 5;
        double heightDiff = Math.abs(targetHeight - optimalHeight);
        double elevationScore = 20.0 * Math.max(0, 1.0 - (heightDiff / 10.0));

        double distanceFromCenter = Math.hypot(startX + width/2.0, startZ + length/2.0);
        double maxDistance = ISLAND_RADIUS;
        double distanceScore = 10.0 * (1.0 - Math.min(distanceFromCenter / maxDistance, 1.0));

        result.score = materialScore + fallbackScore + flatnessScore + elevationScore + distanceScore;
        result.suitable = true;

        return result;
    }

    // ─── Utility Methods ────────────────────────────────────────────────────

    private Location findNearestSolidBlock(Location center, int radius) {
        World world = center.getWorld();
        int cx = center.getBlockX(), cz = center.getBlockZ(), cy = center.getBlockY();
        double best = Double.MAX_VALUE;
        Location bestLoc = null;
        int rSq = radius * radius;

        List<Material> suitableMaterials = Arrays.asList(
                Material.STONE, Material.DIRT, Material.OBSIDIAN,
                Material.SNOW_BLOCK, Material.ICE, Material.BLACKSTONE
        );

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx*dx + dz*dz > rSq) continue;
                int x = cx + dx, z = cz + dz;
                for (int dy = -3; dy <= 3; dy++) {
                    int y = cy + dy;
                    if (y < world.getMinHeight() || y > world.getMaxHeight()) continue;

                    Material blockMaterial = world.getBlockAt(x, y, z).getType();
                    if (suitableMaterials.contains(blockMaterial)) {
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

    // ─── Helper Classes ─────────────────────────────────────────────────────

    private enum HeightPreference {
        HIGHEST, LOWEST, AVERAGE
    }

    private static class LocationAssessment {
        boolean suitable;
        int averageHeight;
        double score;
    }

    private static class ScoredLocation {
        Location location;
        double score;

        ScoredLocation(Location location, double score) {
            this.location = location;
            this.score = score;
        }
    }

    private static class Point {
        final int x, z;
        Point(int x, int z) { this.x = x; this.z = z; }
    }

    // ─── Empty Water Generator ──────────────────────────────────────────────

    private static class EmptyWaterGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World w, Random R, int cx, int cz, BiomeGrid bio) {
            ChunkData d = createChunkData(w);

            // Place bedrock floor at y=0
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    d.setBlock(x, 0, z, Material.BEDROCK);
                }
            }

            // Fill water above it
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
}
