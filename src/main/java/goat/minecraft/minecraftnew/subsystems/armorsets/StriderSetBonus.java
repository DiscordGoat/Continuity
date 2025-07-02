package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowManager;
import goat.minecraft.minecraftnew.subsystems.armorsets.FlowType;
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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Strider full set bonus while the player is wearing the full set.
 * Provides +40% walk speed bonus.
 */
public class StriderSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final FlowManager flowManager;
    private final Map<UUID, Boolean> applied = new HashMap<>();
    private final Map<UUID, Double> baseSpeed = new HashMap<>();

    public StriderSetBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        this.flowManager = FlowManager.getInstance(plugin);
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

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Check if player is actually moving (not just looking around)
        if (event.getFrom().distanceSquared(event.getTo()) > 0.01) {
            // Add flow when moving
            if (Math.random() < 0.1) { // 10% chance per movement to avoid spam
                flowManager.addFlow(player, FlowType.STRIDER, 1);
            }
        }
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Strider")) {
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

        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double currentSpeed = speedAttr.getBaseValue();
            baseSpeed.put(id, currentSpeed);
            double newSpeed = currentSpeed * 1.4; // +40% speed
            speedAttr.setBaseValue(newSpeed);
        }

        applied.put(id, true);
    }

    private void removeBonus(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) {
            return;
        }

        AttributeInstance speedAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttr != null) {
            double originalSpeed = baseSpeed.getOrDefault(id, speedAttr.getBaseValue() / 1.4);
            speedAttr.setBaseValue(originalSpeed);
        }

        applied.put(id, false);
        baseSpeed.remove(id);
    }

    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removeBonus(player);
        }
        applied.clear();
        baseSpeed.clear();
    }
}