package goat.minecraft.minecraftnew.subsystems.combat.bloodlust;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple bloodlust system that tracks stacks and duration per player.
 * <p>
 * This is a lightweight implementation meant to demonstrate how bloodlust
 * could hook into the talent system. The effect values roughly follow the
 * design document but are not fully featured.
 */
public class BloodlustManager {

    private final JavaPlugin plugin;
    private final Map<UUID, BloodlustData> dataMap = new HashMap<>();

    public BloodlustManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Adds stacks when a monster is killed. Also starts bloodlust if the
     * player has the Bloodlust talent.
     */
    public void handleKill(Player player) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;

        int baseDuration = 0;
        if (mgr.hasTalent(player, Talent.BLOODLUST)) {
            baseDuration = 5;
        }

        // Bonus duration from talents
        baseDuration += 4 * mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_I);
        baseDuration += 4 * mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_II);
        baseDuration += 4 * mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_III);
        baseDuration += 4 * mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_IV);

        if (baseDuration > 0) {
            addStacks(player, 2);
            startOrExtend(player, baseDuration);
        }
    }

    /**
     * Chance based stack and duration bonuses from Retribution and Vengeance.
     */
    public void handleHit(Player player) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;

        int retributionLvl = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.RETRIBUTION);
        if (retributionLvl > 0 && ThreadLocalRandom.current().nextDouble() < retributionLvl / 100.0) {
            addStacks(player, 10);
        }

        int vengeanceLvl = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.VENGEANCE);
        if (vengeanceLvl > 0 && ThreadLocalRandom.current().nextDouble() < vengeanceLvl / 100.0) {
            startOrExtend(player, 20);
        }
    }

    private void addStacks(Player player, int amount) {
        BloodlustData data = dataMap.computeIfAbsent(player.getUniqueId(), k -> new BloodlustData(player));
        data.addStacks(amount);
    }

    private void startOrExtend(Player player, int seconds) {
        BloodlustData data = dataMap.computeIfAbsent(player.getUniqueId(), k -> new BloodlustData(player));
        data.startOrExtend(seconds);
    }

    private class BloodlustData {
        private final Player player;
        private int stacks = 0;
        private int timeLeft = 0;
        private BossBar bar;
        private BukkitRunnable task;

        BloodlustData(Player player) {
            this.player = player;
        }

        void addStacks(int amount) {
            stacks = Math.min(stacks + amount, 100);
            updateEffects();
        }

        void startOrExtend(int seconds) {
            timeLeft = Math.min(timeLeft + seconds, 300); // cap at 5min
            if (bar == null) {
                bar = Bukkit.createBossBar(ChatColor.DARK_RED + "Bloodlust", BarColor.RED, BarStyle.SEGMENTED_20);
                bar.addPlayer(player);
            }
            if (task == null) {
                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (timeLeft <= 0) {
                            stop();
                            return;
                        }
                        timeLeft--;
                        bar.setProgress(Math.min(1.0, timeLeft / 100.0));
                    }
                };
                task.runTaskTimer(plugin, 20, 20);
            }
            updateEffects();
        }

        private void stop() {
            if (task != null) {
                task.cancel();
                task = null;
            }
            if (bar != null) {
                bar.removeAll();
                bar = null;
            }
            stacks = 0;
            timeLeft = 0;
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        }

        private void updateEffects() {
            if (timeLeft <= 0) {
                stop();
                return;
            }
            if (bar != null) {
                bar.setProgress(Math.min(1.0, timeLeft / 100.0));
            }
            int speedAmplifier = 0;
            int hasteAmplifier = -1;
            if (stacks >= 90) {
                speedAmplifier = 2;
                hasteAmplifier = 6;
            } else if (stacks >= 80) {
                speedAmplifier = 1;
                hasteAmplifier = 5;
            } else if (stacks >= 60) {
                speedAmplifier = 1;
                hasteAmplifier = 4;
            } else if (stacks >= 40) {
                speedAmplifier = 0;
                hasteAmplifier = 3;
            } else if (stacks >= 20) {
                speedAmplifier = 0;
                hasteAmplifier = 1;
            }
            if (speedAmplifier >= 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, timeLeft * 20, speedAmplifier, true, false, true));
            }
            if (hasteAmplifier >= 0) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, timeLeft * 20, hasteAmplifier, true, false, true));
            }
        }
    }
}
