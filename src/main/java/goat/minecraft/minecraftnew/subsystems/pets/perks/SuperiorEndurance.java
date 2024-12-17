package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperiorEndurance implements Listener {

    private final PetManager petManager;

    public SuperiorEndurance(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Check if the block is a crop
        if (!isCrop(block.getType())) {
            return;
        }

        // Get the player's active pet
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the SUPERIOR_ENDURANCE perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.SUPERIOR_ENDURANCE)) {
            // Increase the player's saturation by 1
            player.setSaturation(20);

            // Notify the player (optional)
        }
    }

    private boolean isCrop(Material material) {
        return switch (material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART, MELON, PUMPKIN -> true;
            default -> false;
        };
    }
}
