package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ForestryPetManager {

    private final MinecraftNew plugin;
    private final File countsFile;
    private final FileConfiguration countsConfig;

    public ForestryPetManager(MinecraftNew plugin) {
        this.plugin = plugin;

        // Ensure the data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Initialize the counts file
        countsFile = new File(plugin.getDataFolder(), "forestry_counts.yml");
        if (!countsFile.exists()) {
            try {
                countsFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Could not create forestry_counts.yml", e);
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
     * Increment the forestry count for a player and check if a milestone has been reached.
     * If a milestone is reached (10, 100, 1000), create an empty reward section.
     *
     * @param player The player who harvested a tree
     */
    public void incrementForestryCount(Player player) {
        PetRegistry petRegistry = new PetRegistry();
        UUID playerUUID = player.getUniqueId();
        String path = playerUUID.toString() + ".forestry_count";

        int currentCount = countsConfig.getInt(path, 0);
        currentCount += 1;

        countsConfig.set(path, currentCount);
        saveCountsConfig();

        // Check for milestones and create empty reward sections
        PetManager petManager = PetManager.getInstance(plugin);
        if (currentCount == 10){
            player.sendMessage(ChatColor.GREEN + "Congratulations! You've harvested " + currentCount + " trees.");
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            petRegistry.addPetByName(player, "Raccoon");        }
        if(currentCount == 500){
            player.sendMessage(ChatColor.GREEN + "Congratulations! You've harvested " + currentCount + " trees.");
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            petRegistry.addPetByName(player, "Monkey");        }
        if(currentCount == 1500) {
            player.sendMessage(ChatColor.GREEN + "Congratulations! You've harvested " + currentCount + " trees.");
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            petRegistry.addPetByName(player, "Ent");        }
    }

    /**
     * Get the current forestry count for a player.
     *
     * @param player The player
     * @return The number of trees they've harvested
     */
    public int getForestryCount(Player player) {
        UUID playerUUID = player.getUniqueId();
        String path = playerUUID.toString() + ".forestry_count";
        return countsConfig.getInt(path, 0);
    }
}
