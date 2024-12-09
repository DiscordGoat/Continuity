package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Fireproof implements Listener {

    private final JavaPlugin plugin;

    public Fireproof(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the FIREPROOF perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.FIREPROOF)) {
            // Check if the player already has fire resistance
            if (!player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE)) {
                // Apply fire resistance for 4 seconds (4 * 20 ticks)
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 4 * 20, 0));

                // Notify the player (optional, you can remove this if spam is a concern)

            }
        }
    }
}
