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

public class Float implements Listener {

    private final PetManager petManager;

    public Float(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the FLOAT perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.FLOAT)) {
            // Apply Slow Falling effect (duration = 40 ticks = 2 seconds, level = 0 for Slow Falling I)
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOW_FALLING,
                    40, // Duration in ticks
                    0,  // Level (0 for Slow Falling I)
                    true, // Ambient effect
                    false, // No particles
                    true  // Show icon
            ));

            // Optionally notify the player (comment this out if not needed)
            if (!player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                player.sendMessage(ChatColor.AQUA + "Your pet's Float perk keeps you light on your feet!");
            }
        } else {
            // If the perk is not active, ensure the effect is removed
            if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
                player.removePotionEffect(PotionEffectType.SLOW_FALLING);
            }
        }
    }
}
