package goat.minecraft.minecraftnew.subsystems.pets;

public enum UniqueTrait {
    NIGHT_VISION("Night Vision", PetManager.PetPerk.ECHOLOCATION),
    STRONG_SWIMMER("Strong Swimmer", PetManager.PetPerk.STRONG_SWIMMER),
    QUICK_DRAW("QuickDraw", PetManager.PetPerk.QUICK_DRAW),
    COLLECTOR("Collector", PetManager.PetPerk.COLLECTOR),
    BLACKLUNG("Blacklung", PetManager.PetPerk.BLACKLUNG),
    FETCH("Fetch", PetManager.PetPerk.FETCH),
    LULLABY("Lullaby", PetManager.PetPerk.LULLABY),
    GREEN_THUMB("Green Thumb", PetManager.PetPerk.GREEN_THUMB),
    WATERLOGGED("Waterlogged", PetManager.PetPerk.WATERLOGGED);

    private final String displayName;
    private final PetManager.PetPerk perk;

    UniqueTrait(String displayName, PetManager.PetPerk perk) {
        this.displayName = displayName;
        this.perk = perk;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PetManager.PetPerk getPerk() {
        return perk;
    }
}
