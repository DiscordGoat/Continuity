package goat.minecraft.minecraftnew.other;

import me.gamercoder215.mobchip.EntityBody;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Pathfinder
{


    public void reinforceZombies(Entity target, double radius) {
                target.getWorld().getNearbyEntities(target.getLocation(), radius, radius, radius).stream()
                .filter(e -> e instanceof Zombie)
                .map(e -> (Monster) e)
                .forEach(monster -> {
                    EntityBrain monsterBrain = BukkitBrain.getBrain(monster);
                    monsterBrain.getController().moveTo(target.getLocation(), 1.2);
                });
    }

    public void moveTo(Mob mob, Location location) {
        EntityBrain brain = BukkitBrain.getBrain(mob);
        brain.getController().moveTo(location);
    }
    public void flyFast(Mob mob) {
        EntityBrain brain = BukkitBrain.getBrain(mob);
        EntityBody body = brain.getBody();
        body.setFlyingSpeed(1000);
    }
    public void lookAt(Mob mob, Player player, int seconds, Plugin plugin) {
        // Get the entity's global brain
        EntityBrain brain = BukkitBrain.getBrain(mob);

        // Create a new repeating task
        new BukkitRunnable() {
            private int tickCount = 0;
            private final int maxTicks = seconds * 20; // 20 ticks per second

            @Override
            public void run() {
                if (tickCount >= maxTicks) {
                    // Cancel the task after the specified duration
                    this.cancel();
                    return;
                }

                // Force the mob to look at the player
                brain.getController().lookAt(player);

                tickCount++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Start immediately, run every tick
    }
    public int getMobLevel(Entity entity) {
        String name = entity.getName();
        String cleanedName = name.replaceAll("(?i)§[0-9a-f]", "");
        String numberString = cleanedName.replaceAll("[^0-9]", "");
        if (numberString.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(numberString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }
    public Player getNearestPlayer(Entity entity, double radius) {
        Location mobLocation = entity.getLocation();
        double nearestDistanceSquared = radius * radius;
        Player nearestPlayer = null;

        for (Player player : entity.getWorld().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(mobLocation);
            if (distanceSquared <= nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }




}
