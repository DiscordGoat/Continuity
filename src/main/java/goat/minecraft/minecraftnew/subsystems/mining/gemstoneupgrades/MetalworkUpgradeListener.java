package goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class MetalworkUpgradeListener implements Listener {
    private final MinecraftNew plugin;
    private static GemstoneUpgradeSystem upgradeSystemInstance;

    public MetalworkUpgradeListener(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    public static void setUpgradeSystemInstance(GemstoneUpgradeSystem upgradeSystem) {
        upgradeSystemInstance = upgradeSystem;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isDiamondTool(tool.getType()) || upgradeSystemInstance == null) return;

        Material blockType = block.getType();
        
        // Handle iron metalwork - (5*level)% chance to drop iron block
        if (blockType == Material.IRON_ORE || blockType == Material.DEEPSLATE_IRON_ORE) {
            int ironLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.METALWORK_IRON);
            if (ironLevel > 0) {
                double chance = ironLevel * 5.0; // 5% per level
                if (Math.random() * 100 < chance) {
                    ItemStack ironBlock = new ItemStack(Material.IRON_BLOCK, 1);
                    block.getWorld().dropItemNaturally(block.getLocation(), ironBlock);
                }
            }
        }
        
        // Handle gold metalwork - (5*level)% chance to drop gold block
        else if (blockType == Material.GOLD_ORE || blockType == Material.DEEPSLATE_GOLD_ORE || blockType == Material.NETHER_GOLD_ORE) {
            int goldLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.METALWORK_GOLD);
            if (goldLevel > 0) {
                double chance = goldLevel * 5.0; // 5% per level
                if (Math.random() * 100 < chance) {
                    ItemStack goldBlock = new ItemStack(Material.GOLD_BLOCK, 1);
                    block.getWorld().dropItemNaturally(block.getLocation(), goldBlock);
                }
            }
        }
    }

    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_AXE ||
               material == Material.DIAMOND_SHOVEL || material == Material.DIAMOND_HOE;
    }
}

