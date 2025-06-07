package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom fire damage handler for non-boss monsters.
 * Cancels vanilla fire damage and applies a stacking "Fire" mechanic.
 */
public class FireDamageHandler implements Listener {

    private final JavaPlugin plugin;
    private final DamageNotificationService notificationService;
    private final Map<UUID, Integer> fireLevels = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> solarFuryTargets = new ConcurrentHashMap<>();

    public FireDamageHandler(JavaPlugin plugin, DamageNotificationService notificationService) {
        this.plugin = plugin;
        this.notificationService = notificationService;
    }

    @EventHandler
    public void onFireDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity instanceof Monster) || isBoss(entity)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FIRE &&
            cause != EntityDamageEvent.DamageCause.FIRE_TICK &&
            cause != EntityDamageEvent.DamageCause.LAVA &&
            cause != EntityDamageEvent.DamageCause.HOT_FLOOR) {
            return;
        }

        event.setCancelled(true);
        entity.setFireTicks(0);

        addFire(entity, 1);
    }

    @EventHandler
    public void onFireAspectHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (!(target instanceof Monster) || isBoss(target)) return;

        int level = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.FIRE_ASPECT);
        if (level > 0) {
            int amount = level * 5;
            if (PotionManager.isActive("Potion of Solar Fury", player)) {
                amount *= 2;
                solarFuryTargets.put(target.getUniqueId(), true);
                sendActionBar(player, ChatColor.GOLD + "Solar Fury: " + ChatColor.RED + "2x" + ChatColor.GOLD + " Fire Level!");

            }
            addFire(target, amount);
        }
    }

    private void addFire(LivingEntity entity, int amount) {
        UUID id = entity.getUniqueId();
        int newLevel = fireLevels.getOrDefault(id, 0) + amount;
        fireLevels.put(id, newLevel);

        spawnFireParticles(entity.getLocation(), newLevel);
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

                int level = fireLevels.getOrDefault(id, 0);
                if (level <= 0) {
                    cleanup();
                    return;
                }

                if (level >= 100) {
                    entity.setHealth(Math.max(0.1, entity.getHealth() / 2.0));
                    spawnExplosion(entity.getLocation());
                    fireLevels.put(id, 20);
                    spreadFire(entity);
                    level = 20;
                }

                double damage = level / 2.0;
                double newHealth = Math.max(0.0, entity.getHealth() - damage);
                entity.setHealth(newHealth);
                if(newHealth <= 0.0 && entity.getWorld() != null) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), ItemRegistry.getVerdantRelicSunflareSeed());
                }
                if (entity.getWorld() != null) {
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_BLAZE_HURT, 1.0f, 1.0f);
                }
                boolean boosted = solarFuryTargets.getOrDefault(id, false);
                notificationService.createFireDamageIndicator(entity.getLocation(), damage, level, boosted);
                spawnFireParticles(entity.getLocation(), level);

                fireLevels.put(id, level - 1);
            }

            private void cleanup() {
                BukkitRunnable t = tasks.remove(id);
                if (t != null) t.cancel();
                fireLevels.remove(id);
                solarFuryTargets.remove(id);
            }
        };

        tasks.put(id, task);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnFireParticles(Location location, int count) {
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.FLAME, location, count, 0.6, 1.0, 0.6, 0.02);
            location.getWorld().spawnParticle(Particle.FLAME, location, count, 0.3, 0.5, 0.3, 0.01);
        }
    }

    private void spawnExplosion(Location location) {
        if (location.getWorld() != null) {
            location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 2);
            location.getWorld().spawnParticle(Particle.FLAME, location, 100, 1, 1, 1, 0.1);
        }
    }

    private void spreadFire(LivingEntity source) {
        if (source.getWorld() == null) return;
        for (Entity e : source.getWorld().getNearbyEntities(source.getLocation(), 10, 10, 10)) {
            if (e instanceof LivingEntity le && le instanceof Monster && !isBoss(le) && !le.equals(source)) {
                addFire(le, 20);
            }
        }
    }

    private boolean isBoss(Entity entity) {
        return entity instanceof Boss ||
                entity.getType().name().contains("DRAGON") ||
                entity.getType().name().contains("WITHER") ||
                entity.getType().name().contains("ELDER_GUARDIAN");
    }

    private void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
