package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class CatTameEvent implements Listener {

    private final PetManager petManager;

    public CatTameEvent(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onCatTame(EntityTameEvent event) {
        // Check if the tamed entity is a cat
        Entity entity = event.getEntity();
        if (entity instanceof Cat) {
            // Check if the tamer is a player
            if (event.getOwner() instanceof Player) {
                Player player = (Player) event.getOwner();
                PetRegistry petRegistry = new PetRegistry();
                // Grant the player the Cat pet
                petRegistry.addPetByName(player, "Cat");
                // Notify the player
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.0f);

                // Remove the tamed cat from the world
                entity.remove();

                // Optionally, spawn a particle effect where the cat was removed
                entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 10, 0.5, 0.5, 0.5);
            }
        }
    }
}
