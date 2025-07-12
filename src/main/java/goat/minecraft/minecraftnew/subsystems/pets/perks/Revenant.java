package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class Revenant implements Listener {
    private final PetManager petManager;
    private final JavaPlugin plugin;

    public Revenant(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PetManager.Pet active = petManager.getActivePet(player);
        if (active == null || !active.hasPerk(PetManager.PetPerk.REVENANT)) return;

        double finalDamage = event.getFinalDamage();
        if (player.getHealth() - finalDamage > 0) return;

        event.setCancelled(true);
        final var loc = player.getLocation();
        player.setGameMode(GameMode.SPECTATOR);
        player.setHealth(1.0);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 120 * 20, 0, false, false));
        BukkitTask repeat = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> player.teleport(loc), 0L, 1L);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            repeat.cancel();
            player.teleport(loc);
            player.setGameMode(GameMode.SURVIVAL);
            player.removePotionEffect(PotionEffectType.DARKNESS);
            player.setHealth(player.getMaxHealth());
        }, 120 * 20L);
    }
}
