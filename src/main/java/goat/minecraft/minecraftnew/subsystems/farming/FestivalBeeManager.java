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
    private final Map<UUID, Integer> bees = new HashMap<>();
    private final Map<World, BukkitTask> timelapseTasks = new HashMap<>();

    private FestivalBeeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        cleanup();
        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L);
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
        bees.put(bee.getUniqueId(), seconds);
        updateRules(world);
    }

    private void tick() {
        Iterator<Map.Entry<UUID, Integer>> it = bees.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> en = it.next();
            Entity e = null;
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (entity.getUniqueId().equals(en.getKey())) {
                        e = entity;
                        break;
                    }
                }
                if (e != null) break;
            }
            if (e == null || e.isDead()) {
                it.remove();
                continue;
            }
            int remaining = en.getValue() - 1;
            if (remaining <= 0) {
                e.remove();
                it.remove();
                continue;
            }
            e.setCustomName(ChatColor.GOLD + "Festival Bee: " + remaining);
            en.setValue(remaining);
        }
        for (World world : Bukkit.getWorlds()) {
            updateRules(world);
        }
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
        int count = bees.size();

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

}
