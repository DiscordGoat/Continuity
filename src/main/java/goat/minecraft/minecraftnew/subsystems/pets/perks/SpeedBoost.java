package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpeedBoost implements Listener {

    private final PetManager petManager;

    public SpeedBoost(PetManager petManager) {
        this.petManager = petManager;
        Bukkit.getPluginManager().registerEvents(this, petManager.getPlugin());
    }

    private static final float DEFAULT_WALK_SPEED = 0.2f; // Minecraft's default walk speed
    private static final float MAX_WALK_SPEED = 0.4f; // Maximum walk speed with the pet boost

    /**
     * Adjusts the player's walk speed based on whether they have the Speed Boost perk active.
     *
     * @param player The player whose speed to adjust.
     */
    private void adjustWalkSpeed(Player player) {
        PetManager.Pet activePet = petManager.getActivePet(player);
        int talent = 0;
        if (SkillTreeManager.getInstance() != null) {
            talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.SPEED_BOOST);
        }

        float speed = DEFAULT_WALK_SPEED;
        if (activePet != null) {
            // Apply Fast trait bonus first
            if (activePet.getTrait() == goat.minecraft.minecraftnew.subsystems.pets.PetTrait.FAST) {
                double bonusPercent = activePet.getTrait().getValueForRarity(activePet.getTraitRarity());
                speed *= (1.0 + bonusPercent / 100.0);
            }

            // Apply Speed Boost perk bonus if present
            if (activePet.hasPerk(PetManager.PetPerk.SPEED_BOOST) || talent > 0) {
                int petLevel = activePet.getLevel();
                speed += DEFAULT_WALK_SPEED * petLevel * 0.004f; // Add 0.5% per level of the pet
                speed *= (1 + talent * 0.10);
            }
        }

        player.setWalkSpeed(Math.min(speed, MAX_WALK_SPEED));
    }

    /**
     * Triggered when a player moves. Updates their walk speed if necessary.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        adjustWalkSpeed(player);
    }

    /**
     * Ensures walk speed is properly set when the player joins.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        adjustWalkSpeed(player);
    }
}
