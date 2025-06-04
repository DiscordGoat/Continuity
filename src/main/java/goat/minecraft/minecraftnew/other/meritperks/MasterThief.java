package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Thief merit perk.
 * <p>
 * Monsters killed by the player have a 50% chance to drop rare items twice.
 * Actual drop logic to be implemented later.
 */
public class MasterThief implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterThief(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Modify monster drop tables when perk owner defeats them.
}
