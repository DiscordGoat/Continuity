package goat.minecraft.minecraftnew.subsystems.farming;

import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FestivalBeeManager {
    private static FestivalBeeManager instance;
    private final JavaPlugin plugin;
    private final Map<UUID, Integer> bees = new HashMap<>();

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
            for (World w : Bukkit.getWorlds()) {
                e = w.getEntity(en.getKey());
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

    private void updateRules(World world) {
        int count = bees.size();
        if (count > 0) {
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3 + count * 100);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setTime(12000);
        } else {
            world.setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        }
    }
}
