package goat.minecraft.minecraftnew.utils;

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
        }.runTaskTimer(plugin, 0L, 20L); // Every 5 seconds
    }

    private void updateAllPlayerTabLists() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerTabList(player);
        }
    }
    private int getDaysPlayed(Player player) {
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return playTimeTicks / 24000; // Convert ticks to days
    }
    private void updatePlayerTabList(Player player) {
        int playerXP = xpManager.getPlayerLevel(player, "Player");
        int playerLevel = xpManager.getPlayerLevel(player, "Player");
        int xpToNextLevel = xpManager.getXPToNextLevel(player, "Player");
        int daysPlayed = getDaysPlayed(player);

        String header = ChatColor.GOLD + "Welcome, " + player.getName() + "!";
        String footer = ChatColor.AQUA + "Player XP: " + playerXP + ChatColor.DARK_PURPLE + " | Level: " + playerLevel;
        footer += ChatColor.GREEN + " | Days Played: " + daysPlayed;

        if (xpToNextLevel != -1) {
            footer += "\nXP to next level: " + xpToNextLevel;
        } else {
            footer += "\nMax Level Reached";
        }

        player.setPlayerListHeaderFooter(header, footer);
    }

}
