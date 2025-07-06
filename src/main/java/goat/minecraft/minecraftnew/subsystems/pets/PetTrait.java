package goat.minecraft.minecraftnew.subsystems.pets;

/**
 * Standard pet traits. Each trait defines a description of its
 * effect and an array of stat values for each {@link TraitRarity}.
 * The array order matches the enum order of TraitRarity.
 */
public enum PetTrait {
    HEALTHY("Bonus health", new double[]{2,4,6,8,10,12,15}),
    FAST("Bonus speed", new double[]{2,3,4,5,6,7,8}),
    STRONG("Bonus damage", new double[]{2,4,6,8,10,12,15}),
    RESILIENT("Damage reduction", new double[]{1,2,3,4,5,6,8}),
    NAUTICAL("Swim speed", new double[]{1,2,3,4,5,6,8}),
    HAUNTED("Spectral damage", new double[]{1,2,3,4,5,6,8}),
    PRECISE("Accuracy", new double[]{1,2,3,4,5,6,8}),
    FINANCIAL("Extra coins", new double[]{1,2,3,4,5,6,8}),
    EVASIVE("Dodge chance", new double[]{1,2,3,4,5,6,8}),
    TREASURED("Treasure chance", new double[]{1,2,3,4,5,6,8});

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
