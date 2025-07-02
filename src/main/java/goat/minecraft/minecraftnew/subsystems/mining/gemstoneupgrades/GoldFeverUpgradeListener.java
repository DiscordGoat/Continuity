package goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GoldFeverUpgradeListener implements Listener {
    private final MinecraftNew plugin;
    private static GemstoneUpgradeSystem upgradeSystemInstance;
    private final Random random = new Random();

    public GoldFeverUpgradeListener(MinecraftNew plugin) {
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

        // Only trigger on ores to match the existing system
        if (!isOre(block.getType())) return;

        // Get Gold Fever upgrade levels
        int[] goldFeverUpgrades = upgradeSystemInstance.getGoldFeverUpgrades(tool);
        int chanceBonus = goldFeverUpgrades[0]; // Bonus percentage chance
        int durationBonus = goldFeverUpgrades[1]; // Bonus duration in seconds
        int potencyBonus = goldFeverUpgrades[2]; // Bonus haste levels
        int rangeBonus = goldFeverUpgrades[3]; // Bonus range in blocks

        // Skip if no Gold Fever upgrades are present
        if (chanceBonus == 0 && durationBonus == 0 && potencyBonus == 0 && rangeBonus == 0) return;

        // Base Gold Fever: 5% chance, 15 seconds, Haste I, 0 range
        int totalChance = 5 + chanceBonus; // Base 5% + upgrades
        int totalDuration = (15 + durationBonus) * 20; // Convert to ticks (20 ticks = 1 second)
        int totalPotency = Math.min(0 + potencyBonus, 2); // Haste levels 0-2 (I-III), cap at 2
        int totalRange = rangeBonus; // Additional range for nearby players

        int roll = random.nextInt(100) + 1; // Roll 1-100

        if (roll <= totalChance) {
            // Apply Gold Fever effect to the mining player
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, totalDuration, totalPotency, false));
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);

            // Apply range effect if Gold Fever Range upgrade is present
            if (totalRange > 0) {
                applyGoldFeverToNearbyPlayers(player, totalDuration, totalPotency, totalRange);
            }
        }
    }

    /**
     * Applies Gold Fever effect to nearby players within range
     */
    private void applyGoldFeverToNearbyPlayers(Player sourcePlayer, int duration, int potency, int range) {
        List<Player> nearbyPlayers = sourcePlayer.getWorld().getPlayers();
        
        for (Player nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer.equals(sourcePlayer)) continue; // Skip the original player
            
            double distance = sourcePlayer.getLocation().distance(nearbyPlayer.getLocation());
            if (distance <= range) {
                // Apply the same Gold Fever effect to nearby players
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, potency, false));
                nearbyPlayer.playSound(nearbyPlayer.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 0.7f, 1.2f);
                nearbyPlayer.sendMessage("§6⚡ You feel the Gold Fever spreading from nearby mining!");
            }
        }
    }

    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_AXE ||
               material == Material.DIAMOND_SHOVEL || material == Material.DIAMOND_HOE;
    }

    private boolean isOre(Material material) {
        List<Material> ores = Arrays.asList(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS
        );
        return ores.contains(material);
    }
}