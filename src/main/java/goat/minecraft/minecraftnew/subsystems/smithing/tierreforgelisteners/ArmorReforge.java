package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener class to handle armor damage reduction based on reforge tiers.
 */
public class ArmorReforge implements Listener {

    private final ReforgeManager reforgeManager;
    // Define a maximum total damage reduction to prevent excessive protection
    private static final double MAX_TOTAL_REDUCTION = 20; // 40%

    /**
     * Constructor to initialize the ReforgeManager.
     *
     * @param reforgeManager Instance of ReforgeManager.
     */
    public ArmorReforge(ReforgeManager reforgeManager) {
        this.reforgeManager = reforgeManager;
    }

    /**
     * Event handler to modify incoming damage based on the player's armor reforge tiers.
     *
     * @param event The EntityDamageEvent.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity damagedEntity = event.getEntity();

        // Check if the damaged entity is a player
        if (!(damagedEntity instanceof Player)) {
            return;
        }

        // Check if the damage is from a monster
        if (!isDamageFromMonster(event.getCause())) {
            return;
        }

        Player player = (Player) damagedEntity;
        ItemStack[] armorContents = player.getInventory().getArmorContents();

        double totalReduction = 0.0;

        // Iterate through each armor piece
        for (ItemStack armorPiece : armorContents) {
            if (reforgeManager.isArmor(armorPiece)) {
                int reforgeTierNumber = reforgeManager.getReforgeTier(armorPiece);
                ReforgeManager.ReforgeTier tier = reforgeManager.getReforgeTierByTier(reforgeTierNumber);

                if (tier != null) {
                    int reduction = tier.getArmorDamageReduction();
                    totalReduction += reduction;
                }
            }
        }

        // Cap the total damage reduction to prevent excessive protection
        if (totalReduction > MAX_TOTAL_REDUCTION) {
            totalReduction = MAX_TOTAL_REDUCTION;
        }

        // Calculate the final damage after reduction
        double originalDamage = event.getDamage();
        double reducedDamage = originalDamage * (1 - (totalReduction / 100.0));

        // Set the new damage value
        event.setDamage(reducedDamage);

        // Optional: Send feedback to the player about the damage reduction
        // Uncomment the following lines if you want players to receive messages about damage reduction

        if (totalReduction > 0) {
            Bukkit.getLogger().info(ChatColor.GREEN + "" + player + "'s reforges reduces incoming damage by " + totalReduction + "%!");
        }
    }

    /**
     * Check if the damage is from a monster.
     *
     * @param cause The cause of the damage.
     * @return True if the damage is from a monster, false otherwise.
     */
    private boolean isDamageFromMonster(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_ATTACK: // Damage from mob attacks
            case PROJECTILE: // Damage from projectiles (e.g., arrows, fireballs)
                return true;
            default:
                return false;
        }
    }
}
