package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Set;

/**
 * Simplified Champion AI - pure aggressive melee combat.
 * Champions relentlessly chase players and attack with their held weapon.
 */
public class ChampionTrait extends Trait {
    private final JavaPlugin plugin;
    private final ChampionType championType;
    private final Set<ChampionBlessing> blessings;
    
    private int taskId = -1;
    private int tickCounter = 0;
    private boolean buffsApplied = false;
    private int lastAttackTick = 0;
    private static final int ATTACK_COOLDOWN = 20; // 1 second (20 ticks)

    public ChampionTrait(JavaPlugin plugin, ChampionType championType, Set<ChampionBlessing> blessings) {
        super("champion_trait");
        this.plugin = plugin;
        this.championType = championType;
        this.blessings = blessings;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        startBehaviorLoop();
    }

    /**
     * Main behavior loop - aggressive melee combat only.
     */
    private void startBehaviorLoop() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            LivingEntity self = (LivingEntity) npc.getEntity();
            self.setGravity(true);

            // Apply buffs on first tick only (like corpses)
            if (!buffsApplied) {
                applyPermanentBuffs(self);
                self.setHealth(self.getMaxHealth());
                // 3 seconds of regeneration to fill health
                self.addPotionEffect(new PotionEffect(
                        PotionEffectType.REGENERATION,
                        20 * 3,
                        255,
                        true, false
                ));
                buffsApplied = true;
            }

            Player target = getNearestPlayer(self.getLocation());
            if (target == null) return;

            // Pure aggressive melee combat
            handleAggressiveMelee(self, target);
            
            tickCounter++;
            
        }, 1L, 1L); // Every tick for maximum responsiveness
    }

    /**
     * Handles aggressive melee combat - always chase and attack.
     */
    private void handleAggressiveMelee(LivingEntity self, Player target) {
        double distance = self.getLocation().distance(target.getLocation());

        // Always move toward player aggressively (continuous walking)
        npc.getNavigator().getDefaultParameters().updatePathRate(1); // every tick
        npc.getNavigator().getDefaultParameters().baseSpeed(2.0f);   // multiplier

        npc.getNavigator().setTarget(target, true);

        // Attack if within melee range and attack cooldown has passed
        if (distance <= 3.0 && (tickCounter - lastAttackTick) >= ATTACK_COOLDOWN) {
            target.attack(getNearestPlayer(target.getLocation()));
            applyBlessingEffects(target);
            lastAttackTick = tickCounter; // Update last attack time
        }
    }

    /**
     * Applies permanent buffs like corpses but with Speed 3 for champions.
     */
    private void applyPermanentBuffs(LivingEntity self) {
        // Clear old effects
        for (PotionEffectType type : Arrays.asList(
                PotionEffectType.HEALTH_BOOST,
                PotionEffectType.RESISTANCE,
                PotionEffectType.SLOWNESS,
                PotionEffectType.STRENGTH,
                PotionEffectType.SPEED)) {
            self.removePotionEffect(type);
        }

        // Calculate health boost for champion HP
        int targetHP = championType.getHealth();
        if (hasBlessing(ChampionBlessing.ENHANCED_VITALITY)) {
            targetHP = 200;
        }
        int hpAmp = Math.max(0, (targetHP - 20) / 4 - 1);

        int infinite = Integer.MAX_VALUE;
        
        // Apply champion buffs
        if (hpAmp > 0) {
            self.addPotionEffect(new PotionEffect(
                    PotionEffectType.HEALTH_BOOST, infinite, hpAmp, true, false
            ));
        }
        self.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, infinite, 1, true, false
        ));
        self.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH, infinite, 1, true, false
        ));
        // Speed 3 for champions (faster than corpses)
        self.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, infinite, 2, true, false
        ));
        
        // Set max health attribute
        self.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(targetHP);
    }

    /**
     * Applies blessing effects when attacking.
     */
    private void applyBlessingEffects(Player target) {
        if (hasBlessing(ChampionBlessing.FLAME_STRIKE)) {
            target.setFireTicks(100); // 5 seconds of fire
        }
        
        if (hasBlessing(ChampionBlessing.REGENERATION_CURSE) && Math.random() < 0.25) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, -100, 0, false, false));
        }
        
        // Add more blessing effects as needed
    }

    private Player getNearestPlayer(Location loc) {
        double best = Double.MAX_VALUE;
        Player closest = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) continue;
            double d = p.getLocation().distanceSquared(loc);
            if (d < best) {
                best = d;
                closest = p;
            }
        }
        return closest;
    }

    /**
     * Checks if this Champion has a specific blessing.
     */
    public boolean hasBlessing(ChampionBlessing blessing) {
        return blessings.contains(blessing);
    }

    @Override
    public void onRemove() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override
    public void load(DataKey key) {}
    
    @Override
    public void save(DataKey key) {}
    
    // Getters for debugging/monitoring
    public ChampionType getChampionType() { return championType; }
    public Set<ChampionBlessing> getBlessings() { return blessings; }
}
