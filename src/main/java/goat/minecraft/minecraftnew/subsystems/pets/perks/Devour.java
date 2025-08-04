package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
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

        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.DEVOUR)) {
            int talent = 0;
            if (SkillTreeManager.getInstance() != null) {
                talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.DEVOUR);
            }

            // Check if the damaged entity is a living entity
            if (event.getEntity() instanceof LivingEntity) {
                int amount = 1;
                if (talent > 0) amount *= 2; // talent doubles the food gains
                // Add hunger points to the player's food level
                player.setFoodLevel(Math.min(player.getFoodLevel() + amount, 20));
                player.setSaturation(Math.min(player.getSaturation() + amount, 20));
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 5, 100);
            }
        }
    }
}
