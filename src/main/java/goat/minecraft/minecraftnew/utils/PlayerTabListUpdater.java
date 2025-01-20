package goat.minecraft.minecraftnew.utils;

import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        }.runTaskTimer(plugin, 0L, 20L); // update tab list every second (or 5s, your preference)
    }

    private void updateAllPlayerTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player);
        }
    }

    private int getDaysPlayed(Player player) {
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
}
