package goat.minecraft.minecraftnew.subsystems.combat.damage;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Context object containing all information needed for damage calculations.
 * This immutable object provides a clean interface for strategy implementations.
 */
public class DamageCalculationContext {
    
    private final EntityDamageByEntityEvent event;
    private final Entity attacker;
    private final Entity target;
    private final double baseDamage;
    private final DamageType damageType;
    private final Player attackerPlayer;
    private final ItemStack weapon;
    private final boolean isProjectile;
    private final Projectile projectile;
    
    private DamageCalculationContext(Builder builder) {
        this.event = builder.event;
        this.attacker = builder.attacker;
        this.target = builder.target;
        this.baseDamage = builder.baseDamage;
        this.damageType = builder.damageType;
        this.attackerPlayer = builder.attackerPlayer;
        this.weapon = builder.weapon;
        this.isProjectile = builder.isProjectile;
        this.projectile = builder.projectile;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters
    public EntityDamageByEntityEvent getEvent() { return event; }
    public Entity getAttacker() { return attacker; }
    public Entity getTarget() { return target; }
    public double getBaseDamage() { return baseDamage; }
    public DamageType getDamageType() { return damageType; }
    public Optional<Player> getAttackerPlayer() { return Optional.ofNullable(attackerPlayer); }
    public Optional<ItemStack> getWeapon() { return Optional.ofNullable(weapon); }
    public boolean isProjectile() { return isProjectile; }
    public Optional<Projectile> getProjectile() { return Optional.ofNullable(projectile); }
    
    /**
     * Builder pattern for creating DamageCalculationContext instances
     */
    public static class Builder {
        private EntityDamageByEntityEvent event;
        private Entity attacker;
        private Entity target;
        private double baseDamage;
        private DamageType damageType;
        private Player attackerPlayer;
        private ItemStack weapon;
        private boolean isProjectile;
        private Projectile projectile;
        
        public Builder event(EntityDamageByEntityEvent event) {
            this.event = event;
            return this;
        }
        
        public Builder attacker(Entity attacker) {
            this.attacker = attacker;
            return this;
        }
        
        public Builder target(Entity target) {
            this.target = target;
            return this;
        }
        
        public Builder baseDamage(double baseDamage) {
            this.baseDamage = baseDamage;
            return this;
        }
        
        public Builder damageType(DamageType damageType) {
            this.damageType = damageType;
            return this;
        }
        
        public Builder attackerPlayer(Player attackerPlayer) {
            this.attackerPlayer = attackerPlayer;
            return this;
        }
        
        public Builder weapon(ItemStack weapon) {
            this.weapon = weapon;
            return this;
        }
        
        public Builder isProjectile(boolean isProjectile) {
            this.isProjectile = isProjectile;
            return this;
        }
        
        public Builder projectile(Projectile projectile) {
            this.projectile = projectile;
            return this;
        }
        
        public DamageCalculationContext build() {
            // Validation
            if (event == null) {
                throw new IllegalArgumentException("Event cannot be null");
            }
            if (attacker == null) {
                throw new IllegalArgumentException("Attacker cannot be null");
            }
            if (target == null) {
                throw new IllegalArgumentException("Target cannot be null");
            }
            if (baseDamage < 0) {
                throw new IllegalArgumentException("Base damage cannot be negative");
            }
            if (damageType == null) {
                throw new IllegalArgumentException("Damage type cannot be null");
            }
            
            return new DamageCalculationContext(this);
        }
    }
    
    /**
     * Enumeration of damage types for classification
     */
    public enum DamageType {
        MELEE,
        RANGED,
        PROJECTILE,
        MAGIC,
        ENVIRONMENTAL,
        OTHER
    }
}