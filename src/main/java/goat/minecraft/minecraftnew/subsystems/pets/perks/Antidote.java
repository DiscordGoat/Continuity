package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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
    private final Map<UUID, Long> lastCureTime = new HashMap<>();
    private static final long ANTIDOTE_COOLDOWN = 60 * 1000; // 1 minute cooldown

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA, // Nausea
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE, // Mining Fatigue
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
                long currentTime = System.currentTimeMillis();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    // Check if the player has an active pet with the Antidote perk
                    PetManager.Pet activePet = petManager.getActivePet(player);
                    int talent = 0;
                    if (SkillTreeManager.getInstance() != null) {
                        talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.ANTIDOTE);
                    }
                    if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.ANTIDOTE)) {
                        if (talent <= 0) continue;
                    }

                    UUID playerId = player.getUniqueId();
                    long lastCure = lastCureTime.getOrDefault(playerId, 0L);
                    long cooldown = talent > 0 ? 0 : ANTIDOTE_COOLDOWN;
                    if (currentTime - lastCure < cooldown) {
                        continue; // Cooldown hasn't expired
                    }

                    if (removeAllNegativePotionEffects(player)) {
                        lastCureTime.put(playerId, currentTime); // Set cooldown
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Initial delay 0, repeat every 20 ticks (1 second)
    }

    private boolean removeAllNegativePotionEffects(Player player) {
        // Collect active negative potion effects
        List<PotionEffect> activeNegativeEffects = player.getActivePotionEffects().stream()
                .filter(effect -> NEGATIVE_EFFECTS.contains(effect.getType()))
                .toList();

        // If there are no negative effects, exit
        if (activeNegativeEffects.isEmpty()) {
            return false;
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
        return removedAny;
    }
}