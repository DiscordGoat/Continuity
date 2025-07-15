package goat.minecraft.minecraftnew.other.skilltree;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import goat.minecraft.minecraftnew.other.skilltree.TalentRegistry;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SkillTreeManager implements Listener {
    private static SkillTreeManager instance;
    private final JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<Skill, List<Talent>> skillTalents = new HashMap<>();

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new SkillTreeManager(plugin);
            Bukkit.getPluginManager().registerEvents(instance, plugin);
        }
    }

    public static SkillTreeManager getInstance() {
        return instance;
    }

    private SkillTreeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initStorage();
    }

    private void initStorage() {
        dataFile = new File(plugin.getDataFolder(), "skill_talents.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveConfig() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =============================================================
    // Public API
    // =============================================================

    public void openSkillTree(Player player, Skill skill) {
        openSkillTree(player, skill, 1);
    }

    private void openSkillTree(Player player, Skill skill, int page) {
        List<Talent> talents = TalentRegistry.getTalents(skill);
        int totalPages = (int) Math.ceil(talents.size() / 40.0);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_GREEN + skill.getDisplayName() + " Skill Tree: Page " + page + "/" + totalPages);

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.setDisplayName(ChatColor.BLACK + "");
        pane.setItemMeta(pm);

        for (int i = 0; i < 9; i++) {
            gui.setItem(i, pane.clone());
        }
        for (int row = 0; row < 5; row++) {
            gui.setItem(9 + row * 9, pane.clone());
        }

        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta meta = prev.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prev.setItemMeta(meta);
            gui.setItem(0, prev);
        }

        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta meta = next.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(meta);
            gui.setItem(8, next);
        }

        ItemStack points = new ItemStack(Material.DIAMOND);
        ItemMeta dmeta = points.getItemMeta();
        dmeta.setDisplayName(ChatColor.AQUA + "Talent Points: " + getAvailableTalentPoints(player, skill));
        points.setItemMeta(dmeta);
        gui.setItem(4, points);

        int startIndex = (page - 1) * 40;
        int endIndex = Math.min(talents.size(), startIndex + 40);
        int slotIndex = 9;
        for (int i = startIndex; i < endIndex; i++) {
            Talent talent = talents.get(i);
            if (slotIndex % 9 == 0) slotIndex++;
            if (slotIndex >= 54) break;

            int currentLevel = getTalentLevel(player.getUniqueId(), skill, talent);
            ItemStack item = new ItemStack(talent.getIcon());
            ItemMeta im = item.getItemMeta();
            im.setDisplayName(talent.getRarity().getColor() + talent.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + talent.getDescription());
            lore.add(ChatColor.GRAY + getDynamicTechnicalDescription(talent, currentLevel));
            lore.add(ChatColor.YELLOW + "Level: " + currentLevel + "/" + talent.getMaxLevel());
            lore.add(ChatColor.RED + "Requires " + skill.getDisplayName() + " " + talent.getLevelRequirement());
            im.setLore(lore);
            if (currentLevel > 0) {
                im.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(im);
            gui.setItem(slotIndex, item);
            slotIndex++;
        }

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, pane.clone());
            }
        }

        player.openInventory(gui);
    }

    public int getAvailableTalentPoints(Player player, Skill skill) {
        XPManager xp = MinecraftNew.getInstance().getXPManager();
        int levelPoints = xp.getPlayerLevel(player, skill.getDisplayName());
        int extra = dataConfig.getInt(player.getUniqueId() + "." + skill + ".extra_points", 0);
        int spent = TalentRegistry.getTalents(skill).stream()
                .mapToInt(t -> getTalentLevel(player.getUniqueId(), skill, t)).sum();
        return levelPoints + extra - spent;
    }

    public int getTalentLevel(UUID uuid, Skill skill, Talent talent) {
        return dataConfig.getInt(uuid + "." + skill + ".talents." + talent.getName(), 0);
    }

    public boolean hasTalent(Player player, Talent talent) {
        Skill skill = TalentRegistry.getSkillForTalent(talent);
        if (skill == null) {
            return false;
        }
        return getTalentLevel(player.getUniqueId(), skill, talent) > 0;
    }

    private void setTalentLevel(UUID uuid, Skill skill, Talent talent, int level) {
        dataConfig.set(uuid + "." + skill + ".talents." + talent.getName(), level);
        saveConfig();
    }

    public void addExtraTalentPoints(UUID uuid, Skill skill, int amount) {
        int current = dataConfig.getInt(uuid + "." + skill + ".extra_points", 0);
        dataConfig.set(uuid + "." + skill + ".extra_points", current + amount);
        saveConfig();
    }

    private String getDynamicTechnicalDescription(Talent talent, int level) {
        switch (talent) {
            case TRIPLE_BATCH:
                double chance = level * 5;
                return ChatColor.YELLOW + "+" + chance + "% " + ChatColor.GRAY + "Chance to brew 3 Potions.";
            case OPTIMAL_CONFIGURATION:
                int reduction = level * 4;
                return ChatColor.YELLOW + "-" + reduction + "s " + ChatColor.GOLD + "Brew Time.";
            case REDSTONE_ONE:
            case REDSTONE_TWO:
                int seconds = level * 4;
                return ChatColor.YELLOW + "+" + seconds + "s " + ChatColor.LIGHT_PURPLE + "Potion Duration, "
                        + ChatColor.GOLD + "+" + seconds + "s " + ChatColor.GOLD + "Brew Time.";
            case RECURVE_MASTERY:
                int recurveDuration = level * 50;
                return ChatColor.YELLOW + "+" + recurveDuration + "s " + ChatColor.LIGHT_PURPLE + "Recurve Duration";
            case REJUVENATION:
                int bonusTime = level * 50;
                return ChatColor.YELLOW + "+" + bonusTime + "s " + ChatColor.GREEN + "Bonus Health" + ChatColor.GRAY + " and " + ChatColor.GREEN + "Potion Surge";
                return ChatColor.YELLOW + "+" + recurveDuration + "s " + ChatColor.LIGHT_PURPLE + "Recurve Duration, "
                        + ChatColor.RED + "+5% Arrow Damage";
            case STRENGTH_MASTERY:
                int strengthDuration = level * 50;
                return ChatColor.YELLOW + "+" + strengthDuration + "s " + ChatColor.LIGHT_PURPLE + "Strength Duration, "
                        + ChatColor.RED + "+5% Damage";
            default:
                return talent.getTechnicalDescription();
        }
    }

    // =============================================================
    // Event Handling
    // =============================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String raw = ChatColor.stripColor(event.getView().getTitle());
        if (!raw.endsWith("Skill Tree") && !raw.contains("Skill Tree: Page")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        Skill skill = Skill.BREWING; // only one supported for now
        int page = 1;
        if (raw.contains("Page")) {
            try {
                String pagePart = raw.substring(raw.indexOf("Page") + 5);
                page = Integer.parseInt(pagePart.split("/")[0]);
            } catch (Exception ignored) {}
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name.equalsIgnoreCase("Next Page")) {
            openSkillTree(player, skill, page + 1);
            return;
        }
        if (name.equalsIgnoreCase("Previous Page")) {
            openSkillTree(player, skill, page - 1);
            return;
        }
        Talent talent = TalentRegistry.getTalents(skill).stream()
                .filter(t -> t.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
        if (talent == null) return;

        int playerLevel = MinecraftNew.getInstance().getXPManager().getPlayerLevel(player, skill.getDisplayName());
        int currentLevel = getTalentLevel(player.getUniqueId(), skill, talent);
        if (playerLevel < talent.getLevelRequirement()) {
            player.sendMessage(ChatColor.RED + "Requires " + skill.getDisplayName() + " " + talent.getLevelRequirement());
            return;
        }
        if (currentLevel >= talent.getMaxLevel()) {
            player.sendMessage(ChatColor.RED + "Talent already maxed.");
            return;
        }
        if (getAvailableTalentPoints(player, skill) <= 0) {
            player.sendMessage(ChatColor.RED + "No talent points available.");
            return;
        }
        setTalentLevel(player.getUniqueId(), skill, talent, currentLevel + 1);
        player.sendMessage(ChatColor.GREEN + "Upgraded " + talent.getName() + " to " + (currentLevel + 1));
        openSkillTree(player, skill, page);
    }

    @EventHandler
    public void onPotionConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        Material type = item.getType();
        if (type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION) {
            Player player = event.getPlayer();
            if (hasTalent(player, Talent.REJUVENATION)) {
                int level = getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.REJUVENATION);
                int duration = level * 50 * 20;
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, duration, 4));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 255));
            }
        }
    }
}
