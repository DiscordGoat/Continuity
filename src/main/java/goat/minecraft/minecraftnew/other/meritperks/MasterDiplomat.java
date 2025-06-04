package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Diplomat merit perk.
 * <p>
 * Grants a chance to avoid notoriety when chopping logs.
 * Players with this perk have a 60% chance to ignore
 * forestry notoriety gained from breaking wood blocks.
 */
public class MasterDiplomat implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterDiplomat(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // Logic handled in Forestry.incrementNotoriety()
}
