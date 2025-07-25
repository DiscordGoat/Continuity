package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Bloodlust system handling stack gains, duration and boss bar display.
 */
public class BloodlustSystem implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, BloodlustData> active = new HashMap<>();
    private final Random random = new Random();

    public BloodlustSystem(JavaPlugin plugin) {
        this.plugin = plugin;
        // update task
        new BukkitRunnable() {
            @Override
            public void run() {
                tickAll();
            }
        }.runTaskTimer(plugin, 0L, 20L); // update each second
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr == null || !mgr.hasTalent(killer, Talent.BLOODLUST)) return;
        BloodlustData data = active.computeIfAbsent(killer.getUniqueId(), k -> new BloodlustData(killer));
        data.addStacks(2);
        int duration = 100; // 5s base
        duration += mgr.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_I) * 80;
        duration += mgr.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_II) * 80;
        duration += mgr.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_III) * 80;
        duration += mgr.getTalentLevel(killer.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_IV) * 80;
        data.start(duration);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        BloodlustData data = active.get(player.getUniqueId());
        if (data == null || !data.isActive()) return;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr != null) {
            int ret = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.RETRIBUTION);
            if (ret > 0 && random.nextDouble() < ret * 0.01) {
                data.addStacks(10);
            }
            int ven = mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.VENGEANCE);
            if (ven > 0 && random.nextDouble() < ven * 0.01) {
                data.extend(ven * 20 * 20); // 20s per level
            }
        }
        // lifesteal
        double healPercent = 0.0;
        if (data.stacks >= 100) healPercent = 0.02;
        else if (data.stacks >= 90) healPercent = 0.015;
        else if (data.stacks >= 70) healPercent = 0.01;
        if (healPercent > 0) {
            double heal = event.getFinalDamage() * healPercent;
            double newHealth = Math.min(player.getHealth() + heal,
                    player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setHealth(newHealth);
        }
        // fury chance at 100 stacks
        if (data.stacks >= 100 && random.nextDouble() < 0.02) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 100, 1));
            player.sendMessage(ChatColor.RED + "Fury unleashed!");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        BloodlustData data = active.remove(player.getUniqueId());
        if (data != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            if (mgr != null && data.stacks >= 100 && mgr.hasTalent(player, Talent.REVENANT)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 2));
            }
            data.end();
        }
    }

    private void tickAll() {
        long now = System.currentTimeMillis();
        active.values().forEach(data -> data.tick(now));
        active.entrySet().removeIf(e -> !e.getValue().isActive());
    }

    private static class BloodlustData {
        private final Player player;
        private final BossBar bar;
        private int stacks = 0;
        private int totalDuration = 0; // ticks
        private long endTime = 0; // ms

        BloodlustData(Player player) {
            this.player = player;
            this.bar = Bukkit.createBossBar(ChatColor.DARK_RED + "Bloodlust", BarColor.RED, BarStyle.SEGMENTED_20);
            this.bar.addPlayer(player);
        }

        void start(int durationTicks) {
            long now = System.currentTimeMillis();
            if (!isActive()) {
                totalDuration = durationTicks;
            } else {
                totalDuration += durationTicks;
            }
            endTime = now + durationTicks * 50L;
            bar.setVisible(true);
        }

        void extend(int ticks) {
            endTime += ticks * 50L;
            totalDuration += ticks;
        }

        void addStacks(int amt) {
            stacks = Math.min(100, stacks + amt);
        }

        void tick(long now) {
            if (!isActive()) {
                end();
                return;
            }
            double remaining = (endTime - now) / 50.0;
            double progress = (totalDuration - remaining) / totalDuration;
            bar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
            applyEffects();
        }

        void applyEffects() {
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            int speedAmp = 0;
            int hasteAmp = -1;
            if (stacks < 10) {
                speedAmp = 0; // Speed I
            } else if (stacks < 30) {
                speedAmp = 0; // still Speed I
            } else if (stacks < 80) {
                speedAmp = 1; // Speed II
            } else {
                speedAmp = 2; // Speed III
            }
            if (stacks >= 20) hasteAmp = 0;
            if (stacks >= 30) hasteAmp = 1;
            if (stacks >= 40) hasteAmp = 2;
            if (stacks >= 50) hasteAmp = 3;
            if (stacks >= 60) hasteAmp = 4;
            if (stacks >= 80) hasteAmp = 5;
            if (stacks >= 90) hasteAmp = 6;
            if (speedAmp >= 0)
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, speedAmp, true, false, false));
            if (hasteAmp >= 0)
                player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, hasteAmp, true, false, false));
        }

        boolean isActive() {
            return System.currentTimeMillis() < endTime;
        }

        void end() {
            bar.removeAll();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            stacks = 0;
        }
    }
}
