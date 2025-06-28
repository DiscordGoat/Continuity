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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SeaCreaturePouchManager implements Listener {
    private static SeaCreaturePouchManager instance;
    private final JavaPlugin plugin;
    private File pouchFile;
    private FileConfiguration pouchConfig;

    private SeaCreaturePouchManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SeaCreaturePouchManager(plugin);
        }
    }

    public static SeaCreaturePouchManager getInstance() {
        return instance;
    }

    private void initFile() {
        pouchFile = new File(plugin.getDataFolder(), "sea_pouches.yml");
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

    private static final Set<String> DROP_NAMES = Set.of(
            "Shallow Shell",
            "Shell",
            "Deep Shell",
            "Abyssal Shell",
            "Fish Bone",
            "Sea Salt",
            "Calamari",
            "Turtle Tactics",
            "Anaklusmos",
            "Lightning Bolt",
            "Loyal Declaration",
            "Verdant Relic Tide",
            "Creature Tooth",
            "Fish Bait",
            "Narwhal Tusk",
            "Lucky",
            "Diving Helmet",
            "Leviathan Heart",
            "Sweeping Edge",
            "Shrapnel",
            "Howl",
            "Unbreakable",
            "Swim Trunks",
            "Verdant Relic Treasury",
            "Abyssal Ink"
    );

    private boolean isSeaCreatureDrop(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        Material type = item.getType();
        if (type == Material.COD || type == Material.SALMON || type == Material.PUFFERFISH || type == Material.TROPICAL_FISH)
            return false;
        if (type == Material.TRIDENT) return true;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (meta.hasDisplayName() && DROP_NAMES.contains(ChatColor.stripColor(meta.getDisplayName()))) {
            return true;
        }
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.contains("Water Technology.") || stripped.contains("Trophy Item") ||
                        stripped.contains("Bait") || stripped.contains("Smithing Item") ||
                        stripped.contains("Artifact") || stripped.contains("Culinary Ingredient") ||
                        stripped.contains("Mastery")) {
                    return true;
                }
            }
        }
        return false;
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

    public int depositDrops(Player player) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isSeaCreatureDrop(item)) {
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
        Inventory inv = Bukkit.createInventory(null, 54, "Sea Creature Pouch");
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
        if (!event.getView().getTitle().equals("Sea Creature Pouch")) return;
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
            // give to player or drop
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
        if (!event.getView().getTitle().equals("Sea Creature Pouch")) return;
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

    public int countDrops(UUID id) {
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
        lore.add(ChatColor.GRAY + "Stores sea creature drops");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store drops");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Drops: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshPouchLore(Player player) {
        int count = countDrops(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Pouch of Sea Creatures")) {
                updateLore(stack, count);
            }
        }
        player.updateInventory();
    }
}
