package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class DiggingClaws implements Listener {

    private final PetManager petManager;

    public DiggingClaws(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the DIGGING_CLAWS perk or talent
        int talent = 0;
        if (SkillTreeManager.getInstance() != null) {
            talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.DIGGING_CLAWS);
        }
        if ((activePet != null && activePet.hasPerk(PetManager.PetPerk.DIGGING_CLAWS)) || talent > 0) {
            int petLevel = activePet != null ? activePet.getLevel() : 0;

            // Calculate the duration of the Haste effect
            int duration = 20 * (5 + petLevel);
            if (talent > 0) {
                duration *= 2; // talent doubles duration
            }

            // Apply Haste II effect
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.HASTE,
                    duration, // Duration in ticks
                    1, // Haste II
                    false,
                    true,
                    true
            ));

            // Optional: Notify the player

        }
    }
}
