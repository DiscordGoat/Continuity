package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.Pet;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.PetPerk;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for calculating a player's Defense stat and applying
 * Defense-based damage reduction.
 */
public final class DefenseManager {

    /** Chat color used when displaying Defense. */
    public static final ChatColor COLOR = ChatColor.AQUA;

    /** Emoji used alongside the Defense name. */
    public static final String EMOJI = "\u26E8"; // shield

    /** Preformatted display name for Defense with color and emoji. */
    public static final String DISPLAY_NAME = COLOR + "Defense " + EMOJI;

    private DefenseManager() {
        // Utility class
    }

    /**
     * Configuration values for Defense calculations.
     */
    public static class Config {
        public double C = 20.0;
        public double minMult = 0.02;

        public double armorToDefense = 12.0;
        public double toughnessToDefense = 10.0;
        public double genericProtLevelToDefense = 6.0;

        public Map<String, Double> envProtPerLevel = Map.of(
            "FIRE", 6.0,
            "BLAST", 6.0,
            "PROJECTILE", 6.0,
            "FALL", 6.0,
            "MAGIC", 4.0
        );
    }

    /**
     * Damage categories used for typed protections.
     */
    public enum DamageTag { GENERIC, FIRE, BLAST, PROJECTILE, FALL, MAGIC }

    /** Default configuration instance. */
    public static Config CONFIG = new Config();

    /**
     * Calculates the player's total Defense value for the given damage tag.
     *
     * @param player the player to evaluate
     * @param tag the damage type tag
     * @return total Defense value
     */
    public static double getDefense(Player player, DamageTag tag) {
        Config cfg = CONFIG;
        double armorPoints = 0.0;
        double armorToughness = 0.0;
        if (player.getAttribute(Attribute.GENERIC_ARMOR) != null) {
            armorPoints = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        }
        if (player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS) != null) {
            armorToughness = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        }

        int genericProtLevels = 0;
        Map<DamageTag, Integer> envProtLevels = new EnumMap<>(DamageTag.class);
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece == null) continue;
            genericProtLevels += piece.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            envProtLevels.merge(DamageTag.FIRE,
                piece.getEnchantmentLevel(Enchantment.PROTECTION_FIRE), Integer::sum);
            envProtLevels.merge(DamageTag.BLAST,
                piece.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS), Integer::sum);
            envProtLevels.merge(DamageTag.PROJECTILE,
                piece.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE), Integer::sum);
            envProtLevels.merge(DamageTag.FALL,
                piece.getEnchantmentLevel(Enchantment.PROTECTION_FALL), Integer::sum);
        }

        double flatAdds = 0.0; // Placeholder for reforges/custom enchants
        double percentDefenseBuff = 0.0;
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            Pet active = pm.getActivePet(player);
            if (active != null && active.hasPerk(PetPerk.WALKING_FORTRESS)) {
                percentDefenseBuff += 0.25; // Tank-style pet buff
            }
        }

        double defense =
            armorPoints * cfg.armorToDefense +
            armorToughness * cfg.toughnessToDefense +
            genericProtLevels * cfg.genericProtLevelToDefense +
            flatAdds;

        int lvl = envProtLevels.getOrDefault(tag, 0);
        double perLevel = cfg.envProtPerLevel.getOrDefault(tag.name(), 0.0);
        defense += lvl * perLevel;

        defense *= (1.0 + percentDefenseBuff);
        return defense;
    }

    /**
     * Applies Defense reduction to base damage for the given player and damage tag.
     *
     * @param baseDamage incoming damage before Defense
     * @param player player taking the damage
     * @param tag damage type
     * @return final damage after Defense reduction
     */
    public static double computeFinalDamage(double baseDamage, Player player, DamageTag tag) {
        double defense = getDefense(player, tag);
        double mult = 1.0 / (1.0 + defense / CONFIG.C);
        mult = Math.max(mult, CONFIG.minMult);
        return baseDamage * mult;
    }

    /**
     * Utility: how much Defense for a 1% absolute drop at current fraction m (0<m<1).
     */
    public static double defenseForOnePercentDrop(double m, double C) {
        return C * (1.0 / (m - 0.01) - 1.0 / m);
    }

    /**
     * Sends a breakdown of the player's Defense sources.
     */
    public static void sendDefenseBreakdown(Player player) {
        player.sendMessage(COLOR + "Defense Breakdown:");
        double armorPoints = player.getAttribute(Attribute.GENERIC_ARMOR) != null
                ? player.getAttribute(Attribute.GENERIC_ARMOR).getValue() : 0.0;
        double armorToughness = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS) != null
                ? player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue() : 0.0;
        int genericProtLevels = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece == null) continue;
            genericProtLevels += piece.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        }
        double defense = getDefense(player, DamageTag.GENERIC);
        player.sendMessage(COLOR + "Armor: " + ChatColor.YELLOW + armorPoints * CONFIG.armorToDefense);
        player.sendMessage(COLOR + "Toughness: " + ChatColor.YELLOW + armorToughness * CONFIG.toughnessToDefense);
        player.sendMessage(COLOR + "Protection: " + ChatColor.YELLOW + genericProtLevels * CONFIG.genericProtLevelToDefense);
        player.sendMessage(COLOR + "Total: " + ChatColor.YELLOW + defense);
    }
}
