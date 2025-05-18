package goat.minecraft.minecraftnew.utils.dimensions.end;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.bukkit.util.Vector;



import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Tropic implements CommandExecutor {
    private static final String WORLD_NAME    = "tropic";
    private static final int    SEA_LEVEL     = 64;
    private static final int    ISLAND_RADIUS = 90;

    private static final int BEACH_IN  = 15;
    private static final int BEACH_OUT = 16;

    private final SimplexNoiseGenerator boundaryNoise = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator floorNoise    = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator ravineNoise   = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator warpNoise     = new SimplexNoiseGenerator(new Random().nextLong());
    private final SimplexNoiseGenerator baseNoise     = new SimplexNoiseGenerator(new Random().nextLong());
    private boolean barPlaced = false;
    private Clipboard barClip;
    private Clipboard seamineClip;
    private final Map<String, Clipboard> schematics = new HashMap<>();


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

            player.sendMessage("§aTropic world created—teleporting in 10s...");
            new BukkitRunnable() {
                @Override public void run() {
                    player.teleport(new Location(tropicWorld, 50, 180, 50));
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("§aWatch each build phase unfold!");

                    prepareChunks();
                    pickPathPoints();
                    startPhase0();
                }
            }.runTaskLater(plugin, 200L);

            return true;
        }

        if (cmd.getName().equalsIgnoreCase("decomission")) {
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
            @Override public void run() {
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







    // ─── Phase 1: Island Base ───────────────────────────────────────────────
    private void startPhase1() {
        player.sendMessage("§ePhase 1: Building island core…");
        new BukkitRunnable(){
            int idx = 0;
            @Override public void run(){
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase2();
                    return;
                }
                ChunkCoord cc = chunkCoords.get(idx++);
                genIslandBase(cc.x, cc.z);
                player.sendMessage("§7Island base at " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
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

                    int maxHeight = 12;
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



    // ─── Phase 2: Beaches ───────────────────────────────────────────────────
    private void startPhase2() {
        player.sendMessage("§ePhase 2: Shaping beaches…");
        new BukkitRunnable(){
            int idx = 0;
            @Override public void run(){
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase3();
                    return;
                }
                ChunkCoord cc = chunkCoords.get(idx++);
                paintBeach(cc.x, cc.z);
                player.sendMessage("§7Beach at chunk " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void paintBeach(int cx, int cz) {
        int ox = cx << 4, oz = cz << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;
                double dist = Math.hypot(wx, wz);
                double br   = ISLAND_RADIUS + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;

                if (dist > br - BEACH_IN && dist <= br + BEACH_OUT) {
                    double total = BEACH_IN + BEACH_OUT;
                    double t     = clampDouble((br + BEACH_OUT - dist) / total, 0, 1);

                    // always sand now—no more gravel patches
                    int h = SEA_LEVEL - 1 + (int)Math.round(t * 3);
                    h = clamp(h, SEA_LEVEL - 1, SEA_LEVEL + 2);
                    tropicWorld.getBlockAt(wx, h, wz).setType(Material.SAND);

                    // clear air above
                    for (int y = h + 1; y <= SEA_LEVEL + 3; y++) {
                        tropicWorld.getBlockAt(wx, y, wz).setType(Material.AIR);
                    }
                }
            }
        }
    }

    // ─── Phase 3: Hills & Logging ───────────────────────────────────────────
    private void startPhase3() {
        player.sendMessage("§ePhase 3: Sculpting hills…");
        new BukkitRunnable(){
            int idx = 0;
            @Override public void run(){
                if (idx >= chunkCoords.size()) {
                    cancel();
                    startPhase4();
                    return;
                }
                ChunkCoord cc = chunkCoords.get(idx++);
                paintHills(cc.x, cc.z);
                player.sendMessage("§7Hills painted in chunk " + cc.x + "," + cc.z);
            }
        }.runTaskTimer(plugin, 0, 1);
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
                    double rawHgt  = (sum + 1) / 2 * 30 * falloff;  // 30 instead of 40 for lower peaks
                    int   top      = SEA_LEVEL + (int)rawHgt;

                    // cap how many blocks we “grow” in one go to avoid sheer walls
                    int cur = tropicWorld.getHighestBlockYAt(wx, wz);
                    int maxRise = 4;
                    if (top > cur + maxRise) {
                        top = cur + maxRise;
                    }

                    // fill from just above current surface up to top
                    for (int y = cur + 1; y <= top; y++) {
                        Material m = (y == top ? Material.GRASS_BLOCK : Material.DIRT);
                        tropicWorld.getBlockAt(wx, y, wz).setType(m);
                    }

                    // occasional flower on the new grass
                    if (top > SEA_LEVEL && Math.random() < 0.02) {
                        Material flower = (Math.random() < 0.5
                                ? Material.DANDELION
                                : Material.POPPY);
                        tropicWorld.getBlockAt(wx, top + 1, wz).setType(flower);
                    }
                }
            }
        }
    }


    // ─── Phase 4: Trees ─────────────────────────────────────────────────────
    private void startPhase4() {
        player.sendMessage("§ePhase 4: Planting trees…");
        new BukkitRunnable(){
            int idx=0;
            @Override public void run(){
                if (idx>=chunkCoords.size()) {
                    cancel();
                    startPhase5();
                    return;
                }
                ChunkCoord cc=chunkCoords.get(idx++);
                plantTree(cc.x, cc.z);
            }
        }.runTaskTimer(plugin,0,1);
    }

    private void plantTree(int cx, int cz) {
        int ox=cx<<4, oz=cz<<4;
        Random R=new Random((long)cx*3418731287L ^ cz*1328979875L);
        for (int i=0; i<2; i++){
            int x=ox+R.nextInt(16), z=oz+R.nextInt(16);
            int y=tropicWorld.getHighestBlockYAt(x,z);
            if (tropicWorld.getBlockAt(x,y-1,z).getType()!=Material.GRASS_BLOCK) continue;
            for (int h=0; h<5; h++)
                tropicWorld.getBlockAt(x,y+h,z).setType(Material.OAK_LOG);
            for (int dx=-2; dx<=2; dx++)
                for (int dz=-2; dz<=2; dz++)
                    for (int dy=3; dy<=6; dy++)
                        tropicWorld.getBlockAt(x+dx,y+dy,z+dz).setType(Material.OAK_LEAVES);
            player.sendMessage("§7Tree planted at " + x + "," + y + "," + z);
        }
    }
    // ─── Phase 5: Flavor Items ─────────────────────────────────────────────────
    private void startPhase5() {
        player.sendMessage("§ePhase 5: Adding flavor items…");

        // track highest grass‐block location
        final int[] maxY = { 0 };
        final int[] maxX = { 0 };
        final int[] maxZ = { 0 };

        new BukkitRunnable(){
            int idx = 0;

            @Override public void run() {
                if (idx >= chunkCoords.size()) {
                    // 1) Spawn 24 boats near the shore
                    for (int i = 0; i < 24; i++) {
                        double ang = Math.random() * 2 * Math.PI;
                        double rad = ISLAND_RADIUS + BEACH_OUT - 1;
                        int bx = (int)(Math.cos(ang) * rad);
                        int bz = (int)(Math.sin(ang) * rad);
                        int by = tropicWorld.getHighestBlockYAt(bx, bz);
                        tropicWorld
                                .spawnEntity(new Location(tropicWorld, bx, by + 1, bz),
                                        EntityType.BOAT);
                    }

                    // 2) Giant jungle tree at the single highest spot
                    if (maxY[0] > 0) {
                        Location topLoc = new Location(
                                tropicWorld, maxX[0], maxY[0] + 1, maxZ[0]
                        );
                        tropicWorld.generateTree(topLoc, TreeType.JUNGLE);
                    }
                    cancel();
                    plugin.getLogger().info("§aFlavor items placed!");
                    return;
                }

                ChunkCoord cc = chunkCoords.get(idx++);
                paintFlavor(cc.x, cc.z, maxX, maxY, maxZ);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private void paintFlavor(int cx, int cz,
                             int[] maxX, int[] maxY, int[] maxZ) {
        int ox = cx << 4, oz = cz << 4;
        Random R = new Random((long)cx * 3418731287L ^ cz * 1328979875L);

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int wx = ox + dx, wz = oz + dz;
                int sy = tropicWorld.getHighestBlockYAt(wx, wz);
                Material top = tropicWorld.getBlockAt(wx, sy, wz).getType();

                // update highest‐point tracker
                if (top == Material.GRASS_BLOCK && sy > maxY[0]) {
                    maxY[0] = sy;
                    maxX[0] = wx;
                    maxZ[0] = wz;
                }

                double dist = Math.hypot(wx, wz);
                double br   = ISLAND_RADIUS
                        + boundaryNoise.noise(wx * 0.03, wz * 0.03) * 30;

                // ─ Tiki torches (common on sand next to grass)
                if (top == Material.SAND && R.nextDouble() < 0.05) {
                    if (isAdjacent(wx, sy, wz, Material.GRASS_BLOCK)) {
                        tropicWorld
                                .getBlockAt(wx, sy + 1, wz)
                                .setType(Material.TORCH);
                    }
                }

                // ─ Bonfires (uncommon on grass next to sand)
                if (top == Material.GRASS_BLOCK && R.nextDouble() < 0.02) {
                    if (isAdjacent(wx, sy, wz, Material.SAND)) {
                        tropicWorld
                                .getBlockAt(wx, sy + 1, wz)
                                .setType(Material.CAMPFIRE);
                    }
                }

                // ─ Jungle saplings/trees above Y=74 (common)
                if (top == Material.GRASS_BLOCK
                        && sy > 74
                        && R.nextDouble() < 0.10) {
                    tropicWorld.generateTree(
                            new Location(tropicWorld, wx, sy + 1, wz),
                            TreeType.JUNGLE
                    );
                }


                // ─ Piers (uncommon in water next to sand)
                if (top == Material.WATER && R.nextDouble() < 0.02) {
                    if (isAdjacent(wx, sy, wz, Material.SAND)) {
                        // build 3-block wooden pier outward from shoreline
                        double dirX = wx / dist, dirZ = wz / dist;
                        for (int l = 1; l <= 3; l++) {
                            int px = (int)Math.round(wx + dirX * l);
                            int pz = (int)Math.round(wz + dirZ * l);
                            int py = tropicWorld.getHighestBlockYAt(px, pz);
                            tropicWorld
                                    .getBlockAt(px, py, pz)
                                    .setType(Material.OAK_PLANKS);
                        }
                    }
                }
            }
        }
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
