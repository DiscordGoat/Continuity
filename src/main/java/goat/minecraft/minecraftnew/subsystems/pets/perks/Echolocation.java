package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class Echolocation implements Listener {

    private final PetManager petManager;

    public Echolocation(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the ECHOLOCATION perk or unique trait
        if (activePet != null && (activePet.hasPerk(PetManager.PetPerk.ECHOLOCATION)
                || activePet.hasUniqueTraitPerk(PetManager.PetPerk.ECHOLOCATION))) {
            int petLevel = activePet.getLevel();

            // Calculate effect duration (e.g., 5 seconds + 1 second per pet level)
            int duration = 80 * 5;

            // Apply Night Vision effect
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION,
                    duration,
                    0, // No amplifier for Night Vision
                    false, // Particles disabled
                    false, // Icon disabled
                    true // Ambient effect
            ));

            // Optional: Notify the player (can be omitted for passive perks)
        }
    }
}
