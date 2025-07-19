package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Cleaver implements Listener {

    private final Random random = new Random();

    /**
     * Handles the event when a player damages an entity.
     * If the player's weapon has the "Feed" enchantment, there's a chance to gain saturation.
     *
     * @param event The EntityDamageByEntityEvent triggered when an entity is damaged by another entity.
     */

    @EventHandler
    public void onHit(EntityDeathEvent event) {
        Entity killed = event.getEntity();
        Entity killer = event.getEntity().getKiller();

        // Check if the damager is a player
        if (!(killer instanceof Player)) {
            return;
        }

        Player player = (Player) killer;
        ItemStack weapon = player.getInventory().getItemInMainHand();

        // Check if the weapon has the "Feed" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(weapon, "Cleaver")) {
            return;
        }

        // Get the level of the "Feed" enchantment
        int feedLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Cleaver");

        // Ensure the enchantment level is valid
        if (feedLevel < 1) {
            return;
        }

        // Define the chance percentage based on enchantment level
        // Example: Level 1 = 20%, Level 2 = 40%, ..., Level 4 = 80%
        int chancePercentage = Math.min(feedLevel, 5); // Caps at 80% for level 4 and above

        int randomNumber = random.nextInt(100) + 1; // Generates a number between 1 and 100

        if (randomNumber <= chancePercentage) {
            // Player gains 1-4 saturation points
            if(killed instanceof Zombie){
                killed.getLocation().getWorld().dropItem(killed.getLocation(), new ItemStack(Material.ZOMBIE_HEAD));
                player.sendMessage(ChatColor.GREEN + "Your " + ChatColor.AQUA + "Cleaver " + feedLevel + ChatColor.GREEN +
                        " enchantment decapitated your enemy!");
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Cleaver");
                CustomDurabilityManager.getInstance().applyDamage(player, weapon, 2 * enchantmentLevel);

            }
            if(killed instanceof Skeleton){
                killed.getLocation().getWorld().dropItem(killed.getLocation(), new ItemStack(Material.SKELETON_SKULL));
                player.sendMessage(ChatColor.GREEN + "Your " + ChatColor.AQUA + "Cleaver " + feedLevel + ChatColor.GREEN +
                        " enchantment decapitated your enemy!");
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Cleaver");
                CustomDurabilityManager.getInstance().applyDamage(player, weapon, enchantmentLevel);

            }
            if(killed instanceof Creeper){
                killed.getLocation().getWorld().dropItem(killed.getLocation(), new ItemStack(Material.CREEPER_HEAD));
                player.sendMessage(ChatColor.GREEN + "Your " + ChatColor.AQUA + "Cleaver " + feedLevel + ChatColor.GREEN +
                        " enchantment decapitated your enemy!");
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Cleaver");
                CustomDurabilityManager.getInstance().applyDamage(player, weapon, enchantmentLevel);

            }
            if(killed instanceof WitherSkeleton){
                killed.getLocation().getWorld().dropItem(killed.getLocation(), new ItemStack(Material.WITHER_SKELETON_SKULL));
                player.sendMessage(ChatColor.GREEN + "Your " + ChatColor.AQUA + "Cleaver " + feedLevel + ChatColor.GREEN +
                        " enchantment decapitated your enemy!");
                int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(weapon, "Cleaver");
                CustomDurabilityManager.getInstance().applyDamage(player, weapon, enchantmentLevel);

            }


        }
    }
}
