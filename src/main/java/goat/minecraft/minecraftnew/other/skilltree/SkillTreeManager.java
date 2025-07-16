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
        page = Math.max(1, Math.min(page, totalPages));

        Inventory gui = Bukkit.createInventory(null, 54,
                ChatColor.DARK_GREEN + skill.getDisplayName() +
                        " Skill Tree: Page " + page + "/" + totalPages);

        // prepare one pane stack
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta pm = pane.getItemMeta();
        pm.setDisplayName(" ");
        pane.setItemMeta(pm);

        // 1) Top row (slots 0–8)
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, pane);
        }

        // 2) Bottom row (slots 45–53)
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, pane);
        }

        // 3) Left & right columns for rows 1–4
        for (int row = 1; row <= 4; row++) {
            int left  = row * 9;
            int right = row * 9 + 8;
            gui.setItem(left,  pane);
            gui.setItem(right, pane);
        }

        // — now your arrows, diamond counter, etc. go into the frame —

        if (page > 1) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta m = prev.getItemMeta();
            m.setDisplayName(ChatColor.YELLOW + "Previous Page");
            prev.setItemMeta(m);
            gui.setItem(0, prev);
        }
        if (page < totalPages) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta m = next.getItemMeta();
            m.setDisplayName(ChatColor.YELLOW + "Next Page");
            next.setItemMeta(m);
            gui.setItem(8, next);
        }
        ItemStack points = new ItemStack(Material.DIAMOND);
        ItemMeta dmeta = points.getItemMeta();
        dmeta.setDisplayName(ChatColor.AQUA + "Talent Points: " +
                getAvailableTalentPoints(player, skill));
        points.setItemMeta(dmeta);
        gui.setItem(4, points);


        // 4) Fill talents into the “interior” (slots 10–16, 19–25, 28–34, 37–43, 46–52)
        int startIndex = (page - 1) * 40;
        int endIndex   = Math.min(talents.size(), startIndex + 40);
        int slot       = 10; // row-1, col-1

        for (int i = startIndex; i < endIndex && slot < 54; i++) {
            Talent t = talents.get(i);

            ItemStack icon = new ItemStack(t.getIcon());
            ItemMeta im = icon.getItemMeta();
            im.setDisplayName(t.getRarity().getColor() + t.getName());
            List<String> lore = List.of(
                    ChatColor.GRAY + t.getDescription(),
                    ChatColor.GRAY + getDynamicTechnicalDescription(t, getTalentLevel(player.getUniqueId(), skill, t)),
                    ChatColor.YELLOW + "Level: " + getTalentLevel(player.getUniqueId(), skill, t) +
                            "/" + t.getMaxLevel(),
                    ChatColor.RED + "Requires " + skill.getDisplayName() + " " + t.getLevelRequirement()
            );
            im.setLore(lore);
            if (getTalentLevel(player.getUniqueId(), skill, t) > 0) {
                im.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            icon.setItemMeta(im);

            gui.setItem(slot, icon);

            // advance slot, skipping the right-border column
            slot++;
            if (slot % 9 == 8) {
                // jumped into the right border → move to next row’s first interior slot
                slot += 2;
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
            case REDSTONE_THREE:
            case REDSTONE_FOUR:
                int seconds = level * 4;
                return ChatColor.YELLOW + "+" + seconds + "s " + ChatColor.LIGHT_PURPLE + "Potion Duration, "
                        + ChatColor.GOLD + "+" + seconds + "s " + ChatColor.GOLD + "Brew Time.";
            case RECURVE_MASTERY:
                int recurveDuration = level * 50;
                return ChatColor.YELLOW + "+" + recurveDuration + "s " + ChatColor.LIGHT_PURPLE + "Recurve Duration, "
                        + ChatColor.RED + "+5% Arrow Damage";
            case REJUVENATION:
                int bonusTime = level * 50;
                return ChatColor.YELLOW + "+" + bonusTime + "s " + ChatColor.GREEN + "Bonus Health" + ChatColor.GRAY + " and " + ChatColor.GREEN + "Health Surge";
            case SOVEREIGNTY_MASTERY:
                int sovDuration = level * 50;
                int deflect = level * 5;
                return ChatColor.YELLOW + "+" + sovDuration + "s " + ChatColor.LIGHT_PURPLE + "Sovereignty Duration, "
                        + ChatColor.RED + "+" + deflect + " Deflection Stacks";
            case STRENGTH_MASTERY:
                int strengthDuration = level * 50;
                return ChatColor.YELLOW + "+" + strengthDuration + "s " + ChatColor.LIGHT_PURPLE + "Strength Duration, "
                        + ChatColor.RED + "+5% Damage";
            case LIQUID_LUCK_MASTERY:
                int luckDuration = level * 50;
                return ChatColor.YELLOW + "+" + luckDuration + "s " + ChatColor.LIGHT_PURPLE + "Liquid Luck Duration";
            case OXYGEN_MASTERY:
                int oxygenDuration = level * 50;
                return ChatColor.YELLOW + "+" + oxygenDuration + "s " + ChatColor.AQUA + "Oxygen Recovery Duration";
            case SWIFT_STEP_MASTERY:
                int swiftDuration = level * 50;
                return ChatColor.YELLOW + "+" + swiftDuration + "s " + ChatColor.LIGHT_PURPLE + "Swift Step Duration, "
                        + ChatColor.AQUA + "+5% Speed";
            case METAL_DETECTION_MASTERY:
                int metalDuration = level * 50;
                double graveBonus = level * 0.01;
                return ChatColor.YELLOW + "+" + metalDuration + "s " + ChatColor.LIGHT_PURPLE + "Metal Detection Duration, "
                        + ChatColor.YELLOW + "+" + graveBonus + ChatColor.GRAY + " grave chance";
            case NIGHT_VISION_MASTERY:
                int nvDuration = level * 50;
                return ChatColor.YELLOW + "+" + nvDuration + "s " + ChatColor.AQUA + "Night Vision Duration";
            case SOLAR_FURY_MASTERY:
                int solarDuration = level * 50;
                return ChatColor.YELLOW + "+" + solarDuration + "s " + ChatColor.GOLD + "Solar Fury Duration";
            case FOUNTAIN_MASTERY:
                int fountainDuration = level * 50;
                return ChatColor.YELLOW + "+" + fountainDuration + "s " + ChatColor.LIGHT_PURPLE + "Fountains Duration, "
                        + ChatColor.AQUA + "+5% Sea Creature Chance";
            case CHARISMA_MASTERY:
                int charismaDuration = level * 50;
                return ChatColor.YELLOW + "+" + charismaDuration + "s " + ChatColor.LIGHT_PURPLE + "Charismatic Bartering Duration, "
                        + ChatColor.GOLD + "+5% Discount";
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
                int bonus = level * 8;
                return ChatColor.RED + "+" + bonus + "% Damage";
            case BOW_MASTERY:
                int arrowBonus = level * 8;
                return ChatColor.RED + "+" + arrowBonus + "% Arrow Damage";
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
        if (!raw.contains("Skill Tree")) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        // Determine the skill from the inventory title. Titles are in the form
        // "<Skill Display> Skill Tree" or "<Skill Display> Skill Tree: Page X/Y".
        int skillEnd = raw.indexOf(" Skill Tree");
        if (skillEnd == -1) return;
        String skillName = raw.substring(0, skillEnd).trim();
        Skill skill = Skill.fromDisplay(skillName);
        if (skill == null) return;

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
}
