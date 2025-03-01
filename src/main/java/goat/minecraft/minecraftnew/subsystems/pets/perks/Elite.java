package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Elite implements Listener {

    private final PetManager petManager;

    public Elite(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerDealMeleeDamage(EntityDamageByEntityEvent event) {
        // Only proceed if the damager is a player (melee damage).
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();

        // Get the player's active pet.
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has an active pet with the ELITE perk.
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.ELITE)) {
            int petLevel = activePet.getLevel();

            // Calculate bonus damage percentage: 0.5% per level, capped at 50%.
            double bonusDamagePercent = Math.min(petLevel * 0.5, 50.0);
            double multiplier = 1 + (bonusDamagePercent / 100.0);

            // Apply the bonus to the melee damage.
            double originalDamage = event.getDamage();
            double newDamage = originalDamage * multiplier;
            event.setDamage(newDamage);

            // Provide feedback to the player.
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 1.0f, 1.0f);
        }
    }
}
