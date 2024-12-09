package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Recovery implements Listener {
    private JavaPlugin plugin;

    // Constructor to pass in XPManager and plugin instance
    public Recovery(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerDamageEnemyRanged(EntityDamageByEntityEvent event) {
        // Check if the damager is an arrow
        if (event.getDamager() instanceof org.bukkit.entity.Arrow arrow) {
            // Check if the arrow's shooter is a player
            if (arrow.getShooter() instanceof Player player) {
                // Get the player's active pet
                PetManager petManager = PetManager.getInstance(plugin);
                PetManager.Pet activePet = petManager.getActivePet(player);

                // Check if the player has an active pet with the RECOVERY perk
                if (activePet != null && activePet.hasPerk(PetManager.PetPerk.RECOVERY)) {
                    int petLevel = activePet.getLevel();

                    // Determine if the perk triggers based on pet level
                    if (Math.random() * 100 < petLevel) { // Percentage chance equal to pet level
                        // Add an arrow to the player's inventory
                        player.getInventory().addItem(new ItemStack(Material.ARROW));

                        // Notify the player

                    }
                }
            }
        }
    }
}
