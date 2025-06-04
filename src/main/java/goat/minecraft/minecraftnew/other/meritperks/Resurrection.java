package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Resurrection merit perk.
 * <p>
 * Functions like a totem of undying. Prevents player death, grants temporary
 * regeneration and absorption, and then disables itself until repurchased.
 */
public class Resurrection implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public Resurrection(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    // TODO: Apply totem-like protection and consume the perk on use.
}
