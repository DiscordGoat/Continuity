package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection Charge 3 perk.
 * <p>
 * Allows the player to have up to three resurrection charges active
 * simultaneously.
 */
public class ResurrectionCharge3 implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public ResurrectionCharge3(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Provide third use of the Resurrection effect.
}
