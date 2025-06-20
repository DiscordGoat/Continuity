package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Botanist merit perk.
 * <p>
 * Reduces the time it takes for Verdant Relics to mature by 20%.
 * The adjustment is applied when the relic is planted in
 * {@link goat.minecraft.minecraftnew.subsystems.farming.VerdantRelicsSubsystem}.
 */
public class MasterBotanist implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public MasterBotanist(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // Functionality handled in VerdantRelicsSubsystem when planting relics.
}
