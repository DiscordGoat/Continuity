package goat.minecraft.minecraftnew.subsystems.villagers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class MarketTrendManager {
    private static MarketTrendManager instance;
    private final JavaPlugin plugin;
    private final Random random = new Random();
    private double trend = 0.0; // -0.5 to 0.5

    private MarketTrendManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTrendTask();
    }

    public static MarketTrendManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new MarketTrendManager(plugin);
        }
        return instance;
    }

    public static MarketTrendManager getInstance() {
        return instance;
    }

    private void startTrendTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double change = (random.nextDouble() - 0.5) * 0.02; // +/-1%
                trend += change;
                if (trend > 0.5) trend = 0.5;
                if (trend < -0.5) trend = -0.5;
            }
        }.runTaskTimer(plugin, 0L, 20L * 60 * 5); // every 5 minutes
    }

    /**
     * @return current trend value in range [-0.5, 0.5]
     */
    public double getTrend() {
        return trend;
    }

    /**
     * Convenience multiplier to apply to prices.
     */
    public double getTrendMultiplier() {
        return 1.0 + trend;
    }
}
