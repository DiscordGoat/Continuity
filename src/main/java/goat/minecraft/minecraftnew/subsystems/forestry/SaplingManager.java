package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages sapling growth prevention and timed orchard growth.
 */
public class SaplingManager implements Listener {
    private static SaplingManager instance;

    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    private final Set<String> saplingLocations = new HashSet<>();
    private int cooldownSeconds;
    private BukkitRunnable timerTask;

    private static final int DEFAULT_COOLDOWN = 30 * 24 * 60 * 60; // 30 days

    private SaplingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "saplings.yml");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException ignored) {}
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
        startTask();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static SaplingManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SaplingManager(plugin);
        }
        return instance;
    }

    public static SaplingManager getInstance() {
        return instance;
    }

    private void startTask() {
        timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().isEmpty()) return;
                if (cooldownSeconds > 0) {
                    cooldownSeconds--;
                    saveData();
                }
                if (cooldownSeconds <= 0) {
                    attemptGrowAll();
                }
            }
        };
        timerTask.runTaskTimer(plugin, 20L, 20L);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        ItemStack item = event.getItemInHand();
        if (isSuperSapling(item)) {
            Material saplingType = normalFromSuper(item);
            block.setType(saplingType);
            spawnSurroundingSaplings(event.getPlayer(), saplingType);
            addSapling(block.getLocation());
            return;
        }
        if (isRegularSapling(block.getType())) {
            addSapling(block.getLocation());
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        Block block = event.getLocation().getBlock();
        if (isRegularSapling(block.getType())) {
            event.setCancelled(true);
        }
    }

    private void attemptGrowAll() {
        boolean allPresent = true;
        Iterator<String> it = saplingLocations.iterator();
        List<Location> toGrow = new ArrayList<>();
        while (it.hasNext()) {
            String key = it.next();
            Location loc = fromLocKey(key);
            if (loc.getBlock().getType().toString().endsWith("SAPLING") || loc.getBlock().getType() == Material.MANGROVE_PROPAGULE) {
                toGrow.add(loc);
            } else {
                it.remove();
                allPresent = false;
            }
        }
        if (!allPresent) {
            saveData();
            return; // wait until next check
        }
        for (Location loc : toGrow) {
            TreeType type = treeTypeFor(loc.getBlock().getType());
            loc.getBlock().setType(Material.AIR);
            loc.getWorld().generateTree(loc, type);
        }
        saplingLocations.clear();
        cooldownSeconds = Math.max(0, DEFAULT_COOLDOWN - calculateRegrowthReduction());
        saveData();
    }

    private int calculateRegrowthReduction() {
        int highest = 0;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        for (Player p : Bukkit.getOnlinePlayers()) {
            int level = 0;
            level += mgr.getTalentLevel(p.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_I);
            level += mgr.getTalentLevel(p.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_II);
            level += mgr.getTalentLevel(p.getUniqueId(), Skill.FORESTRY, Talent.REGROWTH_III);
            if (level > highest) highest = level;
        }
        return highest * 86400;
    }

    private void addSapling(Location loc) {
        saplingLocations.add(toLocKey(loc));
        saveData();
    }

    private boolean isRegularSapling(Material m) {
        return m.toString().endsWith("SAPLING") || m == Material.MANGROVE_PROPAGULE;
    }

    private boolean isSuperSapling(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name != null && name.startsWith("Super ") && name.endsWith("Sapling");
    }

    private Material normalFromSuper(ItemStack item) {
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase();
        if (name.contains("OAK")) return Material.OAK_SAPLING;
        if (name.contains("BIRCH")) return Material.BIRCH_SAPLING;
        if (name.contains("SPRUCE")) return Material.SPRUCE_SAPLING;
        if (name.contains("JUNGLE")) return Material.JUNGLE_SAPLING;
        if (name.contains("ACACIA")) return Material.ACACIA_SAPLING;
        if (name.contains("DARK OAK")) return Material.DARK_OAK_SAPLING;
        if (name.contains("MANGROVE")) return Material.MANGROVE_PROPAGULE;
        if (name.contains("CHERRY")) return Material.CHERRY_SAPLING;
        return Material.OAK_SAPLING;
    }

    private void spawnSurroundingSaplings(Player player, Material saplingType) {
        Random rand = new Random();
        List<Location> placed = new ArrayList<>();
        placed.add(player.getLocation().getBlock().getLocation());
        int attempts = 0;
        while (placed.size() < 64 && attempts < 1000) {
            attempts++;
            double dx = rand.nextInt(32) - 16;
            double dz = rand.nextInt(32) - 16;
            Location loc = player.getLocation().clone().add(dx, 0, dz).getBlock().getLocation();
            boolean far = true;
            for (Location l : placed) {
                if (l.distanceSquared(loc) < 64) { far = false; break; }
            }
            if (!far) continue;
            Block below = loc.clone().add(0, -1, 0).getBlock();
            if (loc.getBlock().getType() == Material.AIR && below.getType().isSolid()) {
                loc.getBlock().setType(saplingType);
                placed.add(loc);
                addSapling(loc);
            }
        }
    }

    private TreeType treeTypeFor(Material sapling) {
        return switch (sapling) {
            case OAK_SAPLING -> TreeType.TREE;
            case BIRCH_SAPLING -> TreeType.BIRCH;
            case SPRUCE_SAPLING -> TreeType.REDWOOD;
            case JUNGLE_SAPLING -> TreeType.JUNGLE;
            case ACACIA_SAPLING -> TreeType.ACACIA;
            case DARK_OAK_SAPLING -> TreeType.DARK_OAK;
            case MANGROVE_PROPAGULE -> TreeType.MANGROVE;
            case CHERRY_SAPLING -> TreeType.CHERRY;
            default -> TreeType.TREE;
        };
    }

    private String toLocKey(Location loc) {
        return loc.getWorld().getName()+":"+loc.getBlockX()+":"+loc.getBlockY()+":"+loc.getBlockZ();
    }

    private Location fromLocKey(String key) {
        String[] p = key.split(":");
        World w = Bukkit.getWorld(p[0]);
        return new Location(w, Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
    }

    private void loadData() {
        this.cooldownSeconds = dataConfig.getInt("cooldownRemaining", DEFAULT_COOLDOWN);
        List<String> locs = dataConfig.getStringList("locations");
        saplingLocations.addAll(locs);
    }

    private void saveData() {
        dataConfig.set("cooldownRemaining", cooldownSeconds);
        dataConfig.set("locations", new ArrayList<>(saplingLocations));
        try { dataConfig.save(dataFile); } catch (IOException ignored) {}
    }

    public int getCooldownSecondsRemaining() {
        return cooldownSeconds;
    }

    public void setCooldownSecondsRemaining(int secs) {
        this.cooldownSeconds = Math.max(0, secs);
        saveData();
    }

    public ItemStack getSuperSaplingForLog(Material log) {
        return switch (log) {
            case OAK_LOG -> ItemRegistry.getSuperOakSapling();
            case BIRCH_LOG -> ItemRegistry.getSuperBirchSapling();
            case SPRUCE_LOG -> ItemRegistry.getSuperSpruceSapling();
            case JUNGLE_LOG -> ItemRegistry.getSuperJungleSapling();
            case ACACIA_LOG -> ItemRegistry.getSuperAcaciaSapling();
            case DARK_OAK_LOG -> ItemRegistry.getSuperDarkOakSapling();
            case MANGROVE_LOG -> ItemRegistry.getSuperMangroveSapling();
            case CHERRY_LOG -> ItemRegistry.getSuperCherrySapling();
            default -> null;
        };
    }

    public void maybeDropSuperSapling(Material log, Location loc) {
        if (log == Material.CRIMSON_STEM || log == Material.WARPED_STEM) return;
        if (Math.random() < 0.01) {
            ItemStack drop = getSuperSaplingForLog(log);
            if (drop != null) {
                loc.getWorld().dropItemNaturally(loc, drop);
            }
        }
    }

    public void shutdown() {
        if (timerTask != null) timerTask.cancel();
        saveData();
    }
}
