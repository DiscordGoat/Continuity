package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.other.additionalfunctionality.RightClickArtifacts;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;
import java.util.UUID;

/**
 * Master Diffuser merit perk.
 * <p>
 * Creepers killed by a player with this perk have a 50% chance to drop a
 * random music disc.
 */
public class MasterDiffuser implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;
    private final Random random = new Random();

    public MasterDiffuser(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onCreeperDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) {
            return;
        }

        Player killer = creeper.getKiller();
        if (killer == null) {
            return;
        }

        UUID id = killer.getUniqueId();
        if (!playerData.hasPerk(id, "Master Diffuser")) {
            return;
        }

        if (random.nextDouble() <= 0.5) {
            ItemStack disc = RightClickArtifacts.getRandomMusicDisc();
            creeper.getWorld().dropItemNaturally(creeper.getLocation(), disc);
        }
    }
}
