package goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class YieldUpgradeListener implements Listener {
    private final MinecraftNew plugin;
    private static GemstoneUpgradeSystem upgradeSystemInstance;

    public YieldUpgradeListener(MinecraftNew plugin) {
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

        if (upgradeSystemInstance == null) return;

        Material blockType = block.getType();
        
        // Handle coal yield
        if (blockType == Material.COAL_ORE || blockType == Material.DEEPSLATE_COAL_ORE) {
            int coalLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.COAL_YIELD);
            if (coalLevel > 0) {
                ItemStack coal = new ItemStack(Material.COAL, coalLevel);
                block.getWorld().dropItemNaturally(block.getLocation(), coal);
            }
        }
        
        // Handle redstone yield
        else if (blockType == Material.REDSTONE_ORE || blockType == Material.DEEPSLATE_REDSTONE_ORE) {
            int redstoneLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.REDSTONE_YIELD);
            if (redstoneLevel > 0) {
                ItemStack redstone = new ItemStack(Material.REDSTONE, redstoneLevel);
                block.getWorld().dropItemNaturally(block.getLocation(), redstone);
            }
        }
        
        // Handle lapis yield
        else if (blockType == Material.LAPIS_ORE || blockType == Material.DEEPSLATE_LAPIS_ORE) {
            int lapisLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.LAPIS_YIELD);
            if (lapisLevel > 0) {
                ItemStack lapis = new ItemStack(Material.LAPIS_LAZULI, lapisLevel);
                block.getWorld().dropItemNaturally(block.getLocation(), lapis);
            }
        }
        
        // Handle diamond yield
        else if (blockType == Material.DIAMOND_ORE || blockType == Material.DEEPSLATE_DIAMOND_ORE) {
            int diamondLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.DIAMOND_YIELD);
            if (diamondLevel > 0) {
                ItemStack diamond = new ItemStack(Material.DIAMOND, diamondLevel);
                block.getWorld().dropItemNaturally(block.getLocation(), diamond);
            }
        }
    }
}