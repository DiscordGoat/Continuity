package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the Bloodlust mechanic. Players gain stacks and duration when
 * killing monsters. Stacks grant temporary buffs and a segmented boss bar
 * shows current progress.
 */
public class BloodlustManager {
    private static BloodlustManager instance;
    private final JavaPlugin plugin;

    private static class BloodlustData {
        int stacks;
        long endTime;
        BossBar bar;
    }

    private final Map<UUID, BloodlustData> data = new HashMap<>();

    private BloodlustManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    /**
     * Initializes and returns the singleton instance.
     */
    public static synchronized BloodlustManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BloodlustManager(plugin);
        }
        return instance;
    }

    /**
     * Returns the singleton instance if it was already initialized.
     */
    public static BloodlustManager getInstance() {
        return instance;
    }

    /**
     * Adds stacks and extends duration when a monster is slain.
     */
    public void addKill(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm == null || !stm.hasTalent(player, Talent.BLOODLUST)) return;
        addStacks(player, 2);
        int extra = getDurationBonus(player);
        extendDuration(player, 5 + extra);
    }

    /**
     * Handles hit-based talents like Retribution and Vengeance.
     */
    public void onHit(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm == null) return;
        int retribution = stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.RETRIBUTION);
        if (retribution > 0 && Math.random() < retribution / 100.0) {
            addStacks(player, 10);
        }
        int vengeance = stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.VENGEANCE);
        if (vengeance > 0 && Math.random() < vengeance / 100.0) {
            extendDuration(player, 20);
        }
    }

    private int getDurationBonus(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        int total = 0;
        total += stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_I) * 4;
        total += stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_II) * 4;
        total += stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_III) * 4;
        total += stm.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_IV) * 4;
        return total;
    }

    private BloodlustData getData(Player player) {
        return data.computeIfAbsent(player.getUniqueId(), k -> new BloodlustData());
    }

    private void addStacks(Player player, int amount) {
        if (amount <= 0) return;
        BloodlustData d = getData(player);
        d.stacks = Math.min(100, d.stacks + amount);
        ensureBar(player, d);
    }

    private void extendDuration(Player player, int seconds) {
        if (seconds <= 0) return;
        BloodlustData d = getData(player);
        ensureBar(player, d);
        long newEnd = System.currentTimeMillis() + seconds * 1000L;
        if (newEnd > d.endTime) {
            d.endTime = newEnd;
        }
    }

    private void ensureBar(Player player, BloodlustData d) {
        if (d.bar == null) {
            d.bar = Bukkit.createBossBar("Bloodlust", BarColor.RED, BarStyle.SEGMENTED_20);
            d.bar.addPlayer(player);
        } else if (!d.bar.getPlayers().contains(player)) {
            d.bar.addPlayer(player);
        }
    }

    private void remove(UUID id) {
        BloodlustData d = data.remove(id);
        if (d != null && d.bar != null) {
            d.bar.removeAll();
        }
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (UUID id : data.keySet().toArray(new UUID[0])) {
                    Player p = Bukkit.getPlayer(id);
                    if (p == null) {
                        remove(id);
                        continue;
                    }
                    BloodlustData d = data.get(id);
                    if (now > d.endTime) {
                        remove(id);
                        continue;
                    }
                    if (d.bar != null) {
                        d.bar.setProgress(Math.max(0.0, Math.min(1.0, d.stacks / 100.0)));
                    }
                    applyEffects(p, d.stacks);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // update every second
    }

    private void applyEffects(Player player, int stacks) {
        if (stacks >= 60) {
            applyEffect(player, PotionEffectType.SPEED, 1);
            applyEffect(player, PotionEffectType.FAST_DIGGING, 2);
        } else if (stacks >= 30) {
            applyEffect(player, PotionEffectType.SPEED, 0);
            applyEffect(player, PotionEffectType.FAST_DIGGING, 1);
        } else if (stacks >= 10) {
            applyEffect(player, PotionEffectType.SPEED, 0);
        }
    }

    private void applyEffect(Player p, PotionEffectType type, int amplifier) {
        p.addPotionEffect(new PotionEffect(type, 40, amplifier, true, false, true));
    }
}
