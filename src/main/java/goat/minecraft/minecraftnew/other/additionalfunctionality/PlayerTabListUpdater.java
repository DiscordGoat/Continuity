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
import java.util.HashMap;
import java.util.Map;

public class PlayerTabListUpdater {

    private final JavaPlugin plugin;
    private final XPManager xpManager;

    // Mapping of potion names to display colors
    private static final Map<String, ChatColor> POTION_COLORS = new HashMap<>();

    static {
        POTION_COLORS.put("Potion of Recurve", ChatColor.DARK_PURPLE);
        POTION_COLORS.put("Potion of Liquid Luck", ChatColor.GOLD);
        POTION_COLORS.put("Potion of Strength", ChatColor.RED);
        POTION_COLORS.put("Potion of Fountains", ChatColor.AQUA);
        POTION_COLORS.put("Potion of Sovereignty", ChatColor.AQUA);
        POTION_COLORS.put("Potion of Swift Step", ChatColor.YELLOW);
        POTION_COLORS.put("Potion of Solar Fury", ChatColor.GOLD);
        POTION_COLORS.put("Potion of Night Vision", ChatColor.LIGHT_PURPLE);
        POTION_COLORS.put("Potion of Charismatic Bartering", ChatColor.GREEN);
        POTION_COLORS.put("Potion of Oxygen Recovery", ChatColor.AQUA);
        POTION_COLORS.put("Potion of Metal Detection", ChatColor.RED);
        POTION_COLORS.put("Potion of Optimal Eating", ChatColor.GOLD);
    }

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
        int playerLevel   = xpManager.getPlayerLevel(player, "Player");
        int xpToNextLevel = xpManager.getXPToNextLevel(player, "Player");

        int secondsLeft = VillagerWorkCycleManager.getInstance(plugin).getSecondsUntilNextWorkCycle();
        String formattedTime = formatSecondsToMMSS(secondsLeft);
        int saplingSec = SaplingManager.getInstance(plugin).getCooldownSecondsRemaining();
        String saplingTime = formatSecondsToDDMMSS(saplingSec);

        StringBuilder header = new StringBuilder();
        header.append(ChatColor.GOLD).append("Players: ")
                .append(ChatColor.WHITE).append(Bukkit.getOnlinePlayers().size())
                .append("/").append(Bukkit.getMaxPlayers());
        header.append("\n").append(ChatColor.GREEN).append("Level ")
                .append(playerLevel).append(" ")
                .append(createXPBar(player));
        if (xpToNextLevel > 0) {
            header.append(" ").append(ChatColor.YELLOW).append("(").append(xpToNextLevel).append(" XP)");
        }

        StringBuilder footer = new StringBuilder();
        footer.append(ChatColor.YELLOW).append("World Stats:");
        footer.append("\n").append(ChatColor.YELLOW).append("Next Work Cycle: ")
                .append(ChatColor.WHITE).append(formattedTime);
        footer.append("\n").append(ChatColor.GREEN).append("Sapling Growth: ")
                .append(ChatColor.WHITE).append(saplingTime);

        Map<String, Integer> effects = PotionManager.getActiveEffects(player);
        if (!effects.isEmpty()) {
            footer.append("\n").append(ChatColor.LIGHT_PURPLE).append("Active Potions:");
            for (Map.Entry<String, Integer> entry : effects.entrySet()) {
                ChatColor color = POTION_COLORS.getOrDefault(entry.getKey(), ChatColor.WHITE);
                footer.append("\n").append(color).append(entry.getKey()).append(": ")
                        .append(createPotionBar(entry.getValue(), color));
            }
        }

        player.setPlayerListHeaderFooter(header.toString(), footer.toString());
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
        int days = totalSeconds / (20 * 60);
        int minutes = (totalSeconds % (20 * 60)) / 60;
        int seconds = totalSeconds % 60;
        return days + "d:" + String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Builds a progress bar for potion durations.
     */
    private String createPotionBar(int seconds, ChatColor color) {
        int segments = 35;
        int max = 10000;
        double ratio = Math.min(1.0, (double) seconds / max);
        int filled = (int) Math.round(ratio * segments);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY).append("[");
        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                bar.append(color).append('|');
            } else {
                bar.append(ChatColor.GRAY).append('|');
            }
        }
        bar.append(ChatColor.DARK_GRAY).append(']');
        return bar.toString();
    }

    /**
     * Builds a progress bar for the Player skill.
     */
    private String createXPBar(Player player) {
        int segments = 25;
        int level = xpManager.getPlayerLevel(player, "Player");
        if (level >= 100) {
            StringBuilder full = new StringBuilder();
            full.append(ChatColor.DARK_GRAY).append('[');
            for (int i = 0; i < segments; i++) {
                full.append(ChatColor.GREEN).append('|');
            }
            full.append(ChatColor.DARK_GRAY).append(']');
            return full.toString();
        }

        int currentXP = xpManager.getXP(player, "Player");
        int start = xpManager.getLevelStartXP(level);
        int end = xpManager.getLevelEndXP(level);
        double ratio = (double) (currentXP - start) / (end - start);
        int filled = (int) Math.round(ratio * segments);

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.DARK_GRAY).append('[');
        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                bar.append(ChatColor.GREEN).append('|');
            } else {
                bar.append(ChatColor.GRAY).append('|');
            }
        }
        bar.append(ChatColor.DARK_GRAY).append(']');
        return bar.toString();
    }
}
