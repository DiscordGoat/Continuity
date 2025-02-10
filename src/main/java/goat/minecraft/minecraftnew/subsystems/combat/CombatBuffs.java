package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.XPManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class CombatBuffs implements Listener {
    XPManager xpManager = new XPManager(MinecraftNew.getInstance());

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            int level = xpManager.getPlayerLevel(player, "Combat");

            // Cap level at 100
            level = Math.min(level, 100);

            // Calculate damage multiplier
            double damageMultiplier = 1 + (level * 0.03);

            // Apply damage multiplier
            double originalDamage = event.getDamage();
            double newDamage = originalDamage * damageMultiplier;

            event.setDamage(newDamage);
        }

        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();

            if (projectile instanceof Arrow) {
                if (projectile.getShooter() instanceof Player) {
                    Player shooter = (Player) projectile.getShooter();

                    // Check if the shooter's bow has Infinity enchantment
                    ItemStack bow = shooter.getInventory().getItemInMainHand();
                    // Retrieve the player's level in the "Combat" skill
                    int level = xpManager.getPlayerLevel(shooter, "Combat");

                    // Cap the level at 100 to prevent excessive multipliers
                    level = Math.min(level, 100);

                    // Calculate damage multiplier based on level
                    double damageMultiplier = 1 + (level * 0.04);

                    // Apply the damage multiplier to the original damage
                    double originalDamage = event.getDamage();
                    double newDamage = originalDamage * damageMultiplier;

                    event.setDamage(newDamage);

                    // Notify the shooter about the damage boost via Action Bar
                    shooter.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                            ChatColor.GREEN + "Damage Boost: x" + String.format("%.2f", damageMultiplier)
                    ));

                    // Play a sound effect to indicate a boosted attack
                    shooter.playSound(shooter.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
                }
            }
        }
    }
}
