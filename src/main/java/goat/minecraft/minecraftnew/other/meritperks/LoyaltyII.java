package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Loyalty II merit perk.
 * <p>
 * Reduces the cooldown of the "loyal" ability from 5 seconds to 1 second.
 * Actual cooldown modifications will be implemented later.
 */
public class LoyaltyII implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public LoyaltyII(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Adjust loyal pet ability cooldown.
}
