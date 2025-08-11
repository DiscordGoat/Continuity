package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;

/**
 * Trait that gives Champions their phase-based AI behavior similar to the Ender Dragon.
 */
public class ChampionTrait extends Trait implements Listener {
    private final JavaPlugin plugin;
    private final ChampionType championType;
    private final Set<ChampionBlessing> blessings;
    
    // Phase management
    private ChampionPhase currentPhase = ChampionPhase.STATUE;
    private long phaseStartTime = System.currentTimeMillis();
    private Player targetPlayer;
    
    // Combat tracking
    private double damageTaken = 0.0;
    private int disengageHitCount = 0;
    private long lastDisengageTime = 0;
    
    // Task IDs for cleanup
    private int mainTaskId = -1;
    private int phaseCheckTaskId = -1;
    
    // Constants
    private static final String STATUE_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc1NDk0OTI5MzMzMCwKICAicHJvZmlsZUlkIiA6ICJjOWM5YzkwOWIxNTI0ZDgzODY5NzU2OTE0M2JmNTY4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6dWtlbWF4IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y2MGRlMThhMmU2MmZmOGQ3ZDBhMzdkYjIxMThhMjcyZmIwYzQ5MTJkZmVmMjRkNGFhY2FmMjM5NmU4MTQwODMiCiAgICB9CiAgfQp9";
    private static final String STATUE_SKIN_SIGNATURE = "A/zgrDRxWjjkqMy13VjacV9X1NC6E9ZRaw5MeJ/FrVRtDNNAQKX9jJ0T0JoO9Wz4DKdnhFISik26VKOi2w6QSWUZ5IOmEAuWvXQdz4zXJUcV5F72aGMqmoqIWudCnBi88DLL66nbr4ylUtTMhkSUt2eBqUs2QsR43ZJ9ZbeT7oKR2B6P/VhCUsw64seIo8YtdOef9nQSCNrZiYYQ2oE7M3I7xgmIvXDAtPuZgeid/9/tXnNkdS6PABRHphh18NBtqEmuw1nJtoN4Zy4iApwveb637IDhNH6u83YxS+SDIl0qkrJ0kK6MI1B9SvLfkOWsLp8eAJtMi1zJXgi8Atu/U8obQ1g0AQDxjom/EmTaPNtDJ6Uzpw6JZAl8DUlFiQ38038+JPhKZWt5EFH926ER/Ms3bjGWDBfY9bITo6VvBsXaA4T0q/2vUBJykC6qoMaSrJogGhERcX5P0YutOyezlAkJvyKWFlUj50Wlp1Qoa6boJJbi8hcw8KxISIm9J5g1noOkHgG6+rSMEF9sk7uD0JpDUh6XW+aEkqg3Z+trAABDxUhQZ5dhCIyIHVbWD+s3oThUJQsXDot1EBlrlDjciNDqqSjAkZVb095sNdqW7u7iYWK06GbvH55BfWoAJAtFE79s4l5BRVKOtEQ/B9hDc0BZqRx7RnUzDk0aFTIY8rk=";

    public ChampionTrait(JavaPlugin plugin, ChampionType championType, Set<ChampionBlessing> blessings) {
        super("champion_trait");
        this.plugin = plugin;
        this.championType = championType;
        this.blessings = blessings;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        // Register events when trait is attached
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        initializeStatuePhase();
        startBehaviorLoop();
        startPhaseCheckLoop();
    }

    /**
     * Initializes the Champion in STATUE phase with appropriate skin and no equipment.
     */
    private void initializeStatuePhase() {
        currentPhase = ChampionPhase.STATUE;
        phaseStartTime = System.currentTimeMillis();
        
        // Set statue skin with delay to ensure NPC is properly spawned
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!npc.isSpawned()) return;
            
            SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
            skin.setFetchDefaultSkin(false);
            skin.setShouldUpdateSkins(false);
            skin.setSkinPersistent("champion_statue", STATUE_SKIN_SIGNATURE, STATUE_SKIN_VALUE);
            
            // Remove all equipment
            Equipment equipment = npc.getOrAddTrait(Equipment.class);
            equipment.set(Equipment.EquipmentSlot.HELMET, null);
            equipment.set(Equipment.EquipmentSlot.CHESTPLATE, null);
            equipment.set(Equipment.EquipmentSlot.LEGGINGS, null);
            equipment.set(Equipment.EquipmentSlot.BOOTS, null);
            equipment.set(Equipment.EquipmentSlot.HAND, null);
            
            // Make NPC look down
            if (npc.getEntity() instanceof LivingEntity living) {
                Location lookDown = living.getLocation();
                lookDown.setPitch(90f); // Look straight down
                living.teleport(lookDown);
            }
        }, 5L);
    }

    /**
     * Main behavior loop that handles phase-specific actions.
     */
    private void startBehaviorLoop() {
        mainTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            
            LivingEntity self = (LivingEntity) npc.getEntity();
            Player nearestPlayer = getNearestPlayer(self.getLocation());
            
            switch (currentPhase) {
                case STATUE -> handleStatuePhase(self, nearestPlayer);
                case AWAKEN -> handleAwakenPhase(self, nearestPlayer);
                case SWORD -> handleSwordPhase(self, nearestPlayer);
                case DISENGAGE -> handleDisengagePhase(self, nearestPlayer);
            }
            
        }, 5L, 5L); // Run every 5 ticks (4 times per second)
    }

    /**
     * Phase check loop that runs every 5 seconds to check for phase transitions.
     */
    private void startPhaseCheckLoop() {
        phaseCheckTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            
            LivingEntity self = (LivingEntity) npc.getEntity();
            Player nearestPlayer = getNearestPlayer(self.getLocation());
            
            // Check for STATUE -> AWAKEN transition
            if (currentPhase == ChampionPhase.STATUE && nearestPlayer != null) {
                double distance = self.getLocation().distance(nearestPlayer.getLocation());
                if (distance <= 15.0) {
                    transitionToAwaken(nearestPlayer);
                }
            }
            
        }, 100L, 100L); // Run every 5 seconds
    }

    private void handleStatuePhase(LivingEntity self, Player nearestPlayer) {
        // Statue phase: look down, don't react to world
        self.getLocation().setPitch(90f);
        npc.getNavigator().cancelNavigation();
    }

    private void handleAwakenPhase(LivingEntity self, Player nearestPlayer) {
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            transitionToStatue();
            return;
        }
        
        // Look at the target player
        Location targetLoc = targetPlayer.getLocation();
        Vector direction = targetLoc.toVector().subtract(self.getLocation().toVector()).normalize();
        Location lookLoc = self.getLocation().clone();
        lookLoc.setDirection(direction);
        self.teleport(lookLoc);
        
        // Check for phase transition conditions
        long timeInPhase = System.currentTimeMillis() - phaseStartTime;
        double distanceToPlayer = self.getLocation().distance(targetPlayer.getLocation());
        
        // Transition to SWORD if 15s passed and player still within 25 blocks, or if attacked
        if ((timeInPhase >= 15000 && distanceToPlayer <= 25.0) || damageTaken > 0) {
            transitionToSword();
        }
    }

    private void handleSwordPhase(LivingEntity self, Player nearestPlayer) {
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            transitionToStatue();
            return;
        }
        
        // Sprint toward player
        npc.getNavigator().setTarget(targetPlayer, true);
        self.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 1, false, false));
        
        double distance = self.getLocation().distance(targetPlayer.getLocation());
        
        // Attack if within 3 blocks
        if (distance <= 3.0) {
            attackPlayer(self, targetPlayer);
        }
        
        // Check for DISENGAGE transition (10% max health damage from non-projectiles)
        double maxHealth = self.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        if (damageTaken >= maxHealth * 0.1) {
            transitionToDisengage();
        }
    }

    private void handleDisengagePhase(LivingEntity self, Player nearestPlayer) {
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            transitionToStatue();
            return;
        }
        
        double distance = self.getLocation().distance(targetPlayer.getLocation());
        long timeInPhase = System.currentTimeMillis() - phaseStartTime;
        
        // Return to SWORD if hit more than once
        if (disengageHitCount > 1) {
            transitionToSword();
            return;
        }
        
        // Return to SWORD if 8s timeout and still within 8 blocks
        if (timeInPhase >= 8000 && distance <= 8.0) {
            transitionToSword();
            return;
        }
        
        // Return to SWORD if successfully reached 15 block distance
        if (distance >= 15.0) {
            transitionToSword();
            return;
        }
        
        // Flee from player
        fleeFromPlayer(self, targetPlayer);
    }

    private void transitionToAwaken(Player player) {
        currentPhase = ChampionPhase.AWAKEN;
        phaseStartTime = System.currentTimeMillis();
        targetPlayer = player;
        damageTaken = 0.0;
        
        // Set champion skin
        SkinTrait skin = npc.getOrAddTrait(SkinTrait.class);
        skin.setSkinPersistent("champion", championType.getSkinSig(), championType.getSkinValue());
        
        // Equip armor and sword
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ChampionEquipmentUtil.setArmorContentsFromFile(plugin, npc, championType.getArmorFile());
            Equipment eq = npc.getOrAddTrait(Equipment.class);
            ItemStack[] armor = ChampionEquipmentUtil.getArmorForEquipment(plugin, championType.getArmorFile());
            eq.set(Equipment.EquipmentSlot.HELMET, armor[0]);
            eq.set(Equipment.EquipmentSlot.CHESTPLATE, armor[1]);
            eq.set(Equipment.EquipmentSlot.LEGGINGS, armor[2]);
            eq.set(Equipment.EquipmentSlot.BOOTS, armor[3]);
            
            ItemStack sword = ChampionEquipmentUtil.getItemFromFile(plugin, championType.getSwordFile());
            eq.set(Equipment.EquipmentSlot.HAND, sword);
        }, 5L);
        
        // Apply health
        LivingEntity self = (LivingEntity) npc.getEntity();
        applyChampionHealth(self);
    }

    private void transitionToSword() {
        currentPhase = ChampionPhase.SWORD;
        phaseStartTime = System.currentTimeMillis();
        damageTaken = 0.0;
        disengageHitCount = 0;
    }

    private void transitionToDisengage() {
        currentPhase = ChampionPhase.DISENGAGE;
        phaseStartTime = System.currentTimeMillis();
        disengageHitCount = 0;
        lastDisengageTime = System.currentTimeMillis();
        
        // Unequip all held items
        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HAND, null);
    }

    private void transitionToStatue() {
        initializeStatuePhase();
        targetPlayer = null;
        damageTaken = 0.0;
        disengageHitCount = 0;
    }

    private void attackPlayer(LivingEntity self, Player target) {
        // Basic melee attack
        target.damage(6.0, self);
        
        // Apply blessing effects
        applyBlessingEffects(target);
    }

    private void fleeFromPlayer(LivingEntity self, Player target) {
        // Calculate direction away from player
        Vector direction = self.getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
        Location fleeTarget = self.getLocation().add(direction.multiply(20));
        
        // Ensure flee target is valid (not in void, etc.)
        World world = self.getWorld();
        if (fleeTarget.getY() < world.getMinHeight()) {
            fleeTarget.setY(world.getMinHeight() + 10);
        }
        if (fleeTarget.getY() > world.getMaxHeight()) {
            fleeTarget.setY(world.getMaxHeight() - 10);
        }
        
        // Sprint jump away
        self.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, 2, false, false));
        self.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 40, 1, false, false));
        
        // Use Citizens navigation
        npc.getNavigator().setTarget(fleeTarget);
    }

    private void applyChampionHealth(LivingEntity self) {
        int targetHP = hasBlessing(ChampionBlessing.ENHANCED_VITALITY) ? 200 : championType.getHealth();
        
        // Apply health boost to reach target HP
        int hpAmp = Math.max(0, (targetHP - 20) / 4 - 1);
        self.addPotionEffect(new PotionEffect(
            PotionEffectType.HEALTH_BOOST, 
            Integer.MAX_VALUE, 
            hpAmp, 
            true, 
            false
        ));
        
        // Set to full health
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            self.setHealth(self.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        }, 5L);
    }

    private void applyBlessingEffects(Player target) {
        // Apply blessing-specific effects when attacking
        if (hasBlessing(ChampionBlessing.FLAME_STRIKE)) {
            target.setFireTicks(100); // 5 seconds of fire
        }
        
        if (hasBlessing(ChampionBlessing.REGENERATION_CURSE) && Math.random() < 0.25) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, -100, 0, false, false));
        }
        
        // Additional blessing effects will be implemented later
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

    /**
     * Handles damage events to track damage taken and phase transitions.
     */
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() != npc.getEntity()) return;
        
        // Track damage for phase transitions
        if (currentPhase == ChampionPhase.SWORD && 
            event.getCause() != EntityDamageEvent.DamageCause.PROJECTILE) {
            damageTaken += event.getFinalDamage();
        }
        
        // Count hits during DISENGAGE
        if (currentPhase == ChampionPhase.DISENGAGE) {
            disengageHitCount++;
        }
        
        // Transition from AWAKEN to SWORD if attacked
        if (currentPhase == ChampionPhase.AWAKEN) {
            transitionToSword();
        }
    }

    @Override
    public void onRemove() {
        if (mainTaskId != -1) {
            Bukkit.getScheduler().cancelTask(mainTaskId);
        }
        if (phaseCheckTaskId != -1) {
            Bukkit.getScheduler().cancelTask(phaseCheckTaskId);
        }
    }

    @Override
    public void load(DataKey key) {}
    
    @Override
    public void save(DataKey key) {}
    
    // Getters for debugging/monitoring
    public ChampionPhase getCurrentPhase() { return currentPhase; }
    public ChampionType getChampionType() { return championType; }
    public Set<ChampionBlessing> getBlessings() { return blessings; }
}
