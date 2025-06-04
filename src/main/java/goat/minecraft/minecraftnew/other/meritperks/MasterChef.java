package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Chef merit perk.
 * <p>
 * Provides a 50% chance to produce two culinary delights instead of one when a
 * recipe completes. Implementation to be added later.
 */
public class MasterChef implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterChef(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Hook into culinary system and double outputs when chance succeeds.
}
