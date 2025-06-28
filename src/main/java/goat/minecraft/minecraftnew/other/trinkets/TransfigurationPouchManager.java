package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.utils.devtools.XPManager;
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
import java.util.*;

public class TransfigurationPouchManager implements Listener {
    private static TransfigurationPouchManager instance;
    private final JavaPlugin plugin;
    private File pouchFile;
    private FileConfiguration pouchConfig;
    private final Map<UUID, Integer> pendingXP = new HashMap<>();
    private XPManager xpManager;

    private static final Map<String, Integer> GEMSTONE_POWER = new HashMap<>();
    private static final Map<String, Integer> EFFIGY_POWER = new HashMap<>();
    private static final Map<String, Integer> SOUL_POWER = new HashMap<>();
    private static final Map<String, Integer> BAIT_POWER = new HashMap<>();
    static {
        // Gemstones
        GEMSTONE_POWER.put("Quartz", 1);
        GEMSTONE_POWER.put("Hematite", 1);
        GEMSTONE_POWER.put("Obsidian", 1);
        GEMSTONE_POWER.put("Agate", 1);
        GEMSTONE_POWER.put("Turquoise", 3);
        GEMSTONE_POWER.put("Amethyst", 3);
        GEMSTONE_POWER.put("Citrine", 3);
        GEMSTONE_POWER.put("Garnet", 3);
        GEMSTONE_POWER.put("Topaz", 7);
        GEMSTONE_POWER.put("Peridot", 7);
        GEMSTONE_POWER.put("Aquamarine", 7);
        GEMSTONE_POWER.put("Tanzanite", 7);
        GEMSTONE_POWER.put("Sapphire", 10);
        GEMSTONE_POWER.put("Ruby", 10);
        GEMSTONE_POWER.put("Emerald", 20);
        GEMSTONE_POWER.put("Diamond", 20);
        // Effigies
        EFFIGY_POWER.put("Oak Effigy", 1);
        EFFIGY_POWER.put("Birch Effigy", 1);
        EFFIGY_POWER.put("Spruce Effigy", 3);
        EFFIGY_POWER.put("Acacia Effigy", 7);
        EFFIGY_POWER.put("Dark Oak Effigy", 10);
        EFFIGY_POWER.put("Crimson Effigy", 20);
        EFFIGY_POWER.put("Warped Effigy", 20);
        // Soul items
        SOUL_POWER.put("Soulshard", 1);
        SOUL_POWER.put("Wisp", 3);
        SOUL_POWER.put("Wraith", 7);
        SOUL_POWER.put("Remnant", 10);
        SOUL_POWER.put("Shade", 20);
        // Bait
        BAIT_POWER.put("Common Bait", 1);
        BAIT_POWER.put("Shrimp Bait", 3);
        BAIT_POWER.put("Leech Bait", 7);
        BAIT_POWER.put("Frog Bait", 10);
        BAIT_POWER.put("Caviar Bait", 20);
    }

    private TransfigurationPouchManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new TransfigurationPouchManager(plugin);
        }
    }

    public static TransfigurationPouchManager getInstance() {
        return instance;
    }

    private void initFile() {
        pouchFile = new File(plugin.getDataFolder(), "transfiguration_pouches.yml");
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

    private boolean isGemstone(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).equals("Gemstone")) return true;
        }
        return false;
    }

    private boolean isEffigy(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).equals("Effigy")) return true;
        }
        return false;
    }

    private boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).equals("Soul Item")) return true;
        }
        return false;
    }

    private boolean isBait(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return false;
        for (String line : item.getItemMeta().getLore()) {
            if (ChatColor.stripColor(line).contains("Bait")) return true;
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

    public int depositAll(Player player) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (isGemstone(item) || isEffigy(item) || isSoulItem(item) || isBait(item)) {
                total += item.getAmount();
                inv.setItem(i, null);
                ItemStack leftover = addToStorage(player.getUniqueId(), item.clone());
                if (leftover != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftover);
                }
            }
        }
        if (total > 0) save();
        return total;
    }

    public void openPouch(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Transfiguration Pouch");
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
        if (!event.getView().getTitle().equals("Transfiguration Pouch") && !event.getView().getTitle().equals("Choose Skill")) return;
        if (event.getView().getTitle().equals("Transfiguration Pouch")) {
            if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
            Player player = (Player) event.getWhoClicked();
            if (event.isLeftClick()) {
                ItemStack toGive = clicked.clone();
                event.getInventory().setItem(event.getSlot(), createPane());
                saveInventory(player, event.getInventory());
                var notFit = player.getInventory().addItem(toGive);
                for (ItemStack left : notFit.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), left);
                }
                refreshPouchLore(player);
            }
        } else if (event.getView().getTitle().equals("Choose Skill")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;
            String skill = ChatColor.stripColor(meta.getDisplayName()).replace(" Skill", "");
            Player player = (Player) event.getWhoClicked();
            Integer xp = pendingXP.remove(player.getUniqueId());
            if (xp != null) {
                xpManager.addXP(player, skill, xp);
                player.closeInventory();
                player.sendMessage(ChatColor.GREEN + "Gained " + xp + " XP in " + skill + "!");
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Transfiguration Pouch")) {
            Player player = (Player) event.getPlayer();
            saveInventory(player, event.getInventory());
            refreshPouchLore(player);
        }
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

    private int calculateTotalPower(UUID id) {
        String base = id.toString();
        int total = 0;
        for (int i = 0; i < 54; i++) {
            ItemStack item = pouchConfig.getItemStack(base + "." + i);
            if (item == null) continue;
            String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            int amount = item.getAmount();
            if (GEMSTONE_POWER.containsKey(name)) total += GEMSTONE_POWER.get(name) * amount;
            else if (EFFIGY_POWER.containsKey(name)) total += EFFIGY_POWER.get(name) * amount;
            else if (SOUL_POWER.containsKey(name)) total += SOUL_POWER.get(name) * amount;
            else if (BAIT_POWER.containsKey(name)) total += BAIT_POWER.get(name) * amount;
        }
        return total;
    }

    private void clearInventory(UUID id) {
        String base = id.toString();
        for (int i = 0; i < 54; i++) {
            pouchConfig.set(base + "." + i, null);
        }
        save();
    }

    public void convertToXP(Player player) {
        int totalPower = calculateTotalPower(player.getUniqueId());
        if (totalPower <= 0) {
            player.sendMessage(ChatColor.RED + "No items to transfigure!");
            return;
        }
        clearInventory(player.getUniqueId());
        int xp = totalPower * 100;
        pendingXP.put(player.getUniqueId(), xp);
        openSkillSelect(player);
        refreshPouchLore(player);
    }

    private void openSkillSelect(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Choose Skill");
        String[] skills = {"Fishing", "Farming", "Mining", "Combat", "Player", "Forestry", "Bartering", "Culinary", "Smithing", "Brewing"};
        Material[] icons = {
                Material.FISHING_ROD,
                Material.WHEAT,
                Material.IRON_PICKAXE,
                Material.IRON_SWORD,
                Material.PLAYER_HEAD,
                Material.GOLDEN_AXE,
                Material.EMERALD,
                Material.FURNACE,
                Material.DAMAGED_ANVIL,
                Material.BREWING_STAND
        };
        for (int i = 0; i < skills.length; i++) {
            ItemStack item = new ItemStack(icons[i]);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + skills[i] + " Skill");
                item.setItemMeta(meta);
            }
            inv.setItem(i * 2, item);
        }
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        if (pm != null) {
            pm.setDisplayName(" ");
            pane.setItemMeta(pm);
        }
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, pane);
        }
        player.openInventory(inv);
    }

    public int countItems(UUID id) {
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
        lore.add(ChatColor.GRAY + "Stores power items");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store items");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.BLUE + "Shift-Left-click" + ChatColor.GRAY + ": Convert to XP");
        lore.add(ChatColor.GRAY + "Items: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshPouchLore(Player player) {
        int count = countItems(player.getUniqueId());
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            if (ChatColor.stripColor(meta.getDisplayName()).equals("Transfiguration Pouch")) {
                updateLore(stack, count);
            }
        }
        player.updateInventory();
    }
}
