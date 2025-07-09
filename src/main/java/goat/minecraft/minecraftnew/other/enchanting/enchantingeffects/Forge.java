package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Random;

public class Forge implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        Player player = event.getPlayer();

        int forgeLevel = CustomEnchantmentManager.getEnchantmentLevel(tool, "Forge");
        if (forgeLevel <= 0) return;

        double smeltChance = 0.2 * forgeLevel;
        if (random.nextDouble() > smeltChance) return;

        Block block = event.getBlock();
        Material smeltedType = getSmeltedMaterial(block.getType());


        // Additional item drop logic
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());
        int miningLevel = xpManager.getPlayerLevel(player, "Mining"); // Assuming mining level is based on player XP level
        if (random.nextDouble() < (miningLevel / 2.0) / 100.0) {
            if(getSmeltedMaterial(block.getType()) != null) {
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Objects.requireNonNull(getSmeltedMaterial(block.getType()))));
            }
        }

        // Tertiary chance if the tool has a diamond gemstone
        if (CustomEnchantmentManager.hasEnchantment(tool, "Forge") && hasDiamondGemstone(tool)) {
            if (random.nextDouble() < 0.1) {
                if(getSmeltedMaterial(block.getType()) != null) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Objects.requireNonNull(getSmeltedMaterial(block.getType()))));
                }
            }
        }
        if (smeltedType != null) {
            block.setType(Material.AIR);
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltedType));
        }
    }

    private boolean hasDiamondGemstone(ItemStack tool) {
        // Logic to check if the tool has a diamond gemstone
        return tool.getItemMeta() != null && tool.getItemMeta().hasLore() && tool.getItemMeta().getLore().contains("Diamond Gemstone");
    }

    private Material getSmeltedMaterial(Material blockType) {
        return switch (blockType) {
            case ACACIA_LOG, SPRUCE_LOG, JUNGLE_LOG, DARK_OAK_LOG, OAK_LOG, BIRCH_LOG -> Material.CHARCOAL;
            case KELP -> Material.DRIED_KELP;
            case CACTUS -> Material.GREEN_DYE;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            case NETHERRACK -> Material.NETHER_BRICK;
            case CHORUS_FRUIT -> Material.POPPED_CHORUS_FRUIT;
            case CLAY -> Material.BRICK;
            case COBBLESTONE -> Material.STONE;
            case POTATO -> Material.BAKED_POTATO;
            case SAND -> Material.GLASS;
            case IRON_ORE -> Material.IRON_INGOT;
            case DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
            case GOLD_ORE -> Material.GOLD_INGOT;
            case WET_SPONGE -> Material.SPONGE;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }
}
