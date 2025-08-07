package goat.minecraft.minecraftnew.subsystems.mining.gemstoneupgrades;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
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

import java.util.Random;

public class UtilityUpgradeListener implements Listener {
    private final MinecraftNew plugin;
    private final Random random = new Random();
    private static GemstoneUpgradeSystem upgradeSystemInstance;
    private final XPManager xpManager;

    public UtilityUpgradeListener(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
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
        boolean isOre = isOreBlock(blockType);
        
        if (isOre) {
            // Handle mining XP boost
            handleMiningXPBoost(player, tool);
            
            // Handle feed effect
            handleFeedEffect(player, tool);
        }
        
        // Handle payout (works on any block break)
        handlePayout(player, tool);
        

    }

    private void handleMiningXPBoost(Player player, ItemStack tool) {
        int xpBoostLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.MINING_XP_BOOST);
        if (xpBoostLevel > 0) {
            int bonusXP = xpBoostLevel * 5; // +5 XP per level
            xpManager.addXP(player, "Mining", bonusXP);
        }
    }


    private void handleFeedEffect(Player player, ItemStack tool) {
        int feedLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.FEED);
        if (feedLevel > 0) {
            double chance = feedLevel * 5.0; // 5% per level
            if (random.nextDouble() * 100 < chance) {
                // Fill hunger bar (not saturation)
                player.setFoodLevel(20);
                // Grant regeneration I for 10 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0, false));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                player.sendMessage(ChatColor.GREEN + "Feed activated! You feel refreshed.");
            }
        }
    }

    private void handlePayout(Player player, ItemStack tool) {
        int payoutLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.PAYOUT);
        if (payoutLevel > 0) {
            double chance = payoutLevel * 2.0; // 2.0% per level (max 8% at level 4)
            if (random.nextDouble() * 100 < chance) {
                // Check if player has deepslate in inventory
                if (removeDeepslateStack(player)) {
                    ItemStack emeralds = new ItemStack(Material.EMERALD, 8);
                    player.getInventory().addItem(emeralds);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
                    player.sendMessage(ChatColor.GOLD + "Payout! You earned 8 emeralds.");
                }
            }
        }
    }
    private boolean removeDeepslateStack(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && (item.getType() == Material.DEEPSLATE || item.getType() == Material.COBBLED_DEEPSLATE) && item.getAmount() >= 64) {
                if (item.getAmount() == 64) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - 64);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isOreBlock(Material material) {
        return material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE ||
               material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE ||
               material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE ||
               material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE ||
               material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE ||
               material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE ||
               material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE ||
               material == Material.NETHER_QUARTZ_ORE || material == Material.NETHER_GOLD_ORE ||
               material == Material.ANCIENT_DEBRIS;
    }

    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_AXE ||
               material == Material.DIAMOND_SHOVEL || material == Material.DIAMOND_HOE;
    }
}