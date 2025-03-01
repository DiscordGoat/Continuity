package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import goat.minecraft.minecraftnew.MinecraftNew;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BowReforge implements Listener {

    // Persistent integer determining additional damage percentage per tier (20% per tier).
    private static final int ADDITIONAL_DAMAGE_PER_TIER = 20;
    // Key to store and retrieve the bow's reforge tier.
    private static final NamespacedKey REFORGE_TIER_KEY = new NamespacedKey(MinecraftNew.getInstance(), "ReforgeTier");

    /**
     * Event handler to modify arrow damage based on the bow’s reforge tier.
     *
     * @param event The EntityDamageByEntityEvent.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damager is a projectile.
        if (!(event.getDamager() instanceof Projectile)) {
            return;
        }

        Projectile projectile = (Projectile) event.getDamager();

        // Check if the projectile is an arrow.
        if (!(projectile instanceof Arrow)) {
            return;
        }

        // Ensure the shooter is a player.
        if (!(projectile.getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) projectile.getShooter();
        // Get the bow from the shooter's main hand.
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        // Verify that the item is a bow.
        if (!isBow(bow)) {
            return;
        }

        // Retrieve the reforge tier from the bow.
        int reforgeTier = getReforgeTier(bow);

        // If the bow hasn't been reforged (tier 0), skip modification.
        if (reforgeTier == 0) {
            return;
        }

        // Calculate the bonus damage percentage: bonusDamage = reforgeTier * ADDITIONAL_DAMAGE_PER_TIER.
        int bonusDamagePercent = reforgeTier * ADDITIONAL_DAMAGE_PER_TIER;

        // Calculate the damage multiplier based on the bonus damage percentage.
        double multiplier = 1 + (bonusDamagePercent / 100.0);
        double originalDamage = event.getDamage();
        double newDamage = originalDamage * multiplier;
        event.setDamage(newDamage);

        // Provide feedback via the action bar and play a sound effect.
        shooter.playSound(shooter.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 1.0F);
    }

    /**
     * Checks if the given item is a bow.
     *
     * @param item The item to check.
     * @return true if the item is a bow, false otherwise.
     */
    private boolean isBow(ItemStack item) {
        return item != null && item.getType() == Material.BOW;
    }

    /**
     * Retrieves the reforge tier from the bow's persistent data container.
     *
     * @param bow The bow item.
     * @return The reforge tier stored, or 0 if not set.
     */
    private int getReforgeTier(ItemStack bow) {
        ReforgeManager reforgeManager = new ReforgeManager();
        return reforgeManager.getReforgeTier(bow);
    }
}
