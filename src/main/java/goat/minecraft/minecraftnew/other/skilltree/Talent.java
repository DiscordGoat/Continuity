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
    REDSTONE_THREE(
            "Redstone III",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+4s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+4s " + ChatColor.GOLD + "Brew Time.",
            10,
            60,
            Material.REDSTONE_TORCH
    ),
    REDSTONE_FOUR(
            "Redstone IV",
            ChatColor.GRAY + "Allows Potions to steep for even longer",
            ChatColor.YELLOW + "+4s " + ChatColor.LIGHT_PURPLE + "Potion Duration, " + ChatColor.GOLD + "+4s " + ChatColor.GOLD + "Brew Time.",
            10,
            80,
            Material.REDSTONE_ORE
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
    ),
    ANGLERS_INSTINCT(
            "Angler's Instinct",
            ChatColor.GRAY + "Hone your talent for reeling in the unusual",
            ChatColor.YELLOW + "+0.25% " + ChatColor.AQUA + "Sea Creature Chance per level",
            25,
            1,
            Material.NAUTILUS_SHELL
    ),
    CHARISMA_MASTERY(
            "Charisma Mastery",
            ChatColor.GRAY + "Add a bribe",
            ChatColor.YELLOW + "+50s " + ChatColor.LIGHT_PURPLE + "Charismatic Bartering Duration, "
                    + ChatColor.GOLD + "+5% Discount",
            4,
            50,
            Material.GOLD_BLOCK
    ),
    WOODEN_SWORD(
            "Wooden Sword",
            ChatColor.GRAY + "Train with a wooden blade",
            ChatColor.RED + "+8% Damage",
            6,
            1,
            Material.WOODEN_SWORD
    ),
    STONE_SWORD(
            "Stone Sword",
            ChatColor.GRAY + "Master the stone blade",
            ChatColor.RED + "+8% Damage",
            6,
            20,
            Material.STONE_SWORD
    ),
    IRON_SWORD(
            "Iron Sword",
            ChatColor.GRAY + "Hone iron sword techniques",
            ChatColor.RED + "+8% Damage",
            6,
            40,
            Material.IRON_SWORD
    ),
    GOLD_SWORD(
            "Gold Sword",
            ChatColor.GRAY + "Wield the golden sword with skill",
            ChatColor.RED + "+8% Damage",
            6,
            60,
            Material.GOLDEN_SWORD
    ),
    DIAMOND_SWORD(
            "Diamond Sword",
            ChatColor.GRAY + "Harness the power of diamond",
            ChatColor.RED + "+8% Damage",
            6,
            80,
            Material.DIAMOND_SWORD
    ),
    NETHERITE_SWORD(
            "Netherite Sword",
            ChatColor.GRAY + "Master the ultimate blade",
            ChatColor.RED + "+8% Damage",
            6,
            90,
            Material.NETHERITE_SWORD
    ),
    BOW_MASTERY(
            "Bow Mastery",
            ChatColor.GRAY + "Sharpen your aim",
            ChatColor.RED + "+8% Arrow Damage",
            25,
            10,
            Material.BOW
    ),
    DONT_MINE_AT_NIGHT(
            "Don't Mine at Night",
            ChatColor.GRAY + "Creepers beware of seasoned fighters",
            ChatColor.YELLOW + "+(10*level)% " + ChatColor.RED + "Creeper Damage",
            6,
            50,
            Material.TNT
    ),
    ULTIMATUM(
            "Ultimatum",
            ChatColor.GRAY + "Occasionally unleash devastating fury",
            ChatColor.YELLOW + "1% chance per level to trigger Fury",
            2,
            50,
            Material.LIGHTNING_ROD
    ),
    VAMPIRIC_STRIKE(
            "Vampiric Strike",
            ChatColor.GRAY + "Harvest souls for brief vitality",
            ChatColor.YELLOW + "1% chance per level to spawn a Soul Orb",
            6,
            50,
            Material.GHAST_TEAR
    ),
    REPAIR_ONE(
            "Repair Mastery I",
            ChatColor.GRAY + "Improve basic repair techniques",
            ChatColor.GREEN + "+(1*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            1,
            Material.ANVIL
    ),
    REPAIR_TWO(
            "Repair Mastery II",
            ChatColor.GRAY + "Further hone repair skills",
            ChatColor.GREEN + "+(2*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            30,
            Material.ANVIL
    ),
    REPAIR_THREE(
            "Repair Mastery III",
            ChatColor.GRAY + "Advanced repair expertise",
            ChatColor.GREEN + "+(3*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            50,
            Material.ANVIL
    ),
    REPAIR_FOUR(
            "Repair Mastery IV",
            ChatColor.GRAY + "Masterful repair proficiency",
            ChatColor.GREEN + "+(4*level) " + ChatColor.GRAY + "Repair Amount",
            10,
            70,
            Material.ANVIL
      ),
    SATIATION_MASTERY(
            "Satiation Mastery",
            ChatColor.GRAY + "Cooked meals keep you fuller",
            ChatColor.YELLOW + "+1 " + ChatColor.GRAY + "Saturation per level",
            5,
            10,
            Material.COOKED_BEEF
    ),
    FEASTING_CHANCE(
            "Feasting Chance",
            ChatColor.GRAY + "Occasionally feel extra nourished",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "chance for Saturation V",
            16,
            1,
            Material.GOLDEN_CARROT
    ),
    MASTER_CHEF(
            "Master Chef",
            ChatColor.GRAY + "Expertise yields extra portions",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "chance to double output",
            16,
            20,
            Material.CAKE
      ),
    BARTER_DISCOUNT(
            "Barter Discount",
            ChatColor.GRAY + "Sharpen your haggling skills",
            ChatColor.YELLOW + "+4% Discount per level",
            10,
            1,
            Material.EMERALD
    ),
    FREE_TRANSACTION(
            "Free Transaction",
            ChatColor.GRAY + "Occasionally pay nothing",
            ChatColor.YELLOW + "1% chance per level for free purchase",
            10,
            20,
            Material.EMERALD_BLOCK
    ),
    SELL_PRICE_BOOST(
            "Sell Price Boost",
            ChatColor.GRAY + "Villagers pay more",
            ChatColor.YELLOW + "+4% Sell Price per level",
            20,
            1,
            Material.EMERALD
    ),
    WORK_CYCLE_EFFICIENCY(
            "Work Cycle Efficiency",
            ChatColor.GRAY + "Reduce villager downtime",
            ChatColor.YELLOW + "-5s Workcycle per level",
            20,
            10,
            Material.CLOCK
      ),
    DOUBLE_LOGS(
            "Double Logs",
            ChatColor.GRAY + "Chance for extra logs",
            ChatColor.YELLOW + "+10% Double Log Chance.",
            10,
            1,
            Material.OAK_LOG
    ),
    FORESTRY_HASTE(
            "Forestry Haste",
            ChatColor.GRAY + "Chance to gain Haste while chopping",
            ChatColor.YELLOW + "+10% Haste chance.",
            10,
            20,
            Material.SUGAR
    ),
    HASTE_POTENCY(
            "Haste Potency",
            ChatColor.GRAY + "Increase Forestry Haste strength",
            ChatColor.YELLOW + "+1 Haste level.",
            4,
            40,
            Material.REDSTONE_TORCH
    ),
    TREECAP_SPIRIT(
            "Treecap Spirit",
            ChatColor.GRAY + "More spirit chance from Treecapitator",
            ChatColor.YELLOW + "+0.1% Spirit Chance",
            15,
            50,
            Material.SOUL_TORCH
    ),
    PET_TRAINER(
            "Pet Trainer",
            ChatColor.GRAY + "Sharpen your pet handling skills",
            ChatColor.YELLOW + "+4% " + ChatColor.GRAY + "Double Pet XP Chance",
            25,
            1,
            Material.BONE
    ),
  VITALITY(
            "Vitality",
            ChatColor.GRAY + "Fortify your body to survive longer",
            ChatColor.GREEN + "+1 Max Health per level",
            20,
            1,
            Material.APPLE
     ),
    CONSERVATIONIST(
            "Conservationist",
            ChatColor.GRAY + "Reduce wear on your tools",
            ChatColor.YELLOW + "+(1*level)% " + ChatColor.GRAY + "durability save chance",
            25,
            1,
            Material.DIAMOND_PICKAXE
    ),
    GRAVE_INTUITION(
            "Grave Intuition",
            ChatColor.GRAY + "Sense where graves may appear",
            ChatColor.YELLOW + "+(0.001*level) " + ChatColor.GRAY + "grave chance",
            10,
            25,
            Material.BONE
    ),
    BOUNTIFUL_HARVEST(
            "Bountiful Harvest",
            ChatColor.GRAY + "Increases chances for extra crops",
            ChatColor.YELLOW + "+(4*level)% " + ChatColor.GRAY + "chance to harvest " + ChatColor.GREEN + "double crops.",
            25,
            1,
            Material.WHEAT
    ),
    VERDANT_TENDING(
            "Verdant Tending",
            ChatColor.GRAY + "Expertise with relic cultivation",
            ChatColor.YELLOW + "-" + ChatColor.WHITE + "(2.5*level)m" + ChatColor.GRAY + " Verdant Relic growth time",
            10,
            40,
            Material.BONE_MEAL
    ),
    RICH_VEINS(
            "Rich Veins",
            ChatColor.GRAY + "Find extra ore when mining",
            ChatColor.YELLOW + "+(4*level)% " + ChatColor.GRAY + "Double Drop Chance",
            25,
            1,
            Material.IRON_PICKAXE
    ),

    DEEP_LUNGS(
            "Deep Lungs",
            ChatColor.GRAY + "Increase oxygen capacity underground",
            ChatColor.YELLOW + "+(20*level) " + ChatColor.AQUA + "Oxygen Capacity",
            25,
            10,
            Material.TURTLE_SCUTE
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
