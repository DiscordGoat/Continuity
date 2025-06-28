package goat.minecraft.minecraftnew.other.trinkets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MiningPouchManager implements Listener {
    private static MiningPouchManager instance;
    private final JavaPlugin plugin;
    private File pouchFile;
    private FileConfiguration pouchConfig;

    private MiningPouchManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MiningPouchManager(plugin);
        }
    }

    public static MiningPouchManager getInstance() {
        return instance;
    }

    private void initFile() {
        pouchFile = new File(plugin.getDataFolder(), "mining_pouches.yml");
        if (!pouchFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                pouchFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pouchConfig = YamlConfiguration.loadConfiguration(pouchFile);
    }

    private void save() {
        try {
            pouchConfig.save(pouchFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Set<Material> ORE_MATERIALS = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE, Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.RAW_IRON, Material.RAW_COPPER, Material.RAW_GOLD,
            Material.IRON_INGOT, Material.GOLD_INGOT, Material.COPPER_INGOT,
            Material.DIAMOND, Material.EMERALD, Material.LAPIS_LAZULI,
            Material.REDSTONE, Material.QUARTZ, Material.NETHERITE_SCRAP,
            Material.NETHERITE_INGOT, Material.COAL
    );

    private boolean isOre(ItemStack item) {
        if (item == null) return false;
        return ORE_MATERIALS.contains(item.getType());
    }

    private ItemStack addToStorage(UUID id, ItemStack stack) {
        String base = id.toString();
        for (int i = 0; i < 54; i++) {
            String path = base + "." + i;
            if (!pouchConfig.contains(path) || pouchConfig.getItemStack(path) == null) {
                pouchConfig.set(path, stack);
                save();
                return null;
            }
        }
        return stack; // no space left
    }

    public int depositOres(Player player) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isOre(item)) {
                total += item.getAmount();
                inv.setItem(i, null);
                ItemStack leftover = addToStorage(player.getUniqueId(), item.clone());
                if (leftover != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }
        if (total > 0) {
            save();
        }
        return total;
    }

    public void openPouch(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Mining Pouch");
        String base = player.getUniqueId().toString();
        for (int i = 0; i < 54; i++) {
            String path = base + "." + i;
            ItemStack stack = pouchConfig.getItemStack(path);
            if (stack != null) {
                inv.setItem(i, stack);
            } else {
                inv.setItem(i, createPane());
            }
        }
        player.openInventory(inv);
    }

    private ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            pane.setItemMeta(meta);
        }
        return pane;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Mining Pouch")) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            return;
        }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Player player = (Player) event.getWhoClicked();
        if (event.isLeftClick()) {
            ItemStack toGive = clicked.clone();
            event.getInventory().setItem(event.getSlot(), createPane());
            saveInventory(player, event.getInventory());
            var notFit = player.getInventory().addItem(toGive);
            if (!notFit.isEmpty()) {
                for (ItemStack left : notFit.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
            }
            refreshPouchLore(player);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Mining Pouch")) return;
        Player player = (Player) event.getPlayer();
        saveInventory(player, event.getInventory());
        refreshPouchLore(player);
    }

    private void saveInventory(Player player, Inventory inv) {
        String base = player.getUniqueId().toString();
        for (int i = 0; i < 54; i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() != Material.AIR && item.getType() != Material.GRAY_STAINED_GLASS_PANE) {
                pouchConfig.set(base + "." + i, item);
            } else {
                pouchConfig.set(base + "." + i, null);
            }
        }
        save();
    }

    public int countOres(UUID id) {
        String base = id.toString();
        int count = 0;
        for (int i = 0; i < 54; i++) {
            ItemStack stack = pouchConfig.getItemStack(base + "." + i);
            if (stack != null) count += stack.getAmount();
        }
        return count;
    }

    private void updateLore(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Stores ores");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store ores");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Ores: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshPouchLore(Player player) {
        int count = countOres(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Mining Pouch")) {
                updateLore(stack, count);
            }
        }
        player.updateInventory();
    }
}
