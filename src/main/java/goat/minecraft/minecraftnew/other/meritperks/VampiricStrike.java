package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Sound;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class VampiricStrike implements Listener {

    private final PlayerMeritManager playerData;
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final long COOLDOWN_TIME = 1000; // 1 seconds cooldown (1 minute)
    private final Map<UUID, Long> lastActivationTime = new HashMap<>();
    private final double PROC_CHANCE = 0.05; // 5% chance

    public VampiricStrike(JavaPlugin plugin, PlayerMeritManager playerData) {
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
        if (!playerData.hasPerk(playerId, "Vampiric Strike")) return;

        // Check cooldown
        long currentTime = System.currentTimeMillis();
        Long lastActivation = lastActivationTime.get(playerId);

        if (lastActivation != null && (currentTime - lastActivation) < COOLDOWN_TIME) {
            // Still on cooldown
            return;
        }

        // Check if this hit procs the vampiric effect (5% chance)
        if (random.nextDouble() <= PROC_CHANCE) {
            // Check if the target is a boss
            boolean isBoss = isBossEntity(target);

            // Heal player to full health
            double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
            player.setHealth(maxHealth);

            // If not a boss, instantly kill the entity
            if (!isBoss) {
                // Some entities might need special handling to avoid game issues
                if (isSpecialEntity(target)) {
                    // Deal massive damage instead of insta-kill for special entities
                    target.damage(100.0);
                } else {
                    target.setHealth(1);
                }
            } else {
                // Deal significant damage to bosses
                target.damage(target.getMaxHealth() * 0.05); // 5% of boss's max health
            }

            // Apply visual effects
            player.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.5f);
            player.getWorld().spawnParticle(org.bukkit.Particle.REDSTONE, target.getLocation().add(0, 1, 0),
                    100, 0.5, 0.5, 0.5, 0.1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));

            // Update last activation time
            lastActivationTime.put(playerId, currentTime);

            // Notify player
            player.sendMessage("§4§lVAMPIRIC STRIKE! §r§4You drain the life force of your enemy!");
        }
    }

    private boolean isBossEntity(Entity entity) {
        // Check if the entity is a boss-type entity
        return entity instanceof Boss ||
                entity.getType().name().contains("DRAGON") ||
                entity.getType().name().contains("WITHER") ||
                entity.getType().name().contains("ELDER_GUARDIAN");
    }

    private boolean isSpecialEntity(Entity entity) {
        // Some entities might cause issues if instantly killed
        // For example, instantly killing an Enderdragon would skip animations
        String entityType = entity.getType().name();
        return entityType.contains("ENDER_CRYSTAL") ||
                entityType.contains("ARMOR_STAND") ||
                entityType.contains("ITEM_FRAME") ||
                entityType.contains("PAINTING");
    }
}