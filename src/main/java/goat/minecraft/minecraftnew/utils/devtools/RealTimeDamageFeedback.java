package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RealTimeDamageFeedback implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        double damage = event.getDamage();

        // Check if the damager is a player (melee damage).
        if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            damager.sendMessage(ChatColor.YELLOW + "You dealt " + String.format("%.2f", damage) + " damage!");
        }
        // Otherwise, if the damager is a projectile, check if its shooter is a player.
        else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();
                shooter.sendMessage(ChatColor.YELLOW + "Your projectile dealt " + String.format("%.2f", damage) + " damage!");
            }
        }

        // Check if a player is receiving damage.
        if (event.getEntity() instanceof Player) {
            Player target = (Player) event.getEntity();
            target.sendMessage(ChatColor.RED + "You took " + String.format("%.2f", damage) + " damage!");
        }
    }
}
