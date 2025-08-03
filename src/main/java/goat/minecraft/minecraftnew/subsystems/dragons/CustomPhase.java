package goat.minecraft.minecraftnew.subsystems.dragons;

/**
 * Represents both vanilla Ender Dragon phases and custom extension phases.
 */
public enum CustomPhase {
    // Vanilla phases
    CIRCLING,
    STRAFING,
    FLY_TO_PORTAL,
    LAND_ON_PORTAL,
    LEAVE_PORTAL,
    BREATH_ATTACK,
    SEARCH_FOR_BREATH_ATTACK_TARGET,
    ROAR_BEFORE_ATTACK,
    CHARGE_PLAYER,
    DYING,
    HOVER,
    // Custom phases
    HEALING,
    SMITE,
    LAUNCH,
    FURY
}
