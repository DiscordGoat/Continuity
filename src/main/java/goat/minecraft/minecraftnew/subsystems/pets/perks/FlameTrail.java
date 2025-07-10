package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class FlameTrail implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Queue<Location>> playerTrails = new HashMap<>();
    private final Map<UUID, Long> lastExplosionTime = new HashMap<>();
    private static final int MAX_TRAIL_LENGTH = 8;
    private static final long EXPLOSION_COOLDOWN = 1000; // 1 second between explosions

    public FlameTrail(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.FLAME_TRAIL)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();
        
        // Only create trail if player actually moved a meaningful distance
        if (to == null || from.distanceSquared(to) < 1.0) {
            return;
        }

        // Check explosion cooldown
        long currentTime = System.currentTimeMillis();
        long lastExplosion = lastExplosionTime.getOrDefault(playerId, 0L);
        if (currentTime - lastExplosion < EXPLOSION_COOLDOWN) {
            return;
        }

        // Get or create trail for this player
        Queue<Location> trail = playerTrails.computeIfAbsent(playerId, k -> new LinkedList<>());

        // Add current location to trail
        Location trailLocation = to.clone();
        trail.offer(trailLocation);

        // Calculate flame intensity based on pet level
        int petLevel = activePet.getLevel();
        
        // Create explosive fire effect
        createFlameExplosion(trailLocation, player, petLevel);
        lastExplosionTime.put(playerId, currentTime);

        // Remove old trail locations if trail is too long
        while (trail.size() > MAX_TRAIL_LENGTH) {
            trail.poll();
        }
    }

    private void createFlameExplosion(Location location, Player owner, int petLevel) {
        // Create visual explosion effect
        spawnExplosionParticles(location, petLevel);
        
        // Damage nearby entities in 8 block radius
        damageNearbyEntities(location, owner, petLevel);
        
        // Play explosion sound
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.5f);
    }

    private void spawnExplosionParticles(Location location, int petLevel) {
        int particleCount = Math.min(10 + (petLevel / 10), 30); // More particles at higher levels
        
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 5) { // Run for only 5 ticks
                    cancel();
                    return;
                }
                
                // Create large flame explosion effect
                location.getWorld().spawnParticle(Particle.FLAME, 
                    location.clone().add(0, 1, 0), 
                    particleCount, 4.0, 2.0, 4.0, 0.1);
                
                // Add some lava particles for extra effect
                location.getWorld().spawnParticle(Particle.LAVA, 
                    location.clone().add(0, 1, 0), 
                    particleCount / 2, 4.0, 2.0, 4.0, 0.0);
                
                // Add explosion particles
                location.getWorld().spawnParticle(Particle.EXPLOSION,
                    location.clone().add(0, 1, 0), 
                    2, 2.0, 1.0, 2.0, 0.0);
                
                // Add smoke for lingering effect
                location.getWorld().spawnParticle(Particle.SMOKE,
                    location.clone().add(0, 2, 0), 
                    particleCount / 3, 3.0, 1.5, 3.0, 0.05);
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // Every tick for smooth effect
    }

    private void damageNearbyEntities(Location location, Player owner, int petLevel) {
        double damage = 3.0 + (petLevel * 0.08); // Base 3 damage + 0.08 per level (higher than before)
        double radius = 8.0; // 8 block radius as requested

        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != owner && !(entity instanceof Player)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                
                // Calculate distance-based damage (closer = more damage)
                double distance = entity.getLocation().distance(location);
                double damageMultiplier = Math.max(0.3, 1.0 - (distance / radius)); // Minimum 30% damage at max range
                double finalDamage = damage * damageMultiplier;
                
                // Set entity on fire for a duration based on pet level
                entity.setFireTicks(60 + (petLevel / 3)); // 3 seconds base + level scaling
                
                // Deal damage
                livingEntity.damage(finalDamage, owner);
                
                // Visual feedback for damaged entities
                entity.getWorld().spawnParticle(Particle.FLAME, 
                    entity.getLocation().add(0, 1, 0), 
                    3, 0.3, 0.5, 0.3, 0.02);
            }
        }
    }
}