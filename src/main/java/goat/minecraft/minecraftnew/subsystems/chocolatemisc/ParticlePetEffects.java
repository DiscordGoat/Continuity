package goat.minecraft.minecraftnew.subsystems.chocolatemisc;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ParticlePetEffects implements Listener {

    private final JavaPlugin plugin;

    public ParticlePetEffects(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Trigger particle effects based on pet type and level when a player or their arrow hits a mob.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Check if the damage is caused by a player or a projectile shot by a player
        Player player = null;

        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
            player = (Player) projectile.getShooter();
        }

        if (player == null) return;

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // No pet associated with the player
        if (activePet == null) return;

        // Get the particle and level for the pet
        Particle particle = getParticleForPet(activePet.getName());
        int level = activePet.getLevel();

        // Play particle explosion around the damaged entity
        if (event.getEntity() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) event.getEntity();
            target.getWorld().spawnParticle(
                    particle,
                    target.getLocation().add(0, 1, 0), // Slightly above the target
                    level, // Number of particles is the pet's level
                    0.5, 0.5, 0.5, // Spread around the target
                    0.05 // Speed
            );
        }
    }

    /**
     * Map pet types to specific particles.
     */
    private Particle getParticleForPet(String petName) {
        return switch (petName.toLowerCase()) {
            case "stray" -> Particle.SNOWFLAKE;
            case "vindicator" -> Particle.CRIT_MAGIC;
            case "warden" -> Particle.SCULK_CHARGE;
            case "yeti" -> Particle.SNOWFLAKE;
            case "piglin brute" -> Particle.CRIMSON_SPORE;
            case "guardian" -> Particle.WATER_WAKE;
            case "skeleton" -> Particle.WHITE_ASH;
            case "wither skeleton" -> Particle.ASH;
            case "zombie" -> Particle.HEART;
            case "iron golem" -> Particle.REDSTONE;
            case "drowned" -> Particle.NAUTILUS;
            case "enderman" -> Particle.SPELL_WITCH;
            case "blaze" -> Particle.FLAME;
            case "leviathan" -> Particle.WATER_WAKE;
            case "cat" -> Particle.FIREWORKS_SPARK;
            case "zombie pigman" -> Particle.DRIPPING_HONEY;
            case "axolotl" -> Particle.BUBBLE_POP;
            default -> Particle.EXPLOSION_NORMAL; // Default particle if pet type is not recognized
        };
    }
}
