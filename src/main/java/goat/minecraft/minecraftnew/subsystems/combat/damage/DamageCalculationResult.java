package goat.minecraft.minecraftnew.subsystems.combat.damage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result object for damage calculations.
 * Contains the final damage value and information about applied modifiers.
 */
public class DamageCalculationResult {
    
    private final double originalDamage;
    private final double finalDamage;
    private final List<DamageModifier> appliedModifiers;
    private final boolean wasModified;
    
    public DamageCalculationResult(double originalDamage, double finalDamage, List<DamageModifier> appliedModifiers) {
        this.originalDamage = originalDamage;
        this.finalDamage = finalDamage;
        this.appliedModifiers = new ArrayList<>(appliedModifiers);
        this.wasModified = Math.abs(originalDamage - finalDamage) > 0.001; // Account for floating point precision
    }
    
    public static DamageCalculationResult noChange(double damage) {
        return new DamageCalculationResult(damage, damage, Collections.emptyList());
    }
    
    public static DamageCalculationResult withModifier(double originalDamage, double finalDamage, DamageModifier modifier) {
        return new DamageCalculationResult(originalDamage, finalDamage, Collections.singletonList(modifier));
    }
    
    // Getters
    public double getOriginalDamage() { return originalDamage; }
    public double getFinalDamage() { return finalDamage; }
    public List<DamageModifier> getAppliedModifiers() { return Collections.unmodifiableList(appliedModifiers); }
    public boolean wasModified() { return wasModified; }
    
    /**
     * Gets the total damage multiplier applied
     */
    public double getTotalMultiplier() {
        if (originalDamage == 0) return 1.0;
        return finalDamage / originalDamage;
    }
    
    /**
     * Gets the absolute damage change
     */
    public double getDamageChange() {
        return finalDamage - originalDamage;
    }
    
    /**
     * Represents a single damage modifier that was applied
     */
    public static class DamageModifier {
        private final String source;
        private final double multiplier;
        private final double additive;
        private final String description;
        
        public DamageModifier(String source, double multiplier, double additive, String description) {
            this.source = source;
            this.multiplier = multiplier;
            this.additive = additive;
            this.description = description;
        }
        
        public static DamageModifier multiplicative(String source, double multiplier, String description) {
            return new DamageModifier(source, multiplier, 0.0, description);
        }
        
        public static DamageModifier additive(String source, double additive, String description) {
            return new DamageModifier(source, 1.0, additive, description);
        }
        
        // Getters
        public String getSource() { return source; }
        public double getMultiplier() { return multiplier; }
        public double getAdditive() { return additive; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            if (multiplier != 1.0 && additive != 0.0) {
                return String.format("%s: %.2fx + %.1f (%s)", source, multiplier, additive, description);
            } else if (multiplier != 1.0) {
                return String.format("%s: %.2fx (%s)", source, multiplier, description);
            } else {
                return String.format("%s: +%.1f (%s)", source, additive, description);
            }
        }
    }
    
    @Override
    public String toString() {
        if (!wasModified) {
            return String.format("No damage modification (%.1f)", originalDamage);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Damage: %.1f → %.1f", originalDamage, finalDamage));
        
        if (!appliedModifiers.isEmpty()) {
            sb.append(" [");
            for (int i = 0; i < appliedModifiers.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(appliedModifiers.get(i).toString());
            }
            sb.append("]");
        }
        
        return sb.toString();
    }
}