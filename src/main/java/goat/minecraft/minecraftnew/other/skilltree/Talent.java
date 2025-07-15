package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Enumeration of all available talents in the plugin.  This acts as a
 * registry so other classes can easily reference a specific talent by
 * using <code>Talent.REDSTONE</code> for example.
 */
public enum Talent {
    TRIPLE_BATCH(
            "Triple Batch Upgrade",
            ChatColor.GRAY + "Catches the excess potion in a glass bottle",
            ChatColor.YELLOW + "+5% " + ChatColor.GRAY + "Chance to brew 3 Potions.",
            10,
            50,
            Material.CAULDRON
    ),
    OPTIMAL_CONFIGURATION(
            "Optimal Brewing Stand Settings",
            ChatColor.GRAY + "Installs an ad-blocker to prevent wasted time",
            ChatColor.YELLOW + "-4s " + ChatColor.GOLD + "Brew Time.",
            10,
            1,
            Material.BREWING_STAND
    ),
    REDSTONE_ONE(
            "Redstone I",
            ChatColor.GRAY + "Allows Potions to steep for longer",
            ChatColor.YELLOW + "+4s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+4s " + ChatColor.GOLD + "Brew Time.",
            10,
            1,
            Material.REDSTONE
    ),
    REDSTONE_TWO(
            "Redstone II",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+4s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+4s " + ChatColor.GOLD + "Brew Time.",
            10,
                    20,
            Material.REDSTONE_BLOCK
    ),
    RECURVE_MASTERY(
            "Recurve Mastery",
            ChatColor.GRAY + "Add a Skeleton Skull",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Recurve Duration, "
                    + ChatColor.RED + "+5% Arrow Damage",
            4,
            25,
            Material.BOW
    ),
    REJUVENATION(
            "Rejuvenation",
            ChatColor.GRAY + "Add a Golden Apple",
            ChatColor.YELLOW + "+50s " + ChatColor.GREEN + "Bonus Health " + ChatColor.GRAY + "and " + ChatColor.GREEN + "Potion Surge",
            4,
            45,
            Material.GHAST_TEAR
    ),
    SOVEREIGNTY_MASTERY(
            "Sovereignty Mastery",
            ChatColor.GRAY + "Add an ender pearl",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Sovereignty Duration, "
                    + ChatColor.RED + "+5 Deflection Stacks",
            4,
            60,
            Material.PRISMARINE_SHARD
    ),
    STRENGTH_MASTERY(
            "Strength Mastery",
            ChatColor.GRAY + "Add a Singularity",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Strength Duration, "
                    + ChatColor.RED + "+5% Damage",
            4,
            25,
            Material.DIAMOND_SWORD
    ),
    LIQUID_LUCK_MASTERY(
            "Liquid Luck Mastery",
            ChatColor.GRAY + "Add a Golden Ingot",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Liquid Luck Duration",
            4,
            60,
            Material.HONEY_BOTTLE
    ),
    OXYGEN_MASTERY(
            "Oxygen Mastery",
            ChatColor.GRAY + "Add an obsidian block",
            ChatColor.YELLOW + "+50s " + ChatColor.AQUA + "Oxygen Recovery Duration",
            4,
            45,
            Material.GLASS_BOTTLE
    ),
    SWIFT_STEP_MASTERY(
            "Swift Step Mastery",
            ChatColor.GRAY + "Add a Pumpkin for added sugar",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Swift Step Duration, "
                    + ChatColor.AQUA + "+5% Speed",
            4,
            35,
            Material.FEATHER
    ),
    METAL_DETECTION_MASTERY(
            "Metal Detection Mastery",
            ChatColor.GRAY + "Add a diamond",
            ChatColor.YELLOW + "+(50*level)s " + ChatColor.LIGHT_PURPLE + "Metal Detection Duration, "
                    + ChatColor.YELLOW + "+(0.01*level) " + ChatColor.GRAY + "grave chance",
            4,
            55,
            Material.ZOMBIE_HEAD
),
    NIGHT_VISION_MASTERY(
            "Night Vision Mastery",
            ChatColor.GRAY + "Add a Spider eye",
            ChatColor.YELLOW + "+50s " + ChatColor.AQUA + "Night Vision Duration",
            4,
            40,
            Material.SPIDER_EYE
      ),
    SOLAR_FURY_MASTERY(
            "Solar Fury Mastery",
            ChatColor.GRAY + "Add a blaze rod",
            ChatColor.YELLOW + "+50s " + ChatColor.GOLD + "Solar Fury Duration",
            4,
            75,
            Material.FIRE_CHARGE
    ),
    FOUNTAIN_MASTERY(
            "Fountain Mastery",
            ChatColor.GRAY + "Add a heart of the sea",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Fountains Duration, "
                    + ChatColor.AQUA + "+5% Sea Creature Chance",
            4,
            60,
            Material.DARK_PRISMARINE
    );


    private final String name;
    private final String description;
    private final String technicalDescription;
    private final int maxLevel;
    private final int levelRequirement;
    private final Material icon;

    Talent(String name, String description, String technicalDescription, int maxLevel, int levelRequirement, Material icon) {
        this.name = name;
        this.description = description;
        this.technicalDescription = technicalDescription;
        this.maxLevel = maxLevel;
        this.levelRequirement = levelRequirement;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
    public String getTechnicalDescription() {
        return technicalDescription;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getLevelRequirement() {
        return levelRequirement;
    }

    public Material getIcon() {
        return icon;
    }

    public TalentRarity getRarity() {
        return TalentRarity.fromRequirement(levelRequirement);
    }
}
