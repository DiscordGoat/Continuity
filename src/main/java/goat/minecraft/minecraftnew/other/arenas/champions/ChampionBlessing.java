package goat.minecraft.minecraftnew.other.arenas.champions;

/**
 * Represents the different blessings/buffs that Champions can have.
 * Each Champion type has specific blessings that augment their combat abilities.
 */
public enum ChampionBlessing {
    /**
     * Deals 50% more arrow damage (Legionnaire)
     */
    ENHANCED_ARCHERY,
    
    /**
     * Has 200 HP instead of 100 (Monolithian)
     */
    ENHANCED_VITALITY,
    
    /**
     * Lights player on fire when striking them (Arsonist)
     */
    FLAME_STRIKE,
    
    /**
     * Incurs darkness every 50s (Dweller)
     */
    DARKNESS_AURA,
    
    /**
     * Disables player's natural regeneration for 5s on 25% of hits (Headless Horseman)
     */
    REGENERATION_CURSE,
    
    /**
     * Summons a Forest Spirit every 50s (Nature's Spirit)
     */
    FOREST_SUMMONING,
    
    /**
     * Immune to arrows (Countersniper)
     */
    ARROW_IMMUNITY,
    
    /**
     * Teleports behind player and enters SWORD phase when hit, 15s cooldown (Shadow Assassin)
     */
    SHADOW_TELEPORT,
    
    /**
     * Deals 50% more sword damage (Butcher)
     */
    ENHANCED_MELEE,
    
    /**
     * Teleports sometimes (Duskblood)
     */
    RANDOM_TELEPORT,
    
    /**
     * Strikes player with lightning for 25% true max health damage, 30s cooldown (Fury)
     */
    LIGHTNING_STRIKE,
    
    /**
     * Summons a random legendary sea creature every 30s (Leviathan)
     */
    SEA_SUMMONING
}
