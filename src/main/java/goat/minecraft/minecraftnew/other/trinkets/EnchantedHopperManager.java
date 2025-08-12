package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles Enchanted Hopper trinkets. Items placed inside the hopper GUI act as a
 * whitelist. Matching items from the player's inventory are periodically moved
 * to the container directly above the hopper in the backpack. Containers may be
 * shulker boxes, satchels or other special items.
 */
public class EnchantedHopperManager implements Listener {
    private static EnchantedHopperManager instance;
    private final JavaPlugin plugin;
    private final NamespacedKey idKey;
    private final NamespacedKey delayKey;
    private File hopperFile;
    private FileConfiguration hopperConfig;
    private final Map<UUID, UUID> openHoppers = new HashMap<>();
    private final Map<UUID, Inventory> previousInventories = new HashMap<>();
    private final Map<UUID, Long> lastRun = new HashMap<>();
    private final Map<UUID, Long> disabledUntil = new HashMap<>();

    private static final long[] DELAY_MS = {500L, 2000L, 30000L, 120000L};

    private EnchantedHopperManager(JavaPlugin plugin) {
        this.plugin = plugin;
        idKey = new NamespacedKey(plugin, "hopper_id");
        delayKey = new NamespacedKey(plugin, "hopper_delay");
        initFile();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTask();
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new EnchantedHopperManager(plugin);
        }
    }

    public static EnchantedHopperManager getInstance() {
        return instance;
    }

    private void initFile() {
        hopperFile = new File(plugin.getDataFolder(), "enchanted_hoppers.yml");
        if (!hopperFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                hopperFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hopperConfig = YamlConfiguration.loadConfiguration(hopperFile);
    }

    private void save() {
        try {
            hopperConfig.save(hopperFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openHopper(Player player, ItemStack hopperItem) {
        UUID id = getOrCreateId(hopperItem);
        Inventory inv = Bukkit.createInventory(null, 9, "Enchanted Hopper");
        String base = id.toString();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = hopperConfig.getItemStack(base + "." + i);
            if (stack != null) {
                inv.setItem(i, stack);
            }
        }
        UUID pid = player.getUniqueId();
        openHoppers.put(pid, id);
        previousInventories.put(pid, player.getOpenInventory().getTopInventory());
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        InventoryType type = event.getInventory().getType();
        if (type != InventoryType.CRAFTING && type != InventoryType.CREATIVE) {
            Player player = (Player) event.getPlayer();
            disabledUntil.put(player.getUniqueId(), System.currentTimeMillis() + 10000L);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Enchanted Hopper")) return;
        Player player = (Player) event.getPlayer();
        UUID pid = player.getUniqueId();
        UUID id = openHoppers.remove(pid);
        if (id == null) return;
        String base = id.toString();
        Inventory inv = event.getInventory();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() != Material.AIR) {
                hopperConfig.set(base + "." + i, stack);
            } else {
                hopperConfig.set(base + "." + i, null);
            }
        }
        save();
        Inventory prev = previousInventories.remove(pid);
        if (prev != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.openInventory(prev);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Enchanted Hopper")) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) return;
        event.setCancelled(false);
    }

    private UUID getOrCreateId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return UUID.randomUUID();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String existing = container.get(idKey, PersistentDataType.STRING);
        if (existing != null) {
            if (!container.has(delayKey, PersistentDataType.INTEGER)) {
                container.set(delayKey, PersistentDataType.INTEGER, 0);
                item.setItemMeta(meta);
            }
            return UUID.fromString(existing);
        }
        UUID id = UUID.randomUUID();
        container.set(idKey, PersistentDataType.STRING, id.toString());
        container.set(delayKey, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return id;
    }

    private List<ItemStack> getWhitelist(UUID id) {
        List<ItemStack> list = new ArrayList<>();
        String base = id.toString();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = hopperConfig.getItemStack(base + "." + i);
            if (stack != null) {
                list.add(stack);
            }
        }
        return list;
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    processPlayer(player);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void processPlayer(Player player) {
        UUID pid = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long until = disabledUntil.get(pid);
        if (until != null) {
            if (now < until) {
                return;
            } else {
                disabledUntil.remove(pid);
            }
        }
        InventoryType openType = player.getOpenInventory().getTopInventory().getType();
        if (openType != InventoryType.CRAFTING && openType != InventoryType.CREATIVE) {
            return; // pause automation when a container is open
        }
        for (int slot = 9; slot < 54; slot++) {
            ItemStack item = CustomBundleGUI.getInstance().getBackpackItem(player, slot);
            if (item == null) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) continue;
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (!name.equals("Enchanted Hopper")) continue;
            UUID id = getOrCreateId(item);
            int delayIndex = getDelayIndex(item);
            if (!shouldRun(id, delayIndex)) continue;
            ItemStack containerItem = CustomBundleGUI.getInstance().getBackpackItem(player, slot - 9);
            if (containerItem == null) continue;
            transferItems(player, containerItem, getWhitelist(id));
            CustomBundleGUI.getInstance().setBackpackItem(player, slot - 9, containerItem);
            CustomBundleGUI.getInstance().setBackpackItem(player, slot, item);
        }
    }

    private void transferItems(Player player, ItemStack containerItem, List<ItemStack> whitelist) {
        if (whitelist.isEmpty()) return;
        for (ItemStack filter : whitelist) {
            for (ItemStack invItem : player.getInventory().getContents()) {
                if (invItem == null || invItem.getType() == Material.AIR) continue;
                if (matches(filter, invItem)) {
                    moveOne(containerItem, invItem, player);
                }
            }
        }
    }

    private boolean matches(ItemStack filter, ItemStack candidate) {
        if (filter.getType() != candidate.getType()) return false;
        if (filter.hasItemMeta() != candidate.hasItemMeta()) return false;
        if (filter.hasItemMeta()) {
            ItemMeta fMeta = filter.getItemMeta();
            ItemMeta cMeta = candidate.getItemMeta();
            if (fMeta.hasDisplayName() != cMeta.hasDisplayName()) return false;
            if (fMeta.hasDisplayName() && !fMeta.getDisplayName().equals(cMeta.getDisplayName())) return false;
            if (fMeta.hasEnchants() != cMeta.hasEnchants()) return false;
        }
        return true;
    }

    private void moveOne(ItemStack containerItem, ItemStack fromInv, Player player) {
        if (fromInv.getAmount() <= 0) return;
        ItemStack toMove = new ItemStack(fromInv.getType(), 1);
        if (fromInv.hasItemMeta()) {
            toMove.setItemMeta(fromInv.getItemMeta());
        }
        boolean success = false;
        ItemMeta meta = containerItem.getItemMeta();
        if (meta instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof ShulkerBox box) {
            success = box.getInventory().addItem(toMove).isEmpty();
            bsm.setBlockState(box);
            containerItem.setItemMeta(bsm);
        } else {
            String name = ChatColor.stripColor(meta != null ? meta.getDisplayName() : null);
            if (name != null && name.endsWith(" Satchel")) {
                SatchelManager.getInstance().depositItem(player, name.split(" ")[0], toMove);
                success = true;
            } else if ("Enchanted Lava Bucket".equals(name)) {
                success = true; // item simply deleted
            }
        }
        if (success) {
            fromInv.setAmount(fromInv.getAmount() - 1);
        }
    }

    private int getDelayIndex(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer index = container.get(delayKey, PersistentDataType.INTEGER);
        if (index == null) {
            index = 0;
            container.set(delayKey, PersistentDataType.INTEGER, index);
            item.setItemMeta(meta);
        }
        return index;
    }

    private boolean shouldRun(UUID id, int index) {
        long now = System.currentTimeMillis();
        long last = lastRun.getOrDefault(id, 0L);
        if (now - last >= DELAY_MS[index]) {
            lastRun.put(id, now);
            return true;
        }
        return false;
    }

    public void cycleDelay(Player player, ItemStack item) {
        UUID id = getOrCreateId(item);
        int index = getDelayIndex(item);
        index = (index + 1) % DELAY_MS.length;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(delayKey, PersistentDataType.INTEGER, index);
            item.setItemMeta(meta);
        }
        updateLore(item, index);
        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Hopper delay set to " + formatDelay(index));
        lastRun.put(id, System.currentTimeMillis());
    }

    private String formatDelay(int index) {
        switch (index) {
            case 0 -> { return "0.5s"; }
            case 1 -> { return "2s"; }
            case 2 -> { return "30s"; }
            case 3 -> { return "120s"; }
            default -> { return ""; }
        }
    }

    private void updateLore(ItemStack item, int index) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Transfers whitelisted items to container above");
        lore.add(ChatColor.BLUE + "Left-click" + ChatColor.GRAY + ": Configure");
        lore.add(ChatColor.BLUE + "Shift-Right-click" + ChatColor.GRAY + ": Change delay");
        lore.add(ChatColor.GRAY + "Delay: " + ChatColor.WHITE + formatDelay(index));
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
