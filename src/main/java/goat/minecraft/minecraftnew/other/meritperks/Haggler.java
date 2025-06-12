package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Haggler merit perk.
 *
 * Grants a flat 10% discount on villager trades.
 * The actual discount logic is applied in {@link goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager}.
 */
public class Haggler implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Haggler(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }
}
