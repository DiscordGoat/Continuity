package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Random;

public class AspectOfTheJourney implements Listener {
    Random random = new Random();
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // Check if the player right-clicked air
        if (!event.getAction().toString().contains("RIGHT_CLICK_AIR")) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if the item has the "Aspect of the Journey" enchantment
        if (CustomEnchantmentManager.hasEnchantment(itemInHand, "Aspect of the Journey")) {
            // Calculate the destination 8 blocks ahead
            Vector direction = player.getLocation().getDirection().normalize();
            Vector offset = direction.multiply(6);
            player.teleport(player.getLocation().add(offset));

            // Deduct 2 saturation if possible, otherwise take 2 hunger
            float currentSaturation = player.getSaturation();
            if (currentSaturation >= 1) {
                player.setSaturation(currentSaturation - 1);
            } else {
                int currentFoodLevel = player.getFoodLevel();
                if(random.nextBoolean()) {
                    player.setFoodLevel(Math.max(currentFoodLevel - 1, 0));
                }
            }

            // Send a message to the player confirming the teleport
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 10);
            int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(itemInHand, "Aspect of the Journey");
            player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

        }
    }
}
