package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Rebreather merit perk.
 * <p>
 * While underwater below Y=50, the player periodically gains extra breathing
 * oxygen (+1 bubble every 3 seconds). Functionality to be implemented later.
 */
public class Rebreather implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Rebreather(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Add scheduled task or event logic to grant underwater breath.
}
