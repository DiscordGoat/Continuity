package goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.stats.StrengthManager;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the reforging system for weapons, armor, tools, bows, and elytras.
 */
public class ReforgeManager {

    private final JavaPlugin plugin = MinecraftNew.getInstance();

    /**
     * Represents the different tiers of reforging with associated properties.
     */
    public enum ReforgeTier {
        TIER_0(0, ChatColor.RESET, "Sword", "Armor", "Tool", "Bow", "Elytra", 0, 0, 0, 0),
        TIER_1(1, ChatColor.WHITE, "Sturdy Blade", "Sturdy Armor", "Sturdy Tool", "Oak Bow", "Gustborne Elytra", 4, 6, 100, 10),
        TIER_2(2, ChatColor.GREEN, "Sharpened Blade", "Reinforced Armor", "Enhanced Tool", "Birch Bow", "Skybound Elytra", 8, 12, 150, 20),
        TIER_3(3, ChatColor.BLUE, "Reinforced Blade", "Fortified Armor", "Refined Tool", "Spruce Bow", "Stormforged Elytra", 12, 18, 200, 30),
        TIER_4(4, ChatColor.DARK_PURPLE, "Lethal Blade", "Battle Armor", "Superior Tool", "Acacia Bow", "Tempest Elytra", 16, 24, 250, 40),
        TIER_5(5, ChatColor.GOLD, "Fatal Blade", "Legendary Armor", "Masterwork Tool", "Dark Oak Bow", "Zephyr Elytra", 20, 30, 400, 50);

        private final int tier;
        private final ChatColor color;
        private final String swordName;
        private final String armorName;
        private final String toolName;
        private final String bowName;
        private final String elytraName;
        private final int weaponDamageIncrease; // In percent
        private final int armorDefenseBonus; // Flat Defense bonus
        private final int toolDurabilityBonus; // Additional max durability
        private final int bowDamageIncrease; // In percent

        /**
         * Constructs a ReforgeTier enum constant.
         */
        ReforgeTier(int tier, ChatColor color, String swordName, String armorName, String toolName, String bowName,
                    String elytraName, int weaponDamageIncrease, int armorDefenseBonus, int toolDurabilityBonus, int bowDamageIncrease) {
            this.tier = tier;
            this.color = color;
            this.swordName = swordName;
            this.armorName = armorName;
            this.toolName = toolName;
            this.bowName = bowName;
            this.elytraName = elytraName;
            this.weaponDamageIncrease = weaponDamageIncrease;
            this.armorDefenseBonus = armorDefenseBonus;
            this.toolDurabilityBonus = toolDurabilityBonus;
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

        public String getElytraName() {
            return elytraName;
        }

        public int getWeaponDamageIncrease() {
            return weaponDamageIncrease;
        }

        public int getArmorDefenseBonus() {
            return armorDefenseBonus;
        }

        public int getToolDurabilityBonus() {
            return toolDurabilityBonus;
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
        boolean isElytra = isElytra(item);

        if (!isSword && !isArmor && !isTool && !isBow && !isElytra) {
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
                : isBow ? targetTier.getColor() + targetTier.getBowName()
                : isElytra ? targetTier.getColor() + targetTier.getElytraName()
                : meta.getDisplayName();
        meta.setDisplayName(newName);

        // Step 6: Update the item's lore with the appropriate percentage, preserving other lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove existing reforge-related lore
        lore.removeIf(line -> line.contains("Damage Increase:")
                || line.contains("Damage Reduction:")
                || line.contains("Defense:")
                || line.contains("Chance to repair durability:")
                || line.contains("Max Durability: +")
                || line.contains("Strength:")
                || line.contains("Max Gear:"));

        // Add the new reforge lore
        if (isSword) {
            lore.add(ChatColor.DARK_GRAY + "Strength: " + StrengthManager.COLOR + "+" +
                    targetTier.getWeaponDamageIncrease() + " " + StrengthManager.EMOJI);
        } else if (isArmor) {
            lore.add(ChatColor.DARK_GRAY + "Defense: " + DefenseManager.COLOR + "+" +
                    targetTier.getArmorDefenseBonus() + " " + DefenseManager.EMOJI);
        } else if (isTool) {
            lore.add(ChatColor.DARK_GRAY + "Max Durability: " + ChatColor.AQUA + "+" + targetTier.getToolDurabilityBonus());
        } else if (isBow) {
            lore.add(ChatColor.DARK_GRAY + "Damage Increase: " + ChatColor.AQUA + targetTier.getBowDamageIncrease() + "%");
        } else if (isElytra) {
            lore.add(ChatColor.DARK_GRAY + "Max Gear: " + ChatColor.AQUA + (targetTier.getTier() + 1));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        if (isTool) {
            CustomDurabilityManager.getInstance().addMaxDurabilityBonus(item, targetTier.getToolDurabilityBonus());
        }

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
     * Removes any reforge from the given item.
     * This clears reforge-related lore, resets the display name color,
     * removes durability bonuses and sets the tier back to 0.
     *
     * @param item The item to strip.
     */
    public void stripReforge(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        int currentTier = getReforgeTier(item);
        if (currentTier <= 0) {
            return;
        }

        ReforgeTier tier = getReforgeTierByTier(currentTier);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = plugin.getServer().getItemFactory().getItemMeta(item.getType());
        }

        // Reset name colouring
        if (meta.hasDisplayName()) {
            meta.setDisplayName(ChatColor.stripColor(meta.getDisplayName()));
        }

        // Remove reforge lore lines
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.contains("Damage Increase:")
                || line.contains("Damage Reduction:")
                || line.contains("Defense:")
                || line.contains("Chance to repair durability:")
                || line.contains("Max Durability: ")
                || line.contains("Max Durability: +")
                || line.contains("Strength:")
                || line.contains("Max Gear:"));
        meta.setLore(lore);
        item.setItemMeta(meta);

        if (isTool(item)) {
            CustomDurabilityManager.getInstance().addMaxDurabilityBonus(item, -tier.getToolDurabilityBonus());
        }

        setReforgeTier(item, 0);
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

    /**
     * Checks if an ItemStack is an elytra.
     */
    public boolean isElytra(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getType() == Material.ELYTRA;
    }
}
