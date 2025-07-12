package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.npc.skin.Skin;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.util.SkinProperty;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

/**
 * Spawns a random Corpse from the registry with an emergence animation.
 */
public class CorpseEvent {
    private final JavaPlugin plugin;

    public CorpseEvent(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawns a random corpse at the given location rising from the ground.
     */
    public void trigger(Location location) {
        Optional<Corpse> optional = CorpseRegistry.getRandomCorpse();
        if (!optional.isPresent()) {
            return;
        }
        spawnCorpse(optional.get(), location);
    }





    private void spawnCorpse(Corpse corpse, Location loc) {
        NPCRegistry registry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        NPC npc = registry.createNPC(EntityType.PLAYER, corpse.getDisplayName());
        npc.setName(corpse.getDisplayName());
        Location spawnLoc = loc.clone().subtract(0, 1, 0);
        npc.getOrAddTrait(SkinTrait.class).setSkinName("Notch");
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("corpse", "pVQOg8wRhJKDcYYTfMxP199IgQZ6A2WwgHFpQWnYuvSqFVQ34X7q5N9tGyNOrs35XKoF/bhYoX91U/GKxgLozkMRN26kWUz4zZlJwGmIO4amztSjrVy5REz8vHdVi4uhVKLdf5GzqCIuC8yRX4gXyUvX2wZY0KU+gPqQkSYB74dI9ioMA518Yn+EnjPf8L9hDtvLt+cXjLC8Smf23VRIcJyWqKj8Vyjt7MQRqTFl3NdkCKDkSwyGkc30trnti7ioYkiL8dJh6OEdOYJJHH3CKDBFjHGQfVNc5TN8ZxtLZoMf0JOkveoYWRV39JrSwfKtNY7EWR8K9wV+U3EH91N+2fV9gLCLiqHb2PluhQ+1JxSOIMIPmb+rV9ToR8aVfqlGhmI+WjFqk5bx2mErX6idJDPalLOzjx+Xrj4i3UnyF/FbKG/Vz0knEALgZ3V+XE/u4kokALWSw37kuSPJv42h68el1CstCIObBQUOb1nzgLxeeMF4UpMDGapocEafB00eAlWM2NR4ZpRoGpdLbeDT/M6D4MPfw6P3woThkckYfHSV9i/VrUlCgo9jFNW0jad/vZj7UmJNMk+eZ6JCtD6PNg288LfIKf28JjohwfSfyodR5S4dBaFglye458WfvoD8X45vGT1UF+zMMV3RP8B4FpNUM0RiT4uKUgNQJYoz8kU=",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzI0MTA4OWFjNDUzNGMxOTgyZTcwYTZmNTAzYjE4MWQxOTgzMmEwOWI1ZWYzODg2NTQ4ZTRmYTU2MTA3MWEwNTEifX19");
        npc.spawn(spawnLoc);
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent("corpse", "pVQOg8wRhJKDcYYTfMxP199IgQZ6A2WwgHFpQWnYuvSqFVQ34X7q5N9tGyNOrs35XKoF/bhYoX91U/GKxgLozkMRN26kWUz4zZlJwGmIO4amztSjrVy5REz8vHdVi4uhVKLdf5GzqCIuC8yRX4gXyUvX2wZY0KU+gPqQkSYB74dI9ioMA518Yn+EnjPf8L9hDtvLt+cXjLC8Smf23VRIcJyWqKj8Vyjt7MQRqTFl3NdkCKDkSwyGkc30trnti7ioYkiL8dJh6OEdOYJJHH3CKDBFjHGQfVNc5TN8ZxtLZoMf0JOkveoYWRV39JrSwfKtNY7EWR8K9wV+U3EH91N+2fV9gLCLiqHb2PluhQ+1JxSOIMIPmb+rV9ToR8aVfqlGhmI+WjFqk5bx2mErX6idJDPalLOzjx+Xrj4i3UnyF/FbKG/Vz0knEALgZ3V+XE/u4kokALWSw37kuSPJv42h68el1CstCIObBQUOb1nzgLxeeMF4UpMDGapocEafB00eAlWM2NR4ZpRoGpdLbeDT/M6D4MPfw6P3woThkckYfHSV9i/VrUlCgo9jFNW0jad/vZj7UmJNMk+eZ6JCtD6PNg288LfIKf28JjohwfSfyodR5S4dBaFglye458WfvoD8X45vGT1UF+zMMV3RP8B4FpNUM0RiT4uKUgNQJYoz8kU=",
                "ewogICJ0aW1lc3RhbXAiIDogMTcyODg4NjE2MzA5NSwKICAicHJvZmlsZUlkIiA6ICJjNmViMzdjNmE4YjM0MDI3OGJjN2FmZGE3ZjMxOWJmMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbFJleUNhbGFiYXphbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNDEwODlhYzQ1MzRjMTk4MmU3MGE2ZjUwM2IxODFkMTk4MzJhMDliNWVmMzg4NjU0OGU0ZmE1NjEwNzFhMDUxIgogICAgfQogIH0KfQ==");
        if (npc.getEntity() instanceof org.bukkit.entity.LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            if (eq != null && corpse.getWeaponMaterial() != null && corpse.getWeaponMaterial() != Material.AIR) {
                eq.setItemInMainHand(new ItemStack(corpse.getWeaponMaterial()));
            }
        }
        npc.data().setPersistent(NPC.Metadata.DEFAULT_PROTECTED, false);
        npc.addTrait(new CorpseTrait(plugin, corpse.getLevel(), corpse.usesBow(),
                corpse.getDisplayName().equalsIgnoreCase("Duskblood") ? 100 : 0));
        npc.getEntity().setMetadata("CORPSE", new FixedMetadataValue(plugin, corpse.getDisplayName()));

        playSpawnSound(loc, corpse.getRarity());
    }

    private void playSpawnSound(Location loc, Rarity rarity) {
        Sound sound;
        float volume = 1.0f;
        float pitch = 1.0f;
        switch (rarity) {
            case UNCOMMON:
                sound = Sound.ENTITY_ZOMBIE_AMBIENT;
                break;
            case RARE:
                sound = Sound.ENTITY_WITHER_SPAWN;
                volume = 1.2f;
                pitch = 0.9f;
                break;
            case EPIC:
                sound = Sound.ENTITY_WITHER_SPAWN;
                volume = 1.5f;
                pitch = 0.8f;
                break;
            case LEGENDARY:
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL;
                volume = 2.0f;
                pitch = 0.7f;
                break;
            default:
                sound = Sound.BLOCK_GRAVEL_BREAK;
                break;
        }
        loc.getWorld().playSound(loc, sound, volume, pitch);
    }
}
