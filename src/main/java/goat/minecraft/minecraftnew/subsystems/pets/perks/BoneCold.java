package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BoneCold implements Listener {
    private final JavaPlugin plugin;

    public BoneCold(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerFreezeDamage(EntityDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof Player player) {
            // Get the player's active pet
            PetManager petManager = PetManager.getInstance(plugin);
            PetManager.Pet activePet = petManager.getActivePet(player);

            // Check if the player has an active pet with the BONE_COLD perk
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BONE_COLD)) {
                // Prevent freeze damage
                if (event.getCause() == EntityDamageEvent.DamageCause.FREEZE) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
