package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Icarus merit perk.
 * <p>
 * Doubles the maximum flying distance provided by the Flight pet perk.
 * Actual flight limit adjustments will be added later.
 */
public class Icarus implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Icarus(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Increase allowable flight distance for players with this perk.
}
