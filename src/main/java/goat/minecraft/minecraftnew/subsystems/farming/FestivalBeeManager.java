package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FestivalBeeManager {
    private static FestivalBeeManager instance;
    private final JavaPlugin plugin;
    private final Map<UUID, Bee> beeRefs = new HashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();
    private final Map<World, BukkitTask> timelapseTasks = new HashMap<>();
    private int festivalBeeCount = 0; // in-memory only

    private FestivalBeeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        cleanup();
    }

    public static FestivalBeeManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new FestivalBeeManager(plugin);
        }
        return instance;
    }

    private void cleanup() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                String name = e.getCustomName();
                if (name != null && name.startsWith(ChatColor.GOLD + "Festival Bee")) {
                    e.remove();
                }
            }
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }
    }

    public void spawnFestivalBee(Location loc, int seconds) {
        World world = loc.getWorld();
        if (world == null) return;
        Bee bee = (Bee) world.spawnEntity(loc.clone().add(0,5,0), EntityType.BEE);
        bee.setCustomName(ChatColor.GOLD + "Festival Bee: " + seconds);
        bee.setCustomNameVisible(true);
        bee.setRemoveWhenFarAway(true);
        beeRefs.put(bee.getUniqueId(), bee);
        festivalBeeCount++;
        updateRules(world);

        // Schedule expiry to remove the bee and decrement count
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                Bee b = beeRefs.remove(bee.getUniqueId());
                expiryTasks.remove(bee.getUniqueId());
                if (b != null && !b.isDead()) {
                    b.remove();
                }
                if (festivalBeeCount > 0) festivalBeeCount--;
                updateRules(world);
            }
        }.runTaskLater(plugin, seconds * 20L);
        expiryTasks.put(bee.getUniqueId(), task);
    }

    public void onFestivalBeeDeath(UUID id) {
        Bee b = beeRefs.remove(id);
        BukkitTask t = expiryTasks.remove(id);
        if (t != null) t.cancel();
        if (festivalBeeCount > 0) festivalBeeCount--;
        // update rules for all worlds
        for (World w : Bukkit.getWorlds()) updateRules(w);
    }
    private void startSunsetTimelapse(World world, long targetTime, int durationTicks) {
        long startTime = world.getTime();
        // compute forward delta (handles wrapping past 24000)
        long delta = ((targetTime - startTime) % 24000 + 24000) % 24000;

        // amount to advance each tick
        double step = delta / (double) durationTicks;
        // accumulator holds our “precise” time
        final double[] acc = { startTime };

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                ticks++;
                acc[0] += step;
                long newTime = (long) acc[0] % 24000;
                world.setTime(newTime);

                if (ticks >= durationTicks) {
                    // snap to exact targetTime & finish
                    world.setTime(targetTime);
                    this.cancel();
                    timelapseTasks.remove(world);
                }
            }
        }.runTaskTimer(MinecraftNew.getInstance(), 0L, 1L);

        timelapseTasks.put(world, task);
    }

    private void updateRules(World world) {
        int count = festivalBeeCount;

        // adjust tick speed always
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3 + count * 2);

        if (count > 0) {
            // Disable natural day/night0
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            // Only start a new timelapse if one isn't already running
            if (!timelapseTasks.containsKey(world)) {
                startSunsetTimelapse(world, 12000L, 600); // 200 ticks = 10s
            }

        } else {
            // No bees: restore
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);

            // Cancel any running timelapse
            BukkitTask task = timelapseTasks.remove(world);
            if (task != null) task.cancel();
        }
    }

    public int getFestivalBeeCount() { return festivalBeeCount; }

}
