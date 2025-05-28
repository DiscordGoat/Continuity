package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class SpiderSteve implements Listener {

    private final PetManager petManager;

    public SpiderSteve(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        plugin.getServer()
                .getPluginManager()
                .registerEvents(this, plugin);
    }

    @EventHandler
    public void onEmptyHandClick(PlayerInteractEvent event) {
        // Only main-hand left-click block, and hand must be empty
        if (event.getHand() != EquipmentSlot.HAND ||
                event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            return;
        }

        // Must have the SpiderSteve perk active
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null ||
                !activePet.hasPerk(PetManager.PetPerk.SPIDER_STEVE)) {
            return;
        }

        // Compute vector toward the clicked block's center
        Location blockLoc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
        Vector direction = blockLoc.toVector()
                .subtract(player.getLocation().toVector())
                .normalize()
                .multiply(0.4);  // horizontal pull strength

        // If player clicked the TOP face, vault up onto the block
        if (event.getBlockFace() == BlockFace.UP) {
            direction.setY(0.5);  // adjust for desired vault height
        }

        // Apply the velocity boost
        player.setVelocity(direction);
    }
}
