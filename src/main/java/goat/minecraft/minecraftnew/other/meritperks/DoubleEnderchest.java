package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Double Enderchest merit perk.
 * <p>
 * Overrides the standard ender chest with a 54-slot GUI stored to file.
 * Works exactly like a vanilla ender chest but provides double capacity.
 */
public class DoubleEnderchest implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;
    private final Map<UUID, Inventory> enderInventories = new HashMap<>();
    private final File inventoriesFile;
    private final FileConfiguration inventoriesConfig;

    public DoubleEnderchest(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
        inventoriesFile = new File(plugin.getDataFolder(), "double_enderchests.yml");
        if (!inventoriesFile.exists()) {
            try {
                inventoriesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inventoriesConfig = YamlConfiguration.loadConfiguration(inventoriesFile);
    }

    @EventHandler
    public void onEnderChestInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.ENDER_CHEST) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!playerData.hasPerk(uuid, "Double Enderchest")) {
            return;
        }

        event.setCancelled(true);

        Inventory inv = enderInventories.get(uuid);
        if (inv == null) {
            inv = loadInventory(uuid);
            enderInventories.put(uuid, inv);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Ender Chest")) return;

        Player player = (Player) event.getPlayer();
        UUID uuid = player.getUniqueId();
        Inventory inv = event.getInventory();
        enderInventories.put(uuid, inv);
        saveInventory(uuid, inv);
    }

    private Inventory createInventory() {
        return Bukkit.createInventory(null, 54, "Ender Chest");
    }

    public Inventory loadInventory(UUID uuid) {
        Inventory inv = createInventory();
        for (int i = 0; i < 54; i++) {
            String path = "players." + uuid + ".slot" + i;
            if (inventoriesConfig.contains(path)) {
                inv.setItem(i, inventoriesConfig.getItemStack(path));
            }
        }
        return inv;
    }

    public void saveInventory(UUID uuid, Inventory inv) {
        for (int i = 0; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            inventoriesConfig.set("players." + uuid + ".slot" + i, item);
        }
        try {
            inventoriesConfig.save(inventoriesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllInventories() {
        for (Map.Entry<UUID, Inventory> entry : enderInventories.entrySet()) {
            saveInventory(entry.getKey(), entry.getValue());
        }
    }
}
