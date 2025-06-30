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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransfigurationPouchManager implements Listener {
    private static TransfigurationPouchManager instance;
    private final JavaPlugin plugin;
    private File pouchFile;
    private FileConfiguration pouchConfig;
    private final Map<UUID, Integer> pendingXP = new HashMap<>();

    private static final String[] SKILLS = {"Fishing","Farming","Mining","Combat","Player","Taming","Forestry","Bartering","Culinary","Smithing","Brewing"};
    private static final Material[] SKILL_ICONS = {
            Material.FISHING_ROD, Material.WHEAT, Material.IRON_PICKAXE,
            Material.IRON_SWORD, Material.PLAYER_HEAD, Material.LEAD, Material.GOLDEN_AXE,
            Material.EMERALD, Material.FURNACE, Material.DAMAGED_ANVIL,
            Material.BREWING_STAND
    };

    private TransfigurationPouchManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new TransfigurationPouchManager(plugin);
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

    private boolean isSoul(ItemStack item) {
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

    private int getItemPower(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return 0;
        Pattern p = Pattern.compile("\\+([0-9]+)");
        for (String line : item.getItemMeta().getLore()) {
            String s = ChatColor.stripColor(line);
            if (s.startsWith("Power:") || s.startsWith("Energy:")) {
                Matcher m = p.matcher(s);
                if (m.find()) {
                    try { return Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        return 0;
    }

    private ItemStack addToStorage(UUID id, ItemStack stack) {
        String base = id.toString() + ".items";
        for (int i=0;i<54;i++) {
            String path = base+"."+i;
            if (!pouchConfig.contains(path) || pouchConfig.getItemStack(path)==null) {
                pouchConfig.set(path, stack);
                save();
                return null;
            }
        }
        return stack;
    }

    public void depositItems(Player player) {
        Inventory inv = player.getInventory();
        for (int i=0;i<inv.getSize();i++) {
            ItemStack item = inv.getItem(i);
            if (isGemstone(item) || isEffigy(item) || isSoul(item) || isBait(item)) {
                inv.setItem(i,null);
                ItemStack leftover = addToStorage(player.getUniqueId(), item.clone());
                if (leftover!=null) player.getWorld().dropItemNaturally(player.getLocation(), leftover);
            }
        }
        save();
        refreshPouchLore(player);
    }

    public void openPouch(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "Transfiguration Pouch");
        String base = player.getUniqueId().toString()+".items";
        for(int i=0;i<54;i++) {
            String path = base+"."+i;
            ItemStack stack = pouchConfig.getItemStack(path);
            if(stack!=null) inv.setItem(i, stack); else inv.setItem(i, createPane());
        }
        player.openInventory(inv);
    }

    private ItemStack createPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if(meta!=null){ meta.setDisplayName(" "); pane.setItemMeta(meta);} 
        return pane;
    }

    @EventHandler
    public void onPouchClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Transfiguration Pouch")) return;
        if (event.getClickedInventory()==null || event.getClickedInventory()!=event.getInventory()) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if(clicked==null || clicked.getType()==Material.AIR || clicked.getType()==Material.GRAY_STAINED_GLASS_PANE) return;
        Player player = (Player) event.getWhoClicked();
        if(event.isLeftClick()) {
            ItemStack give = clicked.clone();
            event.getInventory().setItem(event.getSlot(), createPane());
            saveInventory(player, event.getInventory());
            Map<Integer, ItemStack> notFit = player.getInventory().addItem(give);
            if(!notFit.isEmpty()) for(ItemStack l:notFit.values()) player.getWorld().dropItemNaturally(player.getLocation(), l);
            refreshPouchLore(player);
        }
    }

    @EventHandler
    public void onPouchClose(InventoryCloseEvent event) {
        if(!event.getView().getTitle().equals("Transfiguration Pouch")) return;
        Player player = (Player) event.getPlayer();
        saveInventory(player, event.getInventory());
        refreshPouchLore(player);
    }

    private void saveInventory(Player player, Inventory inv) {
        String base = player.getUniqueId().toString()+".items";
        for(int i=0;i<54;i++) {
            ItemStack item = inv.getItem(i);
            if(item!=null && item.getType()!=Material.AIR && item.getType()!=Material.GRAY_STAINED_GLASS_PANE) {
                pouchConfig.set(base+"."+i,item);
            } else {
                pouchConfig.set(base+"."+i,null);
            }
        }
        save();
    }

    public int countItems(UUID id) {
        String base = id.toString()+".items";
        int c=0;
        for(int i=0;i<54;i++) {
            ItemStack s = pouchConfig.getItemStack(base+"."+i);
            if(s!=null) c+=s.getAmount();
        }
        return c;
    }

    private int calculateTotalPower(UUID id) {
        String base = id.toString()+".items";
        int power=0;
        for(int i=0;i<54;i++) {
            ItemStack s = pouchConfig.getItemStack(base+"."+i);
            if(s!=null) power += getItemPower(s)*s.getAmount();
        }
        return power;
    }

    public void consumeForXP(Player player) {
        int power = calculateTotalPower(player.getUniqueId());
        if(power<=0) return;
        int xp = power * 50;
        clearItems(player.getUniqueId());
        pendingXP.put(player.getUniqueId(), xp);
        openSkillSelect(player);
    }

    private void clearItems(UUID id) {
        String base = id.toString()+".items";
        for(int i=0;i<54;i++) pouchConfig.set(base+"."+i,null);
        save();
    }

    private void openSkillSelect(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Choose Skill");
        for(int i=0;i<SKILLS.length;i++) {
            ItemStack it = new ItemStack(SKILL_ICONS[i]);
            ItemMeta meta = it.getItemMeta();
            if(meta!=null){
                meta.setDisplayName(ChatColor.AQUA + SKILLS[i]);
                meta.setLore(Collections.singletonList(ChatColor.GRAY+"Add XP here"));
                it.setItemMeta(meta);
            }
            inv.setItem(i*2, it);
        }
        ItemStack pane = createPane();
        for(int i=0;i<inv.getSize();i++) if(inv.getItem(i)==null) inv.setItem(i,pane);
        player.openInventory(inv);
    }

    @EventHandler
    public void onSkillSelect(InventoryClickEvent event) {
        if(!event.getView().getTitle().equals("Choose Skill")) return;
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if(item==null || item.getType()==Material.AIR || item.getType()==Material.GRAY_STAINED_GLASS_PANE) return;
        Player player = (Player) event.getWhoClicked();
        String skill = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        Integer xp = pendingXP.remove(player.getUniqueId());
        if(xp!=null) {
            XPManager xpManager = new XPManager(plugin);
            xpManager.addXP(player, skill, xp);
            player.sendMessage(ChatColor.GREEN+"Gained "+xp+" XP in "+skill+".");
        }
        player.closeInventory();
    }

    private void updateLore(ItemStack item, int count) {
        ItemMeta meta = item.getItemMeta();
        if(meta==null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY+"Stores gemstones, effigies, souls and bait");
        lore.add(ChatColor.BLUE+"Left-click"+ChatColor.GRAY+": Store items");
        lore.add(ChatColor.BLUE+"Shift-Left-click"+ChatColor.GRAY+": Convert to XP");
        lore.add(ChatColor.BLUE+"Shift-Right-click"+ChatColor.GRAY+": Open pouch");
        lore.add(ChatColor.GRAY+"Items: "+ChatColor.GREEN+count);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public void refreshPouchLore(Player player) {
        int count = countItems(player.getUniqueId());
        for(ItemStack stack : player.getInventory().getContents()) {
            if(stack==null) continue;
            ItemMeta meta = stack.getItemMeta();
            if(meta==null || !meta.hasDisplayName()) continue;
            if(ChatColor.stripColor(meta.getDisplayName()).equals("Transfiguration Pouch")) {
                updateLore(stack, count);
            }
        }
        player.updateInventory();
    }
}
