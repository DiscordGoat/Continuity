package goat.minecraft.minecraftnew.other.trinkets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaBucketManager implements Listener {
    private static LavaBucketManager instance;
    private final JavaPlugin plugin;

    private LavaBucketManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new LavaBucketManager(plugin);
        }
    }

    public static LavaBucketManager getInstance() {
        return instance;
    }

    public void openTrash(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Lava Bucket");
        player.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Lava Bucket")) {
            event.getInventory().clear();
        }
    }
}
