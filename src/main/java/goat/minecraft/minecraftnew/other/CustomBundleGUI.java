package goat.minecraft.minecraftnew.other;

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

public class CustomBundleGUI implements Listener {

    private final JavaPlugin plugin;
    private final String fileName = "bundle_storage.yml";
    private File storageFile;
    private FileConfiguration storageConfig;

    public CustomBundleGUI(JavaPlugin plugin) {
        this.plugin = plugin;
        initializeStorageFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        return meta != null && meta.hasDisplayName() && meta.getDisplayName().contains(ChatColor.YELLOW + "Backpack");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Backpack")) {
            Player player = (Player) event.getPlayer();
            saveBundleInventory(player, event.getInventory());
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
