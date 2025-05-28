package goat.minecraft.minecraftnew.subsystems.combat.damage.strategies;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationContext;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationResult;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationStrategy;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * Strategy for calculating melee damage bonuses based on player combat skill level.
 */
public class MeleeDamageStrategy implements DamageCalculationStrategy {
    
    private static final Logger logger = Logger.getLogger(MeleeDamageStrategy.class.getName());
    
    private final CombatConfiguration.DamageConfig config;
    private final XPManager xpManager;
    
    public MeleeDamageStrategy(CombatConfiguration.DamageConfig config, XPManager xpManager) {
        this.config = config;
        this.xpManager = xpManager;
    }
    
    @Override
    public DamageCalculationResult calculateDamage(DamageCalculationContext context) {
        if (!isApplicable(context)) {
            return DamageCalculationResult.noChange(context.getBaseDamage());
        }
        
        Player player = context.getAttackerPlayer().get();
        double originalDamage = context.getBaseDamage();
        
        try {
            int combatLevel = xpManager.getPlayerLevel(player, "Combat");
            int cappedLevel = Math.min(combatLevel, config.getMaxSkillLevel());
            
            double multiplier = 1.0 + (cappedLevel * config.getMeleePerLevel());
            double finalDamage = originalDamage * multiplier;
            
            DamageCalculationResult.DamageModifier modifier = 
                DamageCalculationResult.DamageModifier.multiplicative(
                    "Combat Skill", 
                    multiplier, 
                    String.format("Level %d melee bonus", cappedLevel)
                );
            
            logger.fine(String.format("Applied melee damage bonus: %s (level %d) -> %.1f%% increase", 
                       player.getName(), cappedLevel, (multiplier - 1.0) * 100));
            
            return DamageCalculationResult.withModifier(originalDamage, finalDamage, modifier);
            
        } catch (Exception e) {
            logger.warning(String.format("Failed to calculate melee damage for player %s: %s", 
                          player.getName(), e.getMessage()));
            return DamageCalculationResult.noChange(originalDamage);
        }
    }
    
    @Override
    public boolean isApplicable(DamageCalculationContext context) {
        return context.getAttackerPlayer().isPresent() && 
               context.getDamageType() == DamageCalculationContext.DamageType.MELEE &&
               !context.isProjectile();
    }
    
    @Override
    public int getPriority() {
        return 80; // High priority for base skill calculations
    }
    
    @Override
    public String getName() {
        return "Melee Combat Skill Damage";
    }
}