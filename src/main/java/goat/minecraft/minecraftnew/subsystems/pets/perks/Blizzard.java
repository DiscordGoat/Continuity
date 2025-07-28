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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class Blizzard implements Listener {

    private final PetManager petManager;

    public Blizzard(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // Ensure the damager is a player
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the BLIZZARD perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BLIZZARD)) {
            int petLevel = activePet.getLevel();

            // Ensure the damaged entity is a living entity
            if (event.getEntity() instanceof LivingEntity target) {
                // Apply the slowness effect
                int effectDuration = Math.max(petLevel * 20, 40); // Minimum duration of 2 seconds
                int talent = 0;
                if (SkillTreeManager.getInstance() != null) {
                    talent = SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.ASPECT_OF_FROST);
                }
                if (talent > 0) effectDuration *= 2;
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, effectDuration, 1));

                // Notify the player (optional)
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 10, 100);
            }
        }
    }
}
