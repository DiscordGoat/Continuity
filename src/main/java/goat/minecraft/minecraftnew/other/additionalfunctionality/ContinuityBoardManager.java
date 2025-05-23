package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.mining.PlayerOxygenManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ContinuityBoardManager implements Listener {
    private final ScoreboardManager scoreboardManager;

    public ContinuityBoardManager() {
        this.scoreboardManager = Bukkit.getScoreboardManager();
        // Schedule a repeating task to update scoreboards every second.
        Bukkit.getScheduler().runTaskTimer(MinecraftNew.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateScoreboard(player);
            }
        }, 0L, 20L);
    }

    /**
     * Creates a new scoreboard for the given player with an "Environment" objective.
     */
    public void createScoreboard(Player player) {
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("env", "dummy", ChatColor.GREEN + "Environment");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(scoreboard);
    }

    /**
     * Updates the scoreboard for the given player. Displays Notoriety, Saturation, Oxygen, and Temperature.
     */
    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("env");
        if (objective == null) {
            objective = scoreboard.registerNewObjective("env", "dummy", ChatColor.GREEN + "Environment");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Retrieve oxygen, saturation, and temperature from PlayerOxygenManager.
        int currentOxygen = PlayerOxygenManager.getInstance().getPlayerOxygen(player);
        int saturation = PlayerOxygenManager.getInstance().getSaturation(player);
        int temperature = PlayerOxygenManager.getInstance().getTemperature(player);
        // Retrieve notoriety from Forestry.
        int notoriety = Forestry.getInstance().getNotoriety(player);

        // Create display strings.
        String notorietyStr = ChatColor.DARK_RED + "Notoriety: " + ChatColor.WHITE + notoriety;
        String saturationStr = ChatColor.YELLOW + "Saturation: " + ChatColor.WHITE + saturation;
        String oxygenStr = ChatColor.AQUA + "Oxygen: " + ChatColor.WHITE + currentOxygen + "s";
        String temperatureStr = ChatColor.RED + "Temperature: " + ChatColor.WHITE + temperature + "°F";

        // Clear existing scoreboard entries.
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Set scores with ordering (higher score means higher up in the sidebar).
        objective.getScore(notorietyStr).setScore(4);
        objective.getScore(saturationStr).setScore(3);
        objective.getScore(oxygenStr).setScore(2);
        objective.getScore(temperatureStr).setScore(1);

        // Re-apply the scoreboard to the player.
        player.setScoreboard(scoreboard);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        createScoreboard(player);
    }
}
