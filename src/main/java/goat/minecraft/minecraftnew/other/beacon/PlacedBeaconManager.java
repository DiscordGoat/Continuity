package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages physical beacons placed in the world and their effects.
 * Handles activation, persistence, and monster suppression within the
 * beacon's radius.
 */
public class PlacedBeaconManager implements Listener {

    private static PlacedBeaconManager instance;

    private final JavaPlugin plugin;
    private final Map<Location, BeaconEntry> beacons = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    private static final Set<Material> BASE_MATERIALS = EnumSet.of(
            Material.IRON_BLOCK, Material.GOLD_BLOCK,
            Material.EMERALD_BLOCK, Material.DIAMOND_BLOCK);

    private static final Map<Integer, Integer> TIER_RADIUS = Map.of(
            1, 50,
            2, 100,
            3, 200,
            4, 300,
            5, 400
    );

    private PlacedBeaconManager(JavaPlugin plugin) {
        this.plugin = plugin;
        init();
        startRemovalTask();
    }

    /**
     * Initializes the manager and registers it as an event listener.
     */
    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PlacedBeaconManager(plugin);
            plugin.getServer().getPluginManager().registerEvents(instance, plugin);
        }
    }

    public static PlacedBeaconManager getInstance() {
        return instance;
    }

    private void init() {
        dataFile = new File(plugin.getDataFolder(), "heldBeacons.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadBeacons();
    }

    private void loadBeacons() {
        beacons.clear();
        if (!dataConfig.contains("beacons")) return;

        for (String key : dataConfig.getConfigurationSection("beacons").getKeys(false)) {
            Location loc = parseKey(key);
            int tier = dataConfig.getInt("beacons." + key + ".tier", 1);
            ItemStack item = dataConfig.getItemStack("beacons." + key + ".item");
            if (loc != null && item != null) {
                beacons.put(loc, new BeaconEntry(tier, item));
            }
        }
    }

    private void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getKey(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location parseKey(String key) {
        String[] parts = key.split(",");
        if (parts.length != 4) return null;
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(world, x, y, z);
    }

    /**
     * Attempts to activate a beacon pyramid when a player right-clicks a
     * material block while holding a beacon charm.
     */
    public boolean tryActivateBeacon(Player player, Block baseBlock, ItemStack beaconCharm) {
        if (baseBlock == null || !BASE_MATERIALS.contains(baseBlock.getType())) return false;

        int tier = determineTier(baseBlock);
        if (tier <= 0) {
            player.sendMessage(ChatColor.RED + "Invalid beacon pyramid!");
            return false;
        }

        Location beaconLoc = baseBlock.getLocation().add(0, 1, 0);
        Block beaconBlock = beaconLoc.getBlock();
        Block glassBlock = beaconLoc.clone().add(0, 1, 0).getBlock();
        if (beaconBlock.getType() != Material.AIR || glassBlock.getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "Not enough space above the pyramid!");
            return false;
        }

        ItemStack stored = beaconCharm.clone();
        stored.setAmount(1);
        if (beaconCharm.getAmount() > 1) {
            beaconCharm.setAmount(beaconCharm.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        beacons.put(beaconLoc, new BeaconEntry(tier, stored));
        String key = getKey(beaconLoc);
        dataConfig.set("beacons." + key + ".tier", tier);
        dataConfig.set("beacons." + key + ".item", stored);
        saveConfig();

        beaconBlock.setType(Material.BEACON);
        glassBlock.setType(getGlassForTier(tier));
        player.playSound(beaconLoc, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        player.sendMessage(ChatColor.GREEN + "Activated Tier " + tier + " Beacon!");

        return true;
    }

    /**
     * Handles right-clicking an active beacon block to retrieve the stored
     * beacon charm and dismantle the beacon structure.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BEACON) return;

        ItemStack charm = removeBeaconData(block.getLocation());
        if (charm == null) return;

        event.setCancelled(true);
        block.setType(Material.AIR);
        block.getRelative(0, 1, 0).setType(Material.AIR);
        block.getWorld().dropItemNaturally(block.getLocation(), charm);
    }

    /**
     * Handles breaking of beacon blocks that were created via this system.
     */
    public boolean handleBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.BEACON) return false;
        ItemStack charm = removeBeaconData(event.getBlock().getLocation());
        if (charm == null) return false;

        event.setDropItems(false);
        event.getBlock().getRelative(0, 1, 0).setType(Material.AIR);
        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), charm);
        return true;
    }

    private ItemStack removeBeaconData(Location loc) {
        BeaconEntry entry = beacons.remove(loc);
        if (entry == null) return null;
        dataConfig.set("beacons." + getKey(loc), null);
        saveConfig();
        return entry.item;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;

        Location loc = event.getLocation();
        for (Map.Entry<Location, BeaconEntry> entry : beacons.entrySet()) {
            Location beaconLoc = entry.getKey();
            if (!beaconLoc.getWorld().equals(loc.getWorld())) continue;

            int radius = getRadius(entry.getValue().tier);
            if (beaconLoc.distanceSquared(loc) <= radius * radius) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void startRemovalTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                removeNearbyMonsters();
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private void removeNearbyMonsters() {
        for (Map.Entry<Location, BeaconEntry> entry : beacons.entrySet()) {
            Location loc = entry.getKey();
            int radius = getRadius(entry.getValue().tier);
            Collection<Entity> entities = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
            for (Entity e : entities) {
                if (e instanceof Monster) {
                    e.remove();
                }
            }
        }
    }

    private Material getGlassForTier(int tier) {
        return switch (tier) {
            case 1 -> Material.WHITE_STAINED_GLASS_PANE;
            case 2 -> Material.GREEN_STAINED_GLASS_PANE;
            case 3 -> Material.BLUE_STAINED_GLASS_PANE;
            case 4 -> Material.PURPLE_STAINED_GLASS_PANE;
            case 5 -> Material.YELLOW_STAINED_GLASS_PANE;
            default -> Material.WHITE_STAINED_GLASS_PANE;
        };
    }

    private int determineTier(Block topCenter) {
        Location center = topCenter.getLocation();
        int tier = 0;
        for (int layer = 0; layer < 5; layer++) {
            int size = 3 + layer * 2;
            int y = center.getBlockY() - layer;
            for (int x = center.getBlockX() - size / 2; x <= center.getBlockX() + size / 2; x++) {
                for (int z = center.getBlockZ() - size / 2; z <= center.getBlockZ() + size / 2; z++) {
                    Material mat = topCenter.getWorld().getBlockAt(x, y, z).getType();
                    if (!BASE_MATERIALS.contains(mat)) {
                        return tier;
                    }
                }
            }
            tier = layer + 1;
        }
        return tier;
    }

    private int getRadius(int tier) {
        return TIER_RADIUS.getOrDefault(tier, 0);
    }

    private static class BeaconEntry {
        final int tier;
        final ItemStack item;

        BeaconEntry(int tier, ItemStack item) {
            this.tier = tier;
            this.item = item;
        }
    }
}

