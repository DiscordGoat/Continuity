package goat.minecraft.minecraftnew.subsystems.pets.petdrops;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class WitherPetGrantListener implements Listener {

    private final PetManager petManager;

    public WitherPetGrantListener(PetManager petManager) {
        this.petManager = petManager;
    }

    @EventHandler
    public void onWitherSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Wither)) {
            return;
        }
        Wither wither = (Wither) event.getEntity();

        // Schedule a task to run 3 seconds (60 ticks) later.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!wither.isValid()) {
                    return; // Wither is no longer valid.
                }

                // Get the Wither's health attribute.
                AttributeInstance healthAttr = wither.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (healthAttr != null) {
                    // Remove any existing modifiers to prevent stacking.
                    for (AttributeModifier modifier : new ArrayList<>(healthAttr.getModifiers())) {
                        healthAttr.removeModifier(modifier);
                    }

                    // Force the Wither's maximum and current health to 2000.
                    SpawnMonsters spawnMonsters = SpawnMonsters.getInstance(new XPManager(MinecraftNew.getInstance()));
                    spawnMonsters.applyMobAttributes(wither, 100);
                    double newHealth = 2000.0;
                    healthAttr.setBaseValue(newHealth);
                    wither.setHealth(newHealth);

                }

                // Set metadata for mob level (used by projectile damage calculations, etc.).
                wither.setMetadata("mobLevel", new FixedMetadataValue(MinecraftNew.getInstance(), 300));
            }
        }.runTaskLater(MinecraftNew.getInstance(), 60L); // 60 ticks = 3 seconds
    }

    @EventHandler
    public void onWitherDeath(org.bukkit.event.entity.EntityDeathEvent event) {
        // Only proceed if the entity that died is a Wither.
        if (!(event.getEntity() instanceof Wither)) {
            return;
        }
        Wither wither = (Wither) event.getEntity();
        if (wither.getKiller() == null) {
            return;
        }

        // Grant the "Wither" pet to the killer.
        PetRegistry petRegistry = new PetRegistry();
        petRegistry.addPetByName(wither.getKiller(), "Wither");
    }
}//yup
