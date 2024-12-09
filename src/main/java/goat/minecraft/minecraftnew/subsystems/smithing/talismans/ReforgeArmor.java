package goat.minecraft.minecraftnew.subsystems.smithing.talismans;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ReforgeArmor implements Listener {

    private static final double ARMOR_REDUCTION_PER_PIECE = 0.01; // 4% reduction per reforged piece

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            double totalReduction = 0.0;

            // Debug message: Initial damage
            Bukkit.getLogger().info(ChatColor.YELLOW + "Initial damage dealt to " + player.getName() + ": " + event.getDamage());

            // Check each armor piece for "Talisman: Armor" lore and accumulate reductions
            for (ItemStack armorPiece : player.getEquipment().getArmorContents()) {
                if (armorPiece != null && hasReforgedArmorLore(armorPiece)) {
                    totalReduction += ARMOR_REDUCTION_PER_PIECE;

                    // Debug message: Armor piece contributing to reduction
                    ItemMeta meta = armorPiece.getItemMeta();
                    String armorName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "Unnamed Armor";
                    Bukkit.getLogger().info(ChatColor.AQUA + player.getName() + " is wearing " + armorName +
                            " with Talisman: Armor, contributing " + (ARMOR_REDUCTION_PER_PIECE * 100) + "% damage reduction.");
                }
            }

            // Ensure total reduction does not exceed 100%
            totalReduction = Math.min(totalReduction, 1.0);

            // Apply cumulative damage reduction
            double reducedDamage = event.getDamage() * (1 - totalReduction);
            event.setDamage(reducedDamage);

            // Debug message: Final reduction summary
            Bukkit.getLogger().info(ChatColor.GREEN + player.getName() + "'s total damage reduction from talismans: " +
                    (totalReduction * 100) + "%.");
            Bukkit.getLogger().info(ChatColor.GREEN + "Final damage dealt to " + player.getName() + ": " + reducedDamage);
        }
    }

    /**
     * Checks if the item has "Talisman: Armor" in its lore.
     *
     * @param item The ItemStack to check.
     * @return True if the item has "Talisman: Armor" in its lore, false otherwise.
     */
    private boolean hasReforgedArmorLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasLore()) {
            for (String lore : meta.getLore()) {
                if (ChatColor.stripColor(lore).equals("Talisman: Armor")) {
                    return true;
                }
            }
        }
        return false;
    }
}
