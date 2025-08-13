package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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

    // Per-player, per-effect last cure time
    private final Map<UUID, Map<PotionEffectType, Long>> lastCureTimes = new HashMap<>();

    // Default per-effect cooldowns (ms). Wither is handled specially below (no cooldown).
    private static final long DEFAULT_COOLDOWN = 60_000L; // 1 minute
    private static final Map<PotionEffectType, Long> EFFECT_COOLDOWNS = Map.ofEntries(
            Map.entry(PotionEffectType.BLINDNESS,       DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.NAUSEA,          DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.HUNGER,          DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.POISON,          DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.SLOWNESS,        DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.MINING_FATIGUE,  DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.DARKNESS,        DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.UNLUCK,          DEFAULT_COOLDOWN),
            Map.entry(PotionEffectType.WEAKNESS,        DEFAULT_COOLDOWN)
            // WITHER intentionally omitted here (no cooldown)
    );

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.DARKNESS,
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
                long now = System.currentTimeMillis();

                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    PetManager.Pet activePet = petManager.getActivePet(player);
                    if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.ANTIDOTE)) continue;

                    int talent = 0;
                    if (SkillTreeManager.getInstance() != null) {
                        talent = SkillTreeManager.getInstance()
                                .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.ANTIDOTE);
                    }

                    if (removeNegativeEffectsWithPerEffectCooldown(player, now, talent)) {
                        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 1.0F, 1.0F);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private boolean removeNegativeEffectsWithPerEffectCooldown(Player player, long now, int talentLevel) {
        // Get or create the per-effect map for this player
        Map<PotionEffectType, Long> perEffectMap =
                lastCureTimes.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        boolean removedAny = false;

        // Snapshot active negative effects
        List<PotionEffect> activeNegative = player.getActivePotionEffects().stream()
                .filter(pe -> NEGATIVE_EFFECTS.contains(pe.getType()))
                .toList();

        for (PotionEffect effect : activeNegative) {
            PotionEffectType type = effect.getType();

            // WITHER: never has a cooldown — always remove
            if (type.equals(PotionEffectType.WITHER)) {
                player.removePotionEffect(type);
                // (Optional) we can still stamp a time if you want to record it, but it has no gating effect.
                perEffectMap.put(type, now);
                removedAny = true;
                continue;
            }

            // If the talent is active (>0), bypass cooldowns entirely.
            if (talentLevel > 0) {
                player.removePotionEffect(type);
                perEffectMap.put(type, now);
                removedAny = true;
                continue;
            }

            // Check the per-effect cooldown
            long effectCooldown = EFFECT_COOLDOWNS.getOrDefault(type, DEFAULT_COOLDOWN);
            long last = perEffectMap.getOrDefault(type, 0L);

            if (now - last >= effectCooldown) {
                player.removePotionEffect(type);
                perEffectMap.put(type, now);
                removedAny = true;
            }
        }

        return removedAny;
    }
}
