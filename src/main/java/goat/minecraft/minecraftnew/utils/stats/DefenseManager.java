package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.Pet;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.PetPerk;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.utils.devtools.TalismanManager;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Utility class for calculating a player's Defense stat and applying
 * Defense-based damage reduction.
 */
public final class DefenseManager {

    /** Chat color used when displaying Defense. */
    public static final ChatColor COLOR = ChatColor.AQUA;

    /** Emoji used alongside the Defense name. */
    public static final String EMOJI = "\uD83D\uDEE1"; // shield

    /** Preformatted display name for Defense with color and emoji. */
    public static final String DISPLAY_NAME = COLOR + "Defense " + EMOJI;

    private DefenseManager() {
        // Utility class
    }

    /**
     * Configuration values for Defense calculations.
     */
    public static class Config {
        // Generic sources
        public double genericProtLevelToDefense = 6.0;

        // Entity attack sources
        public double armorPointToDefense = 6.0;
        public double physProtLevelToDefense = 5.0;

        // Environmental protections
        public double featherFallingToDefense = 30.0;
        public double blastProtToDefense = 8.0;
        public double fireProtToDefense = 8.0;      // fire tick
        public double hotFloorProtToDefense = 20.0; // magma blocks
        public double projectileProtToDefense = 8.0;
    }

    /**
     * Damage categories used for typed protections.
     */
    public enum DamageTag {
        GENERIC,
        ENTITY_ATTACK,
        PROJECTILE,
        FALL,
        BLAST,
        FIRE_TICK,
        HOT_FLOOR,
        LAVA,
        FIRE
    }

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
        ItemStack[] armor = player.getInventory().getArmorContents();
        int protectionLevels = 0;
        int physicalProtectionLevels = 0;
        int blastProtLevels = 0;
        int projProtLevels = 0;
        int featherLevels = 0;
        int fireProtLevels = 0;
        double reforgeDefense = 0.0;
        double talismanDefense = 0.0;

        ReforgeManager rm = new ReforgeManager();
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            physicalProtectionLevels += CustomEnchantmentManager.getEnchantmentLevel(piece, "Physical Protection");
            protectionLevels += piece.getEnchantmentLevel(Enchantment.PROTECTION);
            blastProtLevels += piece.getEnchantmentLevel(Enchantment.BLAST_PROTECTION);
            projProtLevels += piece.getEnchantmentLevel(Enchantment.PROJECTILE_PROTECTION);
            featherLevels += piece.getEnchantmentLevel(Enchantment.FEATHER_FALLING);
            fireProtLevels += piece.getEnchantmentLevel(Enchantment.FIRE_PROTECTION);

            int tier = rm.getReforgeTier(piece);
            ReforgeManager.ReforgeTier rt = rm.getReforgeTierByTier(tier);
            reforgeDefense += rt.getArmorDefenseBonus();
            talismanDefense += TalismanManager.getDefenseBonus(piece);
        }

        double defense = (protectionLevels * cfg.genericProtLevelToDefense) +
                reforgeDefense + talismanDefense;

        if (tag == DamageTag.ENTITY_ATTACK) {
            double armorPoints = player.getAttribute(Attribute.GENERIC_ARMOR) != null
                    ? player.getAttribute(Attribute.GENERIC_ARMOR).getValue() : 0.0;
            defense += armorPoints * cfg.armorPointToDefense;
            defense += physicalProtectionLevels * cfg.physProtLevelToDefense;
        } else if (tag == DamageTag.FALL) {
            defense += featherLevels * cfg.featherFallingToDefense;
        } else if (tag == DamageTag.BLAST) {
            defense += blastProtLevels * cfg.blastProtToDefense;
        } else if (tag == DamageTag.FIRE_TICK) {
            defense += fireProtLevels * cfg.fireProtToDefense;
        } else if (tag == DamageTag.HOT_FLOOR) {
            defense += fireProtLevels * cfg.hotFloorProtToDefense;
        } else if (tag == DamageTag.PROJECTILE) {
            defense += projProtLevels * cfg.projectileProtToDefense;
        }
        // LAVA and FIRE provide no extra defense beyond generic

        double percentDefenseBuff = 0.0;
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        if (pm != null) {
            Pet active = pm.getActivePet(player);
            if (active != null && active.hasPerk(PetPerk.WALKING_FORTRESS)) {
                defense += 25; // Tank-style pet buff
            }
        }

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
        double mult = Math.max(0.0, 1.0 - defense * 0.002);
        return baseDamage * mult;
    }

    /**
     * Sends a breakdown of the player's Defense sources.
     */
    public static void sendDefenseBreakdown(Player player) {
        player.sendMessage(COLOR + "Defense Breakdown:");
        ItemStack[] armor = player.getInventory().getArmorContents();
        int protectionLevels = 0;
        double reforgeDefense = 0.0;
        double talismanDefense = 0.0;
        ReforgeManager rm = new ReforgeManager();
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            protectionLevels += piece.getEnchantmentLevel(Enchantment.PROTECTION);
            int tier = rm.getReforgeTier(piece);
            ReforgeManager.ReforgeTier rt = rm.getReforgeTierByTier(tier);
            reforgeDefense += rt.getArmorDefenseBonus();
            talismanDefense += TalismanManager.getDefenseBonus(piece);
        }
        double defense = getDefense(player, DamageTag.GENERIC);
        player.sendMessage(COLOR + "Reforges: " + ChatColor.YELLOW + reforgeDefense);
        player.sendMessage(COLOR + "Talismans: " + ChatColor.YELLOW + talismanDefense);
        player.sendMessage(COLOR + "Protection: " + ChatColor.YELLOW + protectionLevels * CONFIG.genericProtLevelToDefense);
        player.sendMessage(COLOR + "Total: " + ChatColor.YELLOW + defense);
    }
}
