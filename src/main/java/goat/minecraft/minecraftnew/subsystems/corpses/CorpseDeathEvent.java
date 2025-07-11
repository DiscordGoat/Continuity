package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
        playDeathEffects(entity, opt.get().getRarity());
        // Future drop logic using opt.get().getDrops()
    }

    private void playDeathEffects(Entity entity, Rarity rarity) {
        if (entity.getWorld() == null) return;
        Sound sound;
        Particle particle = Particle.SMOKE_NORMAL;
        float volume = 1.0f;
        float pitch = 1.0f;
        switch (rarity) {
            case UNCOMMON -> sound = Sound.ENTITY_SKELETON_DEATH;
            case RARE -> {
                sound = Sound.ENTITY_ZOMBIE_VILLAGER_DEATH;
                particle = Particle.CRIT;
            }
            case EPIC -> {
                sound = Sound.ENTITY_WITHER_DEATH;
                particle = Particle.EXPLOSION_LARGE;
                volume = 1.5f;
                pitch = 0.8f;
            }
            case LEGENDARY -> {
                sound = Sound.ENTITY_ENDER_DRAGON_DEATH;
                particle = Particle.EXPLOSION_HUGE;
                volume = 2.0f;
                pitch = 0.6f;
            }
            default -> sound = Sound.ENTITY_ZOMBIE_DEATH;
        }
        entity.getWorld().playSound(entity.getLocation(), sound, volume, pitch);
        entity.getWorld().spawnParticle(particle, entity.getLocation(), 25, 0.5, 0.5, 0.5, 0.1);
    }
}
