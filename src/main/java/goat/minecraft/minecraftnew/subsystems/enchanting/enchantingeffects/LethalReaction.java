package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class LethalReaction implements Listener {

    @EventHandler
    public void onArrowShoot(ProjectileLaunchEvent event) {
        // Check if the projectile is an arrow
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();

        // Verify if the shooter is a player and holding a crossbow
        if (!(arrow.getShooter() instanceof Player)) return;
        Player shooter = (Player) arrow.getShooter();
        ItemStack crossbow = shooter.getInventory().getItemInMainHand();

        // Check if the crossbow has the "Lethal Reaction" enchantment
        if (CustomEnchantmentManager.hasEnchantment(crossbow, "Lethal Reaction")) {
            int lethalLevel = CustomEnchantmentManager.getEnchantmentLevel(crossbow, "Lethal Reaction");

            // Cancel the arrow and spawn a fireball instead
            event.setCancelled(true);

            // Spawn fireball with an increased explosion power based on enchantment level
            Fireball fireball = shooter.launchProjectile(Fireball.class);
            fireball.setYield(2.0f + (lethalLevel * 1.0f)); // Base explosion size of 2.0, increasing with level
            fireball.setIsIncendiary(false); // Fireball sets blocks on fire
            fireball.setVelocity(arrow.getVelocity().multiply(0.8)); // Adjust fireball speed
            shooter.getInventory().getItemInMainHand().setDurability((short) (shooter.getInventory().getItemInMainHand().getDurability() +(1*lethalLevel)));
        }
    }
}
