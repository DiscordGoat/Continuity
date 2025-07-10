package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.Pet;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

public class LaserBeamPerkHandler implements Listener {

    private final PetManager petManager;

    public LaserBeamPerkHandler(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onPlayerDamageEnemyRanged(EntityDamageByEntityEvent event) {
        // Check if the damage was caused by an arrow
        if (event.getDamager() instanceof Arrow arrow) {
            // Ensure the arrow's shooter is a player
            if (arrow.getShooter() instanceof Player player) {
                // Get the player's active pet
                Pet activePet = petManager.getActivePet(player);
                if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.LASER_BEAM)) {
                    return;
                }

                // Define the chance and additional laser damage
                int petLevel = activePet.getLevel();
                double chance = petLevel / 100.0; // Scale chance from 0.01 (1%) to 1.0 (100%)
                double laserDamage = 15.0; // Example value for laser damage

                // Roll for the chance
                Random random = new Random();
                if (random.nextDouble() <= chance) {
                    // Play Guardian sound effect
                    if (event.getEntity() instanceof LivingEntity target) {
                        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_GUARDIAN_DEATH, 1.0f, 1.0f);

                        // Deal additional damage to the target
                        target.damage(laserDamage, player);
                        target.getWorld().spawnParticle(Particle.FISHING, target.getLocation(), 200);

                        // Notify the player
                        player.sendMessage(ChatColor.AQUA + "Your pet zapped the enemy with a laser beam!");
                    }
                }
            }
        }
    }
}
