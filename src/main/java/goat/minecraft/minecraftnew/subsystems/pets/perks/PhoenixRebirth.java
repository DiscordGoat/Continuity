package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PhoenixRebirth implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Long> lastRebirthTime = new HashMap<>();
    private static final long REBIRTH_COOLDOWN = 300000; // 5 minutes in milliseconds

    public PhoenixRebirth(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.PHOENIX_REBIRTH)) {
            return;
        }

        // Check if this damage would kill the player
        double finalDamage = event.getFinalDamage();
        double currentHealth = player.getHealth();
        
        if (currentHealth - finalDamage > 0) {
            return; // Player won't die from this damage
        }

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        long lastRebirth = lastRebirthTime.getOrDefault(playerId, 0L);
        if (currentTime - lastRebirth < REBIRTH_COOLDOWN) {
            long timeLeft = (REBIRTH_COOLDOWN - (currentTime - lastRebirth)) / 1000;
            player.sendMessage(ChatColor.RED + "Phoenix Rebirth is on cooldown for " + timeLeft + " more seconds!");
            return; // Let the player take the damage
        }

        // Cancel the damage that would kill the player
        event.setCancelled(true);
        lastRebirthTime.put(playerId, currentTime);

        // Calculate rebirth health based on pet level (25% base + 0.5% per level)
        int petLevel = activePet.getLevel();
        double rebirthHealthPercent = 0.25 + (petLevel * 0.005);
        double rebirthHealth = Math.min(player.getMaxHealth() * rebirthHealthPercent, player.getMaxHealth());

        // Restore health
        player.setHealth(rebirthHealth);

        // Add fire immunity and effects
        int immunityDuration = 10 + (petLevel / 10); // 10 seconds base + 1 second per 10 levels
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, immunityDuration * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1)); // 3 seconds of regen

        // Visual and audio effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_DEATH, 1.0f, 1.2f);
        player.sendMessage(ChatColor.GOLD + "Your Phoenix has granted you rebirth! Rising from the ashes...");

        // Spawn rebirth particles
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= 60) { // 3 seconds of particles
                    cancel();
                    return;
                }
                
                // Create phoenix rebirth effect with flame particles
                player.getWorld().spawnParticle(Particle.FLAME, 
                    player.getLocation().add(0, 1, 0), 
                    15, 1.0, 1.5, 1.0, 0.1);
                player.getWorld().spawnParticle(Particle.LAVA, 
                    player.getLocation().add(0, 1, 0), 
                    5, 0.5, 1.0, 0.5, 0.0);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}