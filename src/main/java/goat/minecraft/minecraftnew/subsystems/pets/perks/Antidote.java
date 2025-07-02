package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Antidote implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION, // Nausea
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING, // Mining Fatigue
            PotionEffectType.DARKNESS, // Mining Fatigue
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    );

    public Antidote(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
        startAntidoteTask();
    }

    private void startAntidoteTask() {
        // Run every 20 ticks (1 second)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    // Check if the player has an active pet with the Antidote perk
                    PetManager.Pet activePet = petManager.getActivePet(player);
                    if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.ANTIDOTE)) {
                        continue;
                    }

                    removeAllNegativePotionEffects(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Initial delay 0, repeat every 20 ticks (1 second)
    }

    private void removeAllNegativePotionEffects(Player player) {
        // Collect active negative potion effects
        List<PotionEffect> activeNegativeEffects = player.getActivePotionEffects().stream()
                .filter(effect -> NEGATIVE_EFFECTS.contains(effect.getType()))
                .toList();

        // If there are no negative effects, exit
        if (activeNegativeEffects.isEmpty()) {
            return;
        }

        // Remove all negative effects
        boolean removedAny = false;
        for (PotionEffect effect : activeNegativeEffects) {
            player.removePotionEffect(effect.getType());
            removedAny = true;
        }

        // Notify the player only if effects were removed
        if (removedAny) {
            player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1.0F, 1.0F);
        }
    }
}