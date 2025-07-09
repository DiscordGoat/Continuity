package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * The MagicProtectionListener class listens for damage events
 * and reduces the damage taken by players based on their armor's Physical Protection enchantments.
 */
public class MagicProtectionListener implements Listener {

    // Define the maximum total damage reduction percentage
    private static final double MAX_DAMAGE_REDUCTION = 16.0;

    // Define the damage reduction per armor piece with the enchantment
    private static final double DAMAGE_REDUCTION_PER_PIECE = 1.0;

    /**
     * Handles the EntityDamageEvent to detect physical damage and apply protection.
     *
     * @param event The EntityDamageEvent triggered when an entity takes damage.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        // Only proceed for specific types of damage; you can modify this check as needed
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        // Get the original damage
        double originalDamage = event.getDamage();

        // Initialize total protection level (each armor piece provides 1% reduction)
        int armorPieceCount = 0;

        // Retrieve the player's armor contents
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // Count the armor pieces with the "Physical Protection" enchantment
        for (ItemStack armorPiece : armorContents) {
            if (armorPiece == null) continue; // Skip empty slots

            // Check if the armor piece has the "Physical Protection" enchantment
            if (CustomEnchantmentManager.hasEnchantment(armorPiece, "Physical Protection")) {
                armorPieceCount++;
            }
        }

        // Calculate total damage reduction (1% per armor piece)
        double totalReduction = armorPieceCount * DAMAGE_REDUCTION_PER_PIECE;

        // Cap the reduction to the maximum allowed
        if (totalReduction > MAX_DAMAGE_REDUCTION) {
            totalReduction = MAX_DAMAGE_REDUCTION;
        }

        // Calculate the final damage after reduction
        double reducedDamage = originalDamage * ((100 - totalReduction) / 100);

        // Apply the reduced damage to the event
        event.setDamage(reducedDamage);

    }
}
