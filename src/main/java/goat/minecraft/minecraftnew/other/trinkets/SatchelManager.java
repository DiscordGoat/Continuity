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
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SatchelManager implements Listener {
    private static SatchelManager instance;
    private final JavaPlugin plugin;
    private File satchelFile;
    private FileConfiguration satchelConfig;
    private final Map<UUID, Inventory> previousInventories = new HashMap<>();

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
        UUID id = player.getUniqueId();
        previousInventories.put(id, player.getOpenInventory().getTopInventory());
        player.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.endsWith(" Satchel")) return;
        Player player = (Player) event.getPlayer();
        UUID id = player.getUniqueId();
        String color = title.split(" ")[0];
        saveSatchel(player, event.getInventory(), color);
        Inventory prev = previousInventories.remove(id);
        if (prev != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.openInventory(prev);
                }
            }.runTaskLater(plugin, 1L);
        }
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

    /**
     * Deposits a single item stack into the specified satchel color. Stacks with
     * similar type and meta will be combined when possible.
     */
    public void depositItem(Player player, String color, ItemStack toDeposit) {
        String base = player.getUniqueId() + "." + color;
        ItemStack remaining = toDeposit.clone();
        for (int i = 0; i < 54 && remaining.getAmount() > 0; i++) {
            String path = base + "." + i;
            ItemStack existing = satchelConfig.getItemStack(path);
            if (existing == null || existing.getType() == Material.AIR) {
                satchelConfig.set(path, remaining);
                remaining = null;
                break;
            }
            if (existing.isSimilar(remaining) && existing.getAmount() < existing.getMaxStackSize()) {
                int max = existing.getMaxStackSize();
                int total = existing.getAmount() + remaining.getAmount();
                if (total <= max) {
                    existing.setAmount(total);
                    satchelConfig.set(path, existing);
                    remaining = null;
                    break;
                } else {
                    existing.setAmount(max);
                    satchelConfig.set(path, existing);
                    remaining.setAmount(total - max);
                }
            }
        }
        saveSatchelConfig();
        if (remaining != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), remaining);
        }
    }

    private void saveSatchelConfig() {
        try {
            satchelConfig.save(satchelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
