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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Comfortable implements Listener {

    private final JavaPlugin plugin;

    public Comfortable(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the Comfortable perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.COMFORTABLE)) {
            int petLevel = activePet.getLevel();

            // Calculate absorption hearts (0.2 per level, cap at 20)
            int absorptionLevel = (int) Math.min(20, petLevel * 0.05);

            int duration = 2400; // 2 minutes
            int talent = 0;
            if (SkillTreeManager.getInstance() != null) {
                talent = SkillTreeManager.getInstance()
                        .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.COMFORTABLE);
            }
            if (talent > 0) {
                absorptionLevel *= 2;
                duration *= 2;
            }

            // Apply the absorption effect
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.ABSORPTION,
                    duration,
                    Math.max(0, absorptionLevel - 1),
                    true,
                    false
            ));

            // Notify the player
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 5, 100);
        }
    }
}
