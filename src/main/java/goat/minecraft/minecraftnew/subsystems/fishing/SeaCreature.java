package goat.minecraft.minecraftnew.subsystems.fishing;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class SeaCreature {
    private String displayName;
    private Rarity rarity;
    private EntityType entityType;
    private ItemStack alchemyDrops;
    private Color armorColor; // Color for dyed armor
    private String playerHeadName; // Display name for player head
    private int level; // Level based on rarity
    private String skullName; // Holds the base64 URL string

    public SeaCreature(String displayName, Rarity rarity, EntityType entityType, ItemStack alchemyDrops,
                       Color armorColor, String skullName, int level) {
        this.displayName = displayName;
        this.rarity = rarity;
        this.entityType = entityType;
        this.alchemyDrops = alchemyDrops;
        this.armorColor = armorColor;
        this.level = level;
        this.skullName = skullName;
    }

    // Add a getter for the texture
    public String getSkullName() {
        return skullName;
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


    public int getLevel() {
        return level;
    }
    public static ItemStack createDyedLeatherArmor(Material material, Color color) {
        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        meta.setColor(color);
        armor.setItemMeta(meta);
        return armor;
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
