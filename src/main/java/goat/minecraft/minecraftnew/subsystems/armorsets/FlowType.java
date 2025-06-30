package goat.minecraft.minecraftnew.subsystems.armorsets;

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
}
