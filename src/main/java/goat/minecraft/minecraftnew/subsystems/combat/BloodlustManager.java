package goat.minecraft.minecraftnew.subsystems.combat;

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

import java.util.*;

/**
 * Manages the Bloodlust effect and stacks for players.
 */
public class BloodlustManager {

    private static BloodlustManager instance;

    public static synchronized BloodlustManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BloodlustManager(plugin);
        }
        return instance;
    }

    private final JavaPlugin plugin;
    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, Long> expirations = new HashMap<>();
    private final Map<UUID, BossBar> bars = new HashMap<>();

    private BloodlustManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    /** Adds stacks and ensures duration is active */
    public void addStacks(Player player, int amount) {
        if (amount <= 0) return;
        UUID id = player.getUniqueId();
        int current = stacks.getOrDefault(id, 0);
        int newStacks = Math.min(100, current + amount);
        stacks.put(id, newStacks);
        showBar(player);
    }

    /** Adds duration in seconds to a player's bloodlust timer */
    public void addDuration(Player player, int seconds) {
        if (seconds <= 0) return;
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long end = expirations.getOrDefault(id, now);
        if (end < now) end = now;
        expirations.put(id, end + seconds * 1000L);
        showBar(player);
    }

    /** Clears a player's bloodlust state */
    public void clear(Player player) {
        UUID id = player.getUniqueId();
        stacks.remove(id);
        expirations.remove(id);
        BossBar bar = bars.remove(id);
        if (bar != null) {
            bar.removeAll();
        }
    }

    public int getStacks(Player player) {
        return stacks.getOrDefault(player.getUniqueId(), 0);
    }

    private void showBar(Player player) {
        UUID id = player.getUniqueId();
        BossBar bar = bars.computeIfAbsent(id, k -> {
            BossBar b = Bukkit.createBossBar(ChatColor.DARK_RED + "Bloodlust", BarColor.RED, BarStyle.SEGMENTED_20);
            b.addPlayer(player);
            b.setVisible(true);
            return b;
        });
        bar.addPlayer(player);
    }

    private void updateBar(Player player) {
        BossBar bar = bars.get(player.getUniqueId());
        if (bar == null) return;
        int stack = stacks.getOrDefault(player.getUniqueId(), 0);
        bar.setProgress(Math.min(1.0, Math.max(0.0, stack / 100.0)));
        bar.setTitle(ChatColor.DARK_RED + "Bloodlust " + stack + "/100");
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<UUID> it = expirations.keySet().iterator();
                while (it.hasNext()) {
                    UUID id = it.next();
                    Player player = Bukkit.getPlayer(id);
                    long end = expirations.get(id);
                    if (player == null || !player.isOnline()) {
                        // cleanup for offline players
                        it.remove();
                        stacks.remove(id);
                        BossBar bar = bars.remove(id);
                        if (bar != null) bar.removeAll();
                        continue;
                    }
                    if (now >= end) {
                        clear(player);
                        continue;
                    }
                    applyEffects(player);
                    updateBar(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void applyEffects(Player player) {
        int stack = stacks.getOrDefault(player.getUniqueId(), 0);
        int speedAmp = 0;
        int hasteAmp = -1;
        if (stack >= 1 && stack < 10) {
            speedAmp = 0;
        } else if (stack < 20) {
            speedAmp = 0;
        } else if (stack < 30) {
            speedAmp = 0;
            hasteAmp = 0;
        } else if (stack < 40) {
            speedAmp = 1;
            hasteAmp = 1;
        } else if (stack < 50) {
            speedAmp = 1;
            hasteAmp = 2;
        } else if (stack < 60) {
            speedAmp = 2;
            hasteAmp = 3;
        } else if (stack < 70) {
            speedAmp = 3;
            hasteAmp = 4;
        } else if (stack < 80) {
            speedAmp = 3;
            hasteAmp = 4;
        } else if (stack < 90) {
            speedAmp = 4;
            hasteAmp = 5;
        } else if (stack < 100) {
            speedAmp = 4;
            hasteAmp = 6;
        } else if (stack >= 100) {
            speedAmp = 5;
            hasteAmp = 6;
        }
        if (speedAmp >= 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, speedAmp, true, false, true));
        }
        if (hasteAmp >= 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 40, hasteAmp, true, false, true));
        }
    }
}
