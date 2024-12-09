package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class Bloodlust implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onMonsterHit(EntityDamageByEntityEvent event) {
        // Check if the attacker is a player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();

        // Check if the target is a monster
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity)) return;

        // Get the player's weapon
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon has the "Bloodlust" enchantment
        if (CustomEnchantmentManager.hasEnchantment(weapon, "Bloodlust")) {
            if(!(event.getEntity() instanceof LivingEntity)){
                return;
            }
            int bloodlustLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Bloodlust");

            // Calculate chance based on Bloodlust level
            double chance = bloodlustLevel * 1.0;
            if (random.nextDouble() * 100 < chance) {
                // Play heartbeat sound for the player
                player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);

                // Apply potion effects for 30 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1)); // Strength II

                // Optional message to indicate activation
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Bloodlust");
                player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

            }
        }
    }
}
