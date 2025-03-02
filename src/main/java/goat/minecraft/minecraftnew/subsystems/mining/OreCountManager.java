package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class OreCountManager {

    private final MinecraftNew plugin;
    private final File countsFile;
    private final FileConfiguration countsConfig;

    public OreCountManager(MinecraftNew plugin) {
        this.plugin = plugin;

        // Ensure the data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Initialize the counts file
        countsFile = new File(plugin.getDataFolder(), "ore_counts.yml");
        if (!countsFile.exists()) {
            try {
                countsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create ore_counts.yml", e);
            }
        }

        countsConfig = YamlConfiguration.loadConfiguration(countsFile);
        saveCountsConfig(); // Ensure initial saving
    }

    private void saveCountsConfig() {
        try {
            countsConfig.save(countsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Increment the ore count for a player and check if a milestone has been reached.
     * If a milestone is reached (10, 100, 1000), create an empty reward section.
     *
     * @param player The player who mined an ore
     */
    public void incrementOreCount(Player player) {
        UUID playerUUID = player.getUniqueId();
        String path = playerUUID.toString() + ".ore_count";

        int currentCount = countsConfig.getInt(path, 0);
        currentCount += 1;

        countsConfig.set(path, currentCount);
        saveCountsConfig();

        // Check for milestones and create empty reward sections
        PetManager petManager = PetManager.getInstance(plugin);
        PetRegistry petRegistry = new PetRegistry();
        if (currentCount == 10){
            player.sendMessage(ChatColor.GOLD + "Congratulations! You've mined " + currentCount + " ores.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        }
        if(currentCount == 100){
            player.sendMessage(ChatColor.GOLD + "Congratulations! You've mined " + currentCount + " ores.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            petRegistry.addPetByName(player, "Dwarf");
        }
        if(currentCount == 1000) {
            player.sendMessage(ChatColor.GOLD + "Congratulations! You've mined " + currentCount + " ores.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            petRegistry.addPetByName(player, "Warden");
        }
    }

    /**
     * Get the current ore count for a player.
     *
     * @param player The player
     * @return The number of ores they've mined
     */
    public int getOreCount(Player player) {
        UUID playerUUID = player.getUniqueId();
        String path = playerUUID.toString() + ".ore_count";
        return countsConfig.getInt(path, 0);
    }
}
