package goat.minecraft.minecraftnew.other.additionalfunctionality;

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
 * Stores per-player preference for displaying environment sidebar values
 * as numeric values only or with progress bars. Preferences are persisted
 * to a YAML file.
 */
public class EnvironmentSidebarPreferences implements Listener {
    private static final Map<UUID, Boolean> prefs = new HashMap<>();
    private static File prefFile;
    private static FileConfiguration prefConfig;

    /** Initialise the preference manager and load saved data. */
    public static void init(JavaPlugin plugin) {
        prefFile = new File(plugin.getDataFolder(), "togglebars.yml");
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
                boolean enabled = prefConfig.getBoolean(key, false);
                prefs.put(id, enabled);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    /**
     * Check if environment bars are enabled for the given player.
     */
    public static boolean isEnabled(Player player) {
        return prefs.getOrDefault(player.getUniqueId(), false);
    }

    /**
     * Toggle bars for the player and save immediately.
     *
     * @return the new state after toggling
     */
    public static boolean toggle(Player player) {
        boolean newVal = !prefs.getOrDefault(player.getUniqueId(), false);
        prefs.put(player.getUniqueId(), newVal);
        savePlayer(player.getUniqueId());
        return newVal;
    }

    private static void savePlayer(UUID id) {
        prefConfig.set(id.toString(), prefs.getOrDefault(id, false));
        try {
            prefConfig.save(prefFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Save all preferences to disk. */
    public static void saveAll() {
        for (UUID id : prefs.keySet()) {
            savePlayer(id);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        prefs.computeIfAbsent(event.getPlayer().getUniqueId(),
                k -> prefConfig.getBoolean(event.getPlayer().getUniqueId().toString(), false));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        savePlayer(event.getPlayer().getUniqueId());
    }
}
