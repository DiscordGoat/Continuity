package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.dragons.phases.*;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EnderDragonChangePhaseEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * Trait controlling the Fire Dragon's aggressive behaviour and abilities.
 */
public class FireDragonTrait extends Trait implements Listener {

    private final MinecraftNew plugin;
    private final DragonFight fight;

    private BukkitTask flightTask;
    private BukkitTask decisionTask;
    private boolean attacking;
    private long hellfireCooldownEnd;
    private long scorchCooldownEnd;

    private static final long HELLFIRE_COOLDOWN = 90_000L; // 1.5 minutes
    private static final long SCORCH_COOLDOWN = 180_000L; // 3 minutes

    public FireDragonTrait(MinecraftNew plugin, DragonFight fight) {
        super("fire_dragon_trait");
        this.plugin = plugin;
        this.fight = fight;
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
        if (flightTask != null) {
            flightTask.cancel();
        }
        if (decisionTask != null) {
            decisionTask.cancel();
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
            case ROAR_BEFORE_ATTACK:
            case BREATH_ATTACK:
            case SEARCH_FOR_BREATH_ATTACK_TARGET:
                event.setNewPhase(EnderDragon.Phase.CIRCLING);
                break;
            default:
                break;
        }
    }

    private void startDecisionLoop() {
        int interval = fight.getDragonType().getDecisionInterval();
        decisionTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!npc.isSpawned() || attacking) {
                    return;
                }
                long now = System.currentTimeMillis();
                attacking = true;
                double hp = fight.getHealth().getHealthPercentage();
                if (hp <= 0.5 && now >= hellfireCooldownEnd) {
                    new HellfirePhase(plugin, fight, FireDragonTrait.this).start();
                    hellfireCooldownEnd = now + HELLFIRE_COOLDOWN;
                } else if (now >= scorchCooldownEnd) {
                    new ScorchPhase(plugin, fight, FireDragonTrait.this).start();
                    scorchCooldownEnd = now + SCORCH_COOLDOWN;
                } else {
                    new FireballPhase(plugin, fight, FireDragonTrait.this).start();
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void onPhaseComplete() {
        attacking = false;
    }

    @Override public void load(DataKey key) { }
    @Override public void save(DataKey key) { }
}
