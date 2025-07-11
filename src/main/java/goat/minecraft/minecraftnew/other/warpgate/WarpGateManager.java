package goat.minecraft.minecraftnew.other.warpgate;

import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles placement of Warp Gates. When the custom Warp Gate item is placed,
 * the block is replaced with a prismarine lantern (Sea Lantern) and the player is prompted to name
 * the new instance. If the placement is cancelled or the block is broken before
 * naming completes, the original block is restored and the item refunded.
 */
public class WarpGateManager implements Listener {

    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    /** Simple representation of a warp instance. */
    public static class WarpInstance {
        public final Location location;
        public final String name;

        public WarpInstance(Location location, String name) {
            this.location = location;
            this.name = name;
        }
    }

    // Stored warp instances by unique id
    private final Map<UUID, WarpInstance> instances = new HashMap<>();

    /** Data about a pending warp gate placement. */
    private static class PendingGate {
        final BlockState oldState;
        final UUID playerId;
        PendingGate(BlockState oldState, UUID playerId) {
            this.oldState = oldState;
            this.playerId = playerId;
        }
    }

    // Map of location key -> pending gate data
    private final Map<String, PendingGate> pending = new HashMap<>();
    // Player waiting to name their placed gate
    private final Map<UUID, String> naming = new HashMap<>();
    // Tracks slot -> warp instance mapping for open menus
    private final Map<UUID, Map<Integer, UUID>> openMenus = new HashMap<>();
    // Players temporarily blocked from opening warp menus
    private final Map<UUID, Long> blockedMenus = new HashMap<>();
    // Cooldown tracking for remote warp activations via Pocket Portal
    private final Map<UUID, Long> remoteCooldowns = new HashMap<>();
    private static final long REMOTE_COOLDOWN = 10 * 60 * 1000L; // 10 minutes

    public WarpGateManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "warp_instances.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadInstances();
    }

    private String toKey(Location loc) {
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    private Location fromKey(String key) {
        String[] p = key.split(":");
        World w = Bukkit.getWorld(p[0]);
        int x = Integer.parseInt(p[1]), y = Integer.parseInt(p[2]), z = Integer.parseInt(p[3]);
        return new Location(w, x, y, z);
    }

    private boolean isWarpGateItem(ItemStack stack) {
        if (stack == null) return false;
        ItemStack gate = ItemRegistry.getWarpGate();
        if (!stack.hasItemMeta() || !gate.hasItemMeta()) return false;
        ItemMeta sMeta = stack.getItemMeta();
        ItemMeta gMeta = gate.getItemMeta();
        return stack.getType() == gate.getType()
                && sMeta.hasDisplayName()
                && gMeta.hasDisplayName()
                && sMeta.getDisplayName().equals(gMeta.getDisplayName());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!isWarpGateItem(event.getItemInHand())) return;

        Block placed = event.getBlockPlaced();
        BlockState oldState = event.getBlockReplacedState();
        Player player = event.getPlayer();

        // Replace with Sea Lantern (prismarine lantern)
        placed.setType(Material.SEA_LANTERN);

        String key = toKey(placed.getLocation());
        pending.put(key, new PendingGate(oldState, player.getUniqueId()));
        naming.put(player.getUniqueId(), key);

        player.sendMessage(ChatColor.AQUA + "Name this instance in chat. Type 'cancel' to abort.");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String key = naming.remove(id);
        if (key == null) return;
        event.setCancelled(true);
        PendingGate data = pending.remove(key);
        if (data == null) return;

        String msg = event.getMessage().trim();
        if (msg.equalsIgnoreCase("cancel")) {
            // Restore original block and refund item
            data.oldState.update(true, false);
            event.getPlayer().getInventory().addItem(ItemRegistry.getWarpGate());
            event.getPlayer().sendMessage(ChatColor.RED + "Warp Gate placement cancelled.");
            return;
        }

        // Save the named warp instance
        Location loc = fromKey(key);
        UUID warpId = UUID.randomUUID();
        instances.put(warpId, new WarpInstance(loc, msg));
        event.getPlayer().sendMessage(ChatColor.GREEN + "Created instance '" + msg + "'.");
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        String key = toKey(event.getBlock().getLocation());
        PendingGate data = pending.remove(key);
        if (data != null) {
            // Prevent drops and restore original block
            event.setDropItems(false);
            data.oldState.update(true, false);

            // Refund item to the player if they're the placer
            if (data.playerId != null) {
                Player p = Bukkit.getPlayer(data.playerId);
                if (p != null) {
                    p.getInventory().addItem(ItemRegistry.getWarpGate());
                }
            }
            naming.values().remove(key);
        }

        // Remove any saved instance at this location
        UUID instId = findInstanceAt(event.getBlock().getLocation());
        if (instId != null) {
            instances.remove(instId);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.SEA_LANTERN) return;

        // Check if player is temporarily blocked from warping
        UUID pid = event.getPlayer().getUniqueId();
        Long until = blockedMenus.get(pid);
        if (until != null && until > System.currentTimeMillis()) {
            event.getPlayer().sendMessage(ChatColor.RED + "You are too exhausted to use the warp gate.");
            return;
        }

        UUID thisGate = findInstanceAt(event.getClickedBlock().getLocation());
        if (thisGate == null) return;

        event.setCancelled(true);

        Location loc = event.getClickedBlock().getLocation();
        Inventory inv = Bukkit.createInventory(null, 54, "Warp Gate");
        Map<Integer, UUID> slotMap = new HashMap<>();
        int slot = 0;
        for (Map.Entry<UUID, WarpInstance> e : instances.entrySet()) {
            UUID id = e.getKey();
            WarpInstance inst = e.getValue();
            if (id.equals(thisGate)) continue;
            if (!inst.location.getWorld().equals(loc.getWorld())) continue;
            double dist = inst.location.distance(loc);
            if (dist > 2000) continue;
            if (slot >= 54) break;

            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + inst.name);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Oxygen Cost: " + (int) dist);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slotMap.put(slot, id);
            slot++;
        }

        openMenus.put(event.getPlayer().getUniqueId(), slotMap);
        event.getPlayer().openInventory(inv);
    }

    /**
     * Opens the warp menu for the given player from any location. Used by
     * remote activation items like the Pocket Portal.
     *
     * @param player the player for whom to open the menu
     */
    public void openWarpMenu(Player player) {
        // Respect exhaustion lockout
        UUID pid = player.getUniqueId();
        Long until = blockedMenus.get(pid);
        if (until != null && until > System.currentTimeMillis()) {
            player.sendMessage(ChatColor.RED + "You are too exhausted to use the warp gate.");
            return;
        }

        long now = System.currentTimeMillis();
        Long last = remoteCooldowns.get(pid);
        if (last != null && now - last < REMOTE_COOLDOWN) {
            long remaining = (REMOTE_COOLDOWN - (now - last)) / 1000;
            player.sendMessage(ChatColor.RED + "Pocket Portal recharging: " + remaining + "s left.");
            return;
        }
        remoteCooldowns.put(pid, now);

        Location loc = player.getLocation();
        Inventory inv = Bukkit.createInventory(null, 54, "Warp Gate");
        Map<Integer, UUID> slotMap = new HashMap<>();
        int slot = 0;
        for (Map.Entry<UUID, WarpInstance> e : instances.entrySet()) {
            UUID id = e.getKey();
            WarpInstance inst = e.getValue();
            if (!inst.location.getWorld().equals(loc.getWorld())) continue;
            double dist = inst.location.distance(loc);
            if (dist > 2000) continue;
            if (slot >= 54) break;

            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + inst.name);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.GRAY + "Oxygen Cost: " + (int) dist);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(slot, item);
            slotMap.put(slot, id);
            slot++;
        }

        openMenus.put(pid, slotMap);
        player.openInventory(inv);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Warp Gate")) {
            openMenus.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Warp Gate")) return;

        UUID id = event.getWhoClicked().getUniqueId();
        Map<Integer, UUID> map = openMenus.get(id);
        if (map == null) return;

        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getInventory()) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        int slot = event.getSlot();
        UUID warpId = map.get(slot);
        if (warpId == null) return;

        WarpInstance inst = instances.get(warpId);
        if (inst == null) return;

        Player player = (Player) event.getWhoClicked();
        event.getWhoClicked().closeInventory();

        int dist = (int) player.getLocation().distance(inst.location);

        PlayerOxygenManager oxygen = PlayerOxygenManager.getInstance();
        int current = oxygen.getPlayerOxygen(player);
        int newOxy = Math.max(0, current - dist);
        oxygen.setPlayerOxygenLevel(player, newOxy);

        // Warp the player to the block above the gate
        player.teleport(inst.location.clone().add(0, 1, 0));

        if (newOxy <= 0 && !BlessingUtils.hasFullSetBonus(player, "Duskblood")) {
            blockedMenus.put(player.getUniqueId(), System.currentTimeMillis() + 10 * 60 * 1000L);
            player.sendMessage(ChatColor.RED + "You are too exhausted to warp for 10 minutes!");
        }
    }

    public void onDisable() {
        saveAllInstances();
    }

    //=======================================================================
    // Persistence
    //=======================================================================
    private void loadInstances() {
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        for (String key : dataConfig.getKeys(false)) {
            String world = dataConfig.getString(key + ".world");
            World w = Bukkit.getWorld(world);
            if (w == null) {
                plugin.getLogger().warning("[WarpGateManager] Skipping " + key + " because world is null.");
                continue;
            }
            double x = dataConfig.getDouble(key + ".x");
            double y = dataConfig.getDouble(key + ".y");
            double z = dataConfig.getDouble(key + ".z");
            String name = dataConfig.getString(key + ".name", "Unnamed");
            Location loc = new Location(w, x, y, z);
            instances.put(UUID.fromString(key), new WarpInstance(loc, name));
        }
        plugin.getLogger().info("[WarpGateManager] Loaded " + instances.size() + " warp instance(s).");
    }

    private void saveAllInstances() {
        for (String key : dataConfig.getKeys(false)) {
            dataConfig.set(key, null);
        }
        for (Map.Entry<UUID, WarpInstance> e : instances.entrySet()) {
            UUID id = e.getKey();
            WarpInstance inst = e.getValue();
            dataConfig.set(id.toString() + ".world", inst.location.getWorld().getName());
            dataConfig.set(id.toString() + ".x", inst.location.getX());
            dataConfig.set(id.toString() + ".y", inst.location.getY());
            dataConfig.set(id.toString() + ".z", inst.location.getZ());
            dataConfig.set(id.toString() + ".name", inst.name);
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("[WarpGateManager] Saved " + instances.size() + " warp instance(s).");
    }

    private UUID findInstanceAt(Location loc) {
        for (Map.Entry<UUID, WarpInstance> e : instances.entrySet()) {
            Location l = e.getValue().location;
            if (l.getWorld().equals(loc.getWorld())
                    && l.getBlockX() == loc.getBlockX()
                    && l.getBlockY() == loc.getBlockY()
                    && l.getBlockZ() == loc.getBlockZ()) {
                return e.getKey();
            }
        }
        return null;
    }
}
