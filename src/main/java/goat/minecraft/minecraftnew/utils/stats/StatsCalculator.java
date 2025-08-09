package goat.minecraft.minecraftnew.utils.stats;

import goat.minecraft.minecraftnew.other.health.HealthManager;
import goat.minecraft.minecraftnew.other.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.other.resistance.PlayerResistanceManager;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.subsystems.pets.TraitRarity;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class to aggregate various player statistics for display.
 */
public class StatsCalculator {

    private static StatsCalculator instance;
    private final JavaPlugin plugin;

    private StatsCalculator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the calculator singleton.
     * Subsequent calls simply return the existing instance.
     */
    public static synchronized StatsCalculator getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new StatsCalculator(plugin);
        }
        return instance;
    }

    /**
     * Get the already initialised instance.
     * @throws IllegalStateException if init has not yet been called
     */
    public static StatsCalculator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StatsCalculator not initialised");
        }
        return instance;
    }

    /** Calculate max health using HealthManager. */
    public double getHealth(Player player) {
        return HealthManager.getMaxHealth(player);
    }

    /** Approximate melee damage increase percent from reforges and talents. */
    public double getDamageIncrease(Player player) {
        double bonus = 0.0;
        // Strength mastery no longer grants direct damage bonus
        // Sword reforges now grant Strength instead of direct damage
        return bonus;
    }

    /**
     * Calculates the player's Strength value.
     *
     * @param player player to calculate Strength for
     * @return total Strength amount
     */
    public int getStrength(Player player) {
        return StrengthManager.getStrength(player);
    }

    /**
     * Calculates the player's Defense value using {@link DefenseManager}.
     * This aggregates armor, toughness, protections and other bonuses into a
     * single number representing how much incoming damage is mitigated.
     *
     * @param player player to calculate Defense for
     * @return total Defense amount
     */
    public double getDefense(Player player) {
        return DefenseManager.getDefense(player, DefenseManager.DamageTag.GENERIC);
    }

    /** Arrow damage increase percent from talents, potions and reforges. */
    public double getArrowDamageIncrease(Player player) {
        double bonus = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            bonus += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARROW_DAMAGE_INCREASE_I) * 4.0;
            bonus += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARROW_DAMAGE_INCREASE_II) * 8.0;
            bonus += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARROW_DAMAGE_INCREASE_III) * 12.0;
            bonus += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARROW_DAMAGE_INCREASE_IV) * 16.0;
            bonus += mgr.getTalentLevel(player.getUniqueId(), Skill.COMBAT, Talent.ARROW_DAMAGE_INCREASE_V) * 20.0;
        }
        if (CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void") > 0) {
            bonus += CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        }
        if (PetManager.getInstance(plugin).getActivePet(player) != null &&
                PetManager.getInstance(plugin).getActivePet(player).hasPerk(PetManager.PetPerk.SHOTCALLING)) {
            int lvl = PetManager.getInstance(plugin).getActivePet(player).getLevel();
            bonus += lvl * 0.5;
        }
        ReforgeManager rm = new ReforgeManager();
        ItemStack bow = player.getInventory().getItemInMainHand();
        if (rm.isBow(bow)) {
            ReforgeManager.ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(bow));
            bonus += tier.getBowDamageIncrease();
        }
        if (PetManager.getInstance(plugin).getActivePet(player) != null &&
                PetManager.getInstance(plugin).getActivePet(player).getTrait() == PetTrait.PRECISE) {
            TraitRarity rarity = PetManager.getInstance(plugin).getActivePet(player).getTraitRarity();
            double val = PetTrait.PRECISE.getValueForRarity(rarity);
            if (SkillTreeManager.getInstance() != null) {
                int q = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                val *= (1 + q * 0.20);
            }
            bonus += val;
        }
        return bonus;
    }

    /** Calculate total resistance percentage from various sources. */
    public double getResistance(Player player) {
        return PlayerResistanceManager.getInstance(plugin).computeTotalResistance(player);
    }

    /** Maximum flight time in seconds from pet perks and merits. */
    public int getFlightTime(Player player) {
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        int seconds = 0;
        if (pet != null && pet.hasPerk(PetManager.PetPerk.FLIGHT)) {
            seconds = (int) (pet.getLevel() / 100.0 * 60);
            PlayerMeritManager merits = PlayerMeritManager.getInstance(plugin);
            if (merits.hasPerk(player.getUniqueId(), "Icarus")) {
                seconds *= 2;
            }
        }
        return seconds;
    }

    /**
     * Overall grave spawning chance as used by the {@link goat.minecraft.minecraftnew.subsystems.gravedigging.Gravedigging}
     * event. The calculation mirrors the roll performed when a surface block is broken so that both the stats
     * display and the actual event logic remain in sync.
     *
     * @return chance expressed as a percentage
     */
    public double getGraveChance(Player player) {
        double chance = 0.0005; // base chance

        // Shovel enchantment bonus
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool != null && tool.getType().toString().endsWith("_SHOVEL")) {
            int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Lynch");
            chance += level * 0.0002;
        }

        // Pet perks and traits
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null) {
            if (activePet.hasPerk(PetManager.PetPerk.MEMORY)) chance += 0.001;
            if (activePet.hasPerk(PetManager.PetPerk.HAUNTING)) chance += 0.002;
            if (activePet.hasPerk(PetManager.PetPerk.SCREAM)) chance += 0.003;
            if (activePet.hasPerk(PetManager.PetPerk.COLD)) chance += 0.004;
            if (activePet.hasPerk(PetManager.PetPerk.MALIGNANCE)) chance += 0.005;

            if (activePet.getTrait() == PetTrait.PARANORMAL) {
                double val = activePet.getTrait().getValueForRarity(activePet.getTraitRarity());
                SkillTreeManager stm = SkillTreeManager.getInstance();
                if (stm != null) {
                    int q = stm.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                    val *= (1 + q * 0.20);
                }
                chance += val;
            }
        }

        // Potion effects
        if (PotionManager.isActive("Potion of Metal Detection", player)
                && PotionEffectPreferences.isEnabled(player, "Potion of Metal Detection")) {
            chance += 0.001;
        }

        // Talents
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            int g1 = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_I);
            int g2 = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_II);
            int g3 = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_III);
            int g4 = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_IV);
            int g5 = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_DIGGER_V);
            chance += g1 * 0.0001;
            chance += g2 * 0.00015;
            chance += g3 * 0.0002;
            chance += g4 * 0.00025;
            chance += g5 * 0.000725;
        }

        // Catalyst bonus
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.DEATH)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.DEATH);
            if (cat != null) {
                chance += 0.001 + (cm.getCatalystTier(cat) * 0.0001);
            }
        }

        // Night time doubles the chance
        long time = player.getWorld().getTime();
        if (time >= 13000 && time <= 23000) {
            chance = Math.min(1.0, chance * 2);
        }

        return chance * 100.0;
    }

    /** Sea creature chance using same logic as command. */
    public double getSeaCreatureChance(Player player) {
        double total = 0.0;
        int scc1 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.SEA_CREATURE_CHANCE_I);
        int scc2 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.SEA_CREATURE_CHANCE_II);
        int scc3 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.SEA_CREATURE_CHANCE_III);
        int scc4 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.SEA_CREATURE_CHANCE_IV);
        int scc5 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.SEA_CREATURE_CHANCE_V);
        total += scc1 * 0.5;
        total += scc2 * 1.0;
        total += scc3 * 1.5;
        total += scc4 * 2.0;
        total += scc5 * 2.5;
        if (CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void") > 0) {
            total += CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        }
        if (PetManager.getInstance(plugin).getActivePet(player) != null) {
            PetManager.Pet p = PetManager.getInstance(plugin).getActivePet(player);
            if (p.hasPerk(PetManager.PetPerk.ANGLER)) total += 5;
            if (p.hasPerk(PetManager.PetPerk.HEART_OF_THE_SEA)) total += 10;
            if (p.getTrait() == PetTrait.NAUTICAL) {
                double val = p.getTrait().getValueForRarity(p.getTraitRarity());
                if (SkillTreeManager.getInstance() != null) {
                    int q = SkillTreeManager.getInstance()
                            .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                    val *= (1 + q * 0.20);
                }
                total += val;
            }
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.DEPTH)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.DEPTH);
            if (cat != null) {
                total += 5 + cm.getCatalystTier(cat);
            }
        }
        return total;
    }

    /** Treasure chance calculation similar to command. */
    public double getTreasureChance(Player player) {
        double chance = 5.0; // base
        int upgrade = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Piracy");
        chance += upgrade;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.TREASURE_HUNTER)) {
            chance += pet.getLevel() * 0.1;
        }
        if (pet != null && pet.getTrait() == PetTrait.TREASURED) {
            double val = pet.getTrait().getValueForRarity(pet.getTraitRarity());
            if (SkillTreeManager.getInstance() != null) {
                int q = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                val *= (1 + q * 0.20);
            }
            chance += val;
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.DEPTH)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.DEPTH);
            if (cat != null) {
                chance += 5 + cm.getCatalystTier(cat);
            }
        }
        return chance;
    }

    /** Spirit chance calculation simplified. */
    public double getSpiritChance(Player player) {
        double chance = 0.01; // base
        int upgrade = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Effigy Yield");
        chance += upgrade * 0.000333;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.HAUNTED) {
            double val = pet.getTrait().getValueForRarity(pet.getTraitRarity()) / 100.0;
            if (SkillTreeManager.getInstance() != null) {
                int q = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                val *= (1 + q * 0.20);
            }
            chance += val;
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.INSANITY)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.INSANITY);
            if (cat != null) {
                chance += 0.0005 + (cm.getCatalystTier(cat) * 0.0005);
            }
        }
        return chance * 100.0;
    }

    /** Discount percent from traits, pet perks and bartering talents. */
    public double getDiscount(Player player) {
        double discount = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            int level = 0;
            level += mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_I) * 1; // each 0.5%
            level += mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_II) * 2; // 1%
            level += mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_III) * 3; // 1.5%
            level += mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_IV) * 4; // 2%
            level += mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.HAGGLER_V) * 5; // 2.5%
            discount += level * 0.5;
            int billionaire = mgr.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BILLIONAIRE_DISCOUNT);
            discount += billionaire * 5.0;
            discount += mgr.getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.HAGGLE) * 5.0;
        }
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.FINANCIAL) {
            double val = pet.getTrait().getValueForRarity(pet.getTraitRarity());
            if (SkillTreeManager.getInstance() != null) {
                int q = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.QUIRKY);
                val *= (1 + q * 0.20);
            }
            discount += val;
        }
        return discount;
    }

    /** Speed percent relative to normal walk speed. */
    public double getSpeed(Player player) {
        float speed = player.getWalkSpeed();
        return ((speed / 0.2f) - 1.0f) * 100.0f;
    }

    /** Brew time reduction percent from pet perks. */
    public double getBrewTimeReduction(Player player) {
        double reduction = 0.0;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.SPLASH_POTION)) {
            reduction += pet.getLevel() / 2.0;
        }
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.SPLASH_POTION);
            reduction += lvl * 10.0;
        }
        return reduction;
    }

    /** Double ore chance from talent and catalysts. */
    public double getDoubleOreChance(Player player) {
        double chance = 0.0;
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (cat != null) {
                int t = cm.getCatalystTier(cat);
                chance = Math.max(chance, 40 + t * 10);
            }
        }
        return chance;
    }

    /** Double log chance from forestry talent and catalysts. */
    public double getDoubleLogChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            chance += SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.TIMBER_I) * 20.0;
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (cat != null) {
                int t = cm.getCatalystTier(cat);
                chance = Math.max(chance, 40 + t * 10);
            }
        }
        return chance;
    }

    /** Double crop chance from talents and catalysts. */
    public double getDoubleCropChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.BOUNTIFUL_HARVEST);
            chance += lvl * 4.0;
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (cat != null) {
                int t = cm.getCatalystTier(cat);
                chance = Math.max(chance, 40 + t * 10);
            }
        }
        return chance;
    }

    /** Extra crop chance from new farming talents and catalysts. */
    public double getExtraCropChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            chance += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.EXTRA_CROP_CHANCE_I) * 8.0;
            chance += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.EXTRA_CROP_CHANCE_II) * 16.0;
            chance += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.EXTRA_CROP_CHANCE_III) * 24.0;
            chance += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.EXTRA_CROP_CHANCE_IV) * 32.0;
            chance += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.EXTRA_CROP_CHANCE_V) * 40.0;
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (cat != null) {
                int t = cm.getCatalystTier(cat);
                chance = Math.max(chance, 40 + t * 10);
            }
        }
        return chance;
    }

    /** Repair amount from smithing talents. */
    public double getRepairAmount(Player player) {
        double amount = 25.0; // base
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_AMOUNT_I) * 3;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_AMOUNT_II) * 4;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_AMOUNT_III) * 5;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_AMOUNT_IV) * 6;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_AMOUNT_V) * 7;
        }
        return amount;
    }

    /** Repair quality from smithing talents. */
    public double getRepairQuality(Player player) {
        double quality = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.QUALITY_MATERIALS_I) * 1;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.QUALITY_MATERIALS_II) * 2;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.QUALITY_MATERIALS_III) * 3;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.QUALITY_MATERIALS_IV) * 4;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.QUALITY_MATERIALS_V) * 5;
        }
        return quality;
    }

    /**
     * Golden repair amount from smithing talents.
     * Base amount is 10.
     */
    public double getGoldenRepairAmount(Player player) {
        double amount = 10.0; // base
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDEN_REPAIR_I) * 2;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDEN_REPAIR_II) * 2;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDEN_REPAIR_III) * 2;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDEN_REPAIR_IV) * 2;
            amount += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDEN_REPAIR_V) * 2;
        }
        return amount;
    }

    /**
     * Golden repair quality from smithing talents.
     * Base quality is 0.
     */
    public double getGoldenRepairQuality(Player player) {
        double quality = 0.0; // base
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDSMITH_I) * 2;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDSMITH_II) * 2;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDSMITH_III) * 2;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDSMITH_IV) * 2;
            quality += mgr.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.GOLDSMITH_V) * 2;
        }
        return quality;
    }
}
