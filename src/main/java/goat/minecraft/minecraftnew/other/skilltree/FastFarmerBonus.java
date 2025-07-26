package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FastFarmerBonus implements Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Float> baseSpeed = new HashMap<>();

    public FastFarmerBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                checkPlayer(p);
            }
        }, 1L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(e.getPlayer()), 1L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        checkPlayer(e.getPlayer());
    }

    private void checkPlayer(Player player) {
        int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FAST_FARMER);
        UUID id = player.getUniqueId();
        boolean onSoil = player.getLocation().getBlock().getType() == Material.FARMLAND;
        if (level > 0 && onSoil) {
            baseSpeed.putIfAbsent(id, player.getWalkSpeed());
            float newSpeed = baseSpeed.get(id) * (1.0f + 0.20f * level);
            player.setWalkSpeed(Math.min(newSpeed, 1.0f));
        } else if (baseSpeed.containsKey(id)) {
            player.setWalkSpeed(baseSpeed.get(id));
            baseSpeed.remove(id);
        }
    }

    public void removeAll() {
        for (Map.Entry<UUID, Float> en : baseSpeed.entrySet()) {
            Player p = Bukkit.getPlayer(en.getKey());
            if (p != null) p.setWalkSpeed(en.getValue());
        }
        baseSpeed.clear();
    }
}
