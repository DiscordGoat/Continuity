package goat.minecraft.minecraftnew.subsystems.chocolatemisc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
