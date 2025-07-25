package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Handles the Bloodlust mechanic and BossBar display.
 */
public class BloodlustManager implements Listener {

    private static BloodlustManager instance;

    private final JavaPlugin plugin;
    private final Map<UUID, BloodlustData> active = new HashMap<>();

    private BloodlustManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    /** Initialize the manager. */
    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BloodlustManager(plugin);
        }
    }

    /** Shutdown and clear all bars. */
    public static void shutdown() {
        if (instance != null) {
            for (BloodlustData data : instance.active.values()) {
                data.bar.removeAll();
            }
            instance.active.clear();
            instance = null;
        }
    }

    /** Called when a player kills a monster. */
    public static void onKill(Player player) {
        if (instance == null) return;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null || !manager.hasTalent(player, Talent.BLOODLUST)) return;
        int extra = 0;
        extra += manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_I) * 80;
        extra += manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_II) * 80;
        extra += manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_III) * 80;
        extra += manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.BLOODLUST_DURATION_IV) * 80;
        instance.startOrExtend(player, 100 + extra, 2);
    }

    /** Called when a player hits an enemy. */
    public static void onHit(Player player) {
        if (instance == null) return;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager == null) return;
        int ret = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.RETRIBUTION);
        if (ret > 0 && Math.random() < ret * 0.01) {
            instance.addStacks(player, 10);
        }
        int ven = manager.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.VENGEANCE);
        if (ven > 0 && Math.random() < ven * 0.01) {
            instance.extendDuration(player, 20 * 20); // 20 seconds
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BloodlustData data = active.remove(event.getPlayer().getUniqueId());
        if (data != null) {
            data.bar.removeAll();
        }
    }

    private void startOrExtend(Player player, int ticks, int stacks) {
        BloodlustData data = active.get(player.getUniqueId());
        if (data == null) {
            BossBar bar = Bukkit.createBossBar("§cBloodlust", BarColor.RED, BarStyle.SEGMENTED_20);
            bar.addPlayer(player);
            data = new BloodlustData(bar);
            active.put(player.getUniqueId(), data);
        }
        data.stacks = Math.min(100, data.stacks + stacks);
        data.endTime = System.currentTimeMillis() + ticks * 50L;
        applyEffects(player, data);
    }

    private void addStacks(Player player, int stacks) {
        BloodlustData data = active.get(player.getUniqueId());
        if (data == null) return;
        data.stacks = Math.min(100, data.stacks + stacks);
        applyEffects(player, data);
    }

    private void extendDuration(Player player, int ticks) {
        BloodlustData data = active.get(player.getUniqueId());
        if (data == null) return;
        data.endTime += ticks * 50L;
    }

    private void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, BloodlustData>> it = active.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, BloodlustData> e = it.next();
            Player p = Bukkit.getPlayer(e.getKey());
            BloodlustData data = e.getValue();
            if (p == null || now >= data.endTime) {
                data.bar.removeAll();
                if (p != null) {
                    p.removePotionEffect(PotionEffectType.SPEED);
                    p.removePotionEffect(PotionEffectType.FAST_DIGGING);
                }
                it.remove();
                continue;
            }
            double prog = data.stacks / 100.0;
            data.bar.setProgress(prog);
        }
    }

    private void applyEffects(Player player, BloodlustData data) {
        int speedLevel = Math.min(5, data.stacks / 20); // 0-100 -> 0-5
        int hasteLevel = Math.max(0, (data.stacks - 20) / 20);
        int duration = (int) ((data.endTime - System.currentTimeMillis()) / 50);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FAST_DIGGING);
        if (speedLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speedLevel - 1, true, false));
        }
        if (hasteLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel - 1, true, false));
        }
    }

    private static class BloodlustData {
        int stacks = 0;
        long endTime;
        final BossBar bar;
        BloodlustData(BossBar bar) {
            this.bar = bar;
        }
    }
}
