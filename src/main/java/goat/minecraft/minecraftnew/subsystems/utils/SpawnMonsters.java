package goat.minecraft.minecraftnew.subsystems.utils;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SpawnMonsters implements Listener {

    private XPManager xpManager; // Assume you have an XPManager class
    private Plugin plugin;

    private static final int MAX_MONSTER_LEVEL = 200;

    // Constructor to pass in XPManager and plugin instance
    public SpawnMonsters(Plugin plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }

    public static int getDayCount(Player player) {
        int playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        return playTimeTicks / 24000; // 1 Minecraft day = 24000 ticks
    }

    @EventHandler
    public void alterMonsters(EntitySpawnEvent e) {
        Entity entity = e.getEntity();

        // Remove creepers with 80% chance
        if (entity instanceof Creeper && Math.random() < 0.8) {
            entity.remove();
            return;
        }

        Random random = new Random();
        if (entity instanceof LivingEntity) {
            if (entity instanceof ArmorStand) return;

            LivingEntity mob = (LivingEntity) entity;
            if (mob instanceof EnderDragon) {
                int randomValue = Math.min(200 + random.nextInt(101), MAX_MONSTER_LEVEL);
                applyMobAttributes(mob, randomValue);
                return;
            }

            double distance = getDistanceFromOrigin(entity);
            Player nearestPlayer = getNearestPlayer(entity, 100);
            int mobLevel;

            if (nearestPlayer != null) {
                int dayCount = getDayCount(nearestPlayer);
                mobLevel = Math.min(dayCount + getRandomLevelVariation(), MAX_MONSTER_LEVEL);
            } else {
                mobLevel = Math.min((int) (distance / 100) + getRandomLevelVariation(), MAX_MONSTER_LEVEL);
            }

            applyMobAttributes(mob, mobLevel);
        }

        if (entity instanceof WaterMob) {
            Player player = getNearestPlayer(entity, 1000);
            int fishingLevel = xpManager.getPlayerLevel(player, "Fishing");
            int level = Math.min(fishingLevel, MAX_MONSTER_LEVEL);
            applyMobAttributes((LivingEntity) entity, level);
        }
    }

    public double getDistanceFromOrigin(Entity mob) {
        Location mobLocation = mob.getLocation();
        double x = mobLocation.getX();
        double z = mobLocation.getZ();
        return Math.sqrt(x * x + z * z);
    }

    public Player getNearestPlayer(Entity entity, double radius) {
        Location mobLocation = entity.getLocation();
        double nearestDistanceSquared = radius * radius;
        Player nearestPlayer = null;

        for (Player player : entity.getWorld().getPlayers()) {
            double distanceSquared = player.getLocation().distanceSquared(mobLocation);
            if (distanceSquared <= nearestDistanceSquared) {
                nearestDistanceSquared = distanceSquared;
                nearestPlayer = player;
            }
        }
        return nearestPlayer;
    }

    public void applyMobAttributes(LivingEntity mob, int level) {
        level = Math.max(1, Math.min(level, MAX_MONSTER_LEVEL)); // Cap level between 1 and 300

        double healthMultiplier = 1 + (level * 0.1);

        double originalHealth = mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double newHealth = originalHealth * healthMultiplier;
        newHealth = Math.min(newHealth, 2000); // Cap health at 2000
        mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
        mob.setHealth(newHealth);

        mob.setMetadata("mobLevel", new FixedMetadataValue(plugin, level));

        String color = getColorForLevel(level);
        mob.setCustomName(color + "Level: " + level + " " + formatMobType(mob.getType().toString()));
        mob.setCustomNameVisible(true);
        mob.setRemoveWhenFarAway(true);
    }

    private String getColorForLevel(int level) {
        if (level <= 20) return ChatColor.GRAY.toString();
        else if (level <= 40) return ChatColor.GREEN.toString();
        else if (level <= 60) return ChatColor.AQUA.toString();
        else if (level <= 80) return ChatColor.LIGHT_PURPLE.toString();
        else if (level <= 100) return ChatColor.GOLD.toString();
        else if (level <= 120) return ChatColor.BLUE.toString();
        else if (level <= 150) return ChatColor.RED.toString();
        else if (level <= 200) return ChatColor.DARK_RED.toString();
        else if (level <= 250) return ChatColor.DARK_PURPLE.toString();
        else if (level <= 280) return ChatColor.BLACK.toString();
        else return ChatColor.WHITE.toString();
    }

    private String formatMobType(String mobType) {
        String formattedType = mobType.replace('_', ' ').toLowerCase();
        return formattedType.substring(0, 1).toUpperCase() + formattedType.substring(1);
    }

    private int getRandomLevelVariation() {
        Random rand = new Random();
        return rand.nextDouble() < 0.1 ? rand.nextInt(100) + 1 : 0;
    }
}
