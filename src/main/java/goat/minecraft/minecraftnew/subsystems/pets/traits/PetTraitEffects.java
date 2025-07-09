package goat.minecraft.minecraftnew.subsystems.pets.traits;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import org.bukkit.Sound;
import goat.minecraft.minecraftnew.other.health.HealthManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles effects for core pet traits like HEALTHY, FAST, STRONG, PRECISE and EVASIVE.
 */
public class PetTraitEffects implements Listener {

    private static PetTraitEffects instance;
    private final PetManager petManager;

    private final Map<UUID, Float> baseSpeed = new HashMap<>();

    public PetTraitEffects(JavaPlugin plugin) {
        instance = this;
        this.petManager = PetManager.getInstance(plugin);
    }

    public static PetTraitEffects getInstance() {
        return instance;
    }

    // ===== Attribute Helpers =====
    private void applyHealthTrait(Player player) {
        HealthManager.getInstance(petManager.getPlugin()).updateHealth(player);
    }

    private void removeHealthTrait(Player player) {
        HealthManager.getInstance(petManager.getPlugin()).updateHealth(player);
    }

    private void applySpeedTrait(Player player) {
        PetManager.Pet active = petManager.getActivePet(player);
        if (active != null && active.getTrait() == PetTrait.FAST) {
            double bonusPercent = active.getTrait().getValueForRarity(active.getTraitRarity());
            UUID id = player.getUniqueId();
            float base = baseSpeed.computeIfAbsent(id, k -> player.getWalkSpeed());
            float newSpeed = (float) (base * (1.0 + bonusPercent / 100.0));
            player.setWalkSpeed(newSpeed);
            return;
        }
        removeSpeedTrait(player);
    }

    private void removeSpeedTrait(Player player) {
        UUID id = player.getUniqueId();
        if (!baseSpeed.containsKey(id)) return;
        player.setWalkSpeed(baseSpeed.get(id));
        baseSpeed.remove(id);
    }

    // ===== Application Methods =====
    public void applyTraits(Player player) {
        applyHealthTrait(player);
        applySpeedTrait(player);
        HealthManager.getInstance(petManager.getPlugin()).updateHealth(player);
    }

    public void removeTraits(Player player) {
        removeHealthTrait(player);
        removeSpeedTrait(player);
        HealthManager.getInstance(petManager.getPlugin()).updateHealth(player);
    }

    // ===== Event Hooks =====

    @EventHandler
    public void onMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        PetManager.Pet active = petManager.getActivePet(player);
        if (active != null && active.getTrait() == PetTrait.STRONG) {
            double bonus = active.getTrait().getValueForRarity(active.getTraitRarity());
            event.setDamage(event.getDamage() * (1.0 + bonus / 100.0));
        }
    }

    @EventHandler
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player player) {
            PetManager.Pet active = petManager.getActivePet(player);
            if (active != null && active.getTrait() == PetTrait.PRECISE) {
                double bonus = active.getTrait().getValueForRarity(active.getTraitRarity());
                event.setDamage(event.getDamage() * (1.0 + bonus / 100.0));
            }
        }
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PetManager.Pet active = petManager.getActivePet(player);
        if (active != null && active.getTrait() == PetTrait.EVASIVE) {
            double chance = active.getTrait().getValueForRarity(active.getTraitRarity());
            if (Math.random() < chance / 100.0) {
                event.setCancelled(true);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
            }
        }
    }
}
