package goat.minecraft.minecraftnew.subsystems.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Defenestration enchantment effect. When an arrow fired from a bow
 * with this enchantment hits glass, the window shatters temporarily.
 */
public class Defenestration implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.hasEnchantment(bow, "Defenestration")) return;

        Block hit = event.getHitBlock();
        if (hit == null || !isGlass(hit.getType())) return;

        Set<Block> toBreak = findConnectedGlass(hit, 8);
        Map<Block, BlockData> original = new HashMap<>();

        for (Block b : toBreak) {
            original.put(b, b.getBlockData().clone());
            b.getWorld().spawnParticle(Particle.BLOCK_CRACK, b.getLocation().add(0.5,0.5,0.5), 15, 0.3,0.3,0.3, b.getBlockData());
            b.getWorld().playSound(b.getLocation(), Sound.BLOCK_GLASS_BREAK, 1f, 1f);
            b.setType(Material.AIR);
        }
        arrow.remove();
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Block, BlockData> entry : original.entrySet()) {
                    Block block = entry.getKey();
                    if (block.getType() == Material.AIR) {
                        block.setType(entry.getValue().getMaterial());
                        block.setBlockData(entry.getValue());
                    }
                }
            }
        }.runTaskLater(MinecraftNew.getInstance(), 8 * 20L);
    }

    private boolean isGlass(Material material) {
        String name = material.toString();
        return name.contains("GLASS");
    }

    private Set<Block> findConnectedGlass(Block start, int radius) {
        Set<Block> result = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start);
        result.add(start);

        Location origin = start.getLocation();
        int radiusSq = radius * radius;
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            for (BlockFace face : faces) {
                Block neighbor = current.getRelative(face);
                if (result.contains(neighbor)) continue;
                if (neighbor.getLocation().distanceSquared(origin) > radiusSq) continue;
                if (isGlass(neighbor.getType())) {
                    result.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return result;
    }
}
