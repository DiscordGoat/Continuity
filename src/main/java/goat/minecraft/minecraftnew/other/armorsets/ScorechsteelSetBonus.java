package goat.minecraft.minecraftnew.other.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.combat.FireDamageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies the Scorchsteel full set bonus while the player is wearing the full set.
 * Adds 20 Fire Stacks to enemies when hit and reduces damage from Nether monsters by 40%.
 */
public class ScorechsteelSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();

    public ScorechsteelSetBonus(JavaPlugin plugin) {
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

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity target) {
            // Add 20 Fire Stacks to the target
            FireDamageHandler.addFireStacks(target, 20);
            
            // Activate Flow on melee strikes
            FlowManager flowManager = FlowManager.getInstance(plugin);
            flowManager.addFlowStacks(player, 1);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Check if damage is from a Nether monster
        if (event instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity damager = damageByEntity.getDamager();
            if (isNetherMonster(damager)) {
                // Reduce damage by 40%
                event.setDamage(event.getDamage() * 0.6);
            }
        }
    }

    private boolean isNetherMonster(Entity entity) {
        return switch (entity.getType()) {
            case BLAZE, GHAST, MAGMA_CUBE, WITHER_SKELETON, ZOMBIFIED_PIGLIN, 
                 HOGLIN, ZOGLIN, STRIDER, PIGLIN, PIGLIN_BRUTE, WITHER -> true;
            default -> false;
        };
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Scorchsteel")) {
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
        applied.put(id, true);
    }

    private void removeBonus(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) {
            return;
        }
        applied.put(id, false);
    }

    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        applied.clear();
    }
}