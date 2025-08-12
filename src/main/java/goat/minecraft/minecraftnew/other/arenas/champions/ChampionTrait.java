package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Set;

/**
 * Simplified Champion AI based on corpse logic.
 * Champions are faster (Speed 3) and switch between bow and sword based on distance.
 */
public class ChampionTrait extends Trait {
    private final JavaPlugin plugin;
    private final ChampionType championType;
    private final Set<ChampionBlessing> blessings;
    
    private int taskId = -1;
    private int tickCounter = 0;
    private boolean buffsApplied = false;
    
    // Bow mechanics
    private int bowCooldownTimer = 0;
    private static final int BOW_COOLDOWN = 600; // 30 seconds (30 * 20 ticks)
    private boolean usingBow = false;
    private int bowStopTimer = 0;
    private static final int BOW_STOP_DURATION = 30; // 1.5 seconds to aim and shoot

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
     * Main behavior loop based on corpse logic but with bow/sword switching.
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

            double distance = self.getLocation().distance(target.getLocation());
            
            // Check if player is facing away from champion
            boolean playerFacingAway = isPlayerFacingAway(self, target);
            
            // Bow/Sword switching logic - now checks every tick
            handleCombatMode(self, target, distance, playerFacingAway);
            
            tickCounter++;
            
        }, 1L, 1L); // Every tick for responsive bow combat
    }



    /**
     * Checks if the player is facing away from the champion.
     */
    private boolean isPlayerFacingAway(LivingEntity self, Player target) {
        Vector playerDirection = target.getLocation().getDirection();
        Vector toChampion = self.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
        
        // If dot product is negative, player is facing away from champion
        double dotProduct = playerDirection.dot(toChampion);
        return dotProduct < 0;
    }

    /**
     * Handles combat mode switching between bow and sword based on distance and player facing.
     */
    private void handleCombatMode(LivingEntity self, Player target, double distance, boolean playerFacingAway) {
        // Use bow immediately if player is >10 blocks away and facing away
        if (distance > 10.0 && playerFacingAway && !usingBow) {
            switchToBowMode(self);
        }
        
        // Switch to sword if player is within 5 blocks or facing the champion
        if ((distance <= 8.0 || !playerFacingAway) && usingBow) {
            switchToSwordMode(self);
        }
        
        if (usingBow) {
            handleBowCombat(self, target);
        } else {
            handleSwordCombat(self, target);
        }
    }

    /**
     * Switches champion to bow mode - stops moving and aims.
     */
    private void switchToBowMode(LivingEntity self) {
        usingBow = true;
        bowStopTimer = 0;
        
        // Equip bow in main hand using Citizens Equipment trait
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        ItemStack bow = ChampionEquipmentUtil.getItemFromFile(plugin, championType.getBowFile());
        equipment.set(Equipment.EquipmentSlot.HAND, bow);
        
        // Stop navigation
        npc.getNavigator().cancelNavigation();
    }

    /**
     * Switches champion to sword mode - resumes movement and melee.
     */
    private void switchToSwordMode(LivingEntity self) {
        usingBow = false;
        bowStopTimer = 0;
        
        // Equip sword in main hand using Citizens Equipment trait
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        ItemStack sword = ChampionEquipmentUtil.getItemFromFile(plugin, championType.getSwordFile());
        equipment.set(Equipment.EquipmentSlot.HAND, sword);
    }

    /**
     * Handles bow combat - stops moving, aims, and shoots.
     */
    private void handleBowCombat(LivingEntity self, Player target) {
        bowStopTimer++;
        
        // Stop moving while using bow
        npc.getNavigator().cancelNavigation();
        
        // Aim at player
        Vector direction = target.getLocation().toVector().subtract(self.getLocation().toVector()).normalize();
        Location lookLoc = self.getLocation().clone();
        lookLoc.setDirection(direction);
        self.teleport(lookLoc);
        
        // Shoot arrow after aiming for 1.5 seconds (30 ticks)
        if (bowStopTimer >= BOW_STOP_DURATION) {
            shootArrow(self, target);
            bowStopTimer = 0; // Reset timer for continuous shooting while conditions are met
        }
    }

    /**
     * Handles sword combat - moves toward player and attacks.
     */
    private void handleSwordCombat(LivingEntity self, Player target) {
        // Move toward player (like corpses)
        npc.getNavigator().setTarget(target, true);
        
        double distance = self.getLocation().distance(target.getLocation());
        
        // Attack if within melee range
        if (distance <= 3.0) {
            target.damage(6.0, self);
            applyBlessingEffects(target);
        }
    }

    /**
     * Shoots an arrow at the target player.
     */
    private void shootArrow(LivingEntity self, Player target) {
        Location eyeLoc = self.getEyeLocation();
        Vector direction = target.getEyeLocation().toVector().subtract(eyeLoc.toVector()).normalize();
        
        Arrow arrow = self.getWorld().spawnArrow(eyeLoc, direction, 2.0f, 0.1f);
        arrow.setShooter(self);
        
        // Play bow sound
        self.getWorld().playSound(self.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
        
        // Apply enhanced archery blessing if present
        if (hasBlessing(ChampionBlessing.ENHANCED_ARCHERY)) {
            arrow.setDamage(arrow.getDamage() * 1.5); // 50% more damage
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
