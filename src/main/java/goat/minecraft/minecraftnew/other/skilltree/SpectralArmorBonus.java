package goat.minecraft.minecraftnew.other.skilltree;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Reduces incoming damage from Forest Spirits based on the player's
 * {@link Talent#SPECTRAL_ARMOR} talent level. Detection of spirits is done
 * by parsing the entity's custom name rather than relying on metadata.
 */
public class SpectralArmorBonus implements Listener {

    private final JavaPlugin plugin;

    public SpectralArmorBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!isForestSpirit(event.getDamager())) {
            return;
        }

        int level = SkillTreeManager.getInstance()
                .getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.SPECTRAL_ARMOR);
        if (level <= 0) {
            return;
        }

        double reduction = 1.0 - (0.1 * level);
        event.setDamage(event.getDamage() * reduction);
    }

    private boolean isForestSpirit(Entity entity) {
        if (!(entity instanceof Skeleton skeleton)) {
            return false;
        }
        String name = skeleton.getCustomName();
        if (name == null) {
            return false;
        }
        String stripped = ChatColor.stripColor(name);
        int idx = stripped.indexOf("] ");
        if (idx == -1) {
            return false;
        }
        String spiritName = stripped.substring(idx + 2).toLowerCase();
        return spiritName.endsWith("spirit");
    }
}
