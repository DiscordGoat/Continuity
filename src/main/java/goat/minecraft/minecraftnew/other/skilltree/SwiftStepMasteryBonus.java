package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Applies a small permanent speed bonus when the player has the
 * {@link Talent#SWIFT_STEP_MASTERY} talent.
 */
public class SwiftStepMasteryBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Float> baseSpeed = new HashMap<>();

    public SwiftStepMasteryBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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

    private void checkPlayer(Player player) {
        boolean hasTalent = SkillTreeManager.getInstance().hasTalent(player, Talent.SWIFT_STEP_MASTERY);
        UUID id = player.getUniqueId();
        if (hasTalent) {
            if (!baseSpeed.containsKey(id)) {
                baseSpeed.put(id, player.getWalkSpeed());
                player.setWalkSpeed((float) (player.getWalkSpeed() * 1.05));
            }
        } else {
            if (baseSpeed.containsKey(id)) {
                player.setWalkSpeed(baseSpeed.remove(id));
            }
        }
    }

    /**
     * Removes all applied speed bonuses. Called on plugin disable.
     */
    public void removeAllBonuses() {
        for (Map.Entry<UUID, Float> entry : baseSpeed.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                player.setWalkSpeed(entry.getValue());
            }
        }
        baseSpeed.clear();
    }
}
