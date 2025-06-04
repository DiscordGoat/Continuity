package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Master Duelist merit perk.
 * <p>
 * Grants the player a 20% chance to critically strike for 50% extra damage.
 * Damage modification will be coded later.
 */
public class MasterDuelist implements Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;
    private static final double PROC_CHANCE = 0.20;

    public MasterDuelist(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Monster target)) return;
        if (!playerData.hasPerk(player.getUniqueId(), "Master Duelist")) return;

        if (Math.random() > PROC_CHANCE) return;

        double initialDamage = event.getDamage();

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!target.isValid() || target.isDead()) return;
            double extraDamage = initialDamage * 0.20;
            target.damage(extraDamage, player);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.05);
        }, 3L);
    }
    // TODO: Inject critical hit logic into combat system when implemented.
}
