package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PotionOfStrength implements Listener {

    /**
     * Listens for a player drinking a potion.
     * If the potion’s display name (after stripping colors) equals "Potion of Strength",
     * the custom effect is added for 15 seconds.
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Strength")) {
                Player player = event.getPlayer();
                // Add the custom effect for 15 seconds
                PotionManager.addCustomPotionEffect("Potion of Strength", player, 15);
                player.sendMessage(ChatColor.GREEN + "Potion of Strength effect activated for 15 seconds!");
            }
        }
    }

    /**
     * Listens for when a player deals damage.
     * If the damager is a player with an active "Potion of Strength" effect,
     * increases the damage by 15%.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (PotionManager.isActive("Potion of Strength", player)) {
                double extraDamage = event.getDamage() * 0.15;
                event.setDamage(event.getDamage() + extraDamage);
                // Debug notification; in a production environment you might remove or adjust this.
                player.sendMessage(ChatColor.YELLOW + "Potion of Strength: Damage increased by 15%!");
            }
        }
    }
}
