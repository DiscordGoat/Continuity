package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.subsystems.auras.Aura;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Defines per-piece stats and full set data for blessed armor sets.
 */
public enum ArmorSet {
    LOST_LEGION("Lost Legion", new PieceStats(0, 0.5, 0.01), "Full Set Bonus: +25% Arrow Damage", Aura.LOST_LEGION),
    MONOLITH("Monolith", new PieceStats(5, 0, 0), "Full Set Bonus: +20 Health, +20% Defense", Aura.MONOLITH),
    SCORCHSTEEL("Scorchsteel", new PieceStats(0, 1, 0), "Full Set Bonus: +20 Fire Stacks, +40% Nether Monster Damage Reduction", Aura.SCORCHSTEEL),
    DWELLER("Dweller", new PieceStats(2, 0, 0), "Full Set Bonus: +25% Ore Yield, +500 Oxygen", Aura.DWELLER),
    PASTURESHADE("Pastureshade", new PieceStats(0, 0, 0), "Full Set Bonus: +100% Crop Yield, +1 Relic Yield", Aura.PASTURESHADE),
    NATURES_WRATH("Nature's Wrath", new PieceStats(0, 0.5, 0), "Full Set Bonus: +4% Spirit Chance, +25% Spirit Defense, +25% Spirit Damage", Aura.NATURES_WRATH),
    COUNTERSHOT("Countershot", new PieceStats(0, 0, 0), "Full Set Bonus: Arrow Deflection", Aura.COUNTERSHOT),
    SHADOWSTEP("Shadowstep", new PieceStats(0, 0, 0.02), "Full Set Bonus: +60% Dodge Chance", Aura.SHADOWSTEP),
    STRIDER("Strider", new PieceStats(0, 0, 0.03), "Full Set Bonus: +40 Walk Speed", Aura.STRIDER),
    SLAYER("Slayer", new PieceStats(0, 1, 0), "Full Set Bonus: +20% Damage", Aura.SLAYER),
    DUSKBLOOD("Duskblood", new PieceStats(0, 1, 0), "Full Set Bonus: +60 Warp Stacks", Aura.DUSKBLOOD),
    THUNDERFORGE("Thunderforge", new PieceStats(0, 0.5, 0), "Full Set Bonus: +15% Fury Chance", Aura.THUNDERFORGE),
    FATHMIC_IRON("Fathmic Iron", new PieceStats(0, 0, 0), "Full Set Bonus: Removes Common/Uncommon Sea Creatures, -20% Sea Creature Chance.", Aura.FATHMIC_IRON);

    private final String displayName;
    private final PieceStats stats;
    private final String fullSetBonus;
    private final Aura aura;

    ArmorSet(String displayName, PieceStats stats, String fullSetBonus, Aura aura) {
        this.displayName = displayName;
        this.stats = stats;
        this.fullSetBonus = fullSetBonus;
        this.aura = aura;
    }

    public String displayName() {
        return displayName;
    }

    public PieceStats stats() {
        return stats;
    }

    public String fullSetBonus() {
        return fullSetBonus;
    }

    public Aura aura() {
        return aura;
    }

    /**
     * Attempt to resolve an ArmorSet from the item name.
     */
    public static ArmorSet fromItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;
        String name = ChatColor.stripColor(meta.getDisplayName());
        int idx = name.lastIndexOf(' ');
        if (idx <= 0) return null;
        String setName = name.substring(0, idx);
        for (ArmorSet set : values()) {
            if (set.displayName.equalsIgnoreCase(setName)) {
                return set;
            }
        }
        return null;
    }

    /**
     * Simple record storing per-piece stat bonuses.
     */
    public record PieceStats(double health, double attackDamage, double speed) { }
}
