package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.projectiles.Projectile;

import java.util.*;

public class Alpha implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;
    private final Map<UUID, List<Wolf>> playerWolves = new HashMap<>();

    public Alpha(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.ALPHA)) {
            return;
        }

        LivingEntity attacker = null;
        if (event.getDamager() instanceof LivingEntity le) {
            attacker = le;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof LivingEntity le) {
            attacker = le;
        }
        if (attacker == null) {
            return;
        }

        List<Wolf> wolves = playerWolves.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        wolves.removeIf(w -> !w.isValid() || w.isDead());
        if (wolves.size() >= 3) {
            return;
        }

        Wolf wolf = player.getWorld().spawn(player.getLocation(), Wolf.class);
        wolf.setOwner(player);
        wolf.setCustomName(ChatColor.GOLD + "Alpha Wolf: 15");
        wolf.setCustomNameVisible(true);
        wolf.setTarget(attacker);
        wolves.add(wolf);

        new BukkitRunnable() {
            int timer = 15;
            @Override
            public void run() {
                if (!wolf.isValid() || wolf.isDead()) {
                    wolves.remove(wolf);
                    cancel();
                    return;
                }
                timer--;
                if (timer <= 0) {
                    wolf.remove();
                    wolves.remove(wolf);
                    cancel();
                    return;
                }
                wolf.setCustomName(ChatColor.GOLD + "Alpha Wolf: " + timer);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }
}
