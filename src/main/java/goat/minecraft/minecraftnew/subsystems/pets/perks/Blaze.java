package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Blaze implements Listener {

    private final PetManager petManager;

    public Blaze(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        // Ensure the damager is a player
        if (!(event.getDamager() instanceof Player player)) return;

        // Ensure the entity being damaged is a living entity
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the Blaze perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BLAZE)) {
            // Set the target on fire and apply rapid fire damage
            target.setFireTicks(100); // Set the entity on fire for 5 seconds
            int level = activePet.getLevel();
            int damagePerTick = 2 + (level / 10); // Damage scales with pet level

            // Schedule rapid fire damage ticks
            new BukkitRunnable() {
                private int ticks = 10; // Apply damage for 10 ticks (1 second)
                @Override
                public void run() {
                    if (ticks <= 0 || target.isDead() || !target.isValid()) {
                        this.cancel();
                        return;
                    }

                    // Apply fire damage
                    target.damage(damagePerTick, player);

                    // Set the custom name to display the damage
                    target.setCustomName(ChatColor.GOLD + String.valueOf(damagePerTick) + ChatColor.RESET);
                    target.setCustomNameVisible(true);

                    // Play fire sound effect
                    target.getWorld().playSound(target.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 1.0f);

                    // Decrease tick counter
                    ticks--;
                }
            }.runTaskTimer(petManager.getPlugin(), 0L, 2L); // Run every 2 ticks (fast damage)

            // Reset the custom name after fire effect ends
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (target.isValid()) {
                        target.setCustomName(null);
                        target.setCustomNameVisible(false);
                    }
                }
            }.runTaskLater(petManager.getPlugin(), 20L * 5); // Reset after 5 seconds
        }
    }
}
