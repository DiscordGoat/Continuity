package goat.minecraft.minecraftnew.subsystems.fishing;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SeaCreature {
    private String displayName;
    private Rarity rarity;
    private EntityType entityType;
    private ItemStack alchemyDrops;
    private Color armorColor; // Color for dyed armor
    private String playerHeadName; // Display name for player head
    private int level; // Level based on rarity

    public SeaCreature(String displayName, Rarity rarity, EntityType entityType, ItemStack alchemyDrops,
                       Color armorColor, String playerHeadName, int level) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.entityType = entityType;
        this.alchemyDrops = alchemyDrops;
        this.armorColor = armorColor;
        this.playerHeadName = playerHeadName;
        this.level = level;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public Rarity getRarity() {
        return rarity;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public ItemStack getAlchemyDrops() {
        return alchemyDrops;
    }

    public Color getArmorColor() {
        return armorColor;
    }

    public String getPlayerHeadName() {
        return playerHeadName;
    }

    public int getLevel() {
        return level;
    }

    public String getColoredDisplayName() {
        // Prepend color codes based on rarity
        switch (rarity) {
            case COMMON:
                return ChatColor.WHITE + displayName;
            case UNCOMMON:
                return ChatColor.GREEN + displayName;
            case RARE:
                return ChatColor.BLUE + displayName;
            case EPIC:
                return ChatColor.DARK_PURPLE + displayName;
            case LEGENDARY:
                return ChatColor.GOLD + displayName;
            case MYTHIC:
                return ChatColor.DARK_AQUA + displayName;
            default:
                return displayName;
        }
    }
}
