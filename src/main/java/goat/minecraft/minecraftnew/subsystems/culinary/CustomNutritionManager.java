package goat.minecraft.minecraftnew.subsystems.culinary;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.AFKDetector;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CustomNutritionManager implements Listener {
    public enum FoodGroup {FRUITS, GRAINS, PROTEINS, VEGGIES, SUGARS}

    private static CustomNutritionManager instance;
    private final JavaPlugin plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;
    private final Map<UUID, Map<FoodGroup, Integer>> nutrition = new HashMap<>();
    private final Map<UUID, Integer> decayCounters = new HashMap<>();

    private CustomNutritionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "nutrition.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startTask();
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CustomNutritionManager(plugin);
        }
    }

    public static CustomNutritionManager getInstance() {
        return instance;
    }

    private void loadData() {
        for (String key : dataConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            Map<FoodGroup, Integer> map = new EnumMap<>(FoodGroup.class);
            for (FoodGroup g : FoodGroup.values()) {
                map.put(g, dataConfig.getInt(key + "." + g.name(), 0));
            }
            nutrition.put(uuid, map);
        }
    }

    private void saveData() {
        for (UUID uuid : nutrition.keySet()) {
            Map<FoodGroup, Integer> map = nutrition.get(uuid);
            for (FoodGroup g : FoodGroup.values()) {
                dataConfig.set(uuid.toString() + "." + g.name(), map.getOrDefault(g,0));
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startTask() {
        new BukkitRunnable() {
            int saveCounter = 0;
            @Override
            public void run() {
                saveCounter++;
                for (UUID uuid : new HashSet<>(nutrition.keySet())) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) continue;
                    if (PotionManager.isActive("Potion of Optimal Eating", player)
                            && PotionEffectPreferences.isEnabled(player, "Potion of Optimal Eating")) {
                        continue;
                    }
                    int counter = decayCounters.getOrDefault(uuid, 0) + 1;
                    if (counter >= 60) { // 1 minutes
                        Map<FoodGroup, Integer> map = nutrition.get(uuid);
                        for (FoodGroup g : FoodGroup.values()) {
                            int v = map.getOrDefault(g,0);
                            if (v > 0) map.put(g, v-1);
                        }
                        counter = 0;
                    }
                    decayCounters.put(uuid, counter);
                    applyBuffs(player);
                }
                if (saveCounter % 60 == 0) {
                    saveData();
                    saveCounter = 0;
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public int getNutrition(Player player, FoodGroup group) {
        return nutrition
                .computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(FoodGroup.class))
                .getOrDefault(group, 0);
    }

    public void addNutrition(Player player, FoodGroup group, int amount) {
        Map<FoodGroup, Integer> map = nutrition.computeIfAbsent(player.getUniqueId(), k -> new EnumMap<>(FoodGroup.class));
        int val = Math.min(100, map.getOrDefault(group, 0) + amount);
        map.put(group, val);
        decayCounters.put(player.getUniqueId(), 0);
        applyBuffs(player);
        player.sendMessage(ChatColor.GREEN + "+" + amount + " " + capitalize(group.name()) + ". " + val + "/100");
    }

    private String capitalize(String s){
        return s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private void applyBuffs(Player player) {
        Map<FoodGroup, Integer> map = nutrition.get(player.getUniqueId());
        if (map == null) return;
        if (map.getOrDefault(FoodGroup.FRUITS,0) >= 50 && player.getSaturation() >= 17) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, 40, 0, true, false, false));
        }
        int pantryLevel = 0;
        SkillTreeManager manager = SkillTreeManager.getInstance();
        if (manager != null) {
            pantryLevel = manager.getTalentLevel(player.getUniqueId(), Skill.CULINARY, Talent.PANTRY_OF_PLENTY);
        }
        double pantryMultiplier = 1 + pantryLevel * 0.20;

        if (map.getOrDefault(FoodGroup.GRAINS,0) >= 50) {
            int duration = (int) Math.round(40 * pantryMultiplier);
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SATURATION, duration, 0, true, false, false));
        }
        if (map.getOrDefault(FoodGroup.PROTEINS,0) >= 50) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.STRENGTH, 40, 0, true, false, false));
        }
        if (map.getOrDefault(FoodGroup.VEGGIES,0) >= 50) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.RESISTANCE, 40, 0, true, false, false));
        }
        if (map.getOrDefault(FoodGroup.SUGARS,0) >= 50) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SPEED, 40, 0, true, false, false));
        }
    }

    public void openGUI(Player player){
        Inventory inv = Bukkit.createInventory(null, 9, "Nutrition");
        inv.setItem(1, createItem(Material.APPLE, ChatColor.DARK_RED + "Fruits", getNutrition(player, FoodGroup.FRUITS)));
        inv.setItem(2, createItem(Material.BREAD, ChatColor.GOLD + "Grains", getNutrition(player, FoodGroup.GRAINS)));
        inv.setItem(3, createItem(Material.COOKED_BEEF, ChatColor.RED + "Proteins", getNutrition(player, FoodGroup.PROTEINS)));
        inv.setItem(4, createItem(Material.CARROT, ChatColor.GREEN + "Veggies", getNutrition(player, FoodGroup.VEGGIES)));
        inv.setItem(5, createItem(Material.SUGAR, ChatColor.LIGHT_PURPLE + "Sugars", getNutrition(player, FoodGroup.SUGARS)));
        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, int amount){
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if(meta != null){
            meta.setDisplayName(name);
            meta.setLore(List.of("" + ChatColor.GRAY + amount + "/100"));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        UUID id = e.getPlayer().getUniqueId();
        nutrition.computeIfAbsent(id, k -> new EnumMap<>(FoodGroup.class));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        saveData();
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        if(e.getView().getTitle().equals("Nutrition")) {
            e.setCancelled(true);
        }
    }
}
