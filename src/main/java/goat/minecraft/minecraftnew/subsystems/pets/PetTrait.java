package goat.minecraft.minecraftnew.subsystems.pets;

/**
 * Standard pet traits. Each trait defines a description of its
 * effect and an array of stat values for each {@link TraitRarity}.
 * The array order matches the enum order of TraitRarity.
 */
public enum PetTrait {
    HEALTHY("Health Increase", new double[]{2,4,6,8,10,12,15}),
    FAST("Speed Increase", new double[]{2,3,4,5,6,7,8}),
    STRONG("Damage Increase", new double[]{2,4,6,8,10,12,15}),
    RESILIENT("Damage Reduction", new double[]{1,2,3,4,5,6,8}),
    NAUTICAL("Sea Creature Chance", new double[]{1,2,3,4,5,6,8}),
    HAUNTED("Spirit Chance", new double[]{1,2,3,4,5,6,8}),
    PRECISE("Arrow Damage Increase", new double[]{1,2,3,4,5,6,8}),
    FINANCIAL("Discount", new double[]{1,2,3,4,5,6,8}),
    EVASIVE("Dodge Chance", new double[]{1,2,3,4,5,6,8}),
    TREASURED("Treasure Chance", new double[]{1,2,3,4,5,6,8});

    private final String description;
    private final double[] values;

    PetTrait(String description, double[] values) {
        this.description = description;
        this.values = values;
    }

    public String getDescription() {
        return description;
    }

    public double getValueForRarity(TraitRarity rarity) {
        int index = rarity.ordinal();
        if (index < 0 || index >= values.length) {
            return 0;
        }
        return values[index];
    }

    public String getDisplayName() {
        String lower = name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
