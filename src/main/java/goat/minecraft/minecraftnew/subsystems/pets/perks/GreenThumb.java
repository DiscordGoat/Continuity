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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GreenThumb implements Listener {
    private final PetManager petManager;
    private final Map<UUID, Long> lastGrowthTime = new HashMap<>();
    private static final long GROWTH_COOLDOWN = 60 * 1000; // 5 seconds cooldown

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
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        // Check if enough time has passed since last growth
        long currentTime = System.currentTimeMillis();
        if (lastGrowthTime.containsKey(player.getUniqueId()) &&
                currentTime - lastGrowthTime.get(player.getUniqueId()) < GROWTH_COOLDOWN) {
            return;
        }

        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.GREEN_THUMB)) {
            int radius = 10 + activePet.getLevel();
            growCropsAroundPlayer(player, radius);
            lastGrowthTime.put(player.getUniqueId(), currentTime);
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
