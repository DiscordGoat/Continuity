package goat.minecraft.minecraftnew.other.skilltree;

import net.md_5.bungee.api.ChatColor;

public enum TalentRarity {
    COMMON(ChatColor.WHITE),
    UNCOMMON(ChatColor.GREEN),
    RARE(ChatColor.BLUE),
    EPIC(ChatColor.DARK_PURPLE),
    LEGENDARY(ChatColor.GOLD);

    private final ChatColor color;

    TalentRarity(ChatColor color) {
        this.color = color;
    }

    public ChatColor getColor() {
        return color;
    }

    public static TalentRarity fromRequirement(int levelRequirement) {
        if (levelRequirement < 20) return COMMON;
        if (levelRequirement < 40) return UNCOMMON;
        if (levelRequirement < 60) return RARE;
        if (levelRequirement < 80) return EPIC;
        return LEGENDARY;
    }
}
