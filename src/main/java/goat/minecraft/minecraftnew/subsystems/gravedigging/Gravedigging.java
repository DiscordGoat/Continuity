package goat.minecraft.minecraftnew.subsystems.gravedigging;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.gravedigging.corpses.CorpseEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
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
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;

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
    private final Map<java.util.UUID, Integer> blockBreaks = new HashMap<>();
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

        // X Marks The Spot - guaranteed grave after enough blocks
        int xLevel = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(),
                Skill.TERRAFORMING, Talent.X_MARKS_THE_SPOT);
        if (xLevel > 0) {
            int count = blockBreaks.getOrDefault(player.getUniqueId(), 0) + 1;
            double threshold = 512.0 / (xLevel * 0.1 + 1);
            if (count >= threshold) {
                spawnGraveNear(player);
                count = 0;
            }
            blockBreaks.put(player.getUniqueId(), count);
        }

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
        if(loc.getBlock().getType().equals(Material.DIRT) || loc.getBlock().getType().equals(Material.GRASS_BLOCK)) return;
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
        if (PotionManager.isActive("Potion of Metal Detection", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Metal Detection")) {
            chance += 0.01;
            if (SkillTreeManager.getInstance().hasTalent(player, Talent.METAL_DETECTION_MASTERY)) {
                int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.METAL_DETECTION_MASTERY);
                chance += 0.01 * level;
            }
        }
        int gd1 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_I);
        int gd2 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_II);
        int gd3 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_III);
        int gd4 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_IV);
        int gd5 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_V);
        chance += 0.001 * gd1;
        chance += 0.0015 * gd2;
        chance += 0.002 * gd3;
        chance += 0.0025 * gd4;
        chance += 0.00725 * gd5;

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.DEATH)) {
            Catalyst cat = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.DEATH);
            if (cat != null) {
                int tier = catalystManager.getCatalystTier(cat);
                chance += 0.01 + (tier * 0.001);
            }
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

        boolean indicator = false;
        int alive = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(),
                Skill.TERRAFORMING, Talent.ALIVE_TOMBSTONE);
        if (alive > 0 && Math.random() < alive * 0.20) {
            indicator = true;
        }

        for (int i = 0; i < 20; i++) {
            int dx = random.nextInt(17) - 8;
            int dz = random.nextInt(17) - 8;
            Location target = base.clone().add(dx, 0, dz);
            int y = world.getHighestBlockYAt(target) + 1;
            Block block = world.getBlockAt(target.getBlockX(), y - 1, target.getBlockZ());
            if (!block.getType().isAir() && !graves.containsKey(block.getLocation())) {
                // Particle marker & sound when grave appears
                startParticle(block.getLocation(), indicator);
                break;
            }
        }
    }

    private void startParticle(Location loc, boolean indicator) {
        World world = loc.getWorld();
        if (world == null) return;
        Location effectLoc = loc.clone().add(0.5, 1, 0.5);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            world.spawnParticle(Particle.SOUL, effectLoc, 3, 0.1, 0.1, 0.1, 0);
            if (indicator) {
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, effectLoc.clone().add(0,1,0), 1, 0,0,0,0);
            }
        }, 0L, 20L);
        graves.put(loc, task);
    }

    private void triggerEvent(Player player, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        int mass = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(),
                Skill.TERRAFORMING, Talent.MASS_GRAVE);
        double corpseChance = 0.5 + mass * 0.10;

        double roll = random.nextDouble();
        Location center = loc.clone().add(0.5, 2, 0.5);

        if (roll < corpseChance) {
            // --- CORPSE EVENT ---
            world.spawnParticle(Particle.EXPLOSION, center, 10, 0, 0, 0, 0);
            world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
            new CorpseEvent(plugin).trigger(center);
            int dt = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(),
                    Skill.TERRAFORMING, Talent.DOUBLE_TROUBLE);
            if (dt > 0 && Math.random() < dt * 0.01) {
                new CorpseEvent(plugin).trigger(center);
            }
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

        int gy1 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVEYARD_I);
        int gy2 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVEYARD_II);
        int gy3 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVEYARD_III);
        int gy4 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVEYARD_IV);
        int gy5 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVEYARD_V);
        double chainChance = 0.025 * (gy1 + gy2 + gy3 + gy4 + gy5);
        if (Math.random() < chainChance) {
            spawnGraveNear(player);
        }

    }

}
