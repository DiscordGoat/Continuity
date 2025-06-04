package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Botanist merit perk.
 * <p>
 * Reduces the time it takes for Verdant Relics to mature by 50%.
 * Exact handling will be implemented later.
 */
public class MasterBotanist implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterBotanist(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Adjust Verdant Relic timers when player has this perk.
}
