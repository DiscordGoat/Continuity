package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Logger;

/**
 * Spawns and manages repeating assault waves for a single player.
 */
public class AssaultWaveManager {

    private static AssaultWaveManager instance;

    public static AssaultWaveManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new AssaultWaveManager(plugin);
        }
        return instance;
    }

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Random random = new Random();
    private final Range spawnRange = new Range(60, 80);

    private Player player;
    private int waveNumber = 0;
    private List<Monster> currentWave = new ArrayList<>();
    private Location waveOrigin;
    private long lastCombatTime;
    private boolean resting = false;
    private boolean highlighted = false;

    private BukkitRunnable monitorTask;

    private AssaultWaveManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void start(Player player) {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
        this.player = player;
        this.waveNumber = 0;
        this.resting = false;
        this.highlighted = false;
        this.currentWave.clear();
        player.getWorld().setTime(18000L);
        spawnNextWave();
        monitorTask = new MonitorTask();
        monitorTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void skipRest() {
        if (resting) {
            resting = false;
            spawnNextWave();
        }
    }

    private void spawnNextWave() {
        highlighted = false;
        waveNumber++;
        int extra = (waveNumber - 1) * 2;
        waveOrigin = findOptimalMonsterNode(player, spawnRange);
        if (waveOrigin == null) {
            logger.warning("[Bloodmoon] Could not find spawn location for wave");
            return;
        }
        logger.info("[Bloodmoon] Spawning assault wave #" + waveNumber + " for " + player.getName());
        List<SpawnRequest> mobs = buildDistribution(extra);
        for (SpawnRequest req : mobs) {
            for (int i = 0; i < req.count; i++) {
                Location loc = offset(waveOrigin, req.radius).add(0, 1, 0);
                Monster m = (Monster) player.getWorld().spawnEntity(loc, req.type);
                if (m instanceof Zombie zombie) {
                    zombie.setCanPickupItems(false);
                }
                if (m instanceof Skeleton skeleton) {
                    pathfind(skeleton, player.getLocation());
                } else {
                    m.setTarget(player);
                }
                currentWave.add(m);
            }
        }
        lastCombatTime = System.currentTimeMillis();
    }

    private List<SpawnRequest> buildDistribution(int extra) {
        int zombies = 6;
        int skeletons = 4;
        int spiders = 4;
        int creepers = 1;
        if (random.nextDouble() < 0.1) {
            int total = zombies + skeletons + spiders + creepers;
            zombies = skeletons = spiders = creepers = 0;
            for (int i = 0; i < total; i++) {
                switch (pickMonsterType()) {
                    case ZOMBIE -> zombies++;
                    case SKELETON -> skeletons++;
                    case SPIDER -> spiders++;
                    case CREEPER -> creepers++;
                }
            }
        }
        for (int i = 0; i < extra; i++) {
            switch (pickMonsterType()) {
                case ZOMBIE -> zombies++;
                case SKELETON -> skeletons++;
                case SPIDER -> spiders++;
                case CREEPER -> creepers++;
            }
        }
        return Arrays.asList(
            new SpawnRequest(EntityType.SPIDER, spiders, 2),
            new SpawnRequest(EntityType.ZOMBIE, zombies, 5),
            new SpawnRequest(EntityType.CREEPER, creepers, 5),
            new SpawnRequest(EntityType.SKELETON, skeletons, 8)
        );
    }

    private EntityType pickMonsterType() {
        double d = random.nextDouble();
        if (d < 0.25) return EntityType.CREEPER;
        if (d < 0.5) return EntityType.SPIDER;
        if (d < 0.75) return EntityType.SKELETON;
        return EntityType.ZOMBIE;
    }

    private Location offset(Location base, double radius) {
        double angle = random.nextDouble() * Math.PI * 2;
        return base.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    private void pathfind(Monster mob, Location to) {
        EntityBrain brain = BukkitBrain.getBrain(mob);
        if (brain != null) {
            brain.getController().moveTo(to);
        }
    }

    private class MonitorTask extends BukkitRunnable {
        @Override
        public void run() {
            if (player == null || !player.isOnline()) {
                cancel();
                return;
            }
            if (player.isDead() || player.isSleeping()) {
                clearCurrentWave();
                cancel();
                return;
            }
            if (waveOrigin != null && player.getLocation().distanceSquared(waveOrigin) > 2500) {
                clearCurrentWave();
                cancel();
                return;
            }
            currentWave.removeIf(m -> m.isDead() || !m.isValid());
            if (currentWave.isEmpty()) {
                if (!resting) {
                    resting = true;
                    player.sendMessage(ChatColor.GREEN + "Wave cleared! Next wave in 30 seconds. Click to skip: "
                            + ChatColor.AQUA + ChatColor.UNDERLINE + "/skip");
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (resting) {
                            resting = false;
                            spawnNextWave();
                        }
                    }, 20L * 30);
                }
                return;
            }
            long now = System.currentTimeMillis();
            if (now - lastCombatTime > 30000 && !highlighted) {
                highlighted = true;
                for (Monster m : currentWave) {
                    m.setGlowing(true);
                }
            }
            if (highlighted && now - lastCombatTime > 60000) {
                clearCurrentWave();
                spawnNextWave();
            }
        }
    }

    private void clearCurrentWave() {
        for (Monster m : currentWave) {
            if (m.isValid()) m.remove();
        }
        currentWave.clear();
    }

    public void onWaveEntityDamaged(Entity entity) {
        if (entity instanceof Monster mob && currentWave.contains(mob)) {
            lastCombatTime = System.currentTimeMillis();
            if (highlighted) mob.setGlowing(false);
            highlighted = false;
        }
    }

    public Location findOptimalMonsterNode(Player player, Range range) {
        Location best = null;
        double bestScore = Double.MAX_VALUE;
        for (int i = 0; i < 40; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = range.random(random);
            int x = player.getLocation().getBlockX() + (int) (Math.cos(angle) * radius);
            int z = player.getLocation().getBlockZ() + (int) (Math.sin(angle) * radius);
            int y = player.getWorld().getHighestBlockYAt(x, z);
            Location loc = new Location(player.getWorld(), x + 0.5, y, z + 0.5);
            if (!isValidSpawnLocation(loc)) continue;
            double var = terrainVariance(player.getWorld(), x, z);
            if (var < bestScore) {
                bestScore = var;
                best = loc;
            }
        }
        return best;
    }

    private boolean isValidSpawnLocation(Location loc) {
        if (loc.getBlock().isLiquid()) return false;
        if (loc.getBlock().getLightLevel() > 7) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (loc.clone().add(dx, 0, dz).getBlock().isLiquid()) return false;
            }
        }
        return true;
    }

    private double terrainVariance(World world, int baseX, int baseZ) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int y = world.getHighestBlockYAt(baseX + dx, baseZ + dz);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        return maxY - minY;
    }

    private record SpawnRequest(EntityType type, int count, double radius) {}
}
