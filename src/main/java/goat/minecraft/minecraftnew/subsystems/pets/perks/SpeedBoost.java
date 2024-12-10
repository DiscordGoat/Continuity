package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
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

        // If the player has an active pet with the SPEED_BOOST perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SPEED_BOOST)) {
            int petLevel = activePet.getLevel();
            float bonusSpeed = DEFAULT_WALK_SPEED + (DEFAULT_WALK_SPEED * petLevel * 0.004f); // Add 0.5% per level
            player.setWalkSpeed(Math.min(bonusSpeed, MAX_WALK_SPEED)); // Cap walk speed at 0.4
        } else {
            // Reset to default speed
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
        }
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
