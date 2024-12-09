package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GreenThumb implements Listener {

    private final PetManager petManager;

    public GreenThumb(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check if any player has the GREEN_THUMB perk enabled
        boolean isGreenThumbEnabled = Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.isOnGround() && player.getGameMode() == GameMode.SURVIVAL)
                .map(petManager::getActivePet)
                .anyMatch(activePet -> activePet != null && activePet.hasPerk(PetManager.PetPerk.GREEN_THUMB));

        if (isGreenThumbEnabled) {
            int maxLevel = Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.isOnGround() && player.getGameMode() == GameMode.SURVIVAL)
                    .map(petManager::getActivePet)
                    .filter(activePet -> activePet != null && activePet.hasPerk(PetManager.PetPerk.GREEN_THUMB))
                    .mapToInt(PetManager.Pet::getLevel)
                    .max()
                    .orElse(1);

            int tickSpeedIncrease = maxLevel * 1; // Increase tick speed by 1 per level
            Bukkit.getWorld("world").setGameRule(GameRule.RANDOM_TICK_SPEED, tickSpeedIncrease);
        } else {
            // Reset to default tick speed if no player has GREEN_THUMB
            Bukkit.getWorld("world").setGameRule(GameRule.RANDOM_TICK_SPEED, 3);
        }
    }
}
