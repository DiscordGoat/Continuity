package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SoftPaw implements Listener {

    private final JavaPlugin plugin;

    public SoftPaw(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        // Check if the event involves fall damage
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            // Get the player's active pet
            PetManager petManager = PetManager.getInstance(plugin);
            PetManager.Pet activePet = petManager.getActivePet(player);

            // If the player has the Soft_PAW perk, reduce fall damage
            if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SOFT_PAW)) {
                int petLevel = activePet.getLevel();
                double reductionPercentage = petLevel / 100.0; // 1% reduction per level
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * (1 - reductionPercentage);

                // Apply the reduced damage
                event.setDamage(reducedDamage);

                // Notify the player (optional)
                if (petLevel == 100) {
                    player.sendMessage(ChatColor.GREEN + "Soft Paw perk activated: You are immune to fall damage!");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Soft Paw perk activated: Fall damage reduced by " + (int) (reductionPercentage * 100) + "%.");
                }
            }
        }
    }
}
