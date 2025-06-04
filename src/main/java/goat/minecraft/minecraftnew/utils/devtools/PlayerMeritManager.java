package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class PlayerMeritManager {

    private static PlayerMeritManager instance;

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    // Private constructor to prevent external instantiation.
    private PlayerMeritManager(JavaPlugin plugin) {
        this.plugin = plugin;
        init();
    }

    /**
     * Returns the singleton instance of PlayerDataManager.
     * If it does not exist yet, it will be created using the provided plugin.
     *
     * @param plugin The JavaPlugin instance.
     * @return The singleton instance.
     */
    public static PlayerMeritManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PlayerMeritManager(plugin);
        }
        return instance;
    }

    private void init() {
        dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a player's current merit points.
     *
     * @param uuid The player's UUID.
     * @return The number of merit points.
     */
    public int getMeritPoints(UUID uuid) {
        return dataConfig.getInt(uuid.toString() + ".points", 0);
    }

    /**
     * Set a player's current merit points.
     *
     * @param uuid   The player's UUID.
     * @param points The points to set.
     */
    public void setMeritPoints(UUID uuid, int points) {
        dataConfig.set(uuid.toString() + ".points", points);
        saveConfig();
    }

    /**
     * Check if a player has a certain perk.
     *
     * @param uuid      The player's UUID.
     * @param perkTitle The perk name.
     * @return True if the perk is present.
     */
    public boolean hasPerk(UUID uuid, String perkTitle) {
        List<String> perks = dataConfig.getStringList(uuid.toString() + ".perks");
        return perks.contains(perkTitle);
    }

    /**
     * Add a perk to the player's list of purchased perks.
     *
     * @param uuid      The player's UUID.
     * @param perkTitle The perk name to add.
     */
    public void addPerk(UUID uuid, String perkTitle) {
        List<String> perks = dataConfig.getStringList(uuid.toString() + ".perks");
        if (!perks.contains(perkTitle)) {
            perks.add(perkTitle);
        }
        dataConfig.set(uuid.toString() + ".perks", perks);
        saveConfig();
    }

    /**
     * Remove a perk from the player's list of purchased perks.
     *
     * @param uuid      The player's UUID.
     * @param perkTitle The perk name to remove.
     */
    public void removePerk(UUID uuid, String perkTitle) {
        List<String> perks = dataConfig.getStringList(uuid.toString() + ".perks");
        if (perks.contains(perkTitle)) {
            perks.remove(perkTitle);
            dataConfig.set(uuid.toString() + ".perks", perks);
            saveConfig();
        }
    }
}
