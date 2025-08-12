package goat.minecraft.minecraftnew.subsystems.arenas;

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
 * Manages arena locations arranged in rings around spawn.
 */
public class ArenaManager {

    private static final List<Location> ARENAS = new ArrayList<>();

    private ArenaManager() {
        // utility class
    }

    /**
     * Calculates arena locations and saves them to arenaLocations.yml.
     * Runs on plugin startup.
     *
     * @param plugin the plugin instance
     */
    public static void activateArenas(JavaPlugin plugin) {
        ARENAS.clear();
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        File file = new File(plugin.getDataFolder(), "arenaLocations.yml");
        YamlConfiguration config = new YamlConfiguration();

        Random random = new Random();
        World world = Bukkit.getWorlds().get(0);

        int[] radii = {250, 500, 750, 1000};
        for (int ring = 1; ring <= radii.length; ring++) {
            int radius = radii[ring - 1];
            int arenasInRing = ring * 3; // ringNumber*3 arenas
            for (int i = 0; i < arenasInRing; i++) {
                Location loc;
                int attempts = 0;
                do {
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    loc = new Location(world, x, -100, z);
                    attempts++;
                } while (!isFarEnough(loc) && attempts < 1000);
                ARENAS.add(loc);
            }
        }

        // save arenas to file
        for (int i = 0; i < ARENAS.size(); i++) {
            Location loc = ARENAS.get(i);
            String path = "arenas." + i;
            config.set(path + ".world", loc.getWorld().getName());
            config.set(path + ".x", loc.getX());
            config.set(path + ".y", loc.getY());
            config.set(path + ".z", loc.getZ());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isFarEnough(Location location) {
        for (Location existing : ARENAS) {
            if (existing.distanceSquared(location) < 200 * 200) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the arena location nearest to the provided location.
     *
     * @param location input location
     * @return closest arena location, or null if none exist
     */
    public static Location getNearestArena(Location location) {
        Location nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (Location arena : ARENAS) {
            double dist = arena.distanceSquared(location);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = arena;
            }
        }
        return nearest;
    }
}

