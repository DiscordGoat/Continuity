package goat.minecraft.minecraftnew.subsystems.gravedigging;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import goat.minecraft.minecraftnew.subsystems.corpses.CorpseEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;

import java.util.Random;

/**
 * Handles the gravedigging subsystem which spawns "graves" when
 * players break surface level blocks. Hitting a grave with a shovel
 * triggers a random event.
 */
public class Gravedigging implements Listener {
    private static final double BASE_CHANCE = 1.0; // 100% for testing
    private final Random random = new Random();
    private final Map<Location, BukkitTask> graves = new HashMap<>();
    private final JavaPlugin plugin;

    public Gravedigging(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        if (graves.containsKey(loc)) {
            BukkitTask task = graves.remove(loc);
            if (task != null) task.cancel();
            triggerEvent(player, loc);
            return;
        }
        int highest = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() < highest) return; // only surface blocks

        double chance = BASE_CHANCE;
        if (isNight(world)) {
            chance = Math.min(1.0, chance * 2);
        }
        if (random.nextDouble() <= chance) {
            spawnGraveNear(player);
        }
    }

    private void spawnGraveNear(Player player) {
        Location base = player.getLocation();
        World world = base.getWorld();
        if (world == null) return;

        for (int i = 0; i < 20; i++) {
            int dx = random.nextInt(17) - 8;
            int dz = random.nextInt(17) - 8;
            Location target = base.clone().add(dx, 0, dz);
            int y = world.getHighestBlockYAt(target);
            Block block = world.getBlockAt(target.getBlockX(), y - 1, target.getBlockZ());
            if (!block.getType().isAir() && !graves.containsKey(block.getLocation())) {
                startParticle(block.getLocation());
                world.playSound(block.getLocation().add(0.5, 1, 0.5), Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.6f);
                break;
            }
        }
    }

    private void startParticle(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        Location effectLoc = loc.clone().add(0.5, 1, 0.5);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            world.spawnParticle(Particle.SOUL, effectLoc, 3, 0.1, 0.1, 0.1, 0);
        }, 0L, 20L);
        graves.put(loc, task);
    }


    private void triggerEvent(Player player, Location loc) {
        double roll = random.nextDouble();
        if (roll < 0.5) {
            new CorpseEvent(plugin).trigger(loc);
        } else if (roll < 0.85) {
            player.sendMessage(ChatColor.GOLD + "You find something... (LootEvent)");
        } else {
            player.sendMessage(ChatColor.AQUA + "Treasure unearthed! (TreasureEvent)");
        }
        loc.getWorld().spawnParticle(Particle.BLOCK, loc, 20, 0.2, 0.2, 0.2, Material.DIRT.createBlockData());
        loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);
    }
}
