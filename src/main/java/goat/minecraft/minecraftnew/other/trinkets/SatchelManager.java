package goat.minecraft.minecraftnew.other.trinkets;

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

public class SatchelManager implements Listener {
    private static SatchelManager instance;
    private final JavaPlugin plugin;
    private File satchelFile;
    private FileConfiguration satchelConfig;

    private SatchelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SatchelManager(plugin);
        }
    }

    public static SatchelManager getInstance() {
        return instance;
    }

    private void initFile() {
        satchelFile = new File(plugin.getDataFolder(), "satchels.yml");
        if (!satchelFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                satchelFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        satchelConfig = YamlConfiguration.loadConfiguration(satchelFile);
    }

    public void openSatchel(Player player, String color) {
        Inventory inv = Bukkit.createInventory(null, 54, color + " Satchel");
        String base = player.getUniqueId() + "." + color;
        if (satchelConfig.contains(base)) {
            for (int i = 0; i < 54; i++) {
                ItemStack stack = satchelConfig.getItemStack(base + "." + i);
                if (stack != null) {
                    inv.setItem(i, stack);
                }
            }
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.endsWith(" Satchel")) return;
        Player player = (Player) event.getPlayer();
        String color = title.split(" ")[0];
        saveSatchel(player, event.getInventory(), color);
    }

    private void saveSatchel(Player player, Inventory inv, String color) {
        String base = player.getUniqueId() + "." + color;
        for (int i = 0; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                satchelConfig.set(base + "." + i, item);
            } else {
                satchelConfig.set(base + "." + i, null);
            }
        }
        try {
            satchelConfig.save(satchelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
