package goat.minecraft.minecraftnew.subsystems.smithing.talismans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class ReforgeArmorToughness implements Listener {

    private static final double DEFLECT_CHANCE_PER_PIECE = 0.15; // 5% chance per armor piece with the enchantment

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event)  {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Entity damager = event.getDamager();

            // Calculate total deflect chance based on armor pieces
            double totalDeflectChance = 0.0;

            for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
                if (armorPiece != null && hasReforgedToughnessLore(armorPiece)) {
                    totalDeflectChance += DEFLECT_CHANCE_PER_PIECE;
                }
            }

            // Attempt to deflect the attack
            if (Math.random() < totalDeflectChance) {
                // Push the attacker back
                if (damager instanceof Entity) {
                    Vector pushBack = damager.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                    damager.setVelocity(pushBack);

                    // Log the deflection event
                    Bukkit.getLogger().info(ChatColor.AQUA + player.getName() + " deflected the attack from " + damager.getName() + "!");
                }

                // Cancel the damage event
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if the item has "Talisman: Armor Toughness" in its lore.
     *
     * @param item The ItemStack to check.
     * @return True if the item has "Talisman: Armor Toughness" in its lore, false otherwise.
     */
    private boolean hasReforgedToughnessLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Armor Toughness")) {
                    return true;
                }
            }
        }
        return false;
    }
}
