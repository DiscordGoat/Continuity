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
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class to aggregate various player statistics for display.
 */
public class StatsCalculator {

    private final JavaPlugin plugin;

    public StatsCalculator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Calculate max health using HealthManager. */
    public double getHealth(Player player) {
        return HealthManager.getInstance(plugin).computeMaxHealth(player);
    }

    /** Approximate melee damage increase percent from reforges, talents and catalysts. */
    public double getDamageIncrease(Player player) {
        double bonus = 0.0;
        // Strength mastery talent
        if (SkillTreeManager.getInstance() != null &&
                SkillTreeManager.getInstance().hasTalent(player, Talent.STRENGTH_MASTERY)) {
            bonus += 5.0;
        }
        // Weapon reforge bonus
        ReforgeManager rm = new ReforgeManager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (rm.isSword(weapon)) {
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(weapon));
            bonus += tier.getWeaponDamageIncrease();
        }
        // Power catalyst
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.POWER)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.POWER);
            if (cat != null) {
                int t = cm.getCatalystTier(cat);
                bonus += (0.25 + t * 0.05) * 100.0;
            }
        }
        return bonus;
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
            if (mgr.hasTalent(player, Talent.RECURVE_MASTERY)) {
                bonus += 5.0;
            }
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
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(bow));
            bonus += tier.getBowDamageIncrease();
        }
        if (PetManager.getInstance(plugin).getActivePet(player) != null &&
                PetManager.getInstance(plugin).getActivePet(player).getTrait() == PetTrait.PRECISE) {
            TraitRarity rarity = PetManager.getInstance(plugin).getActivePet(player).getTraitRarity();
            bonus += PetTrait.PRECISE.getValueForRarity(rarity);
        }
        return bonus;
    }

    /** Calculate total resistance percentage from various sources. */
    public double getResistance(Player player) {
        return PlayerResistanceManager.getInstance(plugin).computeTotalResistance(player);
    }

    /** Maximum flight distance in km from pet perks and merits. */
    public double getFlightDistance(Player player) {
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        double distance = 0.0;
        if (pet != null && pet.hasPerk(PetManager.PetPerk.FLIGHT)) {
            distance = pet.getLevel() / 100.0; // scales to 1km at level 100
            PlayerMeritManager merits = PlayerMeritManager.getInstance(plugin);
            if (merits.hasPerk(player.getUniqueId(), "Icarus")) {
                distance *= 2;
            }
        }
        return distance;
    }

    /** Grave spawning chance from talents and pet traits. */
    public double getGraveChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_INTUITION);
            chance += lvl * 0.001;
        }
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.PARANORMAL) {
            chance += pet.getTrait().getValueForRarity(pet.getTraitRarity());
        }
        CatalystManager cm = CatalystManager.getInstance();
        if (cm != null && cm.isNearCatalyst(player.getLocation(), CatalystType.DEATH)) {
            Catalyst cat = cm.findNearestCatalyst(player.getLocation(), CatalystType.DEATH);
            if (cat != null) {
                chance += 0.01 + (cm.getCatalystTier(cat) * 0.001);
            }
        }
        return chance * 100.0;
    }

    /** Sea creature chance using same logic as command. */
    public double getSeaCreatureChance(Player player) {
        double total = 0.0;
        int instinctLevel = SkillTreeManager.getInstance()
                .getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.ANGLERS_INSTINCT);
        total += instinctLevel * 0.25;
        if (CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void") > 0) {
            total += CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        }
        if (PetManager.getInstance(plugin).getActivePet(player) != null) {
            PetManager.Pet p = PetManager.getInstance(plugin).getActivePet(player);
            if (p.hasPerk(PetManager.PetPerk.ANGLER)) total += 5;
            if (p.hasPerk(PetManager.PetPerk.HEART_OF_THE_SEA)) total += 10;
            if (p.getTrait() == PetTrait.NAUTICAL) {
                total += p.getTrait().getValueForRarity(p.getTraitRarity());
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
            chance += pet.getTrait().getValueForRarity(pet.getTraitRarity());
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
            chance += pet.getTrait().getValueForRarity(pet.getTraitRarity()) / 100.0;
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
            int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BARTER_DISCOUNT);
            discount += level * 4.0;
            if (SkillTreeManager.getInstance().hasTalent(player, Talent.CHARISMA_MASTERY)) {
                discount += 5.0;
            }
        }
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.FINANCIAL) {
            discount += pet.getTrait().getValueForRarity(pet.getTraitRarity());
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
        return reduction;
    }

    /** Double ore chance from talent and catalysts. */
    public double getDoubleOreChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.RICH_VEINS);
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

    /** Double log chance from forestry talent and catalysts. */
    public double getDoubleLogChance(Player player) {
        double chance = 0.0;
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.DOUBLE_LOGS);
            chance += lvl * 10.0;
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
            int l1 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_ONE);
            amount += l1 * 1;
            int l2 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_TWO);
            amount += l2 * 2;
            int l3 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_THREE);
            amount += l3 * 3;
            int l4 = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_FOUR);
            amount += l4 * 4;
        }
        return amount;
    }

    /** Repair quality stat not yet implemented, returns zero. */
    public double getRepairQuality(Player player) {
        return 0.0;
    }
}
