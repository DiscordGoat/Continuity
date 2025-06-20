package goat.minecraft.minecraftnew.subsystems.beacon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Types of Beacon Catalysts with scaling buffs based on beacon tier.
 */
public enum CatalystType {
    POWER("Catalyst of Power", Material.NETHERITE_SWORD),
    FLIGHT("Catalyst of Flight", Material.ELYTRA),
    FORTITUDE("Catalyst of Fortitude", Material.SHIELD),
    DEPTH("Catalyst of Depth", Material.NAUTILUS_SHELL),
    INSANITY("Catalyst of Insanity", Material.TOTEM_OF_UNDYING),
    OXYGEN("Catalyst of Oxygen", Material.POTION);

    private final String displayName;
    private final Material icon;

    CatalystType(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public ItemStack createItem(int tier) {
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + displayName);
            meta.setLore(getLore(tier));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Generates lore describing the catalyst effects at the given beacon tier.
     */
    public List<String> getLore(int tier) {
        List<String> lore = new ArrayList<>();
        switch (this) {
            case POWER -> {
                double dmg = 0.25 + 0.05 * tier;
                lore.add(ChatColor.GRAY + String.format("Increase damage by +%.0f%%", dmg * 100));
            }
            case FLIGHT -> lore.add(ChatColor.GRAY + "Grants flight while active");
            case FORTITUDE -> {
                double red = 0.40 + 0.05 * tier;
                lore.add(ChatColor.GRAY + String.format("Damage reduction: +%.0f%%", red * 100));
                lore.add(ChatColor.GRAY + "Knockback immunity");
            }
            case DEPTH -> {
                double chance = 0.05 + 0.01 * tier;
                lore.add(ChatColor.GRAY + String.format("Treasure Chance: +%.0f%%", chance * 100));
                lore.add(ChatColor.GRAY + String.format("Sea Creature Chance: +%.0f%%", chance * 100));
            }
            case INSANITY -> {
                double spirit = 0.05 + 0.01 * tier;
                double red = 0.50 + 0.05 * tier;
                lore.add(ChatColor.GRAY + String.format("Spirit chance: +%.0f%%", spirit * 100));
                lore.add(ChatColor.GRAY + String.format("Spirit damage reduction: +%.0f%%", red * 100));
            }
            case OXYGEN -> {
                double interval = 3 - 0.2 * tier;
                lore.add(ChatColor.GRAY + String.format("+1 Oxygen every %.1fs", interval));
            }
        }
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Tier " + tier);
        return lore;
    }
}
