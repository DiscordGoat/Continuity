package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Double Enderchest merit perk.
 * <p>
 * Overrides the standard ender chest with a 54-slot GUI stored to file.
 * Works exactly like a vanilla ender chest but provides double capacity.
 */
public class DoubleEnderchest implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public DoubleEnderchest(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Replace ender chest open behaviour with custom storage GUI.
}
