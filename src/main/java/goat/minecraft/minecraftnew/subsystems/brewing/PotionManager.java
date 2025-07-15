package goat.minecraft.minecraftnew.subsystems.brewing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.SkinManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PotionManager {

    // Map: Player UUID -> (Effect Name -> Remaining Duration in seconds)
    private static Map<UUID, Map<String, Integer>> activeEffects = new HashMap<>();
    private static File effectsFile;
    private static JavaPlugin plugin;

    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        effectsFile = new File(plugin.getDataFolder(), "activeEffects.yml");
        loadEffects();
        startTask();
    }

    /**
     * Adds (or refreshes) a custom potion effect for a player.
     *
     * @param name     the name of the potion effect
     * @param player   the player receiving the effect
     * @param duration duration in seconds
     */
    public static void addCustomPotionEffect(String name, Player player, int duration) {
        UUID uuid = player.getUniqueId();
        PlayerMeritManager playerMeritManager = PlayerMeritManager.getInstance(MinecraftNew.getInstance());

        if(playerMeritManager.hasPerk(player.getUniqueId(), "Strong Digestion")){
            duration = duration * 2;
            Bukkit.getLogger().info("Doubled Effect Duration from Strong Digestion Perk!");
        }
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet pet = petManager.getActivePet(player);
        if(pet != null && pet.hasPerk(PetManager.PetPerk.EXPERIMENTATION)){
            duration += 3 * pet.getLevel();
        }
        if(SkillTreeManager.getInstance().hasTalent(player, Talent.REDSTONE_ONE)){
            int redstoneBuff = (4*SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.REDSTONE_ONE));
            Bukkit.getLogger().info("Potion Duration extended by " + redstoneBuff + "s from " + duration + "s");
            duration += redstoneBuff;
        }
        if(SkillTreeManager.getInstance().hasTalent(player, Talent.REDSTONE_TWO)){
            int redstoneBuff = (4*SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.REDSTONE_TWO));
            Bukkit.getLogger().info("Potion Duration extended by " + redstoneBuff + "s from " + duration + "s");
            duration += redstoneBuff;
        }
        if (SkillTreeManager.getInstance().hasTalent(player, Talent.REJUVENATION)) {
            int level = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.REJUVENATION);
            int hpBoostduration = level * 50 * 20;
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, hpBoostduration, 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 255));
        }
        Map<String, Integer> playerEffects = activeEffects.getOrDefault(uuid, new HashMap<>());
        // If the effect is already active, add the new duration to the current duration
        int newDuration = playerEffects.getOrDefault(name, 0) + duration;
        playerEffects.put(name, newDuration);
        activeEffects.put(uuid, playerEffects);
    }


    /**
     * Checks if a given effect is active for the player.
     *
     * @param name   the effect name
     * @param player the player to check
     * @return true if active, false otherwise
     */
    public static boolean isActive(String name, Player player) {
        UUID uuid = player.getUniqueId();
        if (activeEffects.containsKey(uuid)) {
            return activeEffects.get(uuid).getOrDefault(name, 0) > 0;
        }
        return false;
    }

    /**
     * Returns a copy of active effects for the given player.
     */
    public static Map<String, Integer> getActiveEffects(Player player) {
        UUID uuid = player.getUniqueId();
        if (activeEffects.containsKey(uuid)) {
            return new HashMap<>(activeEffects.get(uuid));
        }
        return new HashMap<>();
    }

    /**
     * Starts a repeating task that:
     * - Decrements all active effect timers every second, but only for online players.
     * - Saves the database to file every 5 seconds.
     */
    private static void startTask() {
        new BukkitRunnable() {
            int counter = 0;
            @Override
            public void run() {
                // Only update potion timers for players who are online.
                for (UUID uuid : activeEffects.keySet()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p == null || !p.isOnline()) {
                        continue; // Skip offline players
                    }
                    Map<String, Integer> effects = activeEffects.get(uuid);
                    // Copy to avoid concurrent modification
                    Map<String, Integer> updatedEffects = new HashMap<>(effects);
                    for (String effect : effects.keySet()) {
                        int newDuration = effects.get(effect) - 1;
                        if (newDuration <= 0) {
                            updatedEffects.remove(effect);
                            p.sendMessage(ChatColor.GRAY + effect + " has expired.");
                        } else {
                            updatedEffects.put(effect, newDuration);
                        }
                    }
                    activeEffects.put(uuid, updatedEffects);
                }
                counter++;
                if (counter % 5 == 0) {
                    saveEffects();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // runs every second (20 ticks)
    }

    /**
     * Saves the active effects to a YAML file.
     */
    private static void saveEffects() {
        YamlConfiguration config = new YamlConfiguration();
        for (UUID uuid : activeEffects.keySet()) {
            for (String effect : activeEffects.get(uuid).keySet()) {
                int duration = activeEffects.get(uuid).get(effect);
                config.set(uuid.toString() + "." + effect, duration);
            }
        }
        try {
            config.save(effectsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the active effects from a YAML file.
     */
    private static void loadEffects() {
        if (!effectsFile.exists()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(effectsFile);
        activeEffects.clear();
        for (String key : config.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            Map<String, Integer> playerEffects = new HashMap<>();
            ConfigurationSection section = config.getConfigurationSection(key);
            for (String effect : section.getKeys(false)) {
                int duration = section.getInt(effect);
                playerEffects.put(effect, duration);
            }
            activeEffects.put(uuid, playerEffects);
        }
    }
}
