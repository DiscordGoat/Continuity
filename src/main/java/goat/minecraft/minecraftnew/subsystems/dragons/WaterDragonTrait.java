package goat.minecraft.minecraftnew.subsystems.dragons;

import goat.minecraft.minecraftnew.MinecraftNew;
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

/**
 * Trait handling the Water Dragon's healing behaviour.
 *
 * <p>When the dragon takes sufficient damage based on its current crystal
 * bias, it flies to the nearest end crystal and orbits it for five seconds
 * before restoring to full health. Each heal reduces the crystal bias making
 * subsequent heals require more damage.</p>
 */
public class WaterDragonTrait extends Trait implements Listener {

    private final MinecraftNew plugin;
    private final DragonFight fight;

    private int crystalBias;
    private BukkitTask healTask;

    public WaterDragonTrait(MinecraftNew plugin, DragonFight fight) {
        super("water_dragon_trait");
        this.plugin = plugin;
        this.fight = fight;
        this.crystalBias = fight.getDragonType().getCrystalBias();
    }

    @Override
    public void onAttach() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onRemove() {
        HandlerList.unregisterAll(this);
        if (healTask != null) {
            healTask.cancel();
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
        Location center = (crystal != null)
                ? crystal.getLocation().clone().add(0, 5, 0)
                : dragon.getLocation().clone();

        healTask = new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!npc.isSpawned()) {
                    cancel();
                    healTask = null;
                    return;
                }
                double angle = ticks * (Math.PI / 20); // ~18 degrees per tick
                double radius = 5.0;
                Location loc = center.clone().add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                dragon.teleport(loc);
                ticks++;
                if (ticks >= 100) {
                    finishHeal();
                    cancel();
                    healTask = null;
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

    @Override public void load(DataKey key) { }
    @Override public void save(DataKey key) { }
}
