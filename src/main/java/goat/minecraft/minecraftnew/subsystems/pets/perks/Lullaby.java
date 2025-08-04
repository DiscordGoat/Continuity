package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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
            // Get the player's active pet and ensure the perk is active
            PetManager.Pet activePet = petManager.getActivePet(player);
            boolean hasPerk = activePet != null && (activePet.hasPerk(PetManager.PetPerk.LULLABY)
                    || activePet.hasUniqueTraitPerk(PetManager.PetPerk.LULLABY));
            if (!hasPerk) {
                continue;
            }

            int petLevel = activePet.getLevel();

            int talentLevel = 0;
            if (SkillTreeManager.getInstance() != null) {
                talentLevel = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.LULLABY);
            }

            // Calculate the radius based on the pet's level and talent level
            double radius = 40 + (4 * petLevel);
            radius *= 1 + (talentLevel * 0.5);

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
