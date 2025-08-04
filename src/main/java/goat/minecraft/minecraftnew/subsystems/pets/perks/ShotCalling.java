package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class ShotCalling implements Listener {
    private final JavaPlugin plugin;

    public ShotCalling(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    private final Random random = new Random();

    @EventHandler
    public void onPlayerDamageEnemyRanged(EntityDamageByEntityEvent event) {
        // Check if the damager is an arrow
        if (event.getDamager() instanceof Arrow arrow) {
            // Check if the arrow's shooter is a player
            if (arrow.getShooter() instanceof Player player) {
                // Get the player's active pet
                PetManager petManager = PetManager.getInstance(plugin);
                PetManager.Pet activePet = petManager.getActivePet(player);
                if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SHOTCALLING)) {
                    int talent = 0;
                    if (SkillTreeManager.getInstance() != null) {
                        talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.SHOTCALLING);
                    }
                    int petLevel = activePet.getLevel();

                    // Calculate damage multiplier based on pet level
                    double damageMultiplier = 1 + (petLevel * 0.005) + (talent * 0.05);

                    // Apply damage multiplier to the event
                    event.setDamage(event.getDamage() * damageMultiplier);

                    // Optional: Notify the player about the damage boost

                }
            }
        }
    }
}
