package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import org.bukkit.entity.Boss;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LordOfThunder implements Listener {

    private final PlayerDataManager playerData;
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final long COOLDOWN_TIME = 1000 * 60 * 8; // 45 seconds cooldown
    private final Map<UUID, Long> lastActivationTime = new HashMap<>();
    private final double PROC_CHANCE = 0.05; // 5% chance

    public LordOfThunder(JavaPlugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check if the attacker is a player
        if (!(event.getDamager() instanceof Player)) return;

        // Check if the entity being hit is a living entity
        if (!(event.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) event.getDamager();
        LivingEntity target = (LivingEntity) event.getEntity();
        UUID playerId = player.getUniqueId();

        // Check if player has the perk
        if (!playerData.hasPerk(playerId, "Lord of Thunder")) return;

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long lastActivation = lastActivationTime.get(playerId);

        if (lastActivation != null && (currentTime - lastActivation) < COOLDOWN_TIME) {
            // Still on cooldown
            return;
        }

        // Check if this hit procs the thunder effect (5% chance)
        if (random.nextDouble() <= PROC_CHANCE) {
            // Check if the target is a boss (we don't affect bosses)
            boolean isBoss = isBossEntity(target);

            if (!isBoss) {
                // Play thunder sound to warn player to move away
                target.getWorld().playSound(target.getLocation(), "entity.lightning_bolt.thunder", 1.0f, 1.0f);

                // Add a visual indicator above the target
                target.getWorld().spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME,
                        target.getLocation().add(0, 2, 0), 30, 0.2, 0.2, 0.2, 0.05);

                // Schedule the lightning strike in 2 seconds
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Only strike if target is still alive
                        if (target.isValid() && !target.isDead()) {
                            // Visual lightning effect
                            target.getWorld().strikeLightningEffect(target.getLocation());

                            // Set health to 1
                            target.setHealth(1.0);

                            // Add slowness effect (Slowness II for 5 seconds)
                            target.addPotionEffect(new PotionEffect(
                                    PotionEffectType.SLOW,
                                    5 * 20, // 5 seconds
                                    1,      // Level II
                                    false,  // No ambient
                                    true,   // Show particles
                                    true    // Show icon
                            ));

                            // Additional visual effects
                            target.getWorld().spawnParticle(org.bukkit.Particle.FLASH,
                                    target.getLocation(), 1, 0, 0, 0, 0);
                        }
                    }
                }.runTaskLater(plugin, 2 * 20); // 2 seconds (40 ticks)

                // Update last activation time
                lastActivationTime.put(playerId, currentTime);

                // Notify player
                player.sendMessage("§e§lLORD OF THUNDER! §r§eRun before lightning strikes your enemy!");
            }
        }
    }

    private boolean isBossEntity(LivingEntity entity) {
        // Check if the entity is a boss-type entity
        return entity instanceof Boss ||
                entity.getType().name().contains("DRAGON") ||
                entity.getType().name().contains("WITHER") ||
                entity.getType().name().contains("ELDER_GUARDIAN");
    }
}