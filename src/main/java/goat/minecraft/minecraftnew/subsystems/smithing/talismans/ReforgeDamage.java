package goat.minecraft.minecraftnew.subsystems.smithing.talismans;

import goat.minecraft.minecraftnew.subsystems.utils.TalismanManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ReforgeDamage implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the entity damaging is a player
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            // Check if the item has the "Reforged: Damage" lore
            String reforgeType = TalismanManager.getReforgeType(itemInHand);
            if ("Damage".equals(reforgeType)) {
                // Increase damage by 10%
                double originalDamage = event.getDamage();
                double bonusDamage = originalDamage * 0.30;
                event.setDamage(originalDamage + bonusDamage);

                // Optionally, notify the player with a message
                //player.sendMessage("" + originalDamage);
                Bukkit.getLogger().info(
                        ChatColor.GOLD + "" + player + " dealt an additional " + Math.round(bonusDamage)  + " damage!");
            }
        }
    }
}
