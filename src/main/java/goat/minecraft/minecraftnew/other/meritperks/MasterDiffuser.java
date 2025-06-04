package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Diffuser merit perk.
 * <p>
 * Creepers killed by the player have a 50% chance to drop a random music disc.
 * Actual drop logic to be implemented later.
 */
public class MasterDiffuser implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterDiffuser(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Inject disc drop logic on creeper death.
}
