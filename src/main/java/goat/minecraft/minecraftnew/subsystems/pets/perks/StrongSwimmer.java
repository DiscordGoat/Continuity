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

public class StrongSwimmer implements Listener {
    private final JavaPlugin plugin;

    public StrongSwimmer(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Get the player from the event
        Player player = event.getPlayer();

        // Check if the player is in water
        if (!player.isInWater()) {
            return;
        }

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the STRONG_SWIMMER perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.STRONG_SWIMMER)) {
            // Apply Dolphin's Grace effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 1));

            // Notify the player (optional)

        }
    }
}
