package goat.minecraft.minecraftnew.subsystems.combat.bloodlust;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Refactored bloodlust system with:
 *  • Natural‐regen freeze (player‐based, via event cancellation)
 *  • Kill‑activated lifesteal
 *  • Fury on fatal damage at 100 stacks
 */
public class BloodlustManager implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, BloodlustData> dataMap = new HashMap<>();

    public BloodlustManager(JavaPlugin plugin) {
        this.plugin = plugin;
        // register for events
        plugin.getServer()
                .getPluginManager()
                .registerEvents(this, plugin);
    }

    /** On monster kill: +2 stacks, +5s (capped), then lifesteal. */
    public void handleKill(Player player) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null || !mgr.hasTalent(player, Talent.BLOODLUST)) return;

        BloodlustData data = dataMap.computeIfAbsent(player.getUniqueId(),
                k -> new BloodlustData(player));
        data.addStacks(1);

        int maxDur = calculateMaxDuration(player.getUniqueId());
        data.startOrExtend(7, maxDur);

        data.applyKillLifesteal();
    }

    /** On hit: chance‑based extra stacks or duration, plus reduced invuln ticks. */
    public void handleHit(Player player, LivingEntity target) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return;

        int retribution = mgr.getTalentLevel(player.getUniqueId(),
                Skill.COMBAT,
                Talent.RETRIBUTION);
        if (retribution > 0 &&
                ThreadLocalRandom.current().nextDouble() < retribution / 100.0) {
            dataMap.computeIfAbsent(player.getUniqueId(),
                            k -> new BloodlustData(player))
                    .addStacks(10);
        }

        int vengeance = mgr.getTalentLevel(player.getUniqueId(),
                Skill.COMBAT,
                Talent.VENGEANCE);
        if (vengeance > 0 &&
                ThreadLocalRandom.current().nextDouble() < vengeance / 100.0) {
            int maxDur = calculateMaxDuration(player.getUniqueId());
            dataMap.computeIfAbsent(player.getUniqueId(),
                            k -> new BloodlustData(player))
                    .startOrExtend(20, maxDur);
        }

        BloodlustData data = dataMap.get(player.getUniqueId());
        if (data != null) {
            data.applyNoDamageModifier(target);
        }
    }

    /** Prevent death & trigger Fury if stacks == 100. */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        BloodlustData data = dataMap.get(player.getUniqueId());
        if (data == null || data.timeLeft <= 0) return;

        double after = player.getHealth() - event.getFinalDamage();
        if (after <= 0 && data.stacks >= 100) {
            // Fury: cancel kill, heal, clear stacks, lightning AOE
            event.setCancelled(true);
            player.setHealth(player.getMaxHealth());
            data.triggerFury();
        }
    }

    /** Cancel any natural or satiated regen while bloodlust is active. */
    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        BloodlustData data = dataMap.get(player.getUniqueId());
        if (data == null || data.timeLeft <= 0) return;

        RegainReason reason = event.getRegainReason();
        if (reason == RegainReason.REGEN || reason == RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    /** 30s base + 4s per duration‑upgrade level. */
    private int calculateMaxDuration(UUID playerId) {
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null) return 30;
        int levels =
                mgr.getTalentLevel(playerId, Skill.COMBAT, Talent.BLOODLUST_DURATION_I) +
                        mgr.getTalentLevel(playerId, Skill.COMBAT, Talent.BLOODLUST_DURATION_II) +
                        mgr.getTalentLevel(playerId, Skill.COMBAT, Talent.BLOODLUST_DURATION_III) +
                        mgr.getTalentLevel(playerId, Skill.COMBAT, Talent.BLOODLUST_DURATION_IV);
        return 30 + 4 * levels;
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
            updateBar();
        }

        void startOrExtend(int secondsToAdd, int maxDuration) {
            boolean first = (bar == null);
            timeLeft = Math.min(timeLeft + secondsToAdd, maxDuration);

            if (first) {
                bar = Bukkit.createBossBar("", BarColor.RED, BarStyle.SEGMENTED_20);
                bar.addPlayer(player);

                task = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (timeLeft <= 0) {
                            stop();
                            return;
                        }
                        timeLeft--;
                        updateBar();
                    }
                };
                task.runTaskTimer(plugin, 20, 20);
            }

            updateEffects();
            updateBar();
        }

        void applyNoDamageModifier(LivingEntity target) {
            if (timeLeft <= 0) return;
            double factor = getNoDamageFactor();
            int maxTicks = target.getMaximumNoDamageTicks();
            int newTicks = (int) Math.round(maxTicks * factor);
            Bukkit.getScheduler()
                    .runTask(plugin,
                            () -> target.setNoDamageTicks(newTicks));
        }

        /** Heal % of max health on each kill, at certain thresholds. */
        void applyKillLifesteal() {
            double pct = 0;
            if      (stacks >= 100) pct = 0.02;
            else if (stacks >= 90)  pct = 0.015;
            else if (stacks >= 80)  pct = 0.01;
            else if (stacks >= 70)  pct = 0.01;

            if (pct > 0) {
                double heal = player.getMaxHealth() * pct;
                player.setHealth(Math.min(player.getHealth() + heal,
                        player.getMaxHealth()));
            }
        }

        /** Clear stacks and strike all nearby mobs when Fury triggers. */
        void triggerFury() {
            stop();
            player.getWorld()
                    .getNearbyEntities(player.getLocation(), 25, 25, 25)
                    .stream()
                    .filter(e -> e instanceof LivingEntity && !(e instanceof Player))
                    .forEach(e -> {
                        LivingEntity mob = (LivingEntity) e;
                        mob.getWorld().strikeLightningEffect(mob.getLocation());
                        mob.damage(100, player);
                    });
        }

        private double getNoDamageFactor() {
            if      (stacks >= 90) return 0.0;
            else if (stacks >= 80) return 0.1;
            else if (stacks >= 70) return 0.2;
            else if (stacks >= 60) return 0.3;
            else if (stacks >= 50) return 0.4;
            else if (stacks >= 40) return 0.5;
            else if (stacks >= 30) return 0.6;
            else if (stacks >= 20) return 0.75;
            else if (stacks >= 10) return 0.9;
            else                   return 1.0;
        }

        private void updateEffects() {
            if (timeLeft <= 0) {
                stop();
                return;
            }

            int speedAmp = -1, hasteAmp = -1;
            if      (stacks >= 90) { speedAmp = 2; hasteAmp = 6; }
            else if (stacks >= 80) { speedAmp = 1; hasteAmp = 5; }
            else if (stacks >= 60) { speedAmp = 1; hasteAmp = 4; }
            else if (stacks >= 40) { speedAmp = 0; hasteAmp = 3; }
            else if (stacks >= 20) { speedAmp = 0; hasteAmp = 1; }

            if (speedAmp >= 0) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        timeLeft * 20,
                        speedAmp,
                        true, false, true
                ));
            }
            if (hasteAmp >= 0) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE,
                        timeLeft * 20,
                        hasteAmp,
                        true, false, true
                ));
            }
        }

        private void updateBar() {
            if (bar != null) {
                bar.setProgress(Math.min(1.0, stacks / 100.0));
                bar.setTitle(""+ChatColor.DARK_RED + timeLeft + "s");
            }
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
            player.removePotionEffect(PotionEffectType.HASTE);
        }
    }
}
