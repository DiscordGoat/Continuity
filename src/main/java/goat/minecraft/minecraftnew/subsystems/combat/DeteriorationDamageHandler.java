package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the Deterioration stacking damage over time.
 */
public class DeteriorationDamageHandler implements Listener {

    /** Damage applied per deterioration stack each tick. */
    private static final double DAMAGE_PER_STACK = 0.5;

    private static DeteriorationDamageHandler instance;

    private final JavaPlugin plugin;
    private final DamageNotificationService notificationService;
    private final Map<UUID, Integer> stacks = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new ConcurrentHashMap<>();

    private DeteriorationDamageHandler(JavaPlugin plugin, DamageNotificationService service) {
        this.plugin = plugin;
        this.notificationService = service;
    }

    public static DeteriorationDamageHandler getInstance(JavaPlugin plugin, DamageNotificationService service) {
        if (instance == null) {
            instance = new DeteriorationDamageHandler(plugin, service);
        }
        return instance;
    }

    public static DeteriorationDamageHandler getInstance() {
        return instance;
    }

    /**
     * Adds deterioration stacks to an entity.
     * <p>
     * The effect normally applies to {@link Monster}s but should
     * also work on registered sea creatures. Sea creatures are
     * identified through the {@code SEA_CREATURE} metadata key.
     */
    public void addDeterioration(LivingEntity entity, int amount) {
        boolean isMonster = entity instanceof Monster;
        boolean isSeaCreature = !entity.getMetadata("SEA_CREATURE").isEmpty();
        if (!isMonster && !isSeaCreature) return;

        UUID id = entity.getUniqueId();
        int newLevel = stacks.getOrDefault(id, 0) + amount;
        stacks.put(id, newLevel);
        startTask(entity);
    }

    private void startTask(LivingEntity entity) {
        UUID id = entity.getUniqueId();
        if (tasks.containsKey(id)) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!entity.isValid() || entity.isDead()) {
                    cleanup();
                    return;
                }

                int level = stacks.getOrDefault(id, 0);
                if (level <= 0) {
                    cleanup();
                    return;
                }

                double damage = level * DAMAGE_PER_STACK;
                entity.damage(damage);
                entity.setNoDamageTicks(0);
                notificationService.createDecayDamageIndicator(entity.getLocation(), damage);
                stacks.put(id, level - 1);
            }

            private void cleanup() {
                BukkitRunnable t = tasks.remove(id);
                if (t != null) t.cancel();
                stacks.remove(id);
            }
        };

        tasks.put(id, task);
        task.runTaskTimer(plugin, 0L, 1L);
    }
}
