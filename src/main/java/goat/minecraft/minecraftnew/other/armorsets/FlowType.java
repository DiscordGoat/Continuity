package goat.minecraft.minecraftnew.other.armorsets;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Types of Flow effects used for armor sets.
 */
public enum FlowType {
    LOST_LEGION(
            Arrays.asList(
                    // Core weapons & ammo
                    Material.BOW,
                    Material.CROSSBOW,
                    Material.ARROW,
                    Material.TIPPED_ARROW,
                    Material.SPECTRAL_ARROW,
                    // Ammunition components
                    Material.FEATHER,
                    Material.STRING,
                    // Crafting & range tools
                    Material.FLETCHING_TABLE,
                    Material.TARGET,
                    Material.HAY_BLOCK
            ),
            Particle.CRIT
    ),

    MONOLITH(
            Arrays.asList(
                    // Heavy metals & blocks
                    Material.IRON_BLOCK,
                    Material.GOLD_BLOCK,
                    Material.DIAMOND_BLOCK,
                    Material.NETHERITE_BLOCK,
                    // Reinforcements
                    Material.OBSIDIAN,
                    Material.ANVIL,
                    Material.BLAST_FURNACE,
                    Material.IRON_BARS,
                    // Functional tank parts
                    Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    Material.IRON_TRAPDOOR,
                    Material.IRON_DOOR
            ),
            Particle.SMOKE_LARGE
    ),

    SCORCHSTEEL(
            Arrays.asList(
                    // Base terrain
                    Material.NETHERRACK,
                    Material.SOUL_SAND,
                    Material.SOUL_SOIL,
                    // Stone variants
                    Material.BASALT,
                    Material.POLISHED_BASALT,
                    Material.BLACKSTONE,
                    Material.POLISHED_BLACKSTONE,
                    // Brickwork
                    Material.NETHER_BRICKS,
                    Material.CRACKED_NETHER_BRICKS,
                    // Fungi & vegetation
                    Material.CRIMSON_NYLIUM,
                    Material.WARPED_NYLIUM,
                    Material.CRIMSON_STEM,
                    Material.WARPED_STEM,
                    Material.NETHER_WART_BLOCK,
                    // Special blocks
                    Material.MAGMA_BLOCK,
                    Material.CRYING_OBSIDIAN,
                    Material.GILDED_BLACKSTONE,
                    // Ores & debris
                    Material.NETHER_GOLD_ORE,
                    Material.NETHER_QUARTZ_ORE,
                    Material.ANCIENT_DEBRIS,
                    // Portal
                    Material.NETHER_PORTAL
            ),
            Particle.FLAME
    ),

    DWELLER(
            Arrays.asList(
                    // Pickaxes
                    Material.WOODEN_PICKAXE,
                    Material.STONE_PICKAXE,
                    Material.IRON_PICKAXE,
                    Material.GOLDEN_PICKAXE,
                    Material.DIAMOND_PICKAXE,
                    Material.NETHERITE_PICKAXE,
                    // Common Ores
                    Material.COAL_ORE,
                    Material.IRON_ORE,
                    Material.GOLD_ORE,
                    Material.REDSTONE_ORE,
                    Material.LAPIS_ORE,
                    Material.DIAMOND_ORE,
                    Material.EMERALD_ORE,
                    Material.COPPER_ORE,
                    Material.NETHER_QUARTZ_ORE,
                    // Stone Variants & Utilities
                    Material.STONE,
                    Material.COBBLESTONE,
                    Material.DEEPSLATE,
                    Material.TORCH
            ),
            Particle.ASH
    ),
    PASTURESHADE(
            Arrays.asList(
                    Material.WHEAT_SEEDS,
                    Material.BEETROOT_SEEDS,
                    Material.MELON_SEEDS,
                    Material.PUMPKIN_SEEDS,
                    Material.SUGAR_CANE,
                    Material.CARROT,
                    Material.POTATO,
                    Material.BEETROOT,
                    Material.WHEAT,
                    Material.BONE_MEAL,
                    Material.WOODEN_HOE,
                    Material.STONE_HOE,
                    Material.IRON_HOE,
                    Material.GOLDEN_HOE,
                    Material.DIAMOND_HOE,
                    Material.FARMLAND
            ),
            Particle.COMPOSTER
    ),
    NATURES_WRATH(
            Arrays.asList(
                    Material.IRON_AXE,
                    Material.IRON_AXE,
                    Material.IRON_AXE,
                    Material.IRON_AXE,
                    Material.IRON_AXE,
                    Material.IRON_AXE,
                    Material.OAK_LOG,
                    Material.OAK_LEAVES,
                    Material.BIRCH_LOG,
                    Material.BIRCH_LEAVES,
                    Material.GRASS_BLOCK,
                    Material.VINE,
                    Material.OAK_SAPLING,
                    Material.SUNFLOWER,
                    Material.DANDELION,
                    Material.LILY_PAD
            ),
            Particle.SPORE_BLOSSOM_AIR
    ),
    COUNTERSHOT(
            Arrays.asList(
                    // Deflecting core
                    Material.SHIELD,
                    // Iron-tier armor
                    Material.IRON_HELMET,
                    Material.IRON_CHESTPLATE,
                    Material.IRON_LEGGINGS,
                    Material.IRON_BOOTS,
                    // Diamond-tier armor
                    Material.DIAMOND_HELMET,
                    Material.DIAMOND_CHESTPLATE,
                    Material.DIAMOND_LEGGINGS,
                    Material.DIAMOND_BOOTS,
                    // Netherite-tier armor
                    Material.NETHERITE_HELMET,
                    Material.NETHERITE_CHESTPLATE,
                    Material.NETHERITE_LEGGINGS,
                    Material.NETHERITE_BOOTS
            ),
            Particle.END_ROD
    ),
    SHADOWSTEP(
            Arrays.asList(
                    // Deflecting core
                    Material.ENDER_EYE,
                    // Iron-tier armor
                    Material.ENDER_PEARL,
                    Material.BLACK_WOOL
            ),
            Particle.PORTAL
    ),
    STRIDER(
            Arrays.asList(
                    // Deflecting core
                    Material.SUGAR,
                    Material.FEATHER
            ),
            Particle.BUBBLE_POP
    ),
    SLAYER(
            Arrays.asList(
                    // Deflecting core
                    Material.IRON_SWORD,
                    Material.GOLDEN_AXE
            ),
            Particle.DAMAGE_INDICATOR
    ),
    DUSKBLOOD(
            Arrays.asList(
                    // Deflecting core
                    Material.ENDER_CHEST
            ),
            Particle.PORTAL
    ),
    THUNDERFORGE(
            Arrays.asList(
                    // Deflecting core
                    Material.GRAY_WOOL
            ),
            Particle.WATER_WAKE
    ),
    /**
     * Flow effect that swirls sea-themed blocks around the player.
     */
    FATHMIC_IRON(
            Arrays.asList(
                    Material.SEA_LANTERN,
                    Material.PRISMARINE,
                    Material.PRISMARINE_SHARD
            ),
            Particle.WATER_BUBBLE
    );
    private static final Random RANDOM = new Random();
    private final List<Material> materials;
    private final Particle particle;

    FlowType(List<Material> materials, Particle particle) {
        this.materials = materials;
        this.particle = particle;
    }

    public ItemStack createItem() {
        Material mat = materials.get(RANDOM.nextInt(materials.size()));
        return new ItemStack(mat);
    }

    public Particle getParticle() {
        return particle;
    }

    public List<Material> getMaterials() {
        return materials;
    }
}
