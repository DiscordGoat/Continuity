package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection Charge 2 perk.
 * <p>
 * Grants the player a second resurrection charge, allowing two automatic
 * deaths to be prevented before needing to repurchase.
 */
public class ResurrectionCharge2 implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public ResurrectionCharge2(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Provide second use of the Resurrection effect.
}
