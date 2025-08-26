package goat.minecraft.minecraftnew.other.elytra;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraFlight implements Listener {
    private final MinecraftNew plugin;
    private final ReforgeManager reforgeManager = new ReforgeManager();
    private final Map<UUID, Integer> gear = new HashMap<>();
    private final Map<UUID, Long> lastBoost = new HashMap<>();
    private final Map<UUID, Long> sneakStart = new HashMap<>();
    private final Map<UUID, Vector> storedVelocity = new HashMap<>();
    private final Map<UUID, BukkitTask> slowTasks = new HashMap<>();

    private static final long BASE_COOLDOWN_MS = 30_000;

    public ElytraFlight(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    private int getMaxGear(Player player) {
        ItemStack chest = player.getInventory().getChestplate();
        int tier = reforgeManager.getReforgeTier(chest);
        return tier + 1;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.FIREWORK_ROCKET && event.getAction().isRightClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
        if (!event.getAction().isLeftClick()) {
            return;
        }

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        int currentGear = gear.getOrDefault(id, 1);
        int maxGear = getMaxGear(player);

        long cooldown = BASE_COOLDOWN_MS;
        if (reforgeManager.getReforgeTier(player.getInventory().getChestplate()) == 5 && currentGear <= 2) {
            cooldown /= 2;
        }

        long last = lastBoost.getOrDefault(id, 0L);
        if (now - last < cooldown) {
            return;
        }

        double boost = 0.5 * currentGear;
        Vector velocity = player.getLocation().getDirection().normalize().multiply(boost);
        player.setVelocity(player.getVelocity().add(velocity));

        lastBoost.put(id, now);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (!player.isGliding()) {
            return;
        }
        UUID id = player.getUniqueId();
        if (event.isSneaking()) {
            sneakStart.put(id, System.currentTimeMillis());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline() || !player.isGliding() || !player.isSneaking()) {
                    return;
                }
                storedVelocity.put(id, player.getVelocity());
                BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                    if (!player.isOnline() || !player.isGliding() || !player.isSneaking()) {
                        BukkitTask t = slowTasks.remove(id);
                        if (t != null) {
                            t.cancel();
                        }
                        return;
                    }
                    player.setVelocity(player.getLocation().getDirection().normalize().multiply(0.05));
                }, 0L, 20L);
                slowTasks.put(id, task);
            }, 20L);
        } else {
            long start = sneakStart.getOrDefault(id, 0L);
            long duration = System.currentTimeMillis() - start;
            sneakStart.remove(id);

            BukkitTask task = slowTasks.remove(id);
            if (task != null) {
                task.cancel();
            }
            Vector vec = storedVelocity.remove(id);
            if (vec != null) {
                player.setVelocity(vec);
            }

            if (duration < 1000) {
                int maxGear = getMaxGear(player);
                int currentGear = gear.getOrDefault(id, 1);
                currentGear++;
                if (currentGear > maxGear) {
                    currentGear = 1;
                }
                gear.put(id, currentGear);
                player.sendMessage(ChatColor.AQUA + "Shifted to gear " + currentGear + ".");
            }
        }
    }

    @EventHandler
    public void onStopGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.isGliding()) {
            return;
        }
        UUID id = player.getUniqueId();
        gear.remove(id);
        lastBoost.remove(id);
        sneakStart.remove(id);
        BukkitTask task = slowTasks.remove(id);
        if (task != null) {
            task.cancel();
        }
        storedVelocity.remove(id);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        BukkitTask task = slowTasks.remove(id);
        if (task != null) {
            task.cancel();
        }
        gear.remove(id);
        lastBoost.remove(id);
        sneakStart.remove(id);
        storedVelocity.remove(id);
    }
}
