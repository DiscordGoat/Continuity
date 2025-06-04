package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Alchemy enchantment effect: Transforms mined blocks into better variants based on enchantment level.
 */
public class Alchemy implements Listener {

    private final Random random = new Random();

    // Mapping from mined block types to their transformed block types
    private static final Map<Material, Material> transformationMap = new HashMap<>();

    static {
        // Standard Ores and their block variants
        transformationMap.put(Material.DIAMOND_ORE, Material.DIAMOND_BLOCK);
        transformationMap.put(Material.GOLD_ORE, Material.GOLD_BLOCK);
        transformationMap.put(Material.NETHER_GOLD_ORE, Material.GOLD_BLOCK);
        transformationMap.put(Material.ANCIENT_DEBRIS, Material.NETHERITE_BLOCK);
        transformationMap.put(Material.IRON_ORE, Material.IRON_BLOCK);
        transformationMap.put(Material.COAL_ORE, Material.COAL_BLOCK);
        transformationMap.put(Material.REDSTONE_ORE, Material.REDSTONE_BLOCK);
        transformationMap.put(Material.LAPIS_ORE, Material.LAPIS_BLOCK);
        transformationMap.put(Material.EMERALD_ORE, Material.EMERALD_BLOCK);
        transformationMap.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ_BLOCK);

        // Deepslate Ores and their block variants
        transformationMap.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_BLOCK);
        transformationMap.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_BLOCK);
        transformationMap.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_BLOCK);
        transformationMap.put(Material.DEEPSLATE_COAL_ORE, Material.COAL_BLOCK);
        transformationMap.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE_BLOCK);
        transformationMap.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_BLOCK);
        transformationMap.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD_BLOCK);
        // Add more transformations as needed
    }
    public static boolean hasSilkTouch(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            return false;
        }
        // Using containsEnchantment is a quick check for the presence of Silk Touch.
        return item.containsEnchantment(Enchantment.SILK_TOUCH);
    }
    /**
     * Handles the BlockBreakEvent to apply the Alchemy enchantment effect.
     *
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // Check if the tool has the "Alchemy" enchantment
        if (!CustomEnchantmentManager.hasEnchantment(tool, "Alchemy")) {
            return;
        }
        if (hasSilkTouch(player)) {
            return;
        }

        int alchemyLevel = CustomEnchantmentManager.getEnchantmentLevel(tool, "Alchemy");
        if (alchemyLevel < 1) {
            return;
        }

        // Calculate chance: 4 * level percent
        int chancePercentage = 4 * alchemyLevel;
        if (chancePercentage > 100) {
            chancePercentage = 100; // Cap at 100%
        }

        int roll = random.nextInt(100) + 1; // 1 to 100
        if (roll > chancePercentage) {
            return; // No transformation occurs
        }

        Material minedBlockType = event.getBlock().getType();
        Material transformedBlockType = transformationMap.get(minedBlockType);
        if (transformedBlockType == null) {
            return; // No transformation defined for this block type
        }

        // Cancel default drops
        event.setDropItems(false);

        // Drop the transformed block
        ItemStack transformedBlock = new ItemStack(transformedBlockType, 1);
        player.getWorld().dropItemNaturally(event.getBlock().getLocation(), transformedBlock);

        // Optionally, send a feedback message to the player
        int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(tool, "Alchemy");
        player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

    }

    /**
     * Formats the material name to a more readable format.
     *
     * @param material The material to format.
     * @return A formatted string of the material name.
     */
    private String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if(word.length() == 0) continue;
            formatted.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return formatted.toString().trim();
    }
}
