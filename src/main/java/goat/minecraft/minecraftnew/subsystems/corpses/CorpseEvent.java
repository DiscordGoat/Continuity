package goat.minecraft.minecraftnew.subsystems.corpses;

import goat.minecraft.minecraftnew.subsystems.fishing.Rarity;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
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
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, corpse.getDisplayName());
        Location spawnLoc = loc.clone().subtract(0, 1, 0);
        npc.spawn(spawnLoc);

        if (npc.getEntity() instanceof org.bukkit.entity.LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            if (eq != null && corpse.getWeaponMaterial() != null && corpse.getWeaponMaterial() != Material.AIR) {
                eq.setItemInMainHand(new ItemStack(corpse.getWeaponMaterial()));
            }
        }
        npc.data().setPersistent(NPC.DEFAULT_PROTECTED_METADATA, false);
        npc.addTrait(new CorpseTrait(plugin, corpse.getLevel(), corpse.usesBow(),
                corpse.getDisplayName().equalsIgnoreCase("Duskblood") ? 100 : 0));
        ChatColor color = SpawnCorpseCommand.getColorForRarityStatic(corpse.getRarity());
        npc.getEntity().setCustomName(ChatColor.GRAY + "[Lvl " + corpse.getLevel() + "] " + color + corpse.getDisplayName());
        npc.getEntity().setCustomNameVisible(true);
        npc.getEntity().setMetadata("CORPSE", new FixedMetadataValue(plugin, corpse.getDisplayName()));

        playSpawnSound(loc, corpse.getRarity());

        // Raise the corpse over 20 ticks with block crack particles
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 20 || !npc.isSpawned()) {
                    cancel();
                    return;
                }
                Location current = npc.getEntity().getLocation();
                npc.teleport(current.add(0, 0.05, 0), PlayerTeleportEvent.TeleportCause.PLUGIN);
                current.getWorld().spawnParticle(Particle.BLOCK, current, 5, 0.2, 0.1, 0.2, current.getBlock().getBlockData());
                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
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
