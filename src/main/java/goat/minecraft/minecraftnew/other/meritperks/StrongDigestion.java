package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Strong Digestion merit perk.
 * <p>
 * Doubles the duration of consumed potions. Actual effect application will be added later.
 */
public class StrongDigestion implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    public StrongDigestion(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    /**
     * When a player drinks any potion, double the resulting potion effect
     * durations if they own this perk.
     */
    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        // Only care about potion items
        if (item == null ||
                !(item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION)) {
            return;
        }

        Player player = event.getPlayer();

        // Verify the player owns the perk
        if (!playerData.hasPerk(player.getUniqueId(), "Strong Digestion")) {
            return;
        }

        // Capture the potion effect types applied by this item
        PotionMeta meta = (item.getItemMeta() instanceof PotionMeta pm) ? pm : null;
        if (meta == null) {
            return;
        }

        // Build list of effects we expect to be applied
        java.util.Set<PotionEffectType> types = new java.util.HashSet<>();
        // Base potion effect
        PotionEffectType baseType = meta.getBasePotionData().getType().getEffectType();
        if (baseType != null) {
            types.add(baseType);
        }
        // Custom effects present on the potion item
        for (PotionEffect effect : meta.getCustomEffects()) {
            types.add(effect.getType());
        }

        // After the potion has been consumed and effects applied (next tick),
        // extend the duration of each matching effect
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (PotionEffectType type : types) {
                PotionEffect current = player.getPotionEffect(type);
                if (current != null) {
                    int newDuration = current.getDuration() * 2;
                    player.addPotionEffect(new PotionEffect(
                            type,
                            newDuration,
                            current.getAmplifier(),
                            current.isAmbient(),
                            current.hasParticles(),
                            current.hasIcon()
                    ), true);
                }
            }
        }, 1L);
    }
}
