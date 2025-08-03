package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class CropCountManager {
    private static CropCountManager instance;
    private final JavaPlugin plugin;
    private final File file;
    private YamlConfiguration config;
    private final Random random = new Random();

    private CropCountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "cropCount.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.config = YamlConfiguration.loadConfiguration(file);
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
            case WHEAT, WHEAT_SEEDS: return "wheat";
            case CARROTS: return "carrots";
            case POTATOES: return "potatoes";
            case BEETROOTS: return "beetroots";
            case PUMPKIN: return "pumpkins";
            case MELON: return "melons";
            case COCOA: return "cocoa";
            default: return null;
        }
    }

    public boolean increment(Player player, Material crop) {
        String k = key(crop);
        if (k == null) return false;
        String uuid = player.getUniqueId().toString();

        int incrementValue = 1;
        PetManager.Pet activePet = PetManager.getInstance(plugin).getActivePet(player);
        if (activePet != null) {
            int level = activePet.getLevel();
            if (crop == Material.WHEAT && activePet.hasPerk(PetManager.PetPerk.HEADLESS_HORSEMAN)) {
                if (random.nextInt(100) < level) incrementValue++;
            } else if (crop == Material.CARROTS && activePet.hasPerk(PetManager.PetPerk.ORANGE)) {
                if (random.nextInt(100) < level) incrementValue++;
            } else if (crop == Material.BEETROOTS && activePet.hasPerk(PetManager.PetPerk.BEETS_ME)) {
                if (random.nextInt(100) < level) incrementValue++;
            } else if (crop == Material.POTATOES && activePet.hasPerk(PetManager.PetPerk.BLOODTHIRSTY)) {
                if (random.nextInt(100) < level) incrementValue++;
            }
        }

        int previous = config.getInt(uuid + "." + k, 0);
        int c = previous + incrementValue;
        config.set(uuid + "." + k, c);
        int totalPrev = config.getInt(uuid + ".cropsHarvested", 0);
        int total = totalPrev + incrementValue;
        config.set(uuid + ".cropsHarvested", total);
        save();

        checkPetThresholds(player, total);
        int reduction = 0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_I);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_II);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_III);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_IV);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_V);
        }
        int requirement = (int) Math.ceil(500 * (1 - reduction / 100.0));
        if (requirement < 1) requirement = 1;
        boolean trigger = false;
        for (int i = previous + 1; i <= c; i++) {
            if (i % requirement == 0) {
                trigger = true;
                break;
            }
        }
        return trigger;
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
