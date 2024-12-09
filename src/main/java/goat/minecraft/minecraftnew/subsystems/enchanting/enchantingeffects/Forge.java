package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Random;

public class Forge implements Listener {

    private final Random random = new Random();

    /**
     * Checks if the player's tool has the Forge enchantment and applies the smelting effect.
     *
     * @param event The BlockBreakEvent triggered when a block is broken.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        Player player = event.getPlayer();

        // Check if the tool has the Forge enchantment and get its level
        int forgeLevel = CustomEnchantmentManager.getEnchantmentLevel(tool, "Forge");
        if (forgeLevel <= 0) return;

        // Calculate the chance to smelt based on the Forge level (20% per level)
        double smeltChance = 0.2 * forgeLevel;
        if (random.nextDouble() > smeltChance) return;

        // Get the block broken and attempt to smelt it if applicable
        Block block = event.getBlock();
        Material smeltedType = getSmeltedMaterial(block.getType());
        if (smeltedType != null) {
            // Set the block drop to the smelted version
            block.setType(Material.AIR); // Remove original block
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltedType));

            int enchantmentLevel = CustomEnchantmentManager.getEnchantmentLevel(tool, "Forge");
            player.getInventory().getItemInMainHand().setDurability((short) (player.getInventory().getItemInMainHand().getDurability() +(1*enchantmentLevel)));

        }
    }

    /**
     * Determines the smelted version of a given block type.
     *
     * @param blockType The type of block being broken.
     * @return The Material of the smelted item, or null if no smelting is possible.
     */
    private Material getSmeltedMaterial(Material blockType) {
        switch (blockType) {
            case ACACIA_LOG, SPRUCE_LOG, JUNGLE_LOG, DARK_OAK_LOG, OAK_LOG, BIRCH_LOG:
                return Material.CHARCOAL;
            case KELP:
                return Material.DRIED_KELP;
            case CACTUS:
                return Material.GREEN_DYE;
            case NETHER_QUARTZ_ORE:
                return Material.QUARTZ;
            case NETHERRACK:
                return Material.NETHER_BRICK;
            case CHORUS_FRUIT:
                return Material.POPPED_CHORUS_FRUIT;
            case CLAY:
                return Material.BRICK;
            case COBBLESTONE:
                return Material.STONE;
            case POTATO:
                return Material.BAKED_POTATO;
            case SAND:
                return Material.GLASS;
            case IRON_ORE:
                return Material.IRON_INGOT;
            case GOLD_ORE:
                return Material.GOLD_INGOT;
            case WET_SPONGE:
                return Material.SPONGE;
            case ANCIENT_DEBRIS:
                return Material.NETHERITE_SCRAP;
            default:
                return null; // No smelting available for other blocks
        }
    }
}
