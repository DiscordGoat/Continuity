package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.other.trinkets.BankAccountManager;
import goat.minecraft.minecraftnew.other.trinkets.PotionPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.CulinaryPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.MiningPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.TransfigurationPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.SeedPouchManager;
import goat.minecraft.minecraftnew.other.trinkets.TrinketManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles the automated behaviour of the Enchanted Clock trinket. Every three
 * minutes the clock will trigger the left click functionality of the trinket
 * located directly above it in the player's backpack storage.
 */
public class EnchantedClockManager {
    private static EnchantedClockManager instance;
    private final JavaPlugin plugin;

    private EnchantedClockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new EnchantedClockManager(plugin);
        }
    }

    public static EnchantedClockManager getInstance() {
        return instance;
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    processPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 20L * 60 * 3, 20L * 60 * 3);
    }

    private void processPlayer(Player player) {
        for (int slot = 9; slot < 54; slot++) {
            ItemStack item = CustomBundleGUI.getInstance().getBackpackItem(player, slot);
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (!name.equals("Enchanted Clock")) continue;
            ItemStack above = CustomBundleGUI.getInstance().getBackpackItem(player, slot - 9);
            if (above == null) continue;
            triggerLeftClick(player, above);
        }
    }

    private void triggerLeftClick(Player player, ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String name = ChatColor.stripColor(meta.getDisplayName());
        switch (name) {
            case "Bank Account" -> {
                int deposited = BankAccountManager.getInstance().depositAll(player);
                TrinketManager.getInstance().refreshBankLore(player);
                if (deposited > 0) {
                    player.sendMessage(ChatColor.GREEN + "Deposited " + deposited + " emeralds.");
                }
            }
            case "Pouch of Potions" -> {
                PotionPouchManager.getInstance().depositPotions(player);
                TrinketManager.getInstance().refreshPotionPouchLore(player);
            }
            case "Pouch of Culinary Delights" -> {
                CulinaryPouchManager.getInstance().depositDelights(player);
                TrinketManager.getInstance().refreshCulinaryPouchLore(player);
            }
            case "Mining Pouch" -> {
                MiningPouchManager.getInstance().depositOres(player);
                TrinketManager.getInstance().refreshMiningPouchLore(player);
            }
            case "Transfiguration Pouch" -> {
                TransfigurationPouchManager.getInstance().depositItems(player);
                TrinketManager.getInstance().refreshTransfigurationPouchLore(player);
            }
            case "Pouch of Seeds" -> {
                SeedPouchManager.getInstance().depositSeeds(player);
                TrinketManager.getInstance().refreshPouchLore(player);
            }
            default -> {
                // do nothing for other trinkets
            }
        }
    }
}
