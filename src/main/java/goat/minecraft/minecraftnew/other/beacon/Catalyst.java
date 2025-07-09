package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.ChatColor;

import java.util.UUID;

public class Catalyst {
    private final Location location;
    private final CatalystType type;
    private final UUID placerUUID;
    private final long endTime;
    private final int range;
    private ArmorStand armorStand;
    private BukkitTask rotationTask;
    private BukkitTask effectTask;
    private float rotationAngle = 0f;

    public Catalyst(Location location, CatalystType type, UUID placerUUID, int durationSeconds, int range) {
        this.location = location.clone();
        this.type = type;
        this.placerUUID = placerUUID;
        this.range = range;
        this.endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        spawnEntity();
        startTasks();
    }

    private void spawnEntity() {
        Location spawnLoc = location.clone().add(0, -1, 0);
        armorStand = (ArmorStand) location.getWorld().spawnEntity(spawnLoc, EntityType.ARMOR_STAND);
        
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setRemoveWhenFarAway(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.setSmall(false);
        armorStand.setInvulnerable(true);
        armorStand.setCustomName(type.getColoredDisplayName());
        armorStand.setCustomNameVisible(true);
        
        ItemStack beacon = new ItemStack(Material.BEACON);
        armorStand.setHelmet(beacon);
    }

    private void startTasks() {
        rotationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (armorStand == null || armorStand.isDead()) {
                    cancel();
                    return;
                }
                
                rotationAngle += 15f;
                if (rotationAngle >= 360f) rotationAngle = 0f;
                
                armorStand.setHeadPose(new EulerAngle(0, Math.toRadians(rotationAngle), 0));
                
                double bobOffset = Math.sin(rotationAngle * Math.PI / 180.0) * 0.3;
                Location newLoc = location.clone().add(0, -1 + bobOffset, 0);
                armorStand.teleport(newLoc);
                
                // Update name tag with remaining duration
                updateNameTag();
            }
        }.runTaskTimer(CatalystManager.getInstance().getPlugin(), 0L, 1L);

        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (armorStand == null || armorStand.isDead()) {
                    cancel();
                    return;
                }
                
                playEffects();
            }
        }.runTaskTimer(CatalystManager.getInstance().getPlugin(), 0L, 2L);
    }

    private void playEffects() {
        Location effectLoc = armorStand.getLocation().add(0, 0.5, 0);
        
        if (type.getParticle() != null) {
            if (type == CatalystType.POWER) {
                location.getWorld().spawnParticle(type.getParticle(), effectLoc, 10, 0.5, 0.5, 0.5, 0.02,
                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
            } else if (type == CatalystType.FLIGHT) {
                location.getWorld().spawnParticle(type.getParticle(), effectLoc, 15, 0.4, 0.4, 0.4, 0.05);
            } else if (type == CatalystType.DEPTH) {
                location.getWorld().spawnParticle(type.getParticle(), effectLoc, 8, 0.3, 0.3, 0.3, 0.02);
            } else if (type == CatalystType.INSANITY) {
                location.getWorld().spawnParticle(type.getParticle(), effectLoc, 12, 0.5, 0.5, 0.5, 0.03);
            }
        }
        
        if (type.getSound() != null) {
            location.getWorld().playSound(effectLoc, type.getSound(), 0.5f, 1.0f);
        }
    }

    public void destroy() {
        if (rotationTask != null) {
            rotationTask.cancel();
        }
        if (effectTask != null) {
            effectTask.cancel();
        }
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= endTime;
    }

    public Location getLocation() {
        return location.clone();
    }

    public CatalystType getType() {
        return type;
    }

    public UUID getPlacerUUID() {
        return placerUUID;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public int getRange() {
        return range;
    }
    
    public int getRemainingTimeSeconds() {
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, (int) (remaining / 1000));
    }
    
    private void updateNameTag() {
        if (armorStand != null && !armorStand.isDead()) {
            int remaining = getRemainingTimeSeconds();
            String timeDisplay = String.format("%02d:%02d", remaining / 60, remaining % 60);
            armorStand.setCustomName(type.getColoredDisplayName() + ChatColor.WHITE + " [" + timeDisplay + "]");
        }
    }
}