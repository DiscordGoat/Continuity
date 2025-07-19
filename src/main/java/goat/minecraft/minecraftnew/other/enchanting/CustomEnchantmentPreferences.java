package goat.minecraft.minecraftnew.other.enchanting;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player preferences for enabling or disabling custom enchantments.
 */
public class CustomEnchantmentPreferences implements Listener {

    private static final Map<UUID, Map<String, Boolean>> prefs = new HashMap<>();
    private static File prefFile;
    private static FileConfiguration prefConfig;

    public static void init(JavaPlugin plugin) {
        prefFile = new File(plugin.getDataFolder(), "customenchantmentpreferences.yml");
        if (!prefFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                prefFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        prefConfig = YamlConfiguration.loadConfiguration(prefFile);
        for (String key : prefConfig.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                ConfigurationSection sec = prefConfig.getConfigurationSection(key);
                if (sec != null) {
                    Map<String, Boolean> map = new HashMap<>();
                    for (String ench : sec.getKeys(false)) {
                        map.put(ench, sec.getBoolean(ench, true));
                    }
                    prefs.put(id, map);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public static boolean isEnabled(Player player, String enchant) {
        Map<String, Boolean> map = prefs.get(player.getUniqueId());
        if (map == null) return true;
        return map.getOrDefault(normalize(enchant), true);
    }

    public static void toggle(Player player, String enchant) {
        Map<String, Boolean> map = prefs.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        String key = normalize(enchant);
        boolean newVal = !map.getOrDefault(key, true);
        map.put(key, newVal);
        savePlayer(player.getUniqueId());
    }

    private static void savePlayer(UUID id) {
        Map<String, Boolean> map = prefs.get(id);
        if (map == null) return;
        prefConfig.set(id.toString(), null);
        for (Map.Entry<String, Boolean> e : map.entrySet()) {
            prefConfig.set(id.toString() + "." + e.getKey(), e.getValue());
        }
        try {
            prefConfig.save(prefFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveAll() {
        for (UUID id : prefs.keySet()) {
            savePlayer(id);
        }
    }

    private static String normalize(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        prefs.computeIfAbsent(event.getPlayer().getUniqueId(), k -> new HashMap<>());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        savePlayer(event.getPlayer().getUniqueId());
    }
}
