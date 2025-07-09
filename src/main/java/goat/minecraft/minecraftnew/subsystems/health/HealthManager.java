package goat.minecraft.minecraftnew.subsystems.health;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.beacon.BeaconPassivesGUI;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Central manager for all player max health modifications.
 */
public class HealthManager {
    private static HealthManager instance;
    private final JavaPlugin plugin;
    private final XPManager xpManager;
    private final PetManager petManager;

    private HealthManager(JavaPlugin plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
        this.petManager = PetManager.getInstance(plugin);
    }

    /**
     * Returns the singleton instance, creating it when first used.
     */
    public static synchronized HealthManager getInstance(JavaPlugin plugin, XPManager xpManager) {
        if (instance == null) {
            instance = new HealthManager(plugin, xpManager);
        }
        return instance;
    }

    /**
     * Recalculate and apply max health for the specified player.
     */
    public void recalculate(Player player) {
        double max = 20.0;

        // Player skill bonus: +2 health per 10 levels
        int level = xpManager.getPlayerLevel(player, "Player");
        max += (level / 10) * 2.0;

        // Beacon Mending passive
        if (BeaconPassivesGUI.hasBeaconPassives(player) &&
                BeaconPassivesGUI.hasPassiveEnabled(player, "mending")) {
            max += 20.0;
        }

        // Monolith armor set bonus
        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            max += 20.0;
        }

        // Healthy pet trait bonus (percent, rounded down to full heart)
        PetManager.Pet pet = petManager.getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.HEALTHY) {
            double percent = pet.getTrait().getValueForRarity(pet.getTraitRarity());
            double withMultiplier = max * (1.0 + percent / 100.0);
            max = Math.floor(withMultiplier / 2.0) * 2.0;
        }

        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(max);
            if (player.getHealth() > max) {
                player.setHealth(max);
            }
        }
    }

    /**
     * Called when the plugin starts. Reapplies max health to online players.
     */
    public void startup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            recalculate(player);
        }
    }

    /**
     * Called on plugin shutdown to reset all players to default health.
     */
    public void shutdown() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(20.0);
                if (player.getHealth() > 20.0) {
                    player.setHealth(20.0);
                }
            }
        }
    }
}
