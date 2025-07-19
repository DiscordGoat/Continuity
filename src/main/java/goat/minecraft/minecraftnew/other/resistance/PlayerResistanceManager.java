package goat.minecraft.minecraftnew.other.resistance;

import goat.minecraft.minecraftnew.other.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Computes total damage reduction for a player from various systems.
 */
public class PlayerResistanceManager {

    private static PlayerResistanceManager instance;
    private final JavaPlugin plugin;

    private static final double MAX_ENCHANT_REDUCTION = 16.0;
    private static final double MAX_REFORGE_REDUCTION = 20.0;

    private PlayerResistanceManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static synchronized PlayerResistanceManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PlayerResistanceManager(plugin);
        }
        return instance;
    }

    public static PlayerResistanceManager getInstance(JavaPlugin pluginIfAbsent) {
        if (instance == null) {
            instance = new PlayerResistanceManager(pluginIfAbsent);
        }
        return instance;
    }

    public double computeTotalResistance(Player player) {
        double reduction = 0.0;

        // Beacon passive
        if (BeaconPassivesGUI.hasBeaconPassives(player) &&
                BeaconPassivesGUI.hasPassiveEnabled(player, "sturdy")) {
            reduction += 15.0;
        }

        // Monolith full set bonus grants resistance effect
        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            reduction += 20.0;
        }

        // Potion effect
        PotionEffect effect = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        if (effect != null) {
            reduction += (effect.getAmplifier() + 1) * 20.0;
        }

        // Armor enchantments - Physical Protection
        int enchantPieces = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (CustomEnchantmentManager.hasEnchantment(piece, "Physical Protection")) {
                enchantPieces++;
            }
        }
        reduction += Math.min(enchantPieces * 1.0, MAX_ENCHANT_REDUCTION);

        // Reforge armor reduction
        ReforgeManager rm = new ReforgeManager();
        double reforge = 0.0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (rm.isArmor(piece)) {
                ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(piece));
                reforge += tier.getArmorDamageReduction();
            }
        }
        reduction += Math.min(reforge, MAX_REFORGE_REDUCTION);

        // Pet traits that give damage reduction
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.RESILIENT) {
            reduction += pet.getTrait().getValueForRarity(pet.getTraitRarity());
        }

        return reduction;
    }
}
