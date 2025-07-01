package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Fetch implements Listener {

    private final PetManager petManager;

    public Fetch(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerHitByArrow(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if(BlessingUtils.hasFullSetBonus(player, "Countershot")){
            event.setCancelled(true);
            arrow.remove();
            player.playSound(player.getLocation(), Sound.ENTITY_PARROT_EAT, 3, 1);
        }

        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.FETCH)) {
            return;
        }

        int level = activePet.getLevel();
        double chance = Math.min(level * 0.04, 1.0);
        if (Math.random() <= chance) {
            event.setCancelled(true);
            arrow.remove();
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.0f);
        }
    }
}
