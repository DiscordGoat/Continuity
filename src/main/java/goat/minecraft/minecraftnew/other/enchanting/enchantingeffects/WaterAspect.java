package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class WaterAspect implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        Player player = null;
        if (damager instanceof Player) {
            player = (Player) damager;
        } else if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Player) {
            player = (Player) ((Projectile) damager).getShooter();
        }
        if (player == null) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.isEnchantmentActive(player, weapon, "Water Aspect")) return;

        int level = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Water Aspect");
        if (level < 1) return;

        double multiplier = 0.0;
        EntityType type = target.getType();
        if (type == EntityType.GUARDIAN || type == EntityType.ELDER_GUARDIAN
                || type == EntityType.ENDERMAN || type == EntityType.ENDERMITE
                || type == EntityType.MAGMA_CUBE || type == EntityType.BLAZE) {
            multiplier = 0.10 * level;
        } else if (target.hasMetadata("SEA_CREATURE")) {
            multiplier = 0.05 * level;
        }

        if (multiplier > 0.0) {
            event.setDamage(event.getDamage() * (1 + multiplier));
        }
    }
}
