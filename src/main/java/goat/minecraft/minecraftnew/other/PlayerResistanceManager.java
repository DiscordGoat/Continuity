package goat.minecraft.minecraftnew.other;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Aggregates sources of damage reduction for a player.
 */
public class PlayerResistanceManager {

    /**
     * Computes the player's total damage reduction percentage.
     */
    public static double computeTotalResistance(Player player) {
        double resistance = 0.0;

        PetManager.Pet pet = PetManager.getInstance().getActivePet(player);
        if (pet != null) {
            if (pet.getTrait() == PetTrait.RESILIENT) {
                resistance += pet.getTrait().getValueForRarity(pet.getTraitRarity());
            }
            if (pet.hasPerk(PetManager.PetPerk.WALKING_FORTRESS) || pet.hasPerk(PetManager.PetPerk.BONE_PLATING)) {
                resistance += pet.getLevel() * 0.5;
            }
        }

        ReforgeManager rm = new ReforgeManager();
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(armor));
            resistance += tier.getArmorDamageReduction();
            if (CustomEnchantmentManager.hasEnchantment(armor, "Physical Protection")) {
                resistance += 1.0;
            }
            resistance += armor.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            resistance += armor.getEnchantmentLevel(Enchantment.PROTECTION_FIRE);
            resistance += armor.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            resistance += armor.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
        }

        PotionEffect effect = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        if (effect != null) {
            resistance += (effect.getAmplifier() + 1) * 20.0;
        }

        return resistance;
    }
}
