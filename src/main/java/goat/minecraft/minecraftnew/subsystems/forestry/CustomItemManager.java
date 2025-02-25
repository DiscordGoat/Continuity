package goat.minecraft.minecraftnew.subsystems.forestry;


import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CustomItemManager implements Listener {
    // Method to create a custom item
    Random random = new Random();
    MinecraftNew plugin = MinecraftNew.getInstance();

    public static ItemStack createCustomItem(Material material, String name, List<String> lore, int amount, boolean unbreakable, boolean addEnchantmentShimmer) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        // Set custom name
        if (name != null) {
            meta.setDisplayName(name);
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        // Set unbreakable
        //meta.setUnbreakable(unbreakable);

        // Add enchantment shimmer if specified

        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // This hides the enchantments from the item's tooltip
        // Set the item meta
        item.setItemMeta(meta);
        if (addEnchantmentShimmer) {
            // Add a dummy enchantment to create the shimmer effect
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1); // This enchantment does not affect gameplay

        }
        return item;
    }


    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        XPManager xpManager = new XPManager(plugin);
        int forbiddenBookChance = random.nextInt(100) + 1; // Generates a number between 1 and 100

        if (event.getEntity().getKiller() != null) {
            double playerLevel = xpManager.getPlayerLevel(Objects.requireNonNull(event.getEntity().getKiller()), "Combat");
            if (event.getEntity().getKiller() instanceof Player) {
                // Check if the forbiddenBookChance equals 1 for a 1% chance, and you can factor in playerLevel if needed
                if (forbiddenBookChance == 1) {
                    event.getEntity().getLocation().getWorld().dropItem(event.getEntity().getLocation(), ItemRegistry.getForbiddenBook());
                    event.getEntity().getKiller().sendMessage(ChatColor.DARK_PURPLE + "A strange book falls to the ground...");
                    event.getEntity().getKiller().playSound(event.getEntity().getLocation(), Sound.ITEM_TRIDENT_THUNDER, 100, 100);
                }
            }
        }
    }

    public static List<Material> logsAndStems = Arrays.asList(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            Material.MANGROVE_LOG
    );

    // Method to alter drops of a block
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material brokenBlock = event.getBlock().getType();
        Player player = event.getPlayer();

        // Check if the broken block is within the logs and stems types
        if (logsAndStems.contains(brokenBlock)) {
            int silkWormChance = random.nextInt(2400) + 1; // Generates a number between 1 and 500

            // Implement a fixed 1/500 chance
            if (silkWormChance == 1) {
                // Drop the custom Silk Worm item at the block's location
                event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation(), ItemRegistry.getSilkWorm());

                // Send a message to the player
                player.sendMessage(ChatColor.DARK_PURPLE + "A small insect falls to the ground...");

                // Play a sound effect to the player
                player.playSound(player.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 1.0f);
            }
        }
    }
}
