package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Masterwork enchantment: Rare chance to discover a Masterwork Ingot while
 * mining iron or gold ores. Chance is based on enchantment level and the
 * Masterwork gemstone upgrade.
 */
public class Masterwork implements Listener {

    private final Random random = new Random();
    private static GemstoneUpgradeSystem upgradeSystem;

    /** Sets the gemstone upgrade system instance. */
    public static void setUpgradeSystemInstance(GemstoneUpgradeSystem system) {
        upgradeSystem = system;
    }

    @EventHandler
    public void onOreMine(BlockBreakEvent event) {
        Block block = event.getBlock();
        Material type = block.getType();

        if (type != Material.IRON_ORE && type != Material.DEEPSLATE_IRON_ORE &&
            type != Material.GOLD_ORE && type != Material.DEEPSLATE_GOLD_ORE) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.hasEnchantment(tool, "Masterwork")) return;

        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Masterwork");
        double chance = level * 0.2; // 0.20% per level

        if (upgradeSystem != null) {
            int bonus = upgradeSystem.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.MASTERWORK);
            chance += bonus * 0.05; // +0.05% per upgrade level
        }

        if (random.nextDouble() * 100 <= chance) {
            block.getWorld().dropItemNaturally(block.getLocation(), ItemRegistry.getMasterworkIngot());
        }
    }
}
