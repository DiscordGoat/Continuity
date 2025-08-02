package goat.minecraft.minecraftnew.other.resistance;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener that reduces the effectiveness of Protection and Feather Falling
 * enchantments against fall damage. Any player wearing gear with these
 * enchantments will take roughly 50% more fall damage.
 */
public class FallDamageNerfListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        boolean hasFeather = false;
        boolean hasProtection = false;

        ItemStack boots = player.getInventory().getBoots();
        if (boots != null && boots.containsEnchantment(Enchantment.FEATHER_FALLING)) {
            hasFeather = true;
        }

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece != null && armorPiece.containsEnchantment(Enchantment.PROTECTION)) {
                hasProtection = true;
                break;
            }
        }

        if (hasFeather || hasProtection) {
            double damage = event.getDamage();
            event.setDamage(damage * 1.3);
        }
    }
}

