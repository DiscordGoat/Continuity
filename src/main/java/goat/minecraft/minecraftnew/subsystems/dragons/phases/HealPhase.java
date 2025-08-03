package goat.minecraft.minecraftnew.subsystems.dragons.phases;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.DragonFight;
import goat.minecraft.minecraftnew.subsystems.dragons.WaterDragonTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Phase handling the healing animation and health restoration using end crystals.
 */
public class HealPhase implements Phase {

    private static final String CRYSTAL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQyMzI4OTUxMGM1NGI2N2RmMDIzNTgwOTc5YzQ2NWQwNDgxYzc2OWM4NjViZjRiNDY1Y2Y0Nzg3NDlmMWM0ZiJ9fX0=";

    private final MinecraftNew plugin;
    private final DragonFight fight;
    private final WaterDragonTrait trait;

    public HealPhase(MinecraftNew plugin, DragonFight fight, WaterDragonTrait trait) {
        this.plugin = plugin;
        this.fight = fight;
        this.trait = trait;
    }

    @Override
    public void start() {
        EnderDragon dragon = fight.getDragonEntity();
        EnderCrystal crystal = findNearestCrystal(dragon);
        if (crystal == null || crystal.isDead()) {
            trait.onPhaseComplete();
            return; // cannot heal without a crystal
        }

        trait.setAttacking(true);
        Location freezeLoc = dragon.getLocation().clone();
        trait.getNPC().getNavigator().cancelNavigation();
        dragon.setVelocity(new Vector(0, 0, 0));

        Location start = crystal.getLocation().clone();
        crystal.remove();

        ArmorStand stand = (ArmorStand) dragon.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.getEquipment().setHelmet(createCrystalSkull());

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!trait.getNPC().isSpawned() || !stand.isValid() || dragon.isDead()) {
                    if (stand.isValid()) stand.remove();
                    trait.onPhaseComplete();
                    cancel();
                    return;
                }

                dragon.teleport(freezeLoc);
                dragon.setVelocity(new Vector(0, 0, 0));

                Location sLoc = stand.getLocation();
                Vector dir = freezeLoc.toVector().subtract(sLoc.toVector()).normalize().multiply(0.5);
                stand.teleport(sLoc.add(dir));
                stand.setHeadPose(stand.getHeadPose().add(0, Math.toRadians(20), 0));
                stand.getWorld().spawnParticle(Particle.END_ROD, sLoc, 2, 0, 0, 0, 0);

                if (sLoc.distanceSquared(freezeLoc) <= 64) {
                    stand.getWorld().createExplosion(sLoc, 6F, false, false);
                    stand.getWorld().spawnParticle(Particle.DRAGON_BREATH, sLoc, 200, 1, 1, 1, 0.01);
                    stand.getWorld().playSound(sLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
                    stand.remove();
                    startSmoothHeal(dragon, freezeLoc);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
        trait.setHealTask(task);
    }

    private void startSmoothHeal(EnderDragon dragon, Location freezeLoc) {
        double missing = fight.getHealth().getMaxHealth() - fight.getHealth().getCurrentHealth();
        if (missing <= 0) {
            trait.onPhaseComplete();
            trait.setHealTask(null);
            return;
        }
        int steps = 100;
        double amountPerStep = missing / steps;
        BukkitTask task = new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (!trait.getNPC().isSpawned() || dragon.isDead()) {
                    trait.onPhaseComplete();
                    trait.setHealTask(null);
                    cancel();
                    return;
                }

                dragon.teleport(freezeLoc);
                dragon.setVelocity(new Vector(0, 0, 0));

                if (step++ >= steps) {
                    trait.setCrystalBias(Math.max(0, trait.getCrystalBias() - 1));
                    trait.onPhaseComplete();
                    trait.setHealTask(null);
                    cancel();
                    return;
                }

                EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                        dragon, amountPerStep, EntityRegainHealthEvent.RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        trait.setHealTask(task);
    }

    private EnderCrystal findNearestCrystal(EnderDragon dragon) {
        EnderCrystal nearest = null;
        double best = Double.MAX_VALUE;
        Location loc = dragon.getLocation();
        for (EnderCrystal crystal : dragon.getWorld().getEntitiesByClass(EnderCrystal.class)) {
            double dist = crystal.getLocation().distanceSquared(loc);
            if (dist < best) {
                best = dist;
                nearest = crystal;
            }
        }
        return nearest;
    }

    private ItemStack createCrystalSkull() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        setCustomSkullTexture(meta, CRYSTAL_TEXTURE);
        head.setItemMeta(meta);
        return head;
    }

    private SkullMeta setCustomSkullTexture(SkullMeta skullMeta, String base64Json) {
        if (skullMeta == null || base64Json == null || base64Json.isEmpty()) {
            return skullMeta;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Json);
            String json = new String(decoded, StandardCharsets.UTF_8);
            var root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            String urlText = root.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(urlText), PlayerTextures.SkinModel.CLASSIC);
            profile.setTextures(textures);
            skullMeta.setOwnerProfile(profile);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return skullMeta;
    }
}
