package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Leap implements Listener {

    private JavaPlugin plugin;

    // Constructor to pass in XPManager and plugin instance
    public Leap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private static final double LEAP_MULTIPLIER = 2.0; // Forward boost multiplier
    private static final double LEAP_VERTICAL_BOOST = 0.5; // Upward boost multiplier

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Check if the player has started sneaking (not stopping)
        if (event.isSneaking()) {
            PetManager petManager = PetManager.getInstance(plugin);
            PetManager.Pet activePet = petManager.getActivePet(player);
            int talent = 0;
            if (SkillTreeManager.getInstance() != null) {
                talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.LEAP);
            }

            // Perform leap if conditions are met
            if (((activePet != null && activePet.hasPerk(PetManager.PetPerk.LEAP))) && player.isOnGround()) {
                performLeap(player, activePet, talent);
            }
        }
    }

    /**
     * Executes the leap ability if the player meets the conditions.
     *
     * @param player   The player executing the leap.
     * @param activePet The player's active pet.
     */
    public static void performLeap(Player player, PetManager.Pet activePet, int talentLevel) {
        // Get the player's current velocity
        Vector direction = player.getLocation().getDirection();

        // Calculate the leap velocity
        Vector leapBoost = direction.multiply(LEAP_MULTIPLIER).setY(LEAP_VERTICAL_BOOST);
        player.setVelocity(leapBoost);

        // Deduct saturation based on pet level
        int petLevel = activePet != null ? activePet.getLevel() : 0;
        int saturationCost = calculateSaturationCost(petLevel);
        if (talentLevel > 0 && Math.random() < talentLevel * 0.5) {
            saturationCost = 0; // chance to remove hunger cost
        }

        float currentSaturation = player.getSaturation();
        player.setSaturation(Math.max(0, currentSaturation - saturationCost));

        // Notify the player
        player.sendMessage(ChatColor.GREEN + "You used your Leap ability!");

        // Play a sound for feedback (optional)
        player.getWorld().playSound(player.getLocation(), "minecraft:entity.player.jump", 1.0f, 1.0f);
    }

    /**
     * Calculates the saturation cost based on the pet's level.
     *
     * @param level The level of the pet.
     * @return The saturation cost (0-2, depending on level).
     */
    private static int calculateSaturationCost(int level) {
        if (level >= 100) {
            return 0; // No saturation cost at level 100
        }
        return 2 - (level / 50); // Scales from 2 to 0 as level increases
    }
}
