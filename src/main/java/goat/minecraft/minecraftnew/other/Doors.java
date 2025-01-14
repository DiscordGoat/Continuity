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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

public class Doors implements Listener {

    @EventHandler
    public void onDoorLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        if (!isDoor(clickedBlock.getType())) {
            return;
        }

        Block bottomDoorBlock = getBottomPart(clickedBlock);
        Door doorData = (Door) bottomDoorBlock.getBlockData();
        if (doorData.isOpen()) {
            return;
        }

        openOrCloseDoor(bottomDoorBlock, true);
        playDoorSound(bottomDoorBlock, true);

        Block doubleDoorBlock = findDoubleDoor(bottomDoorBlock);
        if (doubleDoorBlock != null) {
            openOrCloseDoor(getBottomPart(doubleDoorBlock), true);
            playDoorSound(doubleDoorBlock, true);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                openOrCloseDoor(bottomDoorBlock, false);
                playDoorSound(bottomDoorBlock, false);

                if (doubleDoorBlock != null) {
                    openOrCloseDoor(getBottomPart(doubleDoorBlock), false);
                    playDoorSound(doubleDoorBlock, false);
                }
            }
        }.runTaskLater(MinecraftNew.getInstance(), 40L);
    }

    private boolean isDoor(Material material) {
        return material == Material.IRON_DOOR || material.toString().endsWith("_DOOR");
    }

    private void openOrCloseDoor(Block doorBlock, boolean open) {
        BlockData data = doorBlock.getBlockData();
        if (data instanceof Openable) {
            Openable openable = (Openable) data;
            openable.setOpen(open);
            doorBlock.setBlockData(openable);
        }
    }

    private Block getBottomPart(Block doorBlock) {
        Door doorData = (Door) doorBlock.getBlockData();
        return doorData.getHalf() == Door.Half.TOP ? doorBlock.getRelative(0, -1, 0) : doorBlock;
    }

    private Block findDoubleDoor(Block bottomDoorBlock) {
        Door doorData = (Door) bottomDoorBlock.getBlockData();
        Block[] neighbors = {
                bottomDoorBlock.getRelative(1, 0, 0),
                bottomDoorBlock.getRelative(-1, 0, 0),
                bottomDoorBlock.getRelative(0, 0, 1),
                bottomDoorBlock.getRelative(0, 0, -1)
        };

        for (Block neighbor : neighbors) {
            if (neighbor.getType() == bottomDoorBlock.getType()) {
                Door neighborDoorData = (Door) neighbor.getBlockData();
                if (neighborDoorData.getFacing() == doorData.getFacing()) {
                    return neighbor;
                }
            }
        }
        return null;
    }

    private void playDoorSound(Block doorBlock, boolean opening) {
        Material type = doorBlock.getType();
        Sound sound = (type == Material.IRON_DOOR) ?
                (opening ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE) :
                (opening ? Sound.BLOCK_WOODEN_DOOR_OPEN : Sound.BLOCK_WOODEN_DOOR_CLOSE);

        doorBlock.getWorld().playSound(doorBlock.getLocation(), sound, 1.0f, 1.0f);
    }
}
