package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Trader merit perk.
 * <p>
 * Gives purchases a 5% chance to be free for the player.
 * Implementation to be integrated with the economy subsystem later.
 */
public class MasterTrader implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterTrader(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Apply free purchase chance when a transaction occurs.
}
