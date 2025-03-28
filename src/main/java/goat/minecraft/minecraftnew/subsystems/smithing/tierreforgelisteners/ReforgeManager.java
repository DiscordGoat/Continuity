package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the reforging system for weapons, armor, tools, and bows.
 */
public class ReforgeManager {

    private final JavaPlugin plugin = MinecraftNew.getInstance();

    /**
     * Represents the different tiers of reforging with associated properties.
     */
    public enum ReforgeTier {
        TIER_0(0, ChatColor.RESET, "Sword", "Armor", "Tool", "Bow", 0, 0, 0, 0),
        TIER_1(1, ChatColor.WHITE, "Sturdy Blade", "Sturdy Armor", "Sturdy Tool", "Oak Bow", 4, 1, 5, 10),
        TIER_2(2, ChatColor.GREEN, "Sharpened Blade", "Reinforced Armor", "Enhanced Tool", "Birch Bow", 8, 2, 10, 20),
        TIER_3(3, ChatColor.BLUE, "Reinforced Blade", "Fortified Armor", "Refined Tool", "Spruce Bow", 12, 3, 15, 30),
        TIER_4(4, ChatColor.DARK_PURPLE, "Lethal Blade", "Battle Armor", "Superior Tool", "Acacia Bow", 16, 4, 20, 40),
        TIER_5(5, ChatColor.GOLD, "Fatal Blade", "Legendary Armor", "Masterwork Tool", "Dark Oak Bow", 20, 5, 25, 50);

        private final int tier;
        private final ChatColor color;
        private final String swordName;
        private final String armorName;
        private final String toolName;
        private final String bowName;
        private final int weaponDamageIncrease; // In percent
        private final int armorDamageReduction; // In percent
        private final int toolDurabilityChance; // In percent
        private final int bowDamageIncrease; // In percent

        /**
         * Constructs a ReforgeTier enum constant.
         */
        ReforgeTier(int tier, ChatColor color, String swordName, String armorName, String toolName, String bowName,
                    int weaponDamageIncrease, int armorDamageReduction, int toolDurabilityChance, int bowDamageIncrease) {
            this.tier = tier;
            this.color = color;
            this.swordName = swordName;
            this.armorName = armorName;
            this.toolName = toolName;
            this.bowName = bowName;
            this.weaponDamageIncrease = weaponDamageIncrease;
            this.armorDamageReduction = armorDamageReduction;
            this.toolDurabilityChance = toolDurabilityChance;
            this.bowDamageIncrease = bowDamageIncrease;
        }

        public int getTier() {
            return tier;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getSwordName() {
            return swordName;
        }

        public String getArmorName() {
            return armorName;
        }

        public String getToolName() {
            return toolName;
        }

        public String getBowName() {
            return bowName;
        }

        public int getWeaponDamageIncrease() {
            return weaponDamageIncrease;
        }

        public int getArmorDamageReduction() {
            return armorDamageReduction;
        }

        public int getToolDurabilityChance() {
            return toolDurabilityChance;
        }

        public int getBowDamageIncrease() {
            return bowDamageIncrease;
        }
    }

    /**
     * Applies a reforge to an item, incrementing it up to the specified target tier.
     * The item must be at the previous tier to be upgraded.
     */
    public ItemStack applyReforge(ItemStack item, ReforgeTier targetTier) {
        // Step 1: Validate the item
        if (item == null || item.getType() == Material.AIR) {
            return item; // Item is invalid
        }

        // Step 2: Determine the item type
        boolean isSword = isSword(item);
        boolean isArmor = isArmor(item);
        boolean isTool = isTool(item);
        boolean isBow = isBow(item);

        if (!isSword && !isArmor && !isTool && !isBow) {
            return item; // Not a valid item for reforging
        }

        // Step 3: Retrieve current reforge tier from NBT data
        int currentTier = getReforgeTier(item);

        // Step 4: Validate the reforge progression
        if (currentTier >= targetTier.getTier()) {
            return item; // Item is already at this tier or higher
        }

        if (currentTier != targetTier.getTier() - 1) {
            return item; // Cannot skip tiers
        }

        // Step 5: Apply the reforge
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = plugin.getServer().getItemFactory().getItemMeta(item.getType());
        }

        // Update item's display name and color
        String newName = isSword ? targetTier.getColor() + targetTier.getSwordName()
                : isArmor ? targetTier.getColor() + targetTier.getArmorName()
                : isTool ? targetTier.getColor() + targetTier.getToolName()
                : isBow ? targetTier.getColor() + targetTier.getBowName() : meta.getDisplayName();
        meta.setDisplayName(newName);

        // Step 6: Update the item's lore with the appropriate percentage, preserving other lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove existing reforge-related lore
        lore.removeIf(line -> line.contains("Damage Increase:")
                || line.contains("Damage Reduction:")
                || line.contains("Chance to repair durability:"));

        // Add the new reforge lore
        if (isSword) {
            lore.add(ChatColor.DARK_GRAY + "Damage Increase: " + ChatColor.AQUA + targetTier.getWeaponDamageIncrease() + "%");
        } else if (isArmor) {
            lore.add(ChatColor.DARK_GRAY + "Damage Reduction: " + ChatColor.AQUA + targetTier.getArmorDamageReduction() + "%");
        } else if (isTool) {
            lore.add(ChatColor.DARK_GRAY + "Chance to repair durability: " + ChatColor.AQUA + targetTier.getToolDurabilityChance() + "%");
        } else if (isBow) {
            lore.add(ChatColor.DARK_GRAY + "Damage Increase: " + ChatColor.AQUA + targetTier.getBowDamageIncrease() + "%");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        // Step 7: Store the new reforge tier in NBT data
        setReforgeTier(item, targetTier.getTier());

        return item;
    }

    /**
     * Retrieves the current reforge tier from the item's NBT data.
     */
    public int getReforgeTier(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return ReforgeTier.TIER_0.getTier();
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "ReforgeTier");

        return data.getOrDefault(key, PersistentDataType.INTEGER, ReforgeTier.TIER_0.getTier());
    }

    /**
     * Stores the new reforge tier in the item's NBT data.
     */
    private void setReforgeTier(ItemStack item, int tier) {
        if (item == null) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = plugin.getServer().getItemFactory().getItemMeta(item.getType());
        }

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "ReforgeTier");
        data.set(key, PersistentDataType.INTEGER, tier);
        item.setItemMeta(meta);
    }

    /**
     * Retrieves the ReforgeTier enum based on the tier number.
     */
    public ReforgeTier getReforgeTierByTier(int tier) {
        for (ReforgeTier rt : ReforgeTier.values()) {
            if (rt.getTier() == tier) {
                return rt;
            }
        }
        // Default to TIER_0 if tier not found
        return ReforgeTier.TIER_0;
    }

    /**
     * Checks if an ItemStack is a sword.
     */
    public boolean isSword(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType().name().endsWith("_SWORD");
    }

    /**
     * Checks if an ItemStack is armor.
     */
    public boolean isArmor(ItemStack item) {
        if (item == null) {
            return false;
        }
        String typeName = item.getType().name();
        return typeName.endsWith("_HELMET") || typeName.endsWith("_CHESTPLATE")
                || typeName.endsWith("_LEGGINGS") || typeName.endsWith("_BOOTS");
    }

    /**
     * Checks if an ItemStack is a tool.
     */
    public boolean isTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        String typeName = item.getType().name();
        return typeName.endsWith("_PICKAXE") || typeName.endsWith("_AXE")
                || typeName.endsWith("_SHOVEL") || typeName.endsWith("_HOE")
                || typeName.equals("SHEARS") || typeName.equals("FISHING_ROD");
    }

    /**
     * Checks if an ItemStack is a bow.
     */
    public boolean isBow(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType() == Material.BOW;
    }
}
