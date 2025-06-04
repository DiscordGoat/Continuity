package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Diplomat merit perk.
 * <p>
 * Reduces all notoriety gains for the player by 60%.
 * Implementation detail will be added later when the notoriety system is hooked up.
 */
public class MasterDiplomat implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterDiplomat(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Reduce notoriety gain events when this perk is active.
}
