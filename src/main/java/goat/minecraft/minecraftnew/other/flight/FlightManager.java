package goat.minecraft.minecraftnew.other.flight;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.Pet;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.PetPerk;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Central manager for all flight-related behaviour.
 * Handles flight enabling/disabling, flight value tracking,
 * depletion, regeneration and catalyst bonuses.
 */
public class FlightManager implements Listener {

    private static FlightManager instance;

    private final JavaPlugin plugin;
    private final PetManager petManager;
    private final PlayerMeritManager meritManager;

    private final Map<UUID, Integer> flight = new HashMap<>();
    private final Map<UUID, Integer> flightValue = new HashMap<>();
    private final Map<UUID, Integer> regenCounter = new HashMap<>();

    private BukkitRunnable task;

    public FlightManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
        this.meritManager = PlayerMeritManager.getInstance(plugin);
        instance = this;
        startTask();
    }

    public static FlightManager getInstance() {
        return instance;
    }

    private void startTask() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    handleTick(player);
                }
            }
        };
        task.runTaskTimer(plugin, 20L, 20L); // every second
    }

    private void handleTick(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetPerk.BROOMSTICK)) {
            // Broomstick provides unlimited flight, skip tracking.
            flight.remove(player.getUniqueId());
            flightValue.remove(player.getUniqueId());
            regenCounter.remove(player.getUniqueId());
            return;
        }

        UUID id = player.getUniqueId();

        int max = calculateFlight(player, activePet);
        flight.put(id, max);

        boolean nearCatalyst = false;
        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null) {
            nearCatalyst = catalystManager.isNearCatalyst(player.getLocation(), CatalystType.FLIGHT);
        }

        flightValue.putIfAbsent(id, max);
        if (!nearCatalyst && flightValue.get(id) > max) {
            flightValue.put(id, max);
        }

        boolean hasFlightPerk = activePet != null && activePet.hasPerk(PetPerk.FLIGHT);

        // Deplete while flying if not protected by a catalyst
        if (player.isFlying() && !nearCatalyst) {
            int val = flightValue.get(id) - 1;
            flightValue.put(id, val);
            if (val <= 0) {
                disableFlight(player);
            }
        }

        // Regeneration from perk every 5 seconds
        if (hasFlightPerk) {
            int counter = regenCounter.getOrDefault(id, 0) + 1;
            if (counter >= 5) {
                counter = 0;
                int val = flightValue.get(id) + 1;
                if (!nearCatalyst && val > max) {
                    val = max;
                }
                flightValue.put(id, val);
            }
            regenCounter.put(id, counter);
        }

        // Catalyst regeneration bonus
        if (nearCatalyst && hasFlightPerk) {
            flightValue.put(id, flightValue.get(id) + 1);
        }

        if (flightValue.get(id) > 0 && max > 0) {
            enableFlight(player);
        } else {
            disableFlight(player);
        }
    }

    private int calculateFlight(Player player, Pet activePet) {
        int seconds = 0;
        if (activePet != null && activePet.hasPerk(PetPerk.FLIGHT)) {
            int perkSeconds = activePet.getLevel(); // 1s per level
            if (meritManager.hasPerk(player.getUniqueId(), "Icarus")) {
                perkSeconds *= 2;
            }
            seconds += perkSeconds;
        }
        if (SkillTreeManager.getInstance() != null) {
            int talentLevel = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.FLIGHT);
            seconds += talentLevel * 40;
        }
        return seconds;
    }

    private void enableFlight(Player player) {
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
        }
    }

    private void disableFlight(Player player) {
        if (player.getAllowFlight()) {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Pet pet = petManager.getActivePet(player);
        int max = calculateFlight(player, pet);
        flight.put(player.getUniqueId(), max);
        flightValue.put(player.getUniqueId(), max);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        flight.remove(id);
        flightValue.remove(id);
        regenCounter.remove(id);
    }

    /**
     * Current flight value of player.
     */
    public int getFlightValue(Player player) {
        return flightValue.getOrDefault(player.getUniqueId(), 0);
    }

    /**
     * Maximum flight time for player.
     */
    public int getFlight(Player player) {
        return flight.getOrDefault(player.getUniqueId(), 0);
    }
}
