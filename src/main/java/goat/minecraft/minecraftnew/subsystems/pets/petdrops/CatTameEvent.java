package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
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

                // Grant the player the Cat pet
                petManager.createPet(player, "Cat", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.CLAW, PetManager.PetPerk.SOFT_PAW, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.LEAP);

                // Notify the player
                player.sendMessage(ChatColor.GREEN + "Congratulations! You've tamed a cat and received the Cat pet!");
                player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.0f);

                // Remove the tamed cat from the world
                entity.remove();

                // Optionally, spawn a particle effect where the cat was removed
                entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 10, 0.5, 0.5, 0.5);
            }
        }
    }
}
