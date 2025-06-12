package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tuxedo merit perk.
 *
 * Grants two additional auction items with a chance for discounted rare rewards.
 * Logic is handled in the music subsystem.
 */
public class Tuxedo implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Tuxedo(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }
}
