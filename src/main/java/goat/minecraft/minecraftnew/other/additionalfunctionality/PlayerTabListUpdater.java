package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import goat.minecraft.minecraftnew.subsystems.forestry.SaplingManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;

public class PlayerTabListUpdater {

    private final JavaPlugin plugin;
    private final XPManager xpManager;

    public PlayerTabListUpdater(JavaPlugin plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;

        // Schedule a repeating task to update player tab lists
        new BukkitRunnable() {
            @Override
            public void run() {
                updateAllPlayerTabLists();
            }
        }.runTaskTimer(plugin, 0L, 20L); // update tab list every second
    }

    private void updateAllPlayerTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player);
        }
    }

    public int getDaysPlayed(Player player) {
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        // 1 day in Minecraft = 24000 ticks
        return playTimeTicks / 24000;
    }

    private void updatePlayerTabList(Player player) {
        int playerXP        = xpManager.getPlayerLevel(player, "Player");
        int playerLevel     = xpManager.getPlayerLevel(player, "Player");
        int xpToNextLevel   = xpManager.getXPToNextLevel(player, "Player");
        int daysPlayed      = getDaysPlayed(player);

        // Pull the countdown from your manager
        int secondsLeft = VillagerWorkCycleManager.getInstance(plugin).getSecondsUntilNextWorkCycle();
        String formattedTime = formatSecondsToMMSS(secondsLeft);
        int saplingSec = SaplingManager.getInstance(plugin).getCooldownSecondsRemaining();
        String saplingTime = formatSecondsToDDMMSS(saplingSec);

        String header = ChatColor.GOLD + "Welcome, " + player.getName() + "!";
        String footer = ChatColor.AQUA + "Player XP: " + playerXP
                + ChatColor.DARK_PURPLE + " | Level: " + playerLevel
                + ChatColor.GREEN + " | Days Played: " + daysPlayed;

        if (xpToNextLevel != -1) {
            footer += "\nXP to next level: " + xpToNextLevel;
        } else {
            footer += "\nMax Level Reached";
        }

        // Add your villager work cycle countdown in the footer
        footer += "\n" + ChatColor.YELLOW + "Next Villager Work Cycle: " + ChatColor.WHITE + formattedTime;
        footer += "\n" + ChatColor.GREEN + "Sapling Growth: " + ChatColor.WHITE + saplingTime;

        // New segment: Active Potion Effects
        Map<String, Integer> effects = PotionManager.getActiveEffects(player);
        if (!effects.isEmpty()) {
            footer += "\n" + ChatColor.LIGHT_PURPLE + "Active Potions:";
            for (Map.Entry<String, Integer> entry : effects.entrySet()) {
                footer += "\n" + ChatColor.WHITE + entry.getKey() + ": " + entry.getValue() + "s";
            }
        }

        player.setPlayerListHeaderFooter(header, footer);
    }

    /**
     * Simple helper to format remaining seconds as M:SS.
     */
    private String formatSecondsToMMSS(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
    }

    private String formatSecondsToDDMMSS(int totalSeconds) {
        int days = totalSeconds / 86400;
        int minutes = (totalSeconds % 86400) / 60;
        int seconds = totalSeconds % 60;
        return days + "d:" + String.format("%02d:%02d", minutes, seconds);
    }
}
