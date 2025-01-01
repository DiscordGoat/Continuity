package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BonePlating implements Listener {
    private final PetManager petManager;

    public BonePlating(JavaPlugin plugin) {
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

        // Check if the player has the BONE_PLATING perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BONE_PLATING)) {
            int petLevel = activePet.getLevel();

            // Calculate damage reduction percentage
            double damageReduction = Math.min(petLevel * 0.5, 50.0); // Cap at 50% reduction
            double reductionFactor = 1 - (damageReduction / 100.0);

            // Reduce the damage directly
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * reductionFactor;
            event.setDamage(reducedDamage);

            // Notify the player of the damage reduction


            // Play a sound to indicate the perk activated
            player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_HURT, 1.0f, 1.0f);
        }
    }
}
