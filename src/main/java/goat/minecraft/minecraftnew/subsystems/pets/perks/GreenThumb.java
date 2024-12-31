package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GreenThumb implements Listener {
    private final PetManager petManager;
    private final Map<UUID, Long> lastMovementTime = new HashMap<>();
    private final Map<UUID, Long> lastGrowthTime = new HashMap<>();
    private static final long GROWTH_COOLDOWN = 300 * 1000; // 5 minutes cooldown

    private final List<Material> CROP_TYPES = Arrays.asList(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.MELON_STEM, Material.PUMPKIN_STEM,
            Material.NETHER_WART);

    public GreenThumb(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Track player's last movement time
        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            lastMovementTime.put(playerId, System.currentTimeMillis());
            return;
        }

        // Check if the player is standing still for 5 minutes
        long currentTime = System.currentTimeMillis();
        if (lastMovementTime.containsKey(playerId) &&
                currentTime - lastMovementTime.get(playerId) < GROWTH_COOLDOWN) {
            return; // Player hasn't been idle long enough
        }

        // Check cooldown for crop growth
        if (lastGrowthTime.containsKey(playerId) &&
                currentTime - lastGrowthTime.get(playerId) < GROWTH_COOLDOWN) {
            return; // Growth cooldown hasn't passed
        }

        // Check if player has the Green Thumb perk
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.GREEN_THUMB)) {
            int radius = 10 + activePet.getLevel();
            growCropsAroundPlayer(player, radius);
            player.sendMessage(ChatColor.YELLOW + "Your pet naturally grows nearby crops!");
            lastGrowthTime.put(playerId, currentTime); // Update growth time
        }
    }

    private void growCropsAroundPlayer(Player player, int radius) {
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();

                    if (CROP_TYPES.contains(block.getType())) {
                        if (block.getBlockData() instanceof Ageable crop) {
                            if (crop.getAge() < crop.getMaximumAge()) {
                                crop.setAge(crop.getAge() + 1);
                                block.setBlockData(crop);
                            }
                        }
                    }
                }
            }
        }
    }
}
