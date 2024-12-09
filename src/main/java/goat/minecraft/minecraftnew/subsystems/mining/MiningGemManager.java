package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the application and effects of mining gemstones.
 */
public class MiningGemManager {

    private final JavaPlugin plugin = MinecraftNew.getInstance();

    /**
     * Represents the different types of mining gemstones with associated effects.
     */
    public enum MiningGem {
        DIAMOND_GEM(ChatColor.DARK_AQUA, "Diamond Gem", "Diamond Gemstone"),
        LAPIS_GEM(ChatColor.BLUE, "Lapis Gem", "Lapis Gemstone"),
        REDSTONE_GEM(ChatColor.DARK_RED, "Redstone Gem", "Redstone Gemstone"),
        EMERALD_GEM(ChatColor.GREEN, "Emerald Gem", "Emerald Gemstone");

        private final ChatColor color;
        private final String displayName;
        private final String effectDescription;

        MiningGem(ChatColor color, String displayName, String effectDescription) {
            this.color = color;
            this.displayName = displayName;
            this.effectDescription = effectDescription;
        }

        public ChatColor getColor() {
            return color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEffectDescription() {
            return effectDescription;
        }
    }

    /**
     * Applies a gemstone to an item, ensuring no duplicate gems are applied.
     */
    public ItemStack applyGem(ItemStack item, MiningGem gem) {
        if (item == null || item.getType() == Material.AIR) {
            return item; // Invalid item
        }

        if (!isTool(item)) {
            return item; // Only tools can have gems
        }

        // Retrieve existing gemstones from the tool
        Set<MiningGem> existingGems = getGemsFromItem(item);

        // Check if the gem is already applied
        if (existingGems.contains(gem)) {
            return item; // Gemstone already applied, do nothing
        }

        // Apply the new gem
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = plugin.getServer().getItemFactory().getItemMeta(item.getType());
        }

        // Ensure no duplicate lore entry
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        if (!lore.contains(gem.getColor() + gem.getEffectDescription())) {
            lore.add(gem.getColor() + gem.getEffectDescription());
        }
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Retrieves all gemstones applied to the item by checking its lore.
     */
    public Set<MiningGem> getGemsFromItem(ItemStack item) {
        Set<MiningGem> gems = new HashSet<>();
        if (item == null || !item.hasItemMeta()) {
            return gems;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                for (MiningGem gem : MiningGem.values()) {
                    if (line.contains(gem.getEffectDescription())) {
                        gems.add(gem);
                    }
                }
            }
        }

        return gems;
    }

    /**
     * Checks if an ItemStack is a valid tool.
     */
    public boolean isTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        String typeName = item.getType().name();
        return typeName.endsWith("_PICKAXE") || typeName.endsWith("_SHOVEL") || typeName.endsWith("_AXE");
    }
}
