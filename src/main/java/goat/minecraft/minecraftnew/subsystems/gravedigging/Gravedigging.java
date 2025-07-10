package goat.minecraft.minecraftnew.subsystems.gravedigging;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class Gravedigging implements Listener {
    private static final double TEST_GRAVE_CHANCE = 1.0; // 100% for testing

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        int highest = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() >= highest) {
            if (Math.random() <= TEST_GRAVE_CHANCE) {
                spawnGrave(loc);
            }
        }
    }

    private void spawnGrave(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        Location spawnLoc = loc.clone().add(0.5, 0, 0.5);
        ArmorStand stand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setCustomName("Grave");
        stand.setCustomNameVisible(true);
        stand.getEquipment().setHelmet(new ItemStack(Material.STONE_BRICK_SLAB));
        stand.addScoreboardTag("gravedigging_grave");
        world.spawnParticle(Particle.SOUL, spawnLoc.add(0, 0.5, 0), 20, 0.3, 0.3, 0.3, 0.05);
        world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
    }

    @EventHandler
    public void onGraveHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.getScoreboardTags().contains("gravedigging_grave")) return;
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || !tool.getType().name().endsWith("_SHOVEL")) return;

        event.setCancelled(true);
        stand.remove();

        double roll = Math.random();
        if (roll < 0.50) {
            player.sendMessage(ChatColor.DARK_GREEN + "A Corpse stumbles out of the grave!");
        } else if (roll < 0.85) {
            player.sendMessage(ChatColor.GOLD + "You find some loot buried here.");
        } else {
            player.sendMessage(ChatColor.AQUA + "A hidden treasure gleams in the dirt!");
        }
    }
}
