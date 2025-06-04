package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Brewer merit perk.
 * <p>
 * Cuts the brewing time for potions in half. Functionality will be coded later.
 */
public class MasterBrewer implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterBrewer(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Modify potion brewing timers when this perk is owned.
}
