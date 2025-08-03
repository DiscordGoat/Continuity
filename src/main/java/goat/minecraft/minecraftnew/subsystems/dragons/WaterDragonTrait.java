package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.behaviors.PerformBasicAttack;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Trait handling the Water Dragon's behaviour including flight control,
 * decision making and healing.
 *
 * <p>When the dragon takes sufficient damage based on its current crystal
 * bias, it flies to the nearest end crystal and perches above it for five
 * seconds before restoring to full health. Each heal reduces the crystal bias
 * making subsequent heals require more damage. The trait also slows the
 * dragon's flight speed and periodically triggers a basic lightning attack.</p>
 */
public class WaterDragonTrait extends Trait implements Listener {

    private static final String CRYSTAL_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjQyMzI4OTUxMGM1NGI2N2RmMDIzNTgwOTc5YzQ2NWQwNDgxYzc2OWM4NjViZjRiNDY1Y2Y0Nzg3NDlmMWM0ZiJ9fX0=";

    private final MinecraftNew plugin;
    private final DragonFight fight;

    private int crystalBias;
    private BukkitTask healTask;
    private BukkitTask flightTask;
    private BukkitTask decisionTask;
    private boolean attacking;

    public WaterDragonTrait(MinecraftNew plugin, DragonFight fight) {
        super("water_dragon_trait");
        this.plugin = plugin;
        this.fight = fight;
        this.crystalBias = fight.getDragonType().getCrystalBias();
    }

    @Override
    public void onAttach() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startFlightTask();
        startDecisionLoop();
    }

    @Override
    public void onRemove() {
        HandlerList.unregisterAll(this);
        if (healTask != null) {
            healTask.cancel();
        }
        if (flightTask != null) {
            flightTask.cancel();
        }
        if (decisionTask != null) {
            decisionTask.cancel();
        }
    }

    @EventHandler
    public void onDragonDamage(EntityDamageEvent event) {
        if (!event.getEntity().getUniqueId().equals(fight.getDragonEntity().getUniqueId())) {
            return;
        }
        if (healTask != null || crystalBias <= 0) {
            return;
        }
        // Run after health values have been updated by fight manager
        Bukkit.getScheduler().runTask(plugin, this::checkHealTrigger);
    }

    private void checkHealTrigger() {
        double missing = 1.0 - fight.getHealth().getHealthPercentage();
        double threshold = 1.0 / crystalBias;
        if (missing >= threshold) {
            startHeal();
        }
    }

    private void startHeal() {
        EnderDragon dragon = fight.getDragonEntity();
        EnderCrystal crystal = findNearestCrystal(dragon);
        if (crystal == null || crystal.isDead()) {
            return; // cannot heal without a crystal
        }

        attacking = true;
        Location freezeLoc = dragon.getLocation().clone();
        npc.getNavigator().cancelNavigation();
        dragon.setVelocity(new Vector(0, 0, 0));

        Location start = crystal.getLocation().clone();
        crystal.remove();

        ArmorStand stand = (ArmorStand) dragon.getWorld().spawnEntity(start, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setMarker(true);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.getEquipment().setHelmet(createCrystalSkull());

        healTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() || !stand.isValid() || dragon.isDead()) {
                    if (stand.isValid()) stand.remove();
                    attacking = false;
                    healTask = null;
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

                if (sLoc.distanceSquared(freezeLoc) <= 64) { // within 8 blocks
                    stand.getWorld().createExplosion(sLoc, 6F, false, false);
                    stand.getWorld().spawnParticle(Particle.DRAGON_BREATH, sLoc, 200, 1, 1, 1, 0.01);
                    stand.getWorld().playSound(sLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 10f, 1f);
                    stand.remove();
                    startSmoothHeal(dragon, freezeLoc);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startSmoothHeal(EnderDragon dragon, Location freezeLoc) {
        double missing = fight.getHealth().getMaxHealth() - fight.getHealth().getCurrentHealth();
        if (missing <= 0) {
            attacking = false;
            healTask = null;
            return;
        }
        int steps = 100;
        double amountPerStep = missing / steps;
        healTask = new BukkitRunnable() {
            int step = 0;
            @Override
            public void run() {
                if (!npc.isSpawned() || dragon.isDead()) {
                    attacking = false;
                    healTask = null;
                    cancel();
                    return;
                }

                dragon.teleport(freezeLoc);
                dragon.setVelocity(new Vector(0, 0, 0));

                if (step++ >= steps) {
                    crystalBias = Math.max(0, crystalBias - 1);
                    attacking = false;
                    healTask = null;
                    cancel();
                    return;
                }

                EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                        dragon, amountPerStep, EntityRegainHealthEvent.RegainReason.CUSTOM);
                Bukkit.getPluginManager().callEvent(event);
            }
        }.runTaskTimer(plugin, 0L, 1L);
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
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
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

    private void startFlightTask() {
        EnderDragon dragon = fight.getDragonEntity();
        int speed = fight.getDragonType().getFlightSpeed();
        double multiplier = speed / 5.0;
        dragon.setVelocity(dragon.getVelocity().multiply(multiplier));
        flightTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() || dragon.isDead()) {
                    cancel();
                    return;
                }
                if (attacking) return;
                Vector dir = dragon.getLocation().getDirection().normalize();
                Vector vel = dragon.getVelocity();
                if (multiplier > 1.0) {
                    dragon.setVelocity(vel.add(dir.multiply(multiplier)));
                } else if (multiplier < 1.0) {
                    dragon.setVelocity(vel.multiply(multiplier));
                }
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void startDecisionLoop() {
        int interval = fight.getDragonType().getDecisionInterval();
        decisionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() || attacking) {
                    return;
                }
                attacking = true;
                new PerformBasicAttack(plugin, fight, WaterDragonTrait.this).run();
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void onAttackComplete() {
        attacking = false;
    }

    @Override public void load(DataKey key) { }
    @Override public void save(DataKey key) { }
}
