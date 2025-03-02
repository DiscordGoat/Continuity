package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomBundleGUI implements Listener {

    // === 1) Singleton field ===
    private static CustomBundleGUI instance;

    private final JavaPlugin plugin;
    private final String fileName = "bundle_storage.yml";
    private File storageFile;
    private FileConfiguration storageConfig;

    // === 2) Private constructor ===
    private CustomBundleGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeStorageFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // === 3) Static init method: call once from onEnable() ===
    public static CustomBundleGUI init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CustomBundleGUI(plugin);
        }
        return instance;
    }

    // === 4) Static getter for the instance ===
    public static CustomBundleGUI getInstance() {
        return instance;
    }

    private void initializeStorageFile() {
        storageFile = new File(plugin.getDataFolder(), fileName);
        if (!storageFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        storageConfig = YamlConfiguration.loadConfiguration(storageFile);
    }

    /**
     * Opens the custom bundle GUI for a player.
     *
     * @param player The player opening the GUI.
     */
    public void openBundleGUI(Player player) {
        Inventory bundleInventory = Bukkit.createInventory(null, 54, "Backpack");

        // Load saved items from the file
        String playerUUID = player.getUniqueId().toString();
        if (storageConfig.contains(playerUUID)) {
            for (int i = 0; i < 54; i++) {
                if (storageConfig.contains(playerUUID + "." + i)) {
                    bundleInventory.setItem(i, storageConfig.getItemStack(playerUUID + "." + i));
                }
            }
        }

        player.openInventory(bundleInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Backpack")) {
            ItemStack clickedItem = event.getCurrentItem();
            ItemStack cursorItem = event.getCursor();
            Player player = (Player) event.getWhoClicked();

            // Check if the clicked slot is in the Backpack GUI
            if (event.getClickedInventory() != null
                    && event.getClickedInventory().getType() != InventoryType.PLAYER) {

                // If the player tries to place a backpack inside the backpack
                if (isBackpackItem(cursorItem)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You cannot store your backpack inside itself!");
                }
            }

            // Prevent moving the backpack inside the GUI by clicking on it
            if (isBackpackItem(clickedItem)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot move your backpack while in the backpack GUI!");
            }
        }
    }

    /**
     * Checks if an item is a backpack.
     * Adjust this method to fit your plugin’s backpack detection system.
     */
    private boolean isBackpackItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null
                && meta.hasDisplayName()
                && meta.getDisplayName().contains(ChatColor.YELLOW + "Backpack");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Backpack")) {
            Player player = (Player) event.getPlayer();
            saveBundleInventory(player, event.getInventory());
        }
    }
    public boolean removeRedstoneBlocksFromBackpack(Player player, int neededBlocks) {
        String playerUUID = player.getUniqueId().toString();

        if (!storageConfig.contains(playerUUID)) {
            return false;
        }

        int totalBlocks = 0;
        List<SlotData> blockSlots = new ArrayList<>();

        // 1) Tally up the Redstone Blocks
        for (int slot = 0; slot < 54; slot++) {
            String path = playerUUID + "." + slot;
            if (!storageConfig.contains(path)) continue;

            ItemStack stack = storageConfig.getItemStack(path);
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            int amount = stack.getAmount();

            if (stack.getType() == Material.REDSTONE_BLOCK) {
                totalBlocks += amount;
                blockSlots.add(new SlotData(slot, amount));
            }
        }

        if (totalBlocks < neededBlocks) {
            return false; // Not enough blocks in total
        }

        int stillNeeded = neededBlocks;

        // 2) Remove from the found Redstone Block stacks
        for (SlotData slotData : blockSlots) {
            if (stillNeeded <= 0) break;

            String path = playerUUID + "." + slotData.slotIndex;
            int stackAmt = slotData.amount;

            if (stackAmt <= stillNeeded) {
                // Remove the entire stack
                storageConfig.set(path, null);
                stillNeeded -= stackAmt;
            } else {
                // Partial removal from this stack
                int leftover = stackAmt - stillNeeded;
                stillNeeded = 0;
                ItemStack newStack = new ItemStack(Material.REDSTONE_BLOCK, leftover);
                storageConfig.set(path, newStack);
            }
        }

        // 3) Save changes
        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
    public boolean removeEmeraldsFromBackpack(Player player, int neededEmeralds) {
        String playerUUID = player.getUniqueId().toString();

        if (!storageConfig.contains(playerUUID)) {
            return false;
        }

        int totalEmeralds = 0;
        List<SlotData> emeraldSlots = new ArrayList<>();
        List<SlotData> blockSlots   = new ArrayList<>();

        // 1) Tally up normal emeralds & blocks
        for (int slot = 0; slot < 54; slot++) {
            String path = playerUUID + "." + slot;
            if (!storageConfig.contains(path)) continue;

            ItemStack stack = storageConfig.getItemStack(path);
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }
            int amount = stack.getAmount();

            if (stack.getType() == Material.EMERALD) {
                totalEmeralds += amount;
                emeraldSlots.add(new SlotData(slot, amount));
            } else if (stack.getType() == Material.EMERALD_BLOCK) {
                totalEmeralds += (amount * 9);
                blockSlots.add(new SlotData(slot, amount));
            }
        }

        if (totalEmeralds < neededEmeralds) {
            return false; // Not enough in total
        }

        int stillNeeded = neededEmeralds;

        // 2) Remove from emerald stacks first
        for (SlotData slotData : emeraldSlots) {
            if (stillNeeded <= 0) break;

            String path = playerUUID + "." + slotData.slotIndex;
            int stackAmt = slotData.amount;

            if (stackAmt <= stillNeeded) {
                // Remove entire stack
                storageConfig.set(path, null);
                stillNeeded -= stackAmt;
            } else {
                // Partial removal from an emerald stack
                int leftover = stackAmt - stillNeeded;
                stillNeeded = 0;
                ItemStack newStack = new ItemStack(Material.EMERALD, leftover);
                storageConfig.set(path, newStack);
            }
        }

        // 3) If still needed, remove from emerald blocks
        if (stillNeeded > 0) {
            for (SlotData slotData : blockSlots) {
                if (stillNeeded <= 0) break;

                String path = playerUUID + "." + slotData.slotIndex;
                int blockCount = slotData.amount;
                int blockValue = blockCount * 9; // total emeralds for that stack

                if (blockValue <= stillNeeded) {
                    // Use up entire block stack
                    storageConfig.set(path, null);
                    stillNeeded -= blockValue;
                } else {
                    // We'll remove blocks one-by-one until the shortfall is 0 or we run out
                    // (But typically if blockValue > stillNeeded, we just need 1 block.)
                    while (blockCount > 0 && stillNeeded > 0) {
                        // If using one block covers shortfall with leftover:
                        if (9 >= stillNeeded) {
                            // Remove 1 block from that slot
                            blockCount--;
                            storageConfig.set(path, (blockCount > 0)
                                    ? new ItemStack(Material.EMERALD_BLOCK, blockCount)
                                    : null
                            );

                            int leftoverFromBlock = 9 - stillNeeded; // e.g. leftover 8 if shortfall=1
                            stillNeeded = 0; // we've paid the shortfall

                            // Add leftover to the backpack/inventory/floor
                            storeEmeraldRemainder(
                                    player,
                                    leftoverFromBlock,
                                    playerUUID,
                                    slotData.slotIndex
                            );
                        } else {
                            // If 9 < stillNeeded, we use the entire block with no leftover
                            stillNeeded -= 9;
                            blockCount--;
                            // update the slot
                            if (blockCount > 0) {
                                storageConfig.set(path, new ItemStack(Material.EMERALD_BLOCK, blockCount));
                            } else {
                                storageConfig.set(path, null);
                            }
                        }
                    }
                }
            }
        }

        // Save changes
        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Attempts to place leftoverEmeraldCount into:
     *  1) The slot above blockSlot if possible
     *  2) The next available (or mergeable with EMERALD) slot in the backpack
     *  3) The player's inventory
     *  4) Drops on ground if everything is full
     */
    private void storeEmeraldRemainder(Player player, int leftoverEmeraldCount, String playerUUID, int blockSlotIndex) {
        if (leftoverEmeraldCount <= 0) return;

        // First: slot above blockSlotIndex (blockSlotIndex - 9)
        int aboveSlot = blockSlotIndex - 9;
        if (aboveSlot >= 0) {
            // Try to place leftover emeralds there
            if (tryPlaceInSlot(playerUUID, aboveSlot, leftoverEmeraldCount)) {
                return; // Done if successful
            }
        }

        // Next: any free/mergeable slot in the backpack
        for (int i = 0; i < 54; i++) {
            if (tryPlaceInSlot(playerUUID, i, leftoverEmeraldCount)) {
                return; // Done if successful
            }
        }

        // Next: attempt to put in player's inventory
        leftoverEmeraldCount = placeInPlayerInventory(player, leftoverEmeraldCount);
        if (leftoverEmeraldCount <= 0) {
            return; // fully placed in player inventory
        }

        // If still leftover, drop on ground
        player.getWorld().dropItemNaturally(player.getLocation(), new ItemStack(Material.EMERALD, leftoverEmeraldCount));
    }

    /**
     * Try placing an amount of emeralds into a single backpack slot (either empty or an EMERALD stack).
     * Returns true if all leftoverEmeralds fit, false if not.
     */
    private boolean tryPlaceInSlot(String playerUUID, int slotIndex, int leftoverEmeraldCount) {
        String path = playerUUID + "." + slotIndex;

        // Current item in that slot
        ItemStack current = (ItemStack) storageConfig.get(path);

        // If nothing is in that slot, just put all leftover there
        if (current == null || current.getType() == Material.AIR) {
            storageConfig.set(path, new ItemStack(Material.EMERALD, leftoverEmeraldCount));
            return true;
        }

        // If there's something else, check if it's EMERALD
        if (current.getType() == Material.EMERALD) {
            int maxStack = current.getMaxStackSize(); // typically 64
            int existingAmt = current.getAmount();

            // If there's room
            if (existingAmt < maxStack) {
                int canFit = maxStack - existingAmt;
                if (canFit >= leftoverEmeraldCount) {
                    // All leftover fits here
                    current.setAmount(existingAmt + leftoverEmeraldCount);
                    storageConfig.set(path, current);
                    return true;
                } else {
                    // We can only fit part of leftover
                    current.setAmount(existingAmt + canFit);
                    storageConfig.set(path, current);
                    // leftoverEmeraldCount not fully placed -> but we do not handle partial leftover here,
                    // because the method "tryPlaceInSlot" tries to place all. We'll handle partial
                    // leftover logic outside if needed (but your scenario wants it all or nothing).
                    return false;
                }
            }
        }

        // The slot is either non-emerald or is a full emerald stack
        return false;
    }

    /**
     * Tries to place leftover emeralds into the player's inventory.
     * Returns how many emeralds remain if the player's inventory fills up.
     */
    private int placeInPlayerInventory(Player player, int leftoverEmeraldCount) {
        ItemStack leftoverStack = new ItemStack(Material.EMERALD, leftoverEmeraldCount);
        HashMap<Integer, ItemStack> notFit = player.getInventory().addItem(leftoverStack);

        if (notFit.isEmpty()) {
            return 0; // all fit
        }
        // If some didn't fit
        for (ItemStack remainder : notFit.values()) {
            return remainder.getAmount(); // the leftover
        }
        return 0;
    }

    private static class SlotData {
        int slotIndex;
        int amount;
        SlotData(int slotIndex, int amount) {
            this.slotIndex = slotIndex;
            this.amount = amount;
        }
    }


    private void saveBundleInventory(Player player, Inventory inventory) {
        String playerUUID = player.getUniqueId().toString();

        for (int i = 0; i < 54; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                storageConfig.set(playerUUID + "." + i, item);
            } else {
                storageConfig.set(playerUUID + "." + i, null); // Clear empty slots
            }
        }

        try {
            storageConfig.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
