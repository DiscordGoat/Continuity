package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Handles custom AI behaviours for Bloodmoon mobs using MobChipLite.
 */
public class WaveBehaviorManager extends BukkitRunnable {

    private static final Map<Monster, WaveData> tracked = new HashMap<>();
    private final JavaPlugin plugin;

    public WaveBehaviorManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Registers a spawned mob for AI control. */
    public static void register(Monster mob, Player target, boolean captain) {
        tracked.put(mob, new WaveData(target, captain));
    }

    /** Removes a mob from AI control. */
    public static void unregister(Monster mob) {
        tracked.remove(mob);
    }

    /** Called when a captain is killed to make nearby mobs retreat. */
    public static void onCaptainDeath(Player killer, Location loc) {
        for (var entry : tracked.entrySet()) {
            Monster m = entry.getKey();
            if (m.getLocation().distanceSquared(loc) <= 400) {
                moveAwayStatic(m, loc);
            }
        }
    }

    /** Removes invalid or dead mobs from tracking. */
    private static void cleanup() {
        tracked.entrySet().removeIf(e -> !e.getKey().isValid());
    }

    @Override
    public void run() {
        cleanup();
        for (var entry : tracked.entrySet()) {
            Monster mob = entry.getKey();
            Player target = entry.getValue().target;
            if (!target.isOnline()) continue;
            applyBehaviors(mob, target, entry.getValue());
        }
    }

    private void applyBehaviors(Monster mob, Player target, WaveData data) {
        // Flee from nearby creepers
        for (Entity e : mob.getNearbyEntities(6, 6, 6)) {
            if (e.getType() == EntityType.CREEPER && e != mob) {
                moveAway(mob, e.getLocation());
                return;
            }
        }

        // Flee when below half health
        double max = Objects.requireNonNull(mob.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        if (mob.getHealth() <= max / 2) {
            moveAway(mob, target.getLocation());
            return;
        }

        // Skeleton specific logic
        if (mob instanceof Skeleton skeleton) {
            double dist = skeleton.getLocation().distance(target.getLocation());
            if (dist < 4) {
                moveAway(skeleton, target.getLocation());
                return;
            }
            Location high = findHigherGround(target.getLocation(), skeleton);
            if (high != null) {
                moveTo(skeleton, high);
                return;
            }
            // Flank if no high ground
            Vector dir = target.getLocation().toVector().subtract(skeleton.getLocation().toVector()).normalize();
            Vector perp = new Vector(-dir.getZ(), 0, dir.getX());
            moveTo(skeleton, skeleton.getLocation().add(perp.multiply(4)));
        }

        // Zombie clumping near player
        if (mob instanceof Zombie zombie) {
            double dist = zombie.getLocation().distance(target.getLocation());
            if (dist < 8) {
                moveTo(zombie, target.getLocation());
            }
        }
    }

    private void moveAway(Monster mob, Location from) {
        Vector dir = mob.getLocation().toVector().subtract(from.toVector()).normalize().multiply(10);
        moveTo(mob, mob.getLocation().add(dir));
    }

    private static void moveAwayStatic(Monster mob, Location from) {
        Vector dir = mob.getLocation().toVector().subtract(from.toVector()).normalize().multiply(10);
        EntityBrain brain = BukkitBrain.getBrain(mob);
        if (brain != null) {
            brain.getController().moveTo(mob.getLocation().add(dir));
        }
    }

    private void moveTo(Monster mob, Location loc) {
        EntityBrain brain = BukkitBrain.getBrain(mob);
        if (brain != null) {
            brain.getController().moveTo(loc);
        }
    }

    private Location findHigherGround(Location around, Skeleton skel) {
        int x0 = around.getBlockX();
        int z0 = around.getBlockZ();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int y = around.getWorld().getHighestBlockYAt(x0 + dx, z0 + dz);
                if (y > around.getBlockY() + 1) {
                    return new Location(around.getWorld(), x0 + dx + 0.5, y + 1, z0 + dz + 0.5);
                }
            }
        }
        return null;
    }

    private record WaveData(Player target, boolean captain) {}
}
