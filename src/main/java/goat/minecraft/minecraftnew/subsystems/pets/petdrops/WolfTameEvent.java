package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTameEvent;

public class WolfTameEvent implements Listener {

    private final PetManager petManager;

    public WolfTameEvent(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onWolfTame(EntityTameEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Wolf)) {
            return;
        }
        if (!(event.getOwner() instanceof Player player)) {
            return;
        }
        PetRegistry registry = new PetRegistry();
        registry.addPetByName(player, "Wolf");
        player.playSound(player.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.0f, 1.0f);
        entity.getWorld().spawnParticle(Particle.HEART, entity.getLocation(), 10, 0.5, 0.5, 0.5);
        entity.remove();
    }
}
