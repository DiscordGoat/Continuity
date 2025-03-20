package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Speech {

    private JavaPlugin plugin;
    private Map<UUID, ArmorStand> speechArmorStands = new HashMap<>();

    public Speech(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void createText(Location loc, String text, double duration) {
        ArmorStand armorStand = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(text);
        armorStand.setCustomNameVisible(true);

        // Store the armor stand for removal later
        speechArmorStands.put(armorStand.getUniqueId(), armorStand);

        // Schedule removal of the armor stand after the specified duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            armorStand.remove();
            speechArmorStands.remove(armorStand.getUniqueId());
        }, (long) duration * 20 * 60); // Convert duration from minutes to ticks
    }

    // Call this method on plugin disable to remove all speech armor stands
    public void removeAllSpeech() {
        speechArmorStands.values().forEach(ArmorStand::remove);
        speechArmorStands.clear();
    }
}