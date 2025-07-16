package goat.minecraft.minecraftnew.subsystems.combat.notification;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Displays temporary health bars above damaged monsters.
 * Players can toggle visibility with /togglehealthbars.
 */
public class MonsterHealthBarManager implements Listener {
    private static MonsterHealthBarManager instance;

    private final JavaPlugin plugin;
    private final Map<LivingEntity, ArmorStand> activeBars = new HashMap<>();
    private final Map<LivingEntity, BukkitTask> tasks = new HashMap<>();
    private final Set<UUID> hiddenPlayers = new HashSet<>();

    private File dataFile;
    private FileConfiguration dataConfig;

    private MonsterHealthBarManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public static MonsterHealthBarManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MonsterHealthBarManager(plugin);
            Bukkit.getPluginManager().registerEvents(instance, plugin);
        }
        return instance;
    }

    public static MonsterHealthBarManager getInstance() {
        return instance;
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "healthbars.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        List<String> list = dataConfig.getStringList("hidden");
        for (String s : list) {
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
        dataConfig.set("hidden", list);
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        tasks.values().forEach(BukkitTask::cancel);
        tasks.clear();
        activeBars.values().forEach(Entity::remove);
        activeBars.clear();
        saveData();
    }

    public void toggle(Player player) {
        UUID id = player.getUniqueId();
        if (hiddenPlayers.contains(id)) {
            hiddenPlayers.remove(id);
            player.sendMessage(ChatColor.GREEN + "Monster health bars enabled.");
        } else {
            hiddenPlayers.add(id);
            player.sendMessage(ChatColor.RED + "Monster health bars disabled.");
        }
        saveData();
    }

    public boolean isHidden(Player player) {
        return hiddenPlayers.contains(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Monster entity)) return;
        // Delay to ensure health is updated
        Bukkit.getScheduler().runTask(plugin, () -> displayBar(entity));
    }

    private void displayBar(LivingEntity entity) {
        ArmorStand existing = activeBars.remove(entity);
        if (existing != null && existing.isValid()) existing.remove();
        BukkitTask existingTask = tasks.remove(entity);
        if (existingTask != null) existingTask.cancel();

        if (entity.isDead()) return;

        ArmorStand stand = (ArmorStand) entity.getWorld().spawnEntity(
                entity.getLocation().add(0, entity.getHeight() + 0.5, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setArms(false);
        stand.setCustomNameVisible(true);

        updateName(stand, entity);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isHidden(p)) {
                p.hideEntity(plugin, stand);
            }
        }

        activeBars.put(entity, stand);

        BukkitTask task = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!stand.isValid() || entity.isDead()) {
                    cleanup();
                    return;
                }
                if (ticks >= 20) {
                    cleanup();
                    return;
                }
                stand.teleport(entity.getLocation().add(0, entity.getHeight() + 0.5, 0));
                ticks++;
            }
            private void cleanup() {
                tasks.remove(entity);
                activeBars.remove(entity);
                stand.remove();
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);
        tasks.put(entity, task);
    }

    private void updateName(ArmorStand stand, LivingEntity entity) {
        double max = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double pct = Math.max(0, Math.min(1, entity.getHealth() / max));
        int bars = (int) Math.round(pct * 20);
        StringBuilder sb = new StringBuilder(ChatColor.GREEN.toString());
        for (int i = 0; i < bars; i++) {
            sb.append("|");
        }
        stand.setCustomName(sb.toString());
    }
}
