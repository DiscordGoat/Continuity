package goat.minecraft.minecraftnew.subsystems.combat.notification;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Displays short-lived monster health bars using armor stands.
 */
public class MonsterHealthBarService {

    private final JavaPlugin plugin;
    private final Logger logger;

    private final Map<LivingEntity, BarInfo> activeBars = new ConcurrentHashMap<>();
    private final Set<UUID> hiddenPlayers = ConcurrentHashMap.newKeySet();

    private File dataFile;
    private YamlConfiguration dataConfig;
    private static final String HIDDEN_KEY = "hidden";

    public MonsterHealthBarService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        initStorage();
    }

    private void initStorage() {
        dataFile = new File(plugin.getDataFolder(), "healthbars.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed creating health bar data file", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        List<String> stored = dataConfig.getStringList(HIDDEN_KEY);
        for (String s : stored) {
            try {
                hiddenPlayers.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignore) {}
        }
    }

    private void saveData() {
        List<String> list = new ArrayList<>();
        for (UUID id : hiddenPlayers) {
            list.add(id.toString());
        }
        dataConfig.set(HIDDEN_KEY, list);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed saving health bar data", e);
        }
    }

    /** Toggle health bar visibility for a player. */
    public void toggle(Player player) {
        UUID id = player.getUniqueId();
        if (hiddenPlayers.contains(id)) {
            hiddenPlayers.remove(id);
            player.sendMessage(ChatColor.GREEN + "Monster health bars enabled.");
        } else {
            hiddenPlayers.add(id);
            player.sendMessage(ChatColor.YELLOW + "Monster health bars disabled.");
        }
        saveData();
    }

    /** Shows a health bar above the given entity for 1 second. */
    public void showHealthBar(LivingEntity entity, double health) {
        if (entity == null || entity.isDead()) return;
        // Remove any existing bar
        BarInfo old = activeBars.remove(entity);
        if (old != null) {
            old.task.cancel();
            if (old.stand.isValid()) old.stand.remove();
        }

        String barText = buildBar(entity, health);
        Location loc = entity.getLocation().add(0, entity.getHeight() + 0.5, 0);
        ArmorStand stand = (ArmorStand) entity.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setMarker(true);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomName(barText);
        stand.setCustomNameVisible(true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (hiddenPlayers.contains(p.getUniqueId())) {
                p.hideEntity(plugin, stand);
            }
        }

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!stand.isValid() || entity.isDead()) {
                    cleanup();
                    return;
                }
                Location target = entity.getLocation().add(0, entity.getHeight() + 0.5, 0);
                stand.teleport(target);
                if (ticks++ >= 20) {
                    cleanup();
                }
            }
            private void cleanup() {
                this.cancel();
                if (stand.isValid()) stand.remove();
                activeBars.remove(entity);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        activeBars.put(entity, new BarInfo(stand, task));
    }

    private String buildBar(LivingEntity entity, double health) {
        double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        int segments = 20;
        int filled = (int) Math.round(Math.max(0, Math.min(health, max)) / max * segments);
        StringBuilder sb = new StringBuilder(ChatColor.GREEN.toString());
        for (int i = 0; i < filled; i++) sb.append('|');
        return sb.toString();
    }

    public void cleanup() {
        for (BarInfo info : activeBars.values()) {
            info.task.cancel();
            if (info.stand.isValid()) info.stand.remove();
        }
        activeBars.clear();
        saveData();
    }

    private static class BarInfo {
        final ArmorStand stand;
        final BukkitTask task;
        BarInfo(ArmorStand stand, BukkitTask task) { this.stand = stand; this.task = task; }
    }
}
