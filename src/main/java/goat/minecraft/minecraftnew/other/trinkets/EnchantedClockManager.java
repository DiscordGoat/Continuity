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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the automated behaviour of the Enchanted Clock trinket. Every three
 * minutes the clock will trigger the left click functionality of the trinket
 * located directly above it in the player's backpack storage.
 */
public class EnchantedClockManager {
    private static EnchantedClockManager instance;
    private final JavaPlugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey delayKey;
    private final Map<UUID, Long> lastRun = new HashMap<>();

    private static final long[] DELAY_MS = {30000L, 60000L, 180000L, 600000L};

    private EnchantedClockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        idKey = new NamespacedKey(plugin, "clock_id");
        delayKey = new NamespacedKey(plugin, "clock_delay");
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
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void processPlayer(Player player) {
        InventoryType openType = player.getOpenInventory().getTopInventory().getType();
        if (openType != InventoryType.CRAFTING && openType != InventoryType.CREATIVE) {
            return; // pause while the player has any other inventory open
        }
        for (int slot = 9; slot < 54; slot++) {
            ItemStack item = CustomBundleGUI.getInstance().getBackpackItem(player, slot);
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (!name.equals("Enchanted Clock")) continue;
            UUID id = getOrCreateId(item);
            CustomBundleGUI.getInstance().setBackpackItem(player, slot, item);
            int delayIndex = getDelayIndex(item);
            if (!shouldRun(id, delayIndex)) continue;
            ItemStack above = CustomBundleGUI.getInstance().getBackpackItem(player, slot - 9);
            if (above == null) continue;
            triggerLeftClick(player, above);
            CustomBundleGUI.getInstance().setBackpackItem(player, slot - 9, above);
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

    private UUID getOrCreateId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return UUID.randomUUID();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String existing = container.get(idKey, PersistentDataType.STRING);
        if (existing != null) {
            if (!container.has(delayKey, PersistentDataType.INTEGER)) {
                container.set(delayKey, PersistentDataType.INTEGER, 2);
                item.setItemMeta(meta);
                updateLore(item, 2);
            }
            return UUID.fromString(existing);
        }
        UUID id = UUID.randomUUID();
        container.set(idKey, PersistentDataType.STRING, id.toString());
        container.set(delayKey, PersistentDataType.INTEGER, 2);
        item.setItemMeta(meta);
        updateLore(item, 2);
        return id;
    }

    private int getDelayIndex(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 2;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer index = container.get(delayKey, PersistentDataType.INTEGER);
        if (index == null) {
            index = 2;
            container.set(delayKey, PersistentDataType.INTEGER, index);
            item.setItemMeta(meta);
            updateLore(item, index);
        }
        return index;
    }

    private boolean shouldRun(UUID id, int index) {
        long now = System.currentTimeMillis();
        long last = lastRun.getOrDefault(id, 0L);
        if (now - last >= DELAY_MS[index]) {
            lastRun.put(id, now);
            return true;
        }
        return false;
    }

    public void cycleDelay(Player player, ItemStack item) {
        UUID id = getOrCreateId(item);
        int index = getDelayIndex(item);
        index = (index + 1) % DELAY_MS.length;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(delayKey, PersistentDataType.INTEGER, index);
            item.setItemMeta(meta);
        }
        updateLore(item, index);
        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Clock delay set to " + formatDelay(index));
        lastRun.put(id, System.currentTimeMillis());
    }

    private String formatDelay(int index) {
        switch (index) {
            case 0 -> { return "30s"; }
            case 1 -> { return "1m"; }
            case 2 -> { return "3m"; }
            case 3 -> { return "10m"; }
            default -> { return ""; }
        }
    }

    private void updateLore(ItemStack item, int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        java.util.List<String> lore = new java.util.ArrayList<>();
        lore.add(ChatColor.GRAY + "Activates the trinket above automatically");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Change delay");
        lore.add(ChatColor.BLUE + "Right-click" + ChatColor.GRAY + ": Pick up");
        lore.add(ChatColor.GRAY + "Delay: " + ChatColor.WHITE + formatDelay(index));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
