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
    private volatile boolean dirty = false;
    private org.bukkit.scheduler.BukkitTask flushTask;

    private CropCountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "cropCount.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        // Periodic async flush to disk to avoid synchronous IO on main thread
        flushTask = org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (dirty) {
                save();
            }
        }, 600L, 600L); // every 30 seconds
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
            dirty = false;
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
                if (random.nextInt(100) < level*0.25) incrementValue++;
            } else if (crop == Material.CARROTS && activePet.hasPerk(PetManager.PetPerk.ORANGE)) {
                if (random.nextInt(100) < level*0.25) incrementValue++;
            } else if (crop == Material.BEETROOTS && activePet.hasPerk(PetManager.PetPerk.BEETS_ME)) {
                if (random.nextInt(100) < level*0.25) incrementValue++;
            } else if (crop == Material.POTATOES && activePet.hasPerk(PetManager.PetPerk.BLOODTHIRSTY)) {
                if (random.nextInt(100) < level*0.25) incrementValue++;
            }
        }

        int previous = config.getInt(uuid + "." + k, 0);
        int c = previous + incrementValue;
        config.set(uuid + "." + k, c);
        int totalPrev = config.getInt(uuid + ".cropsHarvested", 0);
        int total = totalPrev + incrementValue;
        config.set(uuid + ".cropsHarvested", total);
        dirty = true;

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
        int requirement = (int) Math.ceil(1000 * (1 - reduction / 100.0));
        if (requirement < 1) requirement = 1;
        // O(1) threshold crossing: if quotient increases, we crossed a multiple of requirement
        int prevBuckets = previous / requirement;
        int newBuckets = c / requirement;
        return newBuckets > prevBuckets;
    }

    /**
     * Bulk increment a crop count and total, returning whether at least one threshold was crossed
     * by applying this increment in one step.
     */
    public boolean bulkIncrement(Player player, Material crop, int count) {
        if (count <= 0) return false;
        String k = key(crop);
        if (k == null) return false;
        String uuid = player.getUniqueId().toString();

        int previous = config.getInt(uuid + "." + k, 0);
        int totalPrev = config.getInt(uuid + ".cropsHarvested", 0);

        int reduction = 0;
        if (SkillTreeManager.getInstance() != null) {
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_I);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_II);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_III);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_IV);
            reduction += mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.REAPER_V);
        }
        int requirement = (int) Math.ceil(1000 * (1 - reduction / 100.0));
        if (requirement < 1) requirement = 1;

        int prevBuckets = previous / requirement;
        int newBuckets = (previous + count) / requirement;

        // Apply updates
        config.set(uuid + "." + k, previous + count);
        config.set(uuid + ".cropsHarvested", totalPrev + count);
        dirty = true;

        // Check pet thresholds using updated total
        checkPetThresholds(player, totalPrev + count);

        return newBuckets > prevBuckets;
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
            dirty = true;
        } else if (total >= 1620 && !config.getBoolean(uuid + ".epic", false)) {
            grantPet(player, PetManager.Rarity.EPIC);
            config.set(uuid + ".epic", true);
            dirty = true;
        } else if (total >= 540 && !config.getBoolean(uuid + ".rare", false)) {
            grantPet(player, PetManager.Rarity.RARE);
            config.set(uuid + ".rare", true);
            dirty = true;
        } else if (total >= 180 && !config.getBoolean(uuid + ".uncommon", false)) {
            grantPet(player, PetManager.Rarity.UNCOMMON);
            config.set(uuid + ".uncommon", true);
            dirty = true;
        } else if (total >= 60 && !config.getBoolean(uuid + ".common", false)) {
            grantPet(player, PetManager.Rarity.COMMON);
            config.set(uuid + ".common", true);
            dirty = true;
        } else {
            return;
        }
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
