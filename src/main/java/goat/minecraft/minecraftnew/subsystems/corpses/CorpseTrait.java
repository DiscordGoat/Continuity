package goat.minecraft.minecraftnew.subsystems.corpses;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class CorpseTrait extends Trait {
    private final JavaPlugin plugin;
    private final Corpse corpse;
    private int taskId = -1;
    private long lastShot = 0L;

    public CorpseTrait(JavaPlugin plugin, Corpse corpse) {
        super("corpse_trait");
        this.plugin = plugin;
        this.corpse = corpse;
    }

    @Override
    public void onAttach() {
        npc.setProtected(false);
        start();
    }

    private Player findNearestPlayer(Location loc) {
        Player nearest = null;
        double dist = Double.MAX_VALUE;
        for (Player p : Bukkit.getOnlinePlayers()) {
            double d = p.getLocation().distanceSquared(loc);
            if (d < dist) {
                dist = d;
                nearest = p;
            }
        }
        return nearest;
    }

    private double getDamage() {
        double base = 2.0;
        double perLevel = 0.06;
        return base * (1.0 + corpse.getLevel() * perLevel);
    }

    private void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (!npc.isSpawned()) return;
            Player target = findNearestPlayer(npc.getEntity().getLocation());
            if (target == null) return;

            npc.getNavigator().setTarget(target, true);
            Entity ent = npc.getEntity();
            if (!(ent instanceof LivingEntity living)) return;

            if (corpse.isRanged()) {
                if (System.currentTimeMillis() - lastShot > 2000) {
                    Arrow arrow = living.launchProjectile(Arrow.class);
                    arrow.setDamage(getDamage());
                    lastShot = System.currentTimeMillis();
                }
            } else {
                if (ent.getLocation().distanceSquared(target.getLocation()) <= 2.5 * 2.5) {
                    target.damage(getDamage(), ent);
                }
            }
        }, 0L, 20L);
    }

    @Override
    public void onRemove() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public void load(DataKey key) {}

    @Override
    public void save(DataKey key) {}
}
