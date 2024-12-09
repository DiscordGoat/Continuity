package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WalkingFortress implements Listener {

    private final PetManager petManager;

    public WalkingFortress(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        // Ensure the entity taking damage is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the WALKING_FORTRESS perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.WALKING_FORTRESS)) {
            int petLevel = activePet.getLevel();

            // Calculate damage reduction percentage
            double damageReduction = Math.min(petLevel * 0.8, 80.0); // Cap at 80% reduction
            double reductionFactor = 1 - (damageReduction / 100.0);

            // Reduce the damage directly
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * reductionFactor;
            event.setDamage(reducedDamage);

            // Notify the player of the damage reduction
            player.sendMessage(ChatColor.GRAY + "Your Walking Fortress perk reduced the damage by " +
                    (int) damageReduction + "%!");
        }
    }
}
