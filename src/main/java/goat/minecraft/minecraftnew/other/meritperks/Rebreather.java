package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Rebreather merit perk.
 * <p>
 * While underwater below Y=50, the player periodically regains breathing
 * oxygen (+1 bubble every 3 seconds) when they would otherwise lose air.
 */
public class Rebreather implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    // Tracks the last time a player received bonus air
    private final Map<UUID, Long> lastGrantTime = new HashMap<>();

    // 3 second cooldown between air grants
    private static final long GRANT_INTERVAL_MS = 3000;
    // Amount of air ticks to grant (~1 bubble)
    private static final int AIR_BUBBLE_TICKS = 30;

    public Rebreather(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * When a player's air level changes while underwater below Y=50, provide
     * a small boost every few seconds.
     */
    @EventHandler
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        UUID id = player.getUniqueId();

        // Check if player owns the perk
        if (!playerData.hasPerk(id, "Rebreather")) {
            return;
        }

        // Only apply when underwater and below Y=50
        if (!player.isInWater() || player.getLocation().getY() >= 50) {
            return;
        }

        // Only trigger when air is decreasing
        if (event.getAmount() >= player.getRemainingAir()) {
            return;
        }

        long now = System.currentTimeMillis();
        long last = lastGrantTime.getOrDefault(id, 0L);
        if (now - last < GRANT_INTERVAL_MS) {
            return;
        }

        int newAir = Math.min(event.getAmount() + AIR_BUBBLE_TICKS, player.getMaximumAir());
        event.setAmount(newAir);
        lastGrantTime.put(id, now);
    }
}
