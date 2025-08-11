package goat.minecraft.minecraftnew.other.arenas;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
    private final List<Location> arenaLocations = new ArrayList<>();
    private final File arenaFile;

    private ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.arenaFile = new File(plugin.getDataFolder(), "arenaLocations.yml");
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
     * Calculates arena locations in concentric rings and stores them in arenaLocations.yml.
     */
    public void activateArenas() {
        arenaLocations.clear();
        plugin.getDataFolder().mkdirs();

        World world = Bukkit.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();
        Random random = new Random();

        for (int ring = 1; ring <= 4; ring++) {
            int radius = ring * 250; // 250, 500, 750, 1000
            int arenasNeeded = ring * 3;
            int placed = 0;
            while (placed < arenasNeeded) {
                double angle = random.nextDouble() * Math.PI * 2;
                double x = spawn.getX() + radius * Math.cos(angle);
                double z = spawn.getZ() + radius * Math.sin(angle);
                Location loc = new Location(world, x, -100, z);
                if (isFarEnough(loc)) {
                    arenaLocations.add(loc);
                    placed++;
                }
            }
        }

        saveArenaLocations();
    }

    private boolean isFarEnough(Location candidate) {
        for (Location loc : arenaLocations) {
            if (loc.getWorld().equals(candidate.getWorld()) && loc.distance(candidate) < 200) {
                return false;
            }
        }
        return true;
    }

    private void saveArenaLocations() {
        YamlConfiguration config = new YamlConfiguration();
        for (int i = 0; i < arenaLocations.size(); i++) {
            Location loc = arenaLocations.get(i);
            String path = "arenas." + i;
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getBlockX());
            config.set(path + ".y", loc.getBlockY());
            config.set(path + ".z", loc.getBlockZ());
        }
        try {
            config.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arena locations: " + e.getMessage());
        }
    }

    /**
     * Returns the location of the nearest arena to the provided location.
     *
     * @param location input location
     * @return nearest arena location or null if none exist in the same world
     */
    public Location getNearestArena(Location location) {
        Location nearest = null;
        double best = Double.MAX_VALUE;
        for (Location loc : arenaLocations) {
            if (!loc.getWorld().equals(location.getWorld())) continue;
            double dist = loc.distanceSquared(location);
            if (dist < best) {
                best = dist;
                nearest = loc;
            }
        }
        return nearest;
    }
}

