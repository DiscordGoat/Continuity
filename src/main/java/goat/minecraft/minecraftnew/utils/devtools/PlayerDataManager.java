package goat.minecraft.minecraftnew.utils.devtools;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class PlayerDataManager {

    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        init();
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
     */
    public int getMeritPoints(UUID uuid) {
        return dataConfig.getInt(uuid.toString() + ".points", 0);
    }

    /**
     * Set a player's current merit points.
     */
    public void setMeritPoints(UUID uuid, int points) {
        dataConfig.set(uuid.toString() + ".points", points);
        saveConfig();
    }

    /**
     * Check if a player has a certain perk.
     */
    public boolean hasPerk(UUID uuid, String perkTitle) {
        List<String> perks = dataConfig.getStringList(uuid.toString() + ".perks");
        return perks.contains(perkTitle);
    }

    /**
     * Add a perk to the player's list of purchased perks.
     */
    public void addPerk(UUID uuid, String perkTitle) {
        List<String> perks = dataConfig.getStringList(uuid.toString() + ".perks");
        if (!perks.contains(perkTitle)) {
            perks.add(perkTitle);
        }
        dataConfig.set(uuid.toString() + ".perks", perks);
        saveConfig();
    }
}
