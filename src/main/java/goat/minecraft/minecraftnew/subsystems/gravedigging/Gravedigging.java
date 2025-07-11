package goat.minecraft.minecraftnew.subsystems.gravedigging;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Random;

/**
 * Handles the gravedigging subsystem which spawns "graves" when
 * players break surface level blocks. Hitting a grave with a shovel
 * triggers a random event.
 */
public class Gravedigging implements Listener {
    private static final double BASE_CHANCE = 1.0; // 100% for testing
    private final Random random = new Random();

    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 13000 && time <= 23000;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        int highest = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() < highest) return; // only surface blocks

        double chance = BASE_CHANCE;
        if (isNight(world)) {
            chance = Math.min(1.0, chance * 2);
        }
        if (random.nextDouble() <= chance) {
            spawnGrave(loc);
        }
    }

    private void spawnGrave(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        Location spawnLoc = loc.clone().add(0.5, 0, 0.5);
        ArmorStand stand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(ChatColor.GRAY + "Grave");
        stand.setMarker(true);
        stand.getEquipment().setHelmet(new ItemStack(Material.STONE_BRICK_SLAB));
        stand.getEquipment().setHelmetDropChance(0f);
        stand.setMetadata("grave", new FixedMetadataValue(MinecraftNew.getInstance(), true));
        world.spawnParticle(Particle.SMOKE_NORMAL, spawnLoc.add(0, 0.5, 0), 20, 0.2, 0.2, 0.2, 0);
        world.playSound(spawnLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 0.6f);
    }

    @EventHandler
    public void onGraveHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand stand)) return;
        if (!stand.hasMetadata("grave")) return;
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.getType().toString().contains("SHOVEL")) {
            return; // only shovels interact
        }
        event.setCancelled(true);
        stand.remove();
        triggerEvent(player, stand.getLocation());
    }

    private void triggerEvent(Player player, Location loc) {
        double roll = random.nextDouble();
        if (roll < 0.5) {
            player.sendMessage(ChatColor.DARK_RED + "A Corpse stirs... (CorpseEvent)");
        } else if (roll < 0.85) {
            player.sendMessage(ChatColor.GOLD + "You find something... (LootEvent)");
        } else {
            player.sendMessage(ChatColor.AQUA + "Treasure unearthed! (TreasureEvent)");
        }
        loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 20, 0.2, 0.2, 0.2, Material.DIRT.createBlockData());
        loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);
    }
}
