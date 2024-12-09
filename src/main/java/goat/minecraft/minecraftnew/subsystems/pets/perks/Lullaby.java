package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Lullaby implements Listener {

    private final PetManager petManager;

    public Lullaby(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        // Check if the entity being spawned is a monster
        if (!(event.getEntity() instanceof Monster)) {
            return;
        }

        Location spawnLocation = event.getLocation();
        World world = spawnLocation.getWorld();

        // Check for players in the world
        for (Player player : world.getPlayers()) {
            // Get the player's active pet
            PetManager.Pet activePet = petManager.getActivePet(player);
            if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.LULLABY)) {
                continue;
            }

            // Calculate the radius based on the pet's level
            int petLevel = activePet.getLevel();
            double radius = 40 + (4 * petLevel);

            // Check if the monster spawn location is within the radius
            if (player.getLocation().distanceSquared(spawnLocation) <= radius * radius) {
                // Cancel the spawn event
                event.setCancelled(true);

                // Optional: Notify the player

                return;
            }
        }
    }
}
