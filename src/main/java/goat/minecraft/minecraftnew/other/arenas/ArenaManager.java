package goat.minecraft.minecraftnew.other.arenas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import goat.minecraft.minecraftnew.utils.devtools.SchemManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles generation and lookup of arena locations around spawn.
 */
public class ArenaManager {

    private static ArenaManager instance;
    private final JavaPlugin plugin;
    private final List<Arena> arenaLocations = new ArrayList<>();
    private final File arenaFile;
    private final SchemManager schemManager;

    private static final int RINGS = 4;
    private static final int RING_STEP = 250;     // ring radius step
    private static final int MIN_SEPARATION = 200; // min distance between arenas

    private ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "arenaLocations.yml");
        this.schemManager = new SchemManager(plugin);
    }

    public static ArenaManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new ArenaManager(plugin);
        }
        return instance;
    }

    public static ArenaManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ArenaManager not initialized");
        }
        return instance;
    }

    /**
     * Loads existing arena locations if present; otherwise generates them once and saves.
     * Safe to call multiple times (idempotent).
     */
    public synchronized void activateArenas() {
        plugin.getDataFolder().mkdirs();

        if (arenaFile.exists()) {
            loadArenaLocations();
        } else {
            arenaLocations.clear();

            World world = Bukkit.getWorlds().get(0);
            Location spawn = world.getSpawnLocation();
            Random random = new Random();

            for (int ring = 1; ring <= RINGS; ring++) {
                int radius = ring * RING_STEP; // 250, 500, 750, 1000
                int arenasNeeded = ring * 3;
                int placed = 0;
                int safety = 0;
                while (placed < arenasNeeded && safety < arenasNeeded * 50) {
                    safety++;
                    double angle = random.nextDouble() * Math.PI * 2;
                    double x = spawn.getX() + radius * Math.cos(angle);
                    double z = spawn.getZ() + radius * Math.sin(angle);
                    Location loc = new Location(world, x, -100, z);
                    if (isFarEnough(loc)) {
                        arenaLocations.add(new Arena(loc, false));
                        placed++;
                    }
                }
            }

            saveArenaLocations();
        }

        startArenaCheckTask();
    }

    private void startArenaCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Location playerLoc = player.getLocation();
                    Arena arena = getNearestArena(playerLoc);
                    if (arena != null) {
                        Location arenaLoc = arena.getLocation();
                        if (playerLoc.getWorld().equals(arenaLoc.getWorld()) &&
                                playerLoc.distanceSquared(arenaLoc) <= 120 * 120 && !arena.isPlaced() && playerLoc.getBlockY() < 0) {
                            schemManager.placeStructure("arena_1", arenaLoc);
                            arena.setPlaced(true);
                            saveArenaLocations();
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
    }

    private boolean isFarEnough(Location candidate) {
        for (Arena arena : arenaLocations) {
            Location loc = arena.getLocation();
            if (loc.getWorld().equals(candidate.getWorld()) && loc.distance(candidate) < MIN_SEPARATION) {
                return false;
            }
        }
        return true;
    }

    private void saveArenaLocations() {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < arenaLocations.size(); i++) {
            Arena arena = arenaLocations.get(i);
            Location loc = arena.getLocation();
            String path = "arenas." + i;
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getBlockX());
            config.set(path + ".y", loc.getBlockY());
            config.set(path + ".z", loc.getBlockZ());
            config.set(path + ".isPlaced", arena.isPlaced());
        }
        try {
            config.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arena locations: " + e.getMessage());
        }
    }

    private void loadArenaLocations() {
        arenaLocations.clear();
        if (!arenaFile.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(arenaFile);
        ConfigurationSection section = config.getConfigurationSection("arenas");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String base = "arenas." + key;
            String worldName = config.getString(base + ".world");
            World world = (worldName == null) ? null : Bukkit.getWorld(worldName);
            if (world == null) continue;

            // Stored as ints; read as doubles is fine.
            double x = config.getDouble(base + ".x");
            double y = config.getDouble(base + ".y");
            double z = config.getDouble(base + ".z");
            boolean placed = config.getBoolean(base + ".isPlaced", false);

            arenaLocations.add(new Arena(new Location(world, x, y, z), placed));
        }
    }

    /**
     * Returns the nearest arena to the provided location.
     *
     * @param location input location
     * @return nearest arena or null if none exist in the same world
     */
    public Arena getNearestArena(Location location) {
        Arena nearest = null;
        double best = Double.MAX_VALUE;
        for (Arena arena : arenaLocations) {
            Location loc = arena.getLocation();
            if (!loc.getWorld().equals(location.getWorld())) continue;
            double dist = loc.distanceSquared(location);
            if (dist < best) {
                best = dist;
                nearest = arena;
            }
        }
        return nearest;
    }
}
