package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerDataManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BerserkersRage implements Listener {

    private final PlayerDataManager playerData;
    private final JavaPlugin plugin;
    // Map to track players' health before damage
    private final Map<UUID, Double> playerHealthMap = new HashMap<>();
    // Set cooldown time (in milliseconds)
    private final long COOLDOWN_TIME = 30000; // 30 seconds cooldown
    // Map to track when each player last activated the effect
    private final Map<UUID, Long> lastActivationTime = new HashMap<>();

    public BerserkersRage(JavaPlugin plugin, PlayerDataManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // Only apply to players
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UUID playerId = player.getUniqueId();

        // Check if player has the perk
        if (!playerData.hasPerk(playerId, "Berserkers Rage")) return;

        // Check if this is the first damage event for this player
        if (!playerHealthMap.containsKey(playerId)) {
            playerHealthMap.put(playerId, player.getHealth());
            return;
        }

        // Get player's health before this damage
        double previousHealth = playerHealthMap.get(playerId);

        // Calculate max health using the attribute system
        // This properly handles modified health from attributes that can go up to 40
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthThreshold = maxHealth * 0.3;

        // Calculate how much damage this event will cause
        double damageTaken = Math.min(previousHealth, event.getFinalDamage());

        // Check if the damage is at least 30% of the player's max health
        if (damageTaken >= healthThreshold) {
            // Check if the cooldown has expired
            long currentTime = System.currentTimeMillis();
            Long lastActivation = lastActivationTime.get(playerId);

            if (lastActivation == null || (currentTime - lastActivation) >= COOLDOWN_TIME) {
                // Activate Berserker's Rage effect
                activateBerserkersRage(player);

                // Update last activation time
                lastActivationTime.put(playerId, currentTime);

                // Send message to player
                player.sendMessage("§c§lBERSERKER'S RAGE ACTIVATED!");
            }
        }

        // Update player's health in our tracking map for next time
        // We subtract the damage from current health (but not below 0)
        playerHealthMap.put(playerId, Math.max(0, player.getHealth() - event.getFinalDamage()));
    }

    private void activateBerserkersRage(Player player) {
        // Apply Strength V effect for 5 seconds
        // Strength V gives +250% attack damage (very strong effect!)
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INCREASE_DAMAGE, // Strength effect
                5 * 20, // 5 seconds (in ticks)
                4, // Strength V (amplifier is 0-indexed, so 4 = level 5)
                false, // Don't show particles
                true, // Show icon
                true // Show particles
        ));

        // Visual and sound effects
        player.getWorld().playSound(player.getLocation(), "entity.ender_dragon.growl", 1.0f, 1.0f);
        player.getWorld().spawnParticle(org.bukkit.Particle.LAVA, player.getLocation(), 50, 0.5, 1, 0.5, 0.1);
    }
}