package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Strong Digestion merit perk.
 * <p>
 * Doubles the duration of consumed potions. Actual effect application will be added later.
 */
public class StrongDigestion implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public StrongDigestion(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Extend potion effect durations after consumption.
}
