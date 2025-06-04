package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Restock merit perk.
 * <p>
 * When the player is holding a bow with no arrows in their inventory and has free space,
 * automatically grants them a single arrow. Costs 1 merit point.
 * Implementation of the actual arrow granting will be added later.
 */
public class Restock implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Restock(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Add event handlers that check bow usage and give arrows when conditions are met.
}
