package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Keepinventory merit perk.
 * <p>
 * When a player with this perk logs in, keep inventory is enabled for them.
 * No further logic is implemented here yet.
 */
public class Keepinventory implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Keepinventory(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Add login handler enabling keep inventory for the player.
}
