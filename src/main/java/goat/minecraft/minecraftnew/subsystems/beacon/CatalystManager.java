package goat.minecraft.minecraftnew.subsystems.beacon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CatalystManager implements Listener {
    
    private static CatalystManager instance;
    private final JavaPlugin plugin;
    private final Map<String, Catalyst> activeCatalysts = new HashMap<>();
    private final Map<String, Integer> catalystTiers = new HashMap<>();
    private final Map<UUID, BukkitTask> cooldownNotifyTasks = new HashMap<>();
    private BukkitTask cleanupTask;
    private BukkitTask debugTask;
    
    public CatalystManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startCleanupTask();
        startDebugTask();
    }
    
    public static CatalystManager getInstance() {
        return instance;
    }
    
    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CatalystManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        }
    }
    
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Handles using a beacon charm to summon its selected catalyst.
     */
    private void handleBeaconUse(Player player, ItemStack item) {
        if (player.hasCooldown(org.bukkit.Material.BEACON)) {
            int remaining = player.getCooldown(org.bukkit.Material.BEACON) / 20;
            player.sendMessage(ChatColor.RED + "Your beacon charm is recharging for "
                    + remaining + " more seconds!");
            return;
        }

        String selectedCatalyst = getSelectedCatalyst(item);
        if (selectedCatalyst == null) {
            player.sendMessage(ChatColor.RED + "No catalyst selected! Use your beacon charm to select one first.");
            return;
        }

        if (!hasBeaconPower(item)) {
            player.sendMessage(ChatColor.RED + "Your beacon charm has no power! Add material blocks to charge it.");
            return;
        }

        CatalystType catalystType = CatalystType.fromDisplayName(selectedCatalyst);
        if (catalystType == null) {
            player.sendMessage(ChatColor.RED + "Invalid catalyst type: " + selectedCatalyst);
            return;
        }

        Location playerLoc = player.getLocation();
        String locationKey = getLocationKey(playerLoc);
        if (activeCatalysts.containsKey(locationKey)) {
            player.sendMessage(ChatColor.RED + "A catalyst is already active at this location!");
            return;
        }

        removeCatalystsByPlayer(player.getUniqueId());

        int tier = BeaconManager.getBeaconTier(item);
        int duration = BeaconManager.getBeaconDuration(item);
        int range = BeaconManager.getBeaconRange(item);

        if (catalystType == CatalystType.PROSPERITY) {
            duration *= 2;
            range *= 2;
        }
        summonCatalyst(playerLoc, catalystType, player.getUniqueId(), duration, range, tier);

        player.sendMessage(ChatColor.GREEN + "Summoned " + catalystType.getDisplayName() +
                         ChatColor.GREEN + " for " + duration + " seconds!");
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        player.setCooldown(org.bukkit.Material.BEACON, 20 * 120);
        scheduleCooldownSound(player, 20 * 120);
    }

    private void scheduleCooldownSound(Player player, int cooldownTicks) {
        UUID id = player.getUniqueId();

        BukkitTask existing = cooldownNotifyTasks.remove(id);
        if (existing != null) {
            existing.cancel();
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                cooldownNotifyTasks.remove(id);
                if (player.isOnline()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                }
            }
        }.runTaskLater(plugin, cooldownTicks);

        cooldownNotifyTasks.put(id, task);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !isBeaconCharm(item)) return;

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            handleBeaconUse(player, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getClick().isRightClick()) return;
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;

        ItemStack item = event.getCurrentItem();
        if (item == null || !isBeaconCharm(item)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        handleBeaconUse(player, item);
    }

    @EventHandler
    public void onCatalystInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;
        if (isCatalystArmorStand(stand)) {
            event.setCancelled(true);
        }
    }
    
    public void summonCatalyst(Location location, CatalystType type, UUID placerUUID, int durationSeconds, int range, int tier) {
        String locationKey = getLocationKey(location);
        
        if (activeCatalysts.containsKey(locationKey)) {
            Catalyst existing = activeCatalysts.get(locationKey);
            existing.destroy();
        }
        
        Catalyst catalyst = new Catalyst(location, type, placerUUID, durationSeconds, range);
        activeCatalysts.put(locationKey, catalyst);
        catalystTiers.put(locationKey, tier);
        
        // Debug output with beacon info
        plugin.getLogger().info("[CATALYST DEBUG] Spawned " + type.getDisplayName() + 
                              " at " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + 
                              " with Tier: " + tier + ", Range: " + range + " blocks, Duration: " + durationSeconds + "s");
    }
    
    public boolean isNearCatalyst(Location loc, CatalystType type) {
        for (Catalyst catalyst : activeCatalysts.values()) {
            if (catalyst.getType() == type && catalyst.getLocation().distance(loc) <= catalyst.getRange()) {
                return true;
            }
        }
        return false;
    }
    
    public Catalyst getCatalystAt(Location location) {
        String locationKey = getLocationKey(location);
        return activeCatalysts.get(locationKey);
    }
    
    public void removeCatalyst(Location location) {
        String locationKey = getLocationKey(location);
        Catalyst catalyst = activeCatalysts.remove(locationKey);
        catalystTiers.remove(locationKey);
        if (catalyst != null) {
            catalyst.destroy();
        }
    }
    
    public void removeCatalystsByPlayer(UUID playerUUID) {
        Iterator<Map.Entry<String, Catalyst>> iterator = activeCatalysts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Catalyst> entry = iterator.next();
            Catalyst catalyst = entry.getValue();
            
            if (catalyst.getPlacerUUID().equals(playerUUID)) {
                catalyst.destroy();
                iterator.remove();
                catalystTiers.remove(entry.getKey());
                
                // Notify the player their previous catalyst was removed
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.YELLOW + "Your previous " + catalyst.getType().getDisplayName() + 
                                     ChatColor.YELLOW + " has been replaced.");
                }
            }
        }
    }
    
    public Catalyst findNearestCatalyst(Location location, CatalystType type) {
        Catalyst nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        
        for (Catalyst catalyst : activeCatalysts.values()) {
            if (catalyst.getType() == type) {
                double distance = catalyst.getLocation().distance(location);
                if (distance <= catalyst.getRange() && distance < nearestDistance) {
                    nearest = catalyst;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearest;
    }
    
    public int getCatalystTier(Catalyst catalyst) {
        String locationKey = getLocationKey(catalyst.getLocation());
        return catalystTiers.getOrDefault(locationKey, 1);
    }
    
    private void startCleanupTask() {
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredCatalysts();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void startDebugTask() {
        debugTask = new BukkitRunnable() {
            @Override
            public void run() {
                debugCatalystDetection();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void debugCatalystDetection() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Location playerLoc = player.getLocation();
            StringBuilder detectedCatalysts = new StringBuilder();
            
            for (CatalystType type : CatalystType.values()) {
                boolean inRange = isNearCatalyst(playerLoc, type);
                if (inRange) {
                    if (detectedCatalysts.length() > 0) {
                        detectedCatalysts.append(", ");
                    }
                    detectedCatalysts.append(type.getColoredDisplayName());
                }
            }
            
            // Check for unknown catalyst types (catalysts that don't match any known type)
            for (Catalyst catalyst : activeCatalysts.values()) {
                if (catalyst.getLocation().distance(playerLoc) <= (catalyst.getRange() *2)) {
                    boolean knownType = false;
                    for (CatalystType type : CatalystType.values()) {
                        if (catalyst.getType() == type) {
                            knownType = true;
                            break;
                        }
                    }
                    if (!knownType) {
                        plugin.getLogger().warning("[CATALYST DEBUG] Player " + player.getName() + 
                                                 " near UNKNOWN catalyst type at " + catalyst.getLocation());
                    }
                }
            }
            
            if (detectedCatalysts.length() > 0) {
                plugin.getLogger().info("[CATALYST DEBUG] Player " + player.getName() + 
                                      " is within range of: " + detectedCatalysts.toString());
            }

        }
    }
    
    private void cleanupExpiredCatalysts() {
        Iterator<Map.Entry<String, Catalyst>> iterator = activeCatalysts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Catalyst> entry = iterator.next();
            Catalyst catalyst = entry.getValue();
            
            if (catalyst.isExpired()) {
                catalyst.destroy();
                iterator.remove();
                catalystTiers.remove(entry.getKey());
                
                Player placer = Bukkit.getPlayer(catalyst.getPlacerUUID());
                if (placer != null && placer.isOnline()) {
                    placer.sendMessage(ChatColor.YELLOW + "Your " + catalyst.getType().getDisplayName() + 
                                     ChatColor.YELLOW + " has expired.");
                }
            }
        }
    }
    
    public void shutdown() {
        for (Catalyst catalyst : activeCatalysts.values()) {
            catalyst.destroy();
        }
        activeCatalysts.clear();
        
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        if (debugTask != null) {
            debugTask.cancel();
        }

        for (BukkitTask task : cooldownNotifyTasks.values()) {
            task.cancel();
        }
        cooldownNotifyTasks.clear();
    }

    private boolean isCatalystArmorStand(ArmorStand stand) {
        for (Catalyst catalyst : activeCatalysts.values()) {
            ArmorStand as = catalyst.getArmorStand();
            if (as != null && as.getUniqueId().equals(stand.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    private String getLocationKey(Location location) {
        return location.getWorld().getName() + ":" +
               location.getBlockX() + ":" +
               location.getBlockY() + ":" +
               location.getBlockZ();
    }
    
    private boolean isBeaconCharm(ItemStack item) {
        if (item == null || item.getType() != org.bukkit.Material.BEACON) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Beacon Charm");
    }
    
    private String getSelectedCatalyst(ItemStack beacon) {
        if (!beacon.hasItemMeta() || !beacon.getItemMeta().hasLore()) return null;
        
        for (String line : beacon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Selected Catalyst: ")) {
                return stripped.replace("Selected Catalyst: ", "");
            }
        }
        return null;
    }
    
    private boolean hasBeaconPower(ItemStack beacon) {
        if (!beacon.hasItemMeta() || !beacon.getItemMeta().hasLore()) return false;
        
        for (String line : beacon.getItemMeta().getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Beacon Power: ")) {
                try {
                    String powerText = stripped.replace("Beacon Power: ", "").replace(",", "");
                    return Integer.parseInt(powerText) > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }
}
