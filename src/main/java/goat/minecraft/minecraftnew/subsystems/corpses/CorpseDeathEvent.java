package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
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

        // 1) Check metadata so we only handle our corpses
        List<MetadataValue> meta = entity.getMetadata("CORPSE");
        if (meta.isEmpty()) return;

        // 2) Get the Citizens NPC wrapper for this entity
        NPC npc = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getNPC(entity);
        if (npc == null) return;  // not a Citizens NPC

        // 3) Clear any item drops (we handle loot ourselves)
        event.getDrops().clear();

        // 4) Fetch your Corpse data by name
        String corpseName = meta.get(0).asString();
        Optional<Corpse> corpseOpt = CorpseRegistry.getCorpseByName(corpseName);
        corpseOpt.ifPresent(corpse -> {
            playDeathEffects(entity, corpse.getRarity());
        });

        // 5) Destroy the NPC so it won’t re-spawn on reload
        npc.destroy();

        Bukkit.getLogger().info("Destroyed corpse NPC #" + npc.getId() + " on death.");
    }


    private void playDeathEffects(Entity entity, Rarity rarity) {
        if (entity.getWorld() == null) return;
        Sound sound;
        Particle particle = Particle.SMOKE;
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
                particle = Particle.EXPLOSION;
                volume = 1.5f;
                pitch = 0.8f;
            }
            case LEGENDARY -> {
                sound = Sound.ENTITY_ENDER_DRAGON_DEATH;
                particle = Particle.EXPLOSION;
                volume = 2.0f;
                pitch = 0.6f;
            }
            default -> sound = Sound.ENTITY_ZOMBIE_DEATH;
        }
        entity.getWorld().playSound(entity.getLocation(), sound, volume, pitch);
        entity.getWorld().spawnParticle(particle, entity.getLocation(), 25, 0.5, 0.5, 0.5, 0.1);
    }
}
