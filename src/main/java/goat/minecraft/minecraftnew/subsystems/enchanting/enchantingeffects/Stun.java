package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Stun implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onEntityHitByArrow(EntityDamageByEntityEvent event) {
        // Check if the damage was caused by an arrow
        if (!(event.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        Player shooter = (Player) arrow.getShooter();
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        // Check if the bow has the "Stun" enchantment
        if (CustomEnchantmentManager.hasEnchantment(bow, "Stun")) {
            int stunLevel = CustomEnchantmentManager.getEnchantmentLevel(bow, "Stun");

            // Calculate chance based on stun level
            int chance = stunLevel;
            if (random.nextInt(100) < chance) {
                Entity hitEntity = event.getEntity();
                if (hitEntity instanceof LivingEntity) {
                    LivingEntity target = (LivingEntity) hitEntity;

                    // Apply Slowness V for 30 seconds
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30 * 20, 4)); // Slowness V is amplifier 4

                    // Notify shooter about the successful stun
                    shooter.sendMessage(ChatColor.AQUA + "Stun effect applied to " + target.getName() + "!");
                    int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(bow, "Stun");
                    shooter.getInventory().getItemInMainHand().setDurability((short) (shooter.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

                }
            }
        }
    }
}
