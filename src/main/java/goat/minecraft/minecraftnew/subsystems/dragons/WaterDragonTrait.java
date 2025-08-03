package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.behaviors.PerformBasicAttack;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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
        Location center = crystal.getLocation().clone().add(0, 5, 0);

        healTask = new BukkitRunnable() {
            int perchTicks = 0;

            @Override
            public void run() {
                if (!npc.isSpawned() || crystal.isDead()) {
                    cancel();
                    healTask = null;
                    return;
                }

                npc.setMoveDestination(center);

                if (dragon.getLocation().distanceSquared(center) <= 4) { // within 2 blocks
                    perchTicks++;
                    if (perchTicks >= 100) {
                        finishHeal();
                        cancel();
                        healTask = null;
                    }
                } else {
                    perchTicks = 0; // reset if the dragon strays away
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void finishHeal() {
        EnderDragon dragon = fight.getDragonEntity();
        double missing = fight.getHealth().getMaxHealth() - fight.getHealth().getCurrentHealth();
        if (missing > 0) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                    dragon, missing, EntityRegainHealthEvent.RegainReason.CUSTOM);
            Bukkit.getPluginManager().callEvent(event);
        }
        crystalBias = Math.max(0, crystalBias - 1);
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
