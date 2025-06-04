package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Angler merit perk.
 * <p>
 * Increases the player's sea creature chance by 5% while fishing.
 * Exact integration with the fishing subsystem will be done later.
 */
public class MasterAngler implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterAngler(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Apply bonus sea creature chance during fishing events.
}
