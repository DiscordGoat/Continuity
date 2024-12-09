package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class XRay implements Listener {

    private final JavaPlugin plugin;
    private final PetManager petManager;
    private final Map<UUID, List<ArmorStand>> xrayArmorStands = new HashMap<>();

    public XRay(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the X-Ray perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.X_RAY)) {
            // Scan for ores and highlight them
            scanAndHighlightOres(player);
        } else {
            // Remove any existing armor stands if the player no longer has the perk
            removeXRayArmorStands(player);
        }
    }

    private void scanAndHighlightOres(Player player) {
        UUID playerId = player.getUniqueId();
        List<ArmorStand> newStands = new ArrayList<>();

        Location playerLocation = player.getLocation();
        int radius = 10; // The radius to scan for ores
        World world = player.getWorld();
        int px = playerLocation.getBlockX();
        int py = playerLocation.getBlockY();
        int pz = playerLocation.getBlockZ();

        // Define a set of ore materials to look for
        Set<Material> oreMaterials = EnumSet.of(
                Material.COAL_ORE,
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.NETHER_GOLD_ORE,
                Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS,
                Material.COPPER_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.DEEPSLATE_COPPER_ORE
        );

        // Remove old armor stands
        removeXRayArmorStands(player);

        // Scan for ores in the radius
        for (int x = px - radius; x <= px + radius; x++) {
            for (int y = py - radius; y <= py + radius; y++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (oreMaterials.contains(block.getType())) {
                        // Spawn an invisible armor stand at the block location
                        Location loc = block.getLocation().add(0.5, 0.5, 0.5); // Center in the block
                        ArmorStand armorStand = world.spawn(loc, ArmorStand.class, stand -> {
                            stand.setVisible(false);
                            stand.setMarker(true);
                            stand.setCustomNameVisible(true);
                            stand.setCustomName(ChatColor.GOLD + formatOreName(block.getType()));
                            stand.setGravity(false);
                            stand.setGlowing(true);
                            stand.setSmall(true);
                            stand.setInvulnerable(true);
                            stand.setMetadata("xrayArmorStand", new FixedMetadataValue(plugin, true));
                        });
                        newStands.add(armorStand);
                    }
                }
            }
        }

        // Save the new armor stands
        xrayArmorStands.put(playerId, newStands);
    }

    private void removeXRayArmorStands(Player player) {
        UUID playerId = player.getUniqueId();
        List<ArmorStand> stands = xrayArmorStands.remove(playerId);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
    }

    private String formatOreName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return ChatColor.stripColor(ChatColor.GOLD + name.substring(0, 1).toUpperCase() + name.substring(1));
    }
}
