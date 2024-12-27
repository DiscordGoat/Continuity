package goat.minecraft.minecraftnew.other;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Doors implements Listener {

    @EventHandler
    public void onDoorLeftClick(PlayerInteractEvent event) {
        // Check if the action is a left-click on a block
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        // Ensure it's actually a door
        if (!isDoor(clickedBlock.getType())) {
            return;
        }

        // If the top of the door was clicked, get the bottom half (or vice versa)
        Block bottomDoorBlock = getBottomPart(clickedBlock);

        // We want to open the bottom half. The top half will automatically update.
        Door doorData = (Door) bottomDoorBlock.getBlockData();
        if (doorData.isOpen()) {
            // Already open, do nothing or handle if you want to close immediately
            return;
        }

        // Open the single (or double) door
        openOrCloseDoor(bottomDoorBlock, true);

        // Play opening sound
        bottomDoorBlock.getWorld().playSound(bottomDoorBlock.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);

        // Check if there is a corresponding double door next to it; if so, open that, too
        Block doubleDoorBlock = findDoubleDoor(bottomDoorBlock);
        if (doubleDoorBlock != null) {
            openOrCloseDoor(getBottomPart(doubleDoorBlock), true);
            // Play opening sound for the double door
            doubleDoorBlock.getWorld().playSound(doubleDoorBlock.getLocation(), Sound.BLOCK_WOODEN_DOOR_OPEN, 1.0f, 1.0f);
        }

        // Schedule a task to close the door(s) after 2 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                // Close main door
                openOrCloseDoor(bottomDoorBlock, false);
                // Play closing sound
                bottomDoorBlock.getWorld().playSound(bottomDoorBlock.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1.0f, 1.0f);

                // Close double door, if any
                if (doubleDoorBlock != null) {
                    openOrCloseDoor(getBottomPart(doubleDoorBlock), false);
                    // Play closing sound for the double door
                    doubleDoorBlock.getWorld().playSound(doubleDoorBlock.getLocation(), Sound.BLOCK_WOODEN_DOOR_CLOSE, 1.0f, 1.0f);
                }
            }
        }.runTaskLater(MinecraftNew.getInstance(), 40L); // 40 ticks = 2 seconds
    }

    /**
     * Checks if a material is a door (could add more types if needed).
     */
    private boolean isDoor(Material material) {
        switch (material) {
            case OAK_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case CRIMSON_DOOR:
            case WARPED_DOOR:
                return true;
            default:
                return false;
        }
    }

    /**
     * Given a door block, open or close it.
     */
    private void openOrCloseDoor(Block doorBlock, boolean open) {
        BlockData data = doorBlock.getBlockData();
        if (data instanceof Openable) {
            Openable openable = (Openable) data;
            openable.setOpen(open);
            doorBlock.setBlockData(openable);
        }
    }

    /**
     * If the clicked part is the top half of the door, return the bottom half (and vice versa).
     * Usually, we need to manipulate the bottom block data for door state changes.
     */
    private Block getBottomPart(Block doorBlock) {
        Door doorData = (Door) doorBlock.getBlockData();
        if (doorData.getHalf() == Door.Half.TOP) {
            return doorBlock.getRelative(0, -1, 0);
        }
        return doorBlock;
    }

    /**
     * Attempt to find an adjacent door block that forms a double door with the given block.
     */
    private Block findDoubleDoor(Block bottomDoorBlock) {
        // The door’s facing direction
        Door doorData = (Door) bottomDoorBlock.getBlockData();
        // The left/right blocks next to the door, depending on door’s facing
        // Typically, double doors are side-by-side along the “left” or “right” relative to the door’s facing.

        // Let's check all four orthogonal directions for another door:
        // For example, to find an adjacent door if facing EAST, we'd check block to the NORTH or SOUTH, etc.

        // For simplicity, we check all horizontal neighbors:
        Block[] neighbors = {
                bottomDoorBlock.getRelative(1, 0, 0),
                bottomDoorBlock.getRelative(-1, 0, 0),
                bottomDoorBlock.getRelative(0, 0, 1),
                bottomDoorBlock.getRelative(0, 0, -1)
        };

        for (Block neighbor : neighbors) {
            if (neighbor.getType() == bottomDoorBlock.getType()) {
                Door neighborDoorData = (Door) neighbor.getBlockData();
                // Check if it’s aligned properly (e.g., same facing, different hinge might still count as a double door)
                if (neighborDoorData.getFacing() == doorData.getFacing()) {
                    return neighbor;
                }
            }
        }

        // If no suitable neighbor was found
        return null;
    }
}
