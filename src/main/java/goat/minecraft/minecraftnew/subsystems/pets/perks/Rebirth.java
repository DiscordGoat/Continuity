package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Rebirth implements Listener {

    private final PetManager petManager;

    public Rebirth(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        // Ensure the entity taking damage is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the REBIRTH perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.REBIRTH)) {
            double finalDamage = event.getFinalDamage();
            double newHealth = player.getHealth() - finalDamage;

            // Activate REBIRTH if health drops to or below zero and the player has sufficient food level
            if (newHealth <= 0 && player.getFoodLevel() > 10) {
                // Cancel the damage event to prevent death
                event.setCancelled(true);

                // Apply REBIRTH effects
                player.setHealth(player.getMaxHealth()); // Restore health to maximum
                player.setFoodLevel(0); // Deplete hunger completely
                player.setSaturation(0); // Deplete saturation completely

                // Notify the player
                // Optional: Add additional effects like particle effects or teleportation
                player.getWorld().strikeLightningEffect(player.getLocation()); // Adds a visual effect
            }
        }
    }
}
