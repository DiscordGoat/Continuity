package goat.minecraft.minecraftnew.subsystems.gravedigging;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Handles the gravedigging subsystem which spawns "graves" when
 * players break surface level blocks. Hitting a grave with a shovel
 * triggers a random event.
 */
public class Gravedigging implements Listener {
    private static final double BASE_CHANCE = 0.0005; // 0.01%
    private final Random random = new Random();
    private final Map<Location, BukkitTask> graves = new HashMap<>();
    private final JavaPlugin plugin;

    public Gravedigging(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void startup() {
        World world = Bukkit.getWorld("world");
        if (world == null) {
            Bukkit.getLogger().warning("World 'world' not found!");
            return;
        }

        // Iterate over all registered NPCs
        for (NPC npc : CitizensAPI.getNPCRegistry()) {
            // Make sure the NPC is spawned in our target world
            if (npc.isSpawned() && npc.getEntity().getWorld().equals(world)) {
                // Despawn and deregister the NPC
                npc.despawn();
                CitizensAPI.getNPCRegistry().deregister(npc);
            }
        }

        Bukkit.getLogger().info("All Citizens NPCs in world 'world' have been cleared.");
    }
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

        // If there's already a grave here, trigger it
        if (graves.containsKey(loc)) {
            BukkitTask task = graves.remove(loc);
            if (task != null) task.cancel();
            triggerEvent(player, loc);
            event.setCancelled(true);
            return;
        }

        // Only surface blocks
        int highest = world.getHighestBlockYAt(loc);
        if (loc.getBlockY() < highest) return;

        // Calculate chance
        double chance = BASE_CHANCE;
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.getType().toString().endsWith("_SHOVEL")) {
            int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Lynch");
            chance += level * 0.0002;
        }
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null) {
            if (activePet.hasPerk(PetManager.PetPerk.MEMORY)) {
                chance += 0.001;
            }
            if (activePet.hasPerk(PetManager.PetPerk.HAUNTING)) {
                chance += 0.002;
            }
            if (activePet.hasPerk(PetManager.PetPerk.SCREAM)) {
                chance += 0.004;
            }
            if (activePet.hasPerk(PetManager.PetPerk.COLD)) {
                chance += 0.005;
            }
            if (activePet.hasPerk(PetManager.PetPerk.MALIGNANCE)) {
                chance += 0.010;
            }
            if (activePet.getTrait() == PetTrait.PARANORMAL) {
                chance += activePet.getTrait().getValueForRarity(activePet.getTraitRarity());
            }
        }
        if (PotionManager.isActive("Potion of Metal Detection", player)) {
            chance += 0.01;
        }
        if (isNight(world)) {
            chance = Math.min(1.0, chance * 2);
        }

        // Roll for grave spawn
        if (random.nextDouble() <= chance) {
            spawnGraveNear(player);
        }
    }

    private void spawnGraveNear(Player player) {
        Location base = player.getLocation();
        World world = base.getWorld();
        if (world == null) return;

        for (int i = 0; i < 20; i++) {
            int dx = random.nextInt(17) - 8;
            int dz = random.nextInt(17) - 8;
            Location target = base.clone().add(dx, 0, dz);
            int y = world.getHighestBlockYAt(target) + 1;
            Block block = world.getBlockAt(target.getBlockX(), y - 1, target.getBlockZ());
            if (!block.getType().isAir() && !graves.containsKey(block.getLocation())) {
                // Particle marker & sound when grave appears
                startParticle(block.getLocation());
                break;
            }
        }
    }

    private void startParticle(Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        Location effectLoc = loc.clone().add(0.5, 1, 0.5);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            world.spawnParticle(Particle.SOUL, effectLoc, 3, 0.1, 0.1, 0.1, 0);
        }, 0L, 20L);
        graves.put(loc, task);
    }

    private void triggerEvent(Player player, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        double roll = random.nextDouble();
        Location center = loc.clone().add(0.5, 2, 0.5);

        if (roll < 0.5) {
            // --- CORPSE EVENT ---
            world.spawnParticle(Particle.EXPLOSION, center, 10, 0, 0, 0, 0);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            new CorpseEvent(plugin).trigger(center);
        } else {
            // --- TREASURE EVENT ---
            ItemStack relic = ItemRegistry.getRandomVerdantRelicSeed();
            world.dropItemNaturally(center, relic);
            world.playSound(center, Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.8f);
            String name = relic.getItemMeta() != null
                    ? relic.getItemMeta().getDisplayName()
                    : "Relic";
            player.sendMessage(ChatColor.AQUA
                    + "You uncover a treasure! ("
                    + name
                    + ChatColor.AQUA
                    + ")");
        }

        // common dust + gravel sound for breaking the grave
        world.spawnParticle(
                Particle.BLOCK,
                loc,
                20,
                0.2, 0.2, 0.2,
                Material.DIRT.createBlockData()
        );
        world.playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1.0f, 1.0f);
    }

}
