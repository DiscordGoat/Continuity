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
    private final XPManager xpManager;
    private File pouchFile;
    private FileConfiguration pouchConfig;
    private final Map<UUID, Integer> pendingXP = new HashMap<>();

    private static final Map<String, Integer> GEM_POWER = new HashMap<>();
    private static final Map<String, Integer> EFFIGY_POWER = new HashMap<>();
    private static final Map<String, Integer> SOUL_POWER = new HashMap<>();
    private static final Map<String, Integer> BAIT_POWER = new HashMap<>();
    static {
        // Gemstones
        GEM_POWER.put("Quartz", 1);
        GEM_POWER.put("Hematite", 1);
        GEM_POWER.put("Obsidian", 1);
        GEM_POWER.put("Agate", 1);
        GEM_POWER.put("Turquoise", 3);
        GEM_POWER.put("Amethyst", 3);
        GEM_POWER.put("Citrine", 3);
        GEM_POWER.put("Garnet", 3);
        GEM_POWER.put("Topaz", 7);
        GEM_POWER.put("Peridot", 7);
        GEM_POWER.put("Aquamarine", 7);
        GEM_POWER.put("Tanzanite", 7);
        GEM_POWER.put("Sapphire", 10);
        GEM_POWER.put("Ruby", 10);
        GEM_POWER.put("Emerald", 20);
        GEM_POWER.put("Diamond", 20);

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

    private TransfigurationPouchManager(JavaPlugin plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin, XPManager xpManager) {
        if (instance == null) {
            instance = new TransfigurationPouchManager(plugin, xpManager);
        }
    }

    public static TransfigurationPouchManager getInstance() { return instance; }

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
        try { pouchConfig.save(pouchFile); } catch (IOException e) { e.printStackTrace(); }
    }

    private boolean isGemstone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            if (ChatColor.stripColor(line).equals("Gemstone")) return true;
        }
        return false;
    }

    private boolean isEffigy(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            if (ChatColor.stripColor(line).equals("Effigy")) return true;
        }
        return false;
    }

    private boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            if (ChatColor.stripColor(line).equals("Soul Item")) return true;
        }
        return false;
    }

    private boolean isBait(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        for (String line : meta.getLore()) {
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
        return stack; // no space
    }

    public int depositItems(Player player) {
        Inventory inv = player.getInventory();
        int total = 0;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
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
    public void onPouchClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Transfiguration Pouch")) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Player player = (Player) event.getWhoClicked();
        if (event.isLeftClick()) {
            ItemStack give = clicked.clone();
            event.getInventory().setItem(event.getSlot(), createPane());
            saveInventory(player, event.getInventory());
            Map<Integer, ItemStack> notFit = player.getInventory().addItem(give);
            for (ItemStack left : notFit.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            refreshLore(player);
        }
    }

    @EventHandler
    public void onPouchClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Transfiguration Pouch")) return;
        Player player = (Player) event.getPlayer();
        saveInventory(player, event.getInventory());
        refreshLore(player);
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
        lore.add(ChatColor.GRAY + "Stores upgrade items");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Store items");
        lore.add(ChatColor.BLUE + "Shift-Left-click" + ChatColor.GRAY + ": Convert to XP");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Open pouch");
        lore.add(ChatColor.GRAY + "Items: " + ChatColor.GREEN + count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshLore(Player player) {
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

    public void convertToXP(Player player) {
        UUID id = player.getUniqueId();
        String base = id.toString();
        int totalPower = 0;
        for (int i = 0; i < 54; i++) {
            String path = base + "." + i;
            ItemStack stack = pouchConfig.getItemStack(path);
            if (stack == null) continue;
            int amount = stack.getAmount();
            String name = ChatColor.stripColor(stack.getItemMeta().getDisplayName());
            Integer val = GEM_POWER.get(name);
            if (val == null) val = EFFIGY_POWER.get(name);
            if (val == null) val = SOUL_POWER.get(name);
            if (val == null) val = BAIT_POWER.get(name);
            if (val != null) totalPower += val * amount;
            pouchConfig.set(path, null);
        }
        save();
        refreshLore(player);
        if (totalPower == 0) {
            player.sendMessage(ChatColor.RED + "No items to convert.");
            return;
        }
        int xp = totalPower * 100;
        pendingXP.put(id, xp);
        openSkillSelectGUI(player);
    }

    private void openSkillSelectGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Select Skill");
        String[] skills = {"Fishing","Farming","Mining","Combat","Player","Forestry","Bartering","Culinary","Smithing","Brewing"};
        Material[] icons = {Material.FISHING_ROD,Material.WHEAT,Material.IRON_PICKAXE,Material.IRON_SWORD,Material.PLAYER_HEAD,Material.GOLDEN_AXE,Material.EMERALD,Material.FURNACE,Material.DAMAGED_ANVIL,Material.BREWING_STAND};
        for (int i=0;i<skills.length;i++) {
            ItemStack it = new ItemStack(icons[i]);
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + skills[i]);
                it.setItemMeta(meta);
            }
            gui.setItem(i*2, it);
        }
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        if (pm != null) { pm.setDisplayName(" "); pane.setItemMeta(pm); }
        for (int i=0;i<gui.getSize();i++) {
            if (gui.getItem(i)==null) gui.setItem(i, pane);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onSkillSelect(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equals("Select Skill")) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        Player player = (Player) event.getWhoClicked();
        UUID id = player.getUniqueId();
        Integer xp = pendingXP.remove(id);
        if (xp == null) return;
        String skill = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        xpManager.addXP(player, skill, xp);
        player.sendMessage(ChatColor.GREEN + "Gained " + xp + " " + skill + " XP!");
        player.closeInventory();
    }
}
