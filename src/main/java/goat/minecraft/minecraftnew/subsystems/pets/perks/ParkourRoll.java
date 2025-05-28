package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class ParkourRoll implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;

    public ParkourRoll(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        // Only care about players and fall damage
        if (!(event.getEntity() instanceof Player) ||
                event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Player player = (Player) event.getEntity();
        PetManager.Pet activePet = petManager.getActivePet(player);

        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.PARKOUR_ROLL)) {
            int level = activePet.getLevel();

            // Flat damage thresholds: min = 4 HP, max scales 4→20 HP over levels 1→100
            double minHp = 0.0;
            double maxHp = minHp + (level - 1) * (20.0 - minHp) / 99.0;
            // Ensure we never exceed 20 HP
            maxHp = Math.min(maxHp, 20.0);

            double damage = event.getDamage();

            if (damage >= minHp && damage <= maxHp) {
                // Cancel that fall damage chunk entirely
                event.setCancelled(true);

                // Simulate a parkour roll by gliding briefly
                player.setGliding(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.setGliding(false);
                    }
                }, 10L);

                // Feedback sound
                player.playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT_DROWN,
                        0.7f, 1.2f);
            }
        }
    }
}
