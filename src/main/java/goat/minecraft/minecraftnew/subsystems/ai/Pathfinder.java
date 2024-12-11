package goat.minecraft.minecraftnew.subsystems.ai;

import goat.minecraft.minecraftnew.MinecraftNew;
import me.gamercoder215.mobchip.EntityBrain;
import me.gamercoder215.mobchip.ai.EntityAI;
import me.gamercoder215.mobchip.ai.controller.NaturalMoveType;
import me.gamercoder215.mobchip.bukkit.BukkitBrain;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Pathfinder
{




    public void moveTo(Mob mob, Location location) {
        EntityBrain brain = BukkitBrain.getBrain(mob);
        brain.getController().moveTo(location);
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






}
