package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class AspectOfTheJourney implements Listener {

    // Tracks usage timestamps for the overheat mechanic
    private final Map<UUID, List<Long>> usageTimestamps = new HashMap<>();
    private final Random random = new Random();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Only care about right-clicking in the air or block (if you want to allow both).
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Player player = event.getPlayer();

        // Require the player to be sneaking (crouching)
        if (!player.isSneaking()) {
            return;
        }

        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check for the custom enchantment
        if (!CustomEnchantmentManager.hasEnchantment(itemInHand, "Aspect of the Journey")) {
            return;
        }

        // Overheat logic: track uses in the last 60 seconds
        long now = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        usageTimestamps.putIfAbsent(uuid, new ArrayList<>());
        List<Long> timestamps = usageTimestamps.get(uuid);

        // Clean out timestamps older than 60 seconds
        timestamps.removeIf(t -> (now - t) > 60_000);

        // Add this use
        timestamps.add(now);

        // How many times used in the last minute (including this one)
        int usesInLastMinute = timestamps.size();

        // Teleport the player 6 blocks ahead
        Vector direction = player.getLocation().getDirection().normalize();
        Vector offset = direction.multiply(6);
        player.teleport(player.getLocation().add(offset));

        // Decide what happens to the player depending on usage count
        if (usesInLastMinute <= 4) {
            // Normal effect: drain saturation if possible, else drain hunger
            float currentSaturation = player.getSaturation();
            if (currentSaturation >= 1) {
                // Remove 1 saturation
                player.setSaturation(currentSaturation - 1);
            } else {
                // Remove 1 hunger (50% chance, if you want to preserve the original random logic)
                if (random.nextBoolean()) {
                    int currentFoodLevel = player.getFoodLevel();
                    player.setFoodLevel(Math.max(currentFoodLevel - 1, 0));
                }
            }
        } else {
            // Overheated usage:
            //  - If hunger > 14, remove 1 hunger
            //  - If hunger <= 14, deal half a heart (1 HP) damage
            int foodLevel = player.getFoodLevel();
            if (foodLevel > 14) {
                player.setFoodLevel(Math.max(foodLevel - 1, 0));
            } else {
                // Half a heart is 1 HP damage
                player.damage(1.0);
            }
        }

        // Play teleport sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 10);

        // Increase item damage based on enchantment level
        int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(itemInHand, "Aspect of the Journey");
        short newDurability = (short) (itemInHand.getDurability() + (1 * enchantmentLevel));
        itemInHand.setDurability(newDurability);
    }
}
