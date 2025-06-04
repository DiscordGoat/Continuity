package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Employer merit perk.
 * <p>
 * When a villager work cycle is performed and at least one online player owns
 * this perk, the timer for the next cycle is reduced by 50%.
 */
public class MasterEmployer implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterEmployer(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // Logic handled in VillagerWorkCycleManager
}
