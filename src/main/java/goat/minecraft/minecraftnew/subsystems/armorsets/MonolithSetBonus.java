package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Monolith full set bonus while the player is wearing the full set.
 * Grants +20 max health and 20% damage resistance. The bonus is automatically
 * applied on login and removed if any armor piece is unequipped.
 */
public class MonolithSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();
    private final Map<UUID, Double> baseHealth = new HashMap<>();

    public MonolithSetBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Reapply bonus for players already online (e.g. during reload)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayer(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(player), 1L);
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
        }
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Monolith")) {
            applyBonus(player);
        } else {
            removeBonus(player);
        }
    }

    private void applyBonus(Player player) {
        UUID id = player.getUniqueId();
        if (applied.getOrDefault(id, false)) {
            return;
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double current = attr.getBaseValue();
            baseHealth.put(id, current);
            double newMax = current + 20.0;
            attr.setBaseValue(newMax);
            if (player.getHealth() > newMax) {
                player.setHealth(newMax);
            }
        }
        applied.put(id, true);
    }

    private void removeBonus(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) {
            return;
        }
        player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double base = baseHealth.getOrDefault(id, attr.getBaseValue() - 20.0);
            attr.setBaseValue(base);
            if (player.getHealth() > base) {
                player.setHealth(base);
            }
        }
        applied.put(id, false);
        baseHealth.remove(id);
    }

    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeBonus(player);
        }
        applied.clear();
        baseHealth.clear();
    }
}
