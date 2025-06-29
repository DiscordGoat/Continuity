package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.Float;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Grants unlimited flight to the player but forces their health to remain at one
 * heart while flying. Hunger and saturation are restored when flight ends.
 */
public class Broomstick implements Listener {

    private final PetManager petManager;
    private final Map<UUID, Double> previousHealth = new HashMap<>();
    private final Map<UUID, Integer> previousFood = new HashMap<>();
    private final Map<UUID, Float> previousSaturation = new HashMap<>();

    public Broomstick(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    /**
     * Ensure players with the perk can take flight.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PetManager.Pet activePet = petManager.getActivePet(player);

        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.BROOMSTICK)) {
            if (!player.getAllowFlight()) {
                player.setAllowFlight(true);
            }
        }
    }

    /**
     * Handle the transition into and out of flight to adjust health accordingly.
     */
    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        PetManager.Pet activePet = petManager.getActivePet(player);

        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.BROOMSTICK)) {
            return;
        }

        if (event.isFlying()) {
            UUID id = player.getUniqueId();
            previousHealth.put(id, player.getHealth());
            previousFood.put(id, player.getFoodLevel());
            previousSaturation.put(id, player.getSaturation());

            if (player.getHealth() > 1.0) {
                player.setHealth(1.0);
            }
        } else {
            UUID id = player.getUniqueId();
            if (previousHealth.containsKey(id)) {
                double prev = previousHealth.remove(id);
                if (player.getHealth() < prev) {
                    player.setHealth(Math.min(prev, player.getMaxHealth()));
                }
            }
            if (previousFood.containsKey(id)) {
                int prevFood = previousFood.remove(id);
                if (player.getFoodLevel() < prevFood) {
                    player.setFoodLevel(Math.min(prevFood, 20));
                }
            }
            if (previousSaturation.containsKey(id)) {
                java.lang.Float prevSat = previousSaturation.remove(id);
                if (player.getSaturation() < prevSat) {
                    player.setSaturation(Math.min(prevSat, player.getFoodLevel()));
                }
            }
        }
    }
}
