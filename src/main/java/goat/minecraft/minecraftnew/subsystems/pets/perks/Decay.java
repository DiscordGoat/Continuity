package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class Decay implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;
    private final Map<LivingEntity, String> originalNames = new HashMap<>();

    public Decay(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player player)) return;

        // Check if the entity being damaged is a LivingEntity
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the Decay perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.DECAY)) {
            // Save the target's original name if not already saved
            originalNames.putIfAbsent(target, target.getCustomName() != null ? target.getCustomName() : target.getName());

            // Apply rapid decay via a repeating task
            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (target.isDead() || ticks >= 40) { // Stop after 5 seconds (100 ticks)
                        resetName(target);
                        this.cancel();
                        return;
                    }

                    // Calculate and apply 10% current health as damage
                    double currentHealth = target.getHealth();
                    double damage = currentHealth * 0.1; // 10% of current health
                    target.damage(damage);

                    // Update the target's name to display current damage
                    target.setCustomName(ChatColor.BLACK + String.format("Damage: %.2f", damage));
                    target.setCustomNameVisible(true);

                    // Play a sound to indicate decay
                    target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_HURT, 1.0f, 1.0f);

                    ticks += 5; // Increment ticks (5 ticks per execution)
                }
            }.runTaskTimer(plugin, 0L, 5L); // Runs every 5 ticks (0.25 seconds)
        }
    }

    private void resetName(LivingEntity target) {
        // Restore the entity's original name
        if (originalNames.containsKey(target)) {
            String originalName = originalNames.remove(target);
            target.setCustomName(originalName);
            target.setCustomNameVisible(originalName != null);
        }
    }
}
