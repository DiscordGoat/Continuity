package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the Antagonize combat talent:
 *  - lets armor take its durability hits
 *  - gives the player absorption equal to the damage, nullifying it instantly
 *  - then re‐applies the damage gradually over time
 */
public class AntagonizeHandler implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, List<DelayedDamage>> damageMap = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new ConcurrentHashMap<>();

    public AntagonizeHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;

        int level = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ANTAGONIZE);
        if (level <= 0) return;

        // capture the damage that *was* applied (armor durability has already been handled)
        double damageDealt = event.getFinalDamage();

        // schedule the gradual re‐damage
        UUID id = player.getUniqueId();
        damageMap.computeIfAbsent(id, k -> new ArrayList<>())
                .add(new DelayedDamage(damageDealt, level * 20));
        startTask(player);

        // immediately heal + give absorption hearts equal to that damage
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) return;

                // heal back what was just taken
                double healedHealth = Math.min(player.getMaxHealth(), player.getHealth() + damageDealt);
                player.setHealth(healedHealth);

                // grant absorption hearts
                player.setAbsorptionAmount(player.getAbsorptionAmount() + damageDealt);
            }
        }.runTask(plugin);
    }

    private void startTask(Player player) {
        UUID id = player.getUniqueId();
        if (tasks.containsKey(id)) return;

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                List<DelayedDamage> list = damageMap.get(id);
                if (list == null || list.isEmpty() || player.isDead() || !player.isOnline()) {
                    cleanup();
                    return;
                }

                double totalThisTick = 0.0;
                for (Iterator<DelayedDamage> it = list.iterator(); it.hasNext(); ) {
                    DelayedDamage dd = it.next();
                    double portion = dd.remaining / dd.ticksLeft;
                    totalThisTick += portion;
                    dd.remaining -= portion;
                    dd.ticksLeft--;
                    if (dd.ticksLeft <= 0 || dd.remaining <= 0) {
                        it.remove();
                    }
                }

                if (totalThisTick > 0.0) {
                    double newHealth = Math.max(0.0, player.getHealth() - totalThisTick);
                    player.setHealth(newHealth);
                }
            }

            private void cleanup() {
                BukkitRunnable t = tasks.remove(id);
                if (t != null) t.cancel();
                damageMap.remove(id);
            }
        };

        tasks.put(id, task);
        task.runTaskTimer(plugin, 1L, 1L);
    }

    private static class DelayedDamage {
        double remaining;
        int ticksLeft;

        DelayedDamage(double damage, int ticks) {
            this.remaining = damage;
            this.ticksLeft = ticks;
        }
    }
}
