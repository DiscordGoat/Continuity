package goat.minecraft.minecraftnew.subsystems.corpses;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

/**
 * Trait giving a Corpse hostile behaviour and stats.
 */
public class CorpseTrait extends Trait {
    private final JavaPlugin plugin;
    private final int level;
    private final boolean ranged;
    private final int teleportInterval;
    private int taskId = -1;
    private int tickCounter = 0;

    private static final double BASE_DAMAGE = 2.0;

    public CorpseTrait(JavaPlugin plugin, int level, boolean ranged, int teleportInterval) {
        super("corpse_trait");
        this.plugin = plugin;
        this.level = level;
        this.ranged = ranged;
        this.teleportInterval = teleportInterval;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        applyAttributes(npc);
        start();
    }

    private void applyAttributes(NPC npc) {
        if (!(npc.getEntity() instanceof LivingEntity entity)) return;
        double healthMultiplier = level <= 10 ? 0.1 + 0.1 * (level - 1) : 1 + ((level - 10) * 0.1);
        double armorValue = Math.min(healthMultiplier * 20, 100);
        if (entity.getAttribute(Attribute.GENERIC_ARMOR) != null) {
            entity.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(armorValue);
        }
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            double baseHealth = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth * healthMultiplier);
            entity.setHealth(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
        }
        entity.setCustomNameVisible(true);
        entity.setMetadata("mobLevel", new org.bukkit.metadata.FixedMetadataValue(plugin, level));
    }

    private void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            LivingEntity self = (LivingEntity) npc.getEntity();
            Player target = getNearestPlayer(self.getLocation());
            if (target == null) return;
            npc.getNavigator().setTarget(target, true);

            tickCounter += 20;
            if (teleportInterval > 0 && tickCounter >= teleportInterval) {
                tickCounter = 0;
                self.teleport(target.getLocation());
            }

            double damageMultiplier = 1.0 + level * 0.06;
            double damage = BASE_DAMAGE * damageMultiplier;

            if (ranged) {
                if (self.getLocation().distanceSquared(target.getLocation()) <= 20 * 20) {
                    Arrow arrow = self.launchProjectile(Arrow.class);
                    Vector vel = target.getLocation().toVector().subtract(self.getLocation().toVector()).normalize().multiply(1.2);
                    arrow.setVelocity(vel);
                    arrow.setDamage(damage);
                }
            } else {
                if (self.getLocation().distanceSquared(target.getLocation()) <= 3 * 3) {
                    target.damage(damage, self);
                }
            }
        }, 20L, 20L);
    }

    private Player getNearestPlayer(Location loc) {
        double best = Double.MAX_VALUE;
        Player closest = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            double d = p.getLocation().distanceSquared(loc);
            if (d < best) {
                best = d;
                closest = p;
            }
        }
        return closest;
    }

    @Override
    public void onRemove() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    @Override public void load(DataKey key) {}
    @Override public void save(DataKey key) {}
}
