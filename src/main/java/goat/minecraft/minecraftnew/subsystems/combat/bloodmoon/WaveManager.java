package goat.minecraft.minecraftnew.subsystems.combat.bloodmoon;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class WaveManager {
    private static final Range FAR_RANGE = new Range(60,80);
    private static final Range MID_RANGE = new Range(40,60);

    private final JavaPlugin plugin;
    private final Random random = new Random();
    private final Logger logger;

    public WaveManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void startSimulation(Player player, WaveDifficulty difficulty) {
        player.getWorld().setTime(18000L);
        logger.info("[Bloodmoon] Starting " + difficulty.name().toLowerCase() + " simulation for " + player.getName());
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }
                if (difficulty == WaveDifficulty.CARNAGE && count >= 15) { cancel(); return; }
                logger.info("[Bloodmoon] Launching " + difficulty.name().toLowerCase() + " wave #" + (count + 1) + " for " + player.getName());
                launchWave(player, difficulty);
                count++;
            }
        }.runTaskTimer(plugin, 0L, 20L * 60L);
    }

    private void launchWave(Player player, WaveDifficulty difficulty) {
        switch (difficulty) {
            case SKIRMISH -> {
                for (int i=0;i<3;i++) spawnGroup(player,4,8,FAR_RANGE,false);
            }
            case CLASH -> {
                for (int i=0;i<3;i++) spawnGroup(player,8,12,MID_RANGE,false);
            }
            case ASSAULT -> spawnGroup(player,20,40,FAR_RANGE,true);
            case ONSLAUGHT -> {
                for (int i=0;i<2;i++) spawnGroup(player,20,40,MID_RANGE,true);
            }
            case CARNAGE -> {
                for (int i=0;i<5;i++) spawnGroup(player,20,20,MID_RANGE,true);
            }
        }
    }

    private void spawnGroup(Player player, int min, int max, Range range, boolean pathfind) {
        Location base = findOptimalMonsterNode(player, range);
        if (base == null) return;
        int amount = random.nextInt(max - min + 1) + min;
        logger.fine("[Bloodmoon] Spawning group of " + amount + " mobs at (" + base.getBlockX() + "," + base.getBlockY() + "," + base.getBlockZ() + ") pathfind=" + pathfind);
        for (int i=0;i<amount;i++) {
            EntityType type = pickMonsterType();
            Monster mob = (Monster) player.getWorld().spawnEntity(base, type);
            if (pathfind) mob.setTarget(player);
        }
        spawnCaptain(player, base, pathfind);
    }

    private EntityType pickMonsterType() {
        if (random.nextDouble() < 0.2) return EntityType.CREEPER;
        List<EntityType> list = Arrays.asList(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER);
        return list.get(random.nextInt(list.size()));
    }

    private void spawnCaptain(Player player, Location base, boolean pathfind) {
        WitherSkeleton captain = (WitherSkeleton) player.getWorld().spawnEntity(base, EntityType.WITHER_SKELETON);
        captain.setCustomName(ChatColor.DARK_RED + "Captain");
        captain.setCustomNameVisible(true);
        ItemStack helm = enchanted(Material.NETHERITE_HELMET);
        ItemStack chest = enchanted(Material.NETHERITE_CHESTPLATE);
        ItemStack legs = enchanted(Material.NETHERITE_LEGGINGS);
        ItemStack boots = enchanted(Material.NETHERITE_BOOTS);
        captain.getEquipment().setHelmet(helm);
        captain.getEquipment().setChestplate(chest);
        captain.getEquipment().setLeggings(legs);
        captain.getEquipment().setBoots(boots);
        captain.getEquipment().setHelmetDropChance(0f);
        captain.getEquipment().setChestplateDropChance(0f);
        captain.getEquipment().setLeggingsDropChance(0f);
        captain.getEquipment().setBootsDropChance(0f);
        if (pathfind) captain.setTarget(player);
        logger.fine("[Bloodmoon] Captain spawned at (" + base.getBlockX() + "," + base.getBlockY() + "," + base.getBlockZ() + ") for " + player.getName());
    }

    private ItemStack enchanted(Material mat) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public Location findOptimalMonsterNode(Player player, Range range) {
        World world = player.getWorld();
        Location best = null;
        double bestScore = Double.MAX_VALUE;
        for (int i=0;i<40;i++) {
            double angle = random.nextDouble()*Math.PI*2;
            double radius = range.random(random);
            int x = player.getLocation().getBlockX() + (int)(Math.cos(angle)*radius);
            int z = player.getLocation().getBlockZ() + (int)(Math.sin(angle)*radius);
            int y = world.getHighestBlockYAt(x, z);
            Location loc = new Location(world, x + 0.5, y, z + 0.5);
            if (!isValidSpawnLocation(loc)) continue;
            double var = terrainVariance(world, x, z);
            if (var < bestScore) {
                bestScore = var;
                best = loc;
            }
        }
        return best;
    }

    private boolean isValidSpawnLocation(Location loc) {
        if (loc.getBlock().isLiquid()) return false;
        if (loc.getBlock().getLightLevel() > 7) return false;
        for (int dx=-1;dx<=1;dx++) {
            for (int dz=-1;dz<=1;dz++) {
                if (loc.clone().add(dx,0,dz).getBlock().isLiquid()) return false;
            }
        }
        return true;
    }

    private double terrainVariance(World world, int baseX, int baseZ) {
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int dx=-1;dx<=1;dx++) {
            for (int dz=-1;dz<=1;dz++) {
                int y = world.getHighestBlockYAt(baseX+dx, baseZ+dz);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        return maxY - minY;
    }
}
