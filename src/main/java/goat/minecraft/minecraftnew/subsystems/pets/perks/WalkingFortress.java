package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WalkingFortress implements Listener {

    private final PetManager petManager;

    public WalkingFortress(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        // Ensure the entity taking damage is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);
        int talent = 0;
        if (SkillTreeManager.getInstance() != null) {
            talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.WALKING_FORTRESS);
        }

        // Check if the player has the WALKING_FORTRESS perk or talent
        if ((activePet != null && activePet.hasPerk(PetManager.PetPerk.WALKING_FORTRESS)) || talent > 0) {
            int petLevel = activePet != null ? activePet.getLevel() : 0;

            // Calculate damage reduction percentage
            double damageReduction = Math.min(petLevel * 0.5, 50.0); // Cap at 80% reduction
            damageReduction += talent * 10;
            double reductionFactor = 1 - (damageReduction / 100.0);

            // Reduce the damage directly
            double originalDamage = event.getDamage();
            double reducedDamage = originalDamage * reductionFactor;
            event.setDamage(reducedDamage);

            // Notify the player of the damage reduction
            player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DAMAGE, 1.0f, 1.0f);
        }
    }
}
