package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener class to handle sword damage based on reforge tiers.
 */
public class SwordReforge implements Listener {

    private final ReforgeManager reforgeManager;

    /**
     * Constructor to initialize the ReforgeManager.
     *
     * @param reforgeManager Instance of ReforgeManager.
     */
    public SwordReforge(ReforgeManager reforgeManager) {
        this.reforgeManager = reforgeManager;
    }

    /**
     * Event handler to modify damage based on sword reforge tier.
     *
     * @param event The EntityDamageByEntityEvent.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon is a sword
        if (!reforgeManager.isSword(weapon)) {
            return;
        }

        // Retrieve the reforge tier from the weapon
        int reforgeTier = reforgeManager.getReforgeTier(weapon);

        // If the weapon hasn't been reforged, no damage modification is needed
        if (reforgeTier == ReforgeManager.ReforgeTier.TIER_0.getTier()) {
            return;
        }

        // Get the corresponding ReforgeTier enum
        ReforgeManager.ReforgeTier tier = reforgeManager.getReforgeTierByTier(reforgeTier);

        if (tier == null || tier.getWeaponDamageIncrease() == 0) {
            return;
        }

        // Calculate the damage multiplier
        double multiplier = 1 + (tier.getWeaponDamageIncrease() / 100.0);

        // Apply the damage multiplier
        double originalDamage = event.getDamage();
        double newDamage = originalDamage * multiplier;

        // Set the new damage value
        event.setDamage(newDamage);
        Bukkit.getLogger().info(ChatColor.GREEN + "" + player + "'s reforge increases outgoing damage by " + newDamage + "!");
        // Optional: Send feedback to the player (can be removed for less spam)

    }
}
