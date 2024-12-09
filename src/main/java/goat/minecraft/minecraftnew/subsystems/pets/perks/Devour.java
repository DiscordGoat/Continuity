package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Devour implements Listener {

    private final JavaPlugin plugin;

    public Devour(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        // Ensure the damager is a player
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the DEVOUR perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.DEVOUR)) {
            // Check if the damaged entity is a living entity
            if (event.getEntity() instanceof LivingEntity) {
                // Add 1 hunger point to the player's food level, capping at 20
                player.setFoodLevel(Math.min(player.getFoodLevel() + 1, 20));
                player.setSaturation(Math.min(player.getSaturation() + 1, 20));
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 5, 100);
            }
        }
    }
}
