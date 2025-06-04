package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Smith merit perk.
 * <p>
 * When repairing items with an anvil using iron ingots, this perk will add
 * an extra 50 durability to the resulting item. Actual repair logic will
 * be implemented later.
 */
public class MasterSmith implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterSmith(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Hook into anvil repairs and modify durability when perk is owned.
}
