package goat.minecraft.minecraftnew.other.skilltree;

public enum Skill {
    BREWING("Brewing"),
    COMBAT("Combat"),
    PLAYER("Player"),
    TERRAFORMING("Terraforming"),
    FARMING("Farming"),
    FISHING("Fishing"),
    MINING("Mining");
    private final String displayName;

    Skill(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Skill fromDisplay(String name) {
        for (Skill s : values()) {
            if (s.displayName.equalsIgnoreCase(name)) {
                return s;
            }
        }
        return null;
    }
}
