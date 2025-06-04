package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * Helper class that provides the core resurrection logic shared by all
 * resurrection perk listeners.
 */
public final class ResurrectionUtil {

    private ResurrectionUtil() {
        // Utility class
    }

    /**
     * Attempts to consume one resurrection charge for the given player and
     * applies the totem of undying effects. The highest available charge is
     * consumed first.
     *
     * @param player     The player to resurrect.
     * @param playerData The merit manager used to check and remove perks.
     * @return {@code true} if a charge was consumed and the player was
     *         resurrected, {@code false} otherwise.
     */
    public static boolean tryResurrect(Player player, PlayerMeritManager playerData) {
        UUID uuid = player.getUniqueId();

        String perkToConsume = null;
        if (playerData.hasPerk(uuid, "Resurrection Charge 3")) {
            perkToConsume = "Resurrection Charge 3";
        } else if (playerData.hasPerk(uuid, "Resurrection Charge 2")) {
            perkToConsume = "Resurrection Charge 2";
        } else if (playerData.hasPerk(uuid, "Resurrection")) {
            perkToConsume = "Resurrection";
        }

        if (perkToConsume == null) {
            return false; // No available charges
        }

        // Consume the perk
        playerData.removePerk(uuid, perkToConsume);

        // Apply totem of undying effects
        player.setFireTicks(0);
        player.setHealth(Math.max(1.0, Math.min(player.getMaxHealth(), 1.0)));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 45 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 40 * 20, 0));

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        player.getWorld().spawnParticle(Particle.TOTEM, player.getLocation().add(0, 1, 0), 30);

        return true;
    }
}

