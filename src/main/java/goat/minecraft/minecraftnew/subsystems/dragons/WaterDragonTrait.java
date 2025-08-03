package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.phases.*;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Random;

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
    private CustomPhase currentPhase;
    private long furyCooldownEnd;
    private long launchCooldownEnd;
    private long smiteCooldownEnd;
    private final Random random = new Random();

    private static final long FURY_COOLDOWN = 60000L;
    private static final long LAUNCH_COOLDOWN = 20000L;
    private static final long SMITE_COOLDOWN = 10000L;

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

    @EventHandler
    public void onPhaseChange(EnderDragonChangePhaseEvent event) {
        if (!event.getEntity().getUniqueId().equals(fight.getDragonEntity().getUniqueId())) {
            return;
        }
        EnderDragon.Phase phase = event.getNewPhase();
        switch (phase) {
            case LAND_ON_PORTAL:
            case FLY_TO_PORTAL:
            case LEAVE_PORTAL:
            case HOVER:
            case BREATH_ATTACK:
            case SEARCH_FOR_BREATH_ATTACK_TARGET:
                event.setNewPhase(EnderDragon.Phase.CIRCLING);
                break;
            default:
                break;
        }
    }

    private void checkHealTrigger() {
        double missing = 1.0 - fight.getHealth().getHealthPercentage();
        double threshold = 1.0 / crystalBias;
        if (missing >= threshold) {
            currentPhase = CustomPhase.HEALING;
            new HealPhase(plugin, fight, this).start();
        }
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
                if (random.nextBoolean()) {
                    attacking = true;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> attacking = false, interval);
                    return; // decision fails
                }
                long now = System.currentTimeMillis();
                attacking = true;
                if (fight.getHealth().getHealthPercentage() < 0.5 && now >= furyCooldownEnd) {
                    currentPhase = CustomPhase.FURY;
                    new FuryPhase(plugin, fight, WaterDragonTrait.this).start();
                    furyCooldownEnd = now + FURY_COOLDOWN;
                } else if (now >= launchCooldownEnd) {
                    currentPhase = CustomPhase.LAUNCH;
                    new LaunchPhase(plugin, fight, WaterDragonTrait.this).start();
                    launchCooldownEnd = now + LAUNCH_COOLDOWN;
                } else {
                    currentPhase = CustomPhase.SMITE;
                    new SmitePhase(plugin, fight, WaterDragonTrait.this).start();
                    smiteCooldownEnd = now + SMITE_COOLDOWN;
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void onPhaseComplete() {
        attacking = false;
    }

    public void setHealTask(BukkitTask task) {
        this.healTask = task;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public int getCrystalBias() {
        return crystalBias;
    }

    public void setCrystalBias(int crystalBias) {
        this.crystalBias = crystalBias;
    }

    @Override public void load(DataKey key) { }
    @Override public void save(DataKey key) { }
}
