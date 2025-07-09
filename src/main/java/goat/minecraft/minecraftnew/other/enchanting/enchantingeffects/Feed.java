package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Feed implements Listener {

    private final Random random = new Random();

    /**
     * Handles the event when a player damages an entity.
     * If the player's weapon has the "Feed" enchantment, there's a chance to gain saturation.
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        Entity damagerEntity = event.getDamager();

        // Check if the damager is a player
        if (!(damagerEntity instanceof Player)) {
            return;
        }

        Player player = (Player) damagerEntity;
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon has the "Feed" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Feed")) {
            return;
        }

        // Get the level of the "Feed" enchantment
        int feedLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Feed");

        // Ensure the enchantment level is valid
        if (feedLevel < 1) {
            return;
        }
        if(!(event.getEntity() instanceof LivingEntity)){
            return;
        }

        // Define the chance percentage based on enchantment level
        // Example: Level 1 = 20%, Level 2 = 40%, ..., Level 4 = 80%
        int chancePercentage = Math.min(feedLevel * 10, 16); // Caps at 80% for level 4 and above

        int randomNumber = random.nextInt(100) + 1; // Generates a number between 1 and 100

        if (randomNumber <= chancePercentage) {
            // Player gains 1-4 saturation points
            int saturationPoints = random.nextInt(4) + 1; // 1 to 4 inclusive
            double currentSaturation = player.getSaturation();
            double currentHunger = player.getFoodLevel();
            double maxSaturation = 20;

            double newSaturation = Math.min(currentSaturation + saturationPoints, maxSaturation);
            double newHunger = Math.min(currentSaturation + currentHunger, maxSaturation);
            player.setSaturation((float) newSaturation);
            player.setFoodLevel((int) newHunger);

            // Send feedback to the player
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 5,10);
            int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Feed");
            player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

        }
    }
}
