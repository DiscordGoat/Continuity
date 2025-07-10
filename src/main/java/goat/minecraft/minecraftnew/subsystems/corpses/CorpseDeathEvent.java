package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Optional;

/**
 * Handles drops and cleanup when a Corpse NPC dies.
 */
public class CorpseDeathEvent implements Listener {
    @EventHandler
    public void onCorpseDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        List<MetadataValue> data = entity.getMetadata("CORPSE");
        if (data == null || data.isEmpty()) {
            return;
        }
        if (entity instanceof LivingEntity) {
            EntityEquipment eq = ((LivingEntity) entity).getEquipment();
            if (eq != null) {
                eq.setItemInMainHandDropChance(0);
            }
        }
        String name = data.get(0).asString();
        Optional<Corpse> opt = CorpseRegistry.getCorpseByName(name);
        if (!opt.isPresent()) return;

        event.getDrops().clear();
        // Future drop logic using opt.get().getDrops()
    }
}
