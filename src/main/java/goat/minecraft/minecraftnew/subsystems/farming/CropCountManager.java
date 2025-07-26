package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Sound;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class CropCountManager {
    private static CropCountManager instance;
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final XPManager xpManager;

    private CropCountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "cropCount.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        this.xpManager = new XPManager(plugin);
    }

    public static CropCountManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CropCountManager(plugin);
        }
        return instance;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String key(Material mat) {
        switch (mat) {
            case WHEAT_SEEDS: return "wheat";
            case CARROTS: return "carrots";
            case POTATOES: return "potatoes";
            case BEETROOTS: return "beetroots";
            case PUMPKIN: return "pumpkins";
            case MELON: return "melons";
            case COCOA: return "cocoa";
            default: return null;
        }
    }

    public void increment(Player player, Material crop) {
        String k = key(crop);
        if (k == null) return;
        String uuid = player.getUniqueId().toString();
        int c = config.getInt(uuid + "." + k, 0) + 1;
        config.set(uuid + "." + k, c);
        int total = config.getInt(uuid + ".cropsHarvested", 0) + 1;
        config.set(uuid + ".cropsHarvested", total);
        save();
        handleRewards(player, crop, c, total);
    }

    private void handleRewards(Player player, Material crop, int cropCount, int total) {
        checkPetThresholds(player, total);
        if (cropCount % 500 == 0) {
            switch (crop) {
                case WHEAT, WHEAT_SEEDS:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(), ItemRegistry.getWheatSeeder());
                    break;
                case CARROTS:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(),ItemRegistry.getCarrotSeeder());
                    break;
                case POTATOES:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(),ItemRegistry.getPotatoSeeder());
                    break;
                case BEETROOTS:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(),ItemRegistry.getBeetrootSeeder());
                    break;
                case PUMPKIN:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(),ItemRegistry.getJackOLantern());
                    break;
                case MELON:
                    Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(),ItemRegistry.getWatermelon());
                    break;
                default:
                    break;
            }
            xpManager.addXP(player, "Farming", 100);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    public void checkPetThresholds(Player player) {
        int total = config.getInt(player.getUniqueId().toString() + ".cropsHarvested", 0);
        checkPetThresholds(player, total);
    }

    private void checkPetThresholds(Player player, int total) {
        String uuid = player.getUniqueId().toString();
        if (total >= 4860 && !config.getBoolean(uuid + ".legendary", false)) {
            grantPet(player, PetManager.Rarity.LEGENDARY);
            config.set(uuid + ".legendary", true);
        } else if (total >= 1620 && !config.getBoolean(uuid + ".epic", false)) {
            grantPet(player, PetManager.Rarity.EPIC);
            config.set(uuid + ".epic", true);
        } else if (total >= 540 && !config.getBoolean(uuid + ".rare", false)) {
            grantPet(player, PetManager.Rarity.RARE);
            config.set(uuid + ".rare", true);
        } else if (total >= 180 && !config.getBoolean(uuid + ".uncommon", false)) {
            grantPet(player, PetManager.Rarity.UNCOMMON);
            config.set(uuid + ".uncommon", true);
        } else if (total >= 60 && !config.getBoolean(uuid + ".common", false)) {
            grantPet(player, PetManager.Rarity.COMMON);
            config.set(uuid + ".common", true);
        } else {
            return;
        }
        save();
    }

    private void grantPet(Player player, PetManager.Rarity rarity) {
        PetRegistry petRegistry = new PetRegistry();
        switch (rarity) {
            case COMMON:
                petRegistry.addPetByName(player, "Squirrel");
                break;
            case UNCOMMON:
                petRegistry.addPetByName(player, "Sheep");
                break;
            case RARE:
                petRegistry.addPetByName(player, "Cow");
                break;
            case EPIC:
                petRegistry.addPetByName(player, "Mooshroom");
                break;
            case LEGENDARY:
                petRegistry.addPetByName(player, "Pig");
                break;
            default:
                break;
        }
    }
}
