package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Projectile;

public class Velocity implements Listener {

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        ItemStack bow = event.getBow();
        if (bow == null || !bow.getType().toString().contains("BOW")) return;

        if (!CustomEnchantmentManager.hasEnchantment(bow, "Velocity")) return;

        int level = CustomEnchantmentManager.getEnchantmentLevel(bow, "Velocity");
        if (level < 1) return;

        Projectile projectile = (Projectile) event.getProjectile();
        projectile.setVelocity(projectile.getVelocity().multiply(1 + 0.25 * level));
    }
}
