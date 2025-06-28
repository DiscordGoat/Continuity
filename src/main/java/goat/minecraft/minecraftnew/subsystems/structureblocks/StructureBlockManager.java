package goat.minecraft.minecraftnew.subsystems.structureblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StructureBlockManager implements Listener {

    private static StructureBlockManager instance;
    private final JavaPlugin plugin;

    private File dataFile;
    private FileConfiguration dataConfig;

    private final NamespacedKey powerKey;
    private final NamespacedKey idKey;
    private final NamespacedKey functionKey;

    private StructureBlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.powerKey = new NamespacedKey(plugin, "sb_power");
        this.idKey = new NamespacedKey(plugin, "sb_id");
        this.functionKey = new NamespacedKey(plugin, "sb_function");
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new StructureBlockManager(plugin);
        }
    }

    public static StructureBlockManager getInstance() {
        return instance;
    }

    private void initFile() {
        dataFile = new File(plugin.getDataFolder(), "structureblocks.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    // Create a new Structure Block charm with starting power 0
    public ItemStack createStructureBlock() {
        ItemStack item = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Structure Block Charm");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "Power: " + ChatColor.YELLOW + "0");
        lore.add(ChatColor.GRAY + "Function: None");
        meta.setLore(lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(powerKey, PersistentDataType.INTEGER, 0);
        container.set(idKey, PersistentDataType.STRING, UUID.randomUUID().toString());
        container.set(functionKey, PersistentDataType.STRING, "None");
        item.setItemMeta(meta);
        return item;
    }

    /** Determines if the given ItemStack is a Structure Block Charm. */
    public boolean isStructureBlock(ItemStack item) {
        if (item == null || item.getType() != Material.STRUCTURE_BLOCK) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Structure Block Charm");
    }

    /** Sets the power of a Structure Block Charm. */
    public void setStructureBlockPower(ItemStack item, int power) {
        if (item == null) return;
        setPower(item, power);
    }

    /** Returns the current power stored in a Structure Block Charm. */
    public int getStructureBlockPower(ItemStack item) {
        return getPower(item);
    }

    private int getPower(ItemStack item) {
        if (item == null || item.getType() != Material.STRUCTURE_BLOCK) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer val = meta.getPersistentDataContainer().get(powerKey, PersistentDataType.INTEGER);
        return val == null ? 0 : val;
    }

    private void setPower(ItemStack item, int power) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(powerKey, PersistentDataType.INTEGER, power);
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < lore.size(); i++) {
            String s = ChatColor.stripColor(lore.get(i));
            if (s.startsWith("Power:")) {
                lore.set(i, ChatColor.AQUA + "Power: " + ChatColor.YELLOW + power);
                found = true;
                break;
            }
        }
        if (!found) {
            lore.add(0, ChatColor.AQUA + "Power: " + ChatColor.YELLOW + power);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private UUID getId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String idStr = item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (idStr == null) return null;
        try { return UUID.fromString(idStr); } catch (IllegalArgumentException e) { return null; }
    }

    private void setFunction(ItemStack item, String function) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(functionKey, PersistentDataType.STRING, function);
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < lore.size(); i++) {
            String s = ChatColor.stripColor(lore.get(i));
            if (s.startsWith("Function:")) {
                lore.set(i, ChatColor.GRAY + "Function: " + ChatColor.YELLOW + function);
                found = true;
                break;
            }
        }
        if (!found) {
            lore.add(ChatColor.GRAY + "Function: " + ChatColor.YELLOW + function);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private String getFunction(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return "None";
        String val = item.getItemMeta().getPersistentDataContainer().get(functionKey, PersistentDataType.STRING);
        return val == null ? "None" : val;
    }

    private ItemStack getStoredMaterial(UUID id) {
        if (id == null) return null;
        return dataConfig.getItemStack(id.toString());
    }

    private void saveStoredMaterial(UUID id, ItemStack stack) {
        if (id == null) return;
        if (stack == null || stack.getType() == Material.AIR) {
            dataConfig.set(id.toString(), null);
        } else {
            dataConfig.set(id.toString(), stack);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BlockFace rotateRight(BlockFace face) {
        switch (face) {
            case NORTH: return BlockFace.EAST;
            case EAST:  return BlockFace.SOUTH;
            case SOUTH: return BlockFace.WEST;
            case WEST:  return BlockFace.NORTH;
            default:    return BlockFace.EAST;
        }
    }

    private void applyWall3x3(Block clicked, BlockFace clickedFace, Player player, ItemStack item) {
    private void applyWall3x3(Block clicked, BlockFace face, Player player, ItemStack item) {
        ItemStack stored = getStoredMaterial(getId(item));
        if (stored == null || stored.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "No material stored in Structure Block!");
            return;
        }

        int power = getPower(item);
        if (power <= 0) {
            player.sendMessage(ChatColor.RED + "Structure Block is out of power!");
            return;
        }

        Block start = clicked.getRelative(clickedFace);
        BlockFace playerFacing = player.getFacing();
        BlockFace right = rotateRight(playerFacing);
        Block start = clicked.getRelative(face);
        BlockFace right = rotateRight(face);

        int placed = 0;
        for (int w = -1; w <= 1; w++) {
            for (int h = 0; h < 3; h++) {
                if (power <= 0) break;
                Block target = start.getRelative(right, w).getRelative(BlockFace.UP, h);
                if (target.getType() == Material.AIR) {
                    target.setType(stored.getType());
                    power--;
                    placed++;
                }
            }
        }

        if (placed > 0) {
            setPower(item, power);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            player.sendMessage(ChatColor.GREEN + "Placed " + placed + " blocks using Structure Block.");
        } else {
            player.sendMessage(ChatColor.RED + "No space to place blocks.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isStructureBlock(item)) return;

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            new StructureBlockGUI(plugin, item).open(player);
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Block clicked = event.getClickedBlock();
            if (clicked == null) return;

            String function = getFunction(item);
            if (function.equalsIgnoreCase("3x3")) {
                applyWall3x3(clicked, event.getBlockFace(), player, item);
            }
        }
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.STRUCTURE_BLOCK) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (!name.equals("Structure Block Charm")) return;
        event.setCancelled(true);
        new StructureBlockGUI(plugin, item).open(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        if (cursor == null || clicked == null) return;
        if (clicked.getType() != Material.STRUCTURE_BLOCK) return;
        if (!clicked.hasItemMeta() || !ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).equals("Structure Block Charm")) return;
        if (!cursor.getType().isBlock()) return;
        // Only handle top inventory or player inventory? Follows beacon logic.
        int add = cursor.getType() == Material.OBSIDIAN ? 10 : 1;
        int amount = cursor.getAmount();
        int current = getPower(clicked);
        int newVal = Math.min(current + add * amount, 10000);
        if (newVal == current) {
            player.sendMessage(ChatColor.RED + "This Structure Block is already at maximum power (10,000)!");
            return;
        }
        setPower(clicked, newVal);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
        player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.YELLOW + amount + "x " + cursor.getType().name().toLowerCase().replace('_',' ') + ChatColor.GOLD + " (+" + (add*amount) + " Power)" );
        event.setCursor(null);
        event.setCancelled(true);
    }

    class StructureBlockGUI implements Listener {
        private final JavaPlugin plugin;
        private final ItemStack blockItem;
        private final String title;
        private final UUID id;
        private org.bukkit.inventory.Inventory gui;

        StructureBlockGUI(JavaPlugin plugin, ItemStack item) {
            this.plugin = plugin;
            this.blockItem = item;
            this.title = ChatColor.LIGHT_PURPLE + "Structure Block";
            this.id = getId(item);
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        void open(Player player) {
            gui = Bukkit.createInventory(null, 27, title);
            ItemStack stored = getStoredMaterial(id);
            if (stored != null) gui.setItem(13, stored);

            ItemStack func = new ItemStack(Material.WRITABLE_BOOK);
            ItemMeta fm = func.getItemMeta();
            fm.setDisplayName(ChatColor.GREEN + "Functions");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current: " + ChatColor.YELLOW + getFunction(blockItem));
            lore.add(ChatColor.YELLOW + "Click to change!");
            fm.setLore(lore);
            func.setItemMeta(fm);
            gui.setItem(11, func);

            ItemStack coming = new ItemStack(Material.BARRIER);
            ItemMeta cm = coming.getItemMeta();
            cm.setDisplayName(ChatColor.RED + "Coming Soon");
            coming.setItemMeta(cm);
            gui.setItem(15, coming);

            fill(gui);
            player.openInventory(gui);
        }

        private void fill(org.bukkit.inventory.Inventory inv) {
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = filler.getItemMeta();
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
            for (int i = 0; i < inv.getSize(); i++) {
                if (i == 13) continue;
                if (inv.getItem(i) == null) inv.setItem(i, filler);
            for (int i=0;i<inv.getSize();i++) {
                if (inv.getItem(i)==null) inv.setItem(i, filler);
            }
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(title)) return;
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            if (slot == 13) {
                // allow placing one item as material
                ItemStack cursor = event.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    ItemStack single = cursor.clone();
                    single.setAmount(1);
                    gui.setItem(13, single);
                    event.setCursor(null);
                }
            } else if (slot == 11) {
                new FunctionGUI(plugin, blockItem).open(player);
            }
        }

        @EventHandler
        public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
            if (!event.getView().getTitle().equals(title)) return;
            org.bukkit.inventory.Inventory inv = event.getInventory();
            ItemStack mat = inv.getItem(13);
            if (mat != null) {
                ItemMeta im = mat.getItemMeta();
                if (im == null || !im.hasDisplayName() || ChatColor.stripColor(im.getDisplayName()).isEmpty()) {
                    mat = null;
                }
            }
            saveStoredMaterial(id, mat);
            HandlerList.unregisterAll(this);
        }

    }

    class FunctionGUI implements Listener {
        private final JavaPlugin plugin;
        private final ItemStack blockItem;
        private final String title = ChatColor.GREEN + "Select Function";

        FunctionGUI(JavaPlugin plugin, ItemStack item) {
            this.plugin = plugin;
            this.blockItem = item;
            Bukkit.getPluginManager().registerEvents(this, plugin);
        }

        void open(Player player) {
            org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 9, title);
            inv.setItem(1, create(Material.STONE, "3x3 Wall", "3x3"));
            inv.setItem(3, create(Material.LADDER, "Vertical Fill", "Vertical"));
            inv.setItem(5, create(Material.OAK_PLANKS, "Horizontal Fill", "Horizontal"));
            inv.setItem(7, create(Material.CHEST, "Custom Cuboid", "Custom"));
            player.openInventory(inv);
        }

        private ItemStack create(Material mat, String name, String id) {
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + name);
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "func"), PersistentDataType.STRING, id);
            item.setItemMeta(meta);
            return item;
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().equals(title)) return;
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String func = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "func"), PersistentDataType.STRING);
            if (func == null) return;
            setFunction(blockItem, func);
            ((Player)event.getWhoClicked()).sendMessage(ChatColor.GREEN + "Function set to " + func);
            event.getWhoClicked().closeInventory();
            HandlerList.unregisterAll(this);
        }

        @EventHandler
        public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
            if (event.getView().getTitle().equals(title)) {
                HandlerList.unregisterAll(this);
            }
        }
    }
}

