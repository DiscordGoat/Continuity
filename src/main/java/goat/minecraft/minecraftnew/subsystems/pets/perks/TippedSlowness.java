package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TippedSlowness implements Listener {
    private final JavaPlugin plugin;

    public TippedSlowness(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerShootArrow(EntityShootBowEvent event) {
        // Check if the projectile is an arrow
        if (event.getProjectile() instanceof Arrow arrow) {
            // Check if the shooter is a player
            if (event.getEntity() instanceof Player player) {
                // Get the player's active pet
                PetManager petManager = PetManager.getInstance(plugin);
                PetManager.Pet activePet = petManager.getActivePet(player);

                // Check if the player has an active pet with the TIPPED_SLOWNESS perk
                if (activePet != null && activePet.hasPerk(PetManager.PetPerk.TIPPED_SLOWNESS)) {
                    // Apply the slowness effect to the arrow
                    PotionEffect slowness = new PotionEffect(PotionEffectType.SLOWNESS, 100, 0); // 5 seconds of Slowness I
                    arrow.addCustomEffect(slowness, true);

                    // Notify the player
                }
            }
        }
    }
}
