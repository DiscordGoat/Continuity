package goat.minecraft.minecraftnew.other.arenas.champions;

/**
 * Represents the different phases a Champion can be in during combat.
 * Champions have an Ender Dragon-like phase-based AI system.
 */
public enum ChampionPhase {
    /**
     * The "sleeping" phase. Champion has no equipment, looks at floor, doesn't react.
     * Default state when arena is generated and champion is summoned.
     */
    STATUE,
    
    /**
     * Initial phase when awakened. Champion gets skin, spawns, equips armor,
     * applies health, holds sword while staring at player.
     * Lasts until attacked or 15s with player within 25 blocks.
     */
    AWAKEN,
    
    /**
     * Combat phase. Champion equips sword and sprints toward player.
     * Attacks repeatedly when within 3 blocks.
     * Lasts until taking 10% non-projectile max health damage.
     */
    SWORD,
    
    /**
     * Retreat phase. Champion unequips items and flees to 15 block distance.
     * Lasts until 15 blocks away or 8s timeout, then returns to SWORD.
     * If hit more than once, immediately returns to SWORD.
     */
    DISENGAGE
}
