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
import goat.minecraft.minecraftnew.other.health.HealthManager;
import goat.minecraft.minecraftnew.subsystems.forestry.SaplingManager;
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
                double chance = level * 10;
                return ChatColor.YELLOW + "+" + chance + "% " + ChatColor.GRAY + "Chance to brew 3 Potions.";
            case OPTIMAL_CONFIGURATION:
                int reduction = level * 5;
                return ChatColor.YELLOW + "-" + reduction + "s " + ChatColor.GOLD + "Brew Time.";
            case REDSTONE_ONE:
            case REDSTONE_TWO:
            case REDSTONE_THREE:
            case REDSTONE_FOUR:
            case REDSTONE_FIVE:
                int seconds = switch (talent) {
                    case REDSTONE_THREE -> level * 20;
                    case REDSTONE_FOUR, REDSTONE_FIVE -> level * 30;
                    default -> level * 10;
                };
                return ChatColor.YELLOW + "+" + seconds + "s " + ChatColor.LIGHT_PURPLE + "Potion Duration, "
                        + ChatColor.GOLD + "+" + seconds + "s " + ChatColor.GOLD + "Brew Time.";
            case RECURVE_MASTERY:
                int recurveDuration = level * 200;
                return ChatColor.YELLOW + "+" + recurveDuration + "s " + ChatColor.LIGHT_PURPLE + "Recurve Duration";
            case SOVEREIGNTY_MASTERY:
                int sovDuration = level * 200;
                return ChatColor.YELLOW + "+" + sovDuration + "s " + ChatColor.LIGHT_PURPLE + "Sovereignty Duration";
            case STRENGTH_MASTERY:
                int strengthDuration = level * 200;
                return ChatColor.YELLOW + "+" + strengthDuration + "s " + ChatColor.LIGHT_PURPLE + "Strength Duration";
            case LIQUID_LUCK_MASTERY:
                int luckDuration = level * 200;
                return ChatColor.YELLOW + "+" + luckDuration + "s " + ChatColor.LIGHT_PURPLE + "Liquid Luck Duration";
            case OXYGEN_MASTERY:
                int oxygenDuration = level * 200;
                return ChatColor.YELLOW + "+" + oxygenDuration + "s " + ChatColor.AQUA + "Oxygen Recovery Duration";
            case SWIFT_STEP_MASTERY:
                int swiftDuration = level * 200;
                return ChatColor.YELLOW + "+" + swiftDuration + "s " + ChatColor.LIGHT_PURPLE + "Swift Step Duration";
            case METAL_DETECTION_MASTERY:
                int metalDuration = level * 200;
                return ChatColor.YELLOW + "+" + metalDuration + "s " + ChatColor.LIGHT_PURPLE + "Metal Detection Duration";
            case NIGHT_VISION_MASTERY:
                int nvDuration = level * 200;
                return ChatColor.YELLOW + "+" + nvDuration + "s " + ChatColor.AQUA + "Night Vision Duration";
            case SOLAR_FURY_MASTERY:
                int solarDuration = level * 200;
                return ChatColor.YELLOW + "+" + solarDuration + "s " + ChatColor.GOLD + "Solar Fury Duration";
            case FOUNTAIN_MASTERY:
                int fountainDuration = level * 200;
                return ChatColor.YELLOW + "+" + fountainDuration + "s " + ChatColor.LIGHT_PURPLE + "Fountains Duration";
            case ANGLERS_INSTINCT:
                double seaBonus = level * 0.25;
                return ChatColor.YELLOW + "+" + seaBonus + "% " + ChatColor.AQUA + "Sea Creature Chance";
            case CHARISMA_MASTERY:
                int charismaDuration = level * 200;
                return ChatColor.YELLOW + "+" + charismaDuration + "s " + ChatColor.LIGHT_PURPLE + "Charismatic Bartering Duration";
            case NUTRITION_MASTERY:
                int nutritionDuration = level * 200;
                return ChatColor.YELLOW + "+" + nutritionDuration + "s " + ChatColor.LIGHT_PURPLE + "Optimal Eating Duration";
            case ETERNAL_ELIXIR:
                double infiniteChance = level * 0.25;
                return ChatColor.YELLOW + "+" + infiniteChance + "% " + ChatColor.GRAY + "Chance for infinite duration";
            case SWORD_DAMAGE_I:
            case SWORD_DAMAGE_II:
            case SWORD_DAMAGE_III:
            case SWORD_DAMAGE_IV:
            case SWORD_DAMAGE_V:
                int bonus = level * 4;
                return ChatColor.RED + "+" + bonus + "% Sword Damage";
            case ARROW_DAMAGE_INCREASE_I:
            case ARROW_DAMAGE_INCREASE_II:
            case ARROW_DAMAGE_INCREASE_III:
            case ARROW_DAMAGE_INCREASE_IV:
            case ARROW_DAMAGE_INCREASE_V:
                int arrowBonus = switch (talent) {
                    case ARROW_DAMAGE_INCREASE_I -> level * 4;
                    case ARROW_DAMAGE_INCREASE_II -> level * 8;
                    case ARROW_DAMAGE_INCREASE_III -> level * 12;
                    case ARROW_DAMAGE_INCREASE_IV -> level * 16;
                    default -> level * 20;
                };
                return ChatColor.RED + "+" + arrowBonus + "% Arrow Damage";
            case DONT_MINE_AT_NIGHT:
                int creeperBonus = level * 10;
                return ChatColor.YELLOW + "+" + creeperBonus + "% " + ChatColor.RED + "Creeper Damage";
            case HELLBENT:
                int threshold = level * 10;
                return ChatColor.RED + "+25% Damage below " + threshold + "% Health";
            case BLOODLUST:
                return ChatColor.RED + "Activates Bloodlust for 5s on kill";
            case BLOODLUST_DURATION_I:
            case BLOODLUST_DURATION_II:
            case BLOODLUST_DURATION_III:
            case BLOODLUST_DURATION_IV:
                int extra = level * 4;
                return ChatColor.YELLOW + "+" + extra + "s Bloodlust Duration";
            case RETRIBUTION:
                return ChatColor.YELLOW + "+" + level + "% chance for +10 Bloodlust Stacks";
            case VENGEANCE:
                int secondsBL = level * 20;
                return ChatColor.YELLOW + "+" + level + "% chance for +" + secondsBL + "s Bloodlust";
            case ANTAGONIZE:
                return ChatColor.YELLOW + "Damage received over " + level + "s";
            case ULTIMATUM:
                double furyChance = level * 0.25;
                return ChatColor.YELLOW + "+" + furyChance + "% " + ChatColor.GRAY + "Fury Chance";
            case REVENANT:
                return ChatColor.YELLOW + "Triggers Fury on death with 100 Stacks";
            case VAMPIRIC_STRIKE:
                double vampChance = level;
                return ChatColor.YELLOW + "+" + vampChance + "% " + ChatColor.GRAY + "Soul Orb chance";
            case REPAIR_AMOUNT_I:
                return ChatColor.GREEN + "+" + (level * 3) + ChatColor.GRAY + " Repair Amount";
            case QUALITY_MATERIALS_I:
                return ChatColor.GREEN + "+" + (level * 1) + ChatColor.GRAY + " Repair Quality";
            case ALLOY_I:
                return ChatColor.YELLOW + "+" + (level * 1.5) + "% Max Durability Chance";
            case NOVICE_SMITH:
                return ChatColor.YELLOW + "+" + (level * 25) + "% Common Reforge Chance";
            case SCRAPS_I:
                return ChatColor.YELLOW + "-" + (level * 3) + " Reforge Mats";
            case NOVICE_FOUNDATIONS:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Anvil Degrade Chance";

            case REPAIR_AMOUNT_II:
                return ChatColor.GREEN + "+" + (level * 4) + ChatColor.GRAY + " Repair Amount";
            case QUALITY_MATERIALS_II:
                return ChatColor.GREEN + "+" + (level * 2) + ChatColor.GRAY + " Repair Quality";
            case ALLOY_II:
                return ChatColor.YELLOW + "+" + (level * 1.5) + "% Max Durability Chance";
            case APPRENTICE_SMITH:
                return ChatColor.YELLOW + "+" + (level * 25) + "% Uncommon Reforge Chance";
            case SCRAPS_II:
                return ChatColor.YELLOW + "-" + (level * 3) + " Reforge Mats";
            case APPRENTICE_FOUNDATIONS:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Anvil Degrade Chance";

            case REPAIR_AMOUNT_III:
                return ChatColor.GREEN + "+" + (level * 5) + ChatColor.GRAY + " Repair Amount";
            case QUALITY_MATERIALS_III:
                return ChatColor.GREEN + "+" + (level * 3) + ChatColor.GRAY + " Repair Quality";
            case ALLOY_III:
                return ChatColor.YELLOW + "+" + (level * 1.5) + "% Max Durability Chance";
            case JOURNEYMAN_SMITH:
                return ChatColor.YELLOW + "+" + (level * 25) + "% Rare Reforge Chance";
            case SCRAPS_III:
                return ChatColor.YELLOW + "-" + (level * 3) + " Reforge Mats";
            case JOURNEYMAN_FOUNDATIONS:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Anvil Degrade Chance";

            case REPAIR_AMOUNT_IV:
                return ChatColor.GREEN + "+" + (level * 6) + ChatColor.GRAY + " Repair Amount";
            case QUALITY_MATERIALS_IV:
                return ChatColor.GREEN + "+" + (level * 4) + ChatColor.GRAY + " Repair Quality";
            case ALLOY_IV:
                return ChatColor.YELLOW + "+" + (level * 1.5) + "% Max Durability Chance";
            case EXPERT_SMITH:
                return ChatColor.YELLOW + "+" + (level * 25) + "% Epic Reforge Chance";
            case SCRAPS_IV:
                return ChatColor.YELLOW + "-" + (level * 3) + " Reforge Mats";
            case EXPERT_FOUNDATIONS:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Anvil Degrade Chance";

            case REPAIR_AMOUNT_V:
                return ChatColor.GREEN + "+" + (level * 7) + ChatColor.GRAY + " Repair Amount";
            case QUALITY_MATERIALS_V:
                return ChatColor.GREEN + "+" + (level * 5) + ChatColor.GRAY + " Repair Quality";
            case ALLOY_V:
                return ChatColor.YELLOW + "+" + (level * 0.5) + "% Max Durability Chance";
            case MASTER_SMITH:
                return ChatColor.YELLOW + "+" + (level * 25) + "% Legendary Reforge Chance";
            case SCRAPS_V:
                return ChatColor.YELLOW + "-" + (level * 3) + " Reforge Mats";
            case MASTER_FOUNDATIONS:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Anvil Degrade Chance";
            case SATIATION_MASTERY_I:
            case SATIATION_MASTERY_II:
            case SATIATION_MASTERY_III:
            case SATIATION_MASTERY_IV:
            case SATIATION_MASTERY_V:
                return ChatColor.YELLOW + "+" + level + " Bonus Saturation";
            case CUTTING_BOARD_I:
            case CUTTING_BOARD_II:
            case CUTTING_BOARD_III:
            case CUTTING_BOARD_IV:
            case CUTTING_BOARD_V:
                double cutChance = level * 4;
                return ChatColor.YELLOW + "+" + cutChance + "% Chance For Double Culinary Yield";
            case LUNCH_RUSH_I:
            case LUNCH_RUSH_II:
            case LUNCH_RUSH_III:
            case LUNCH_RUSH_IV:
            case LUNCH_RUSH_V:
                double rushReduce = level * 4;
                return ChatColor.YELLOW + "-" + rushReduce + "% Cook time";
            case SWEET_TOOTH:
                double fruitBonus = level * 10;
                return ChatColor.YELLOW + "+" + fruitBonus + "% Fruits Gains";
            case GOLDEN_APPLE:
                int regen = level * 3;
                return ChatColor.YELLOW + "+" + regen + "s Regeneration I when eating";
            case GRAINS_GAINS:
                double grainBonus = level * 10;
                return ChatColor.YELLOW + "+" + grainBonus + "% Grains Gains";
            case PORTAL_PANTRY:
                double pantryChance = level * 20;
                return ChatColor.YELLOW + "+" + pantryChance + "% Chance to automatically grab ingredient";
            case AXE_BODY_SPRAY:
                double proteinBonus = level * 10;
                return ChatColor.YELLOW + "+" + proteinBonus + "% Protein Gains";
            case I_DO_NOT_NEED_A_SNACK:
                double refundChance = level * 5;
                return ChatColor.YELLOW + "+" + refundChance + "% Chance to Refund eaten items";
            case RABBIT:
                double veggieBonus = level * 10;
                return ChatColor.YELLOW + "+" + veggieBonus + "% Veggie Gains";
            case PANTRY_OF_PLENTY:
                double satChance = level * 4;
                return ChatColor.YELLOW + "+" + satChance + "% Chance to gain 20 Saturation when eating Culinary Delights";
            case CAVITY:
                double sugarBonus = level * 10;
                return ChatColor.YELLOW + "+" + sugarBonus + "% Sugar Gains";
            case CHEFS_KISS:
                double recipeRefund = level * 20;
                return ChatColor.YELLOW + "+" + recipeRefund + "% Chance to Refund Recipe Papers";
            case HAGGLER_I:
                return ChatColor.YELLOW + "+" + (level * 0.005) + " Discount";
            case STONKS_I:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Emerald Gain when selling";
            case SHUT_UP_AND_TAKE_MY_MONEY:
                return ChatColor.YELLOW + "Right click purchases 2x";
            case SWEATSHOP_SUPERVISOR:
                return ChatColor.YELLOW + "-" + (level * 10) + "s Workcycle Cooldown";
            case CORPORATE_BENEFITS:
                return ChatColor.YELLOW + "-" + (level * 5) + "d Villager Tier Threshold";

            case HAGGLER_II:
                return ChatColor.YELLOW + "+" + (level * 0.01) + " Discount";
            case STONKS_II:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Emerald Gain when selling";
            case BULK:
                return ChatColor.YELLOW + "-" + (level * 10) + "% cost for 20s";
            case DEADLINE_DICTATOR:
                return ChatColor.YELLOW + "-" + (level * 10) + "s Workcycle Cooldown";
            case UNIFORM:
                return ChatColor.YELLOW + "+" + (level * 10) + "% Villager Damage Resistance";

            case HAGGLER_III:
                return ChatColor.YELLOW + "+" + (level * 0.015) + " Discount";
            case STONKS_III:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Emerald Gain when selling";
            case INTEREST:
                return ChatColor.YELLOW + "+" + (level) + "% chance for 1% bank interest";
            case TASKMASTER_TYRANT:
                return ChatColor.YELLOW + "-" + (level * 10) + "s Workcycle Cooldown";
            case OVERSTOCKED:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Free purchase chance";

            case HAGGLER_IV:
                return ChatColor.YELLOW + "+" + (level * 0.02) + " Discount";
            case STONKS_IV:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Emerald Gain when selling";
            case OVERTIME_OVERLORD:
                return ChatColor.YELLOW + "-" + (level * 10) + "s Workcycle Cooldown";
            case ITS_ALIVE:
                return ChatColor.YELLOW + "+" + (level * 20) + "% Transformation failure";

            case HAGGLER_V:
                return ChatColor.YELLOW + "+" + (level * 0.025) + " Discount";
            case STONKS_V:
                return ChatColor.YELLOW + "+" + (level * 2) + "% Emerald Gain when selling";
            case SLAVE_DRIVER:
                return ChatColor.YELLOW + "-" + (level * 10) + "s Workcycle Cooldown";
            case BILLIONAIRE_DISCOUNT:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Discount";
            case TIMBER_I:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "Double Logs Chance";
            case TIMBER_II:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "Triple Logs Chance";
            case TIMBER_III:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "Quadruple Logs Chance";
            case LEVERAGE_I:
            case LEVERAGE_II:
            case LEVERAGE_III:
            case LEVERAGE_IV:
            case LEVERAGE_V:
                double hasteChance = level * 2;
                return ChatColor.YELLOW + "+" + hasteChance + "% " + ChatColor.GRAY + "Haste Chance";
            case FOREST_FRENZY:
                int frenzyDur = level * 10;
                return ChatColor.YELLOW + "+" + frenzyDur + "s Haste Duration";
            case PHOTOSYNTHESIS:
                int heal = level;
                return ChatColor.GREEN + "+" + heal + " Health on Treecapitator use";
            case REGROWTH_I:
            case REGROWTH_II:
            case REGROWTH_III:
                return ChatColor.YELLOW + "-" + level + "d Sapling Growth Cooldown";
            case ONE_HUNDRED_ACRE_WOODS:
                double honeyChance = level * 1;
                return ChatColor.YELLOW + "+" + honeyChance + "% Honey Bottle Chance";
            case SPECTRAL_ARMOR:
                double spiritReduction = level * 10;
                return ChatColor.YELLOW + "-" + spiritReduction + "% Spirit Damage";
            case DEFORESTATION:
                return ChatColor.YELLOW + "+" + level + " Potency of Haste";
            case HEADHUNTER:
                double dmg = level * 10;
                return ChatColor.RED + "+" + dmg + "% Damage to Spirits";
            case SPIRIT_CHANCE_I:
                double sc1 = level * 0.02;
                return ChatColor.YELLOW + "+" + sc1 + "% " + ChatColor.GRAY + "Spirit Chance";
            case SPIRIT_CHANCE_II:
                double sc2 = level * 0.04;
                return ChatColor.YELLOW + "+" + sc2 + "% " + ChatColor.GRAY + "Spirit Chance";
            case SPIRIT_CHANCE_III:
                double sc3 = level * 0.06;
                return ChatColor.YELLOW + "+" + sc3 + "% " + ChatColor.GRAY + "Spirit Chance";
            case SPIRIT_CHANCE_IV:
                double sc4 = level * 0.08;
                return ChatColor.YELLOW + "+" + sc4 + "% " + ChatColor.GRAY + "Spirit Chance";
            case SPIRIT_CHANCE_V:
                double sc5 = level * 0.10;
                return ChatColor.YELLOW + "+" + sc5 + "% " + ChatColor.GRAY + "Spirit Chance";
            case ANCIENT_CONFUSION:
                int lost = level * 10;
                return ChatColor.DARK_GRAY + "-" + lost + " Spirit Level";
            case REDEMPTION:
                return ChatColor.GREEN + "x" + (level + 1) + " Super Sapling Chance";
            case PET_TRAINER:
                double xpChance = level * 4;
                return ChatColor.YELLOW + "+" + xpChance + "% " + ChatColor.GRAY + "Double Pet XP chance";
            case HEALTH_I:
            case HEALTH_II:
            case HEALTH_III:
            case HEALTH_IV:
            case HEALTH_V:
                int extraHealth = level;
                return ChatColor.GREEN + "+" + extraHealth + " Bonus Health";
            case STUDY_BREWING:
                return ChatColor.YELLOW + "+" + level + " Brewing Talent Point";
            case STUDY_SMITHING:
                return ChatColor.YELLOW + "+" + level + " Smithing Talent Point";
            case STUDY_CULINARY:
                return ChatColor.YELLOW + "+" + level + " Culinary Talent Point";
            case STUDY_BARTERING:
                return ChatColor.YELLOW + "+" + level + " Bartering Talent Point";
            case STUDY_FORESTRY:
                return ChatColor.YELLOW + "+" + level + " Forestry Talent Point";
            case STUDY_TAMING:
                return ChatColor.YELLOW + "+" + level + " Taming Talent Point";
            case STUDY_COMBAT:
                return ChatColor.YELLOW + "+" + level + " Combat Talent Point";
            case STUDY_TERRAFORMING:
                return ChatColor.YELLOW + "+" + level + " Terraforming Talent Point";
            case STUDY_MINING:
                return ChatColor.YELLOW + "+" + level + " Mining Talent Point";
            case STUDY_FARMING:
                return ChatColor.YELLOW + "+" + level + " Farming Talent Point";
            case STUDY_FISHING:
                return ChatColor.YELLOW + "+" + level + " Fishing Talent Point";
            case GRAVE_DIGGER_I:
                return ChatColor.YELLOW + "+" + String.format("%.3f", level * 0.001) + ChatColor.GRAY + " grave chance";
            case POST_MORTEM_COMPLICATIONS_I:
                return ChatColor.YELLOW + "+" + (level * 5) + "% " + ChatColor.GRAY + "damage to corpses";
            case PROSPEKT:
                return ChatColor.YELLOW + "+" + (level * 50) + " Max Durability";
            case GRAVEYARD_I:
                return ChatColor.YELLOW + "+" + (level * 2.5) + "% " + ChatColor.GRAY + "chance for another grave";
            case X_MARKS_THE_SPOT:
                double blocks = 512 / (level * 0.1 + 1);
                return ChatColor.YELLOW + "1 every " + String.format("%.1f", blocks) + ChatColor.GRAY + " blocks";
            case GRAVE_DIGGER_II:
                return ChatColor.YELLOW + "+" + String.format("%.4f", level * 0.0015) + ChatColor.GRAY + " grave chance";
            case POST_MORTEM_COMPLICATIONS_II:
                return ChatColor.YELLOW + "+" + (level * 5) + "% " + ChatColor.GRAY + "damage to corpses";
            case GRAVEYARD_II:
                return ChatColor.YELLOW + "+" + (level * 2.5) + "% " + ChatColor.GRAY + "chance for another grave";
            case NECROTIC_I:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Damage Reduction from Corpses";
            case MASS_GRAVE:
                return ChatColor.YELLOW + "+" + (level * 10) + "% " + ChatColor.GRAY + "corpse chance";
            case GRAVE_DIGGER_III:
                return ChatColor.YELLOW + "+" + String.format("%.3f", level * 0.002) + ChatColor.GRAY + " grave chance";
            case POST_MORTEM_COMPLICATIONS_III:
                return ChatColor.YELLOW + "+" + (level * 5) + "% " + ChatColor.GRAY + "damage to corpses";
            case GRAVEYARD_III:
                return ChatColor.YELLOW + "+" + (level * 2.5) + "% " + ChatColor.GRAY + "chance for another grave";
            case NECROTIC_II:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Damage Reduction from Corpses";
            case DOUBLE_TROUBLE:
                return ChatColor.YELLOW + "+" + (level) + "% " + ChatColor.GRAY + "chance to spawn two corpses";
            case GRAVE_DIGGER_IV:
                return ChatColor.YELLOW + "+" + String.format("%.4f", level * 0.0025) + ChatColor.GRAY + " grave chance";
            case POST_MORTEM_COMPLICATIONS_IV:
                return ChatColor.YELLOW + "+" + (level * 5) + "% " + ChatColor.GRAY + "damage to corpses";
            case GRAVEYARD_IV:
                return ChatColor.YELLOW + "+" + (level * 2.5) + "% " + ChatColor.GRAY + "chance for another grave";
            case NECROTIC_III:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Damage Reduction from Corpses";
            case ALIVE_TOMBSTONE:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "indicator chance";
            case GRAVE_DIGGER_V:
                return ChatColor.YELLOW + "+" + String.format("%.5f", level * 0.00725) + ChatColor.GRAY + " grave chance";
            case POST_MORTEM_COMPLICATIONS_V:
                return ChatColor.YELLOW + "+" + (level * 5) + "% " + ChatColor.GRAY + "damage to corpses";
            case GRAVEYARD_V:
                return ChatColor.YELLOW + "+" + (level * 2.5) + "% " + ChatColor.GRAY + "chance for another grave";
            case NECROTIC_IV:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Damage Reduction from Corpses";
            case MURDER_MYSTERY:
                return ChatColor.YELLOW + "+" + (level * 50) + "% " + ChatColor.GRAY + "damage to Mass Murderers";
            case BOUNTIFUL_HARVEST:
                double cropChance = level * 4;
                return ChatColor.YELLOW + "+" + cropChance + "% " + ChatColor.GRAY + "chance to harvest " + ChatColor.GREEN + "double crops.";
            case VERDANT_TENDING:
                double minutes = level * 2.5;
                return ChatColor.YELLOW + "-" + minutes + "m " + ChatColor.GRAY + "Verdant Relic growth time";
            case RICH_VEINS:
                double dropChance = level * 4;
                return ChatColor.YELLOW + "+" + dropChance + "% " + ChatColor.GRAY + "Double Drop Chance";
            case DEEP_LUNGS:
                int oxygenBonus = level * 20;
                return ChatColor.YELLOW + "+" + oxygenBonus + " " + ChatColor.AQUA + "Oxygen Capacity";
            case EXTRA_CROP_CHANCE_I:
                return ChatColor.YELLOW + "+" + (level * 8) + "% " + ChatColor.GRAY + "Extra Crop Chance";
            case FOR_THE_STREETS:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "till 9x9 area chance";
            case REAPER_I:
                return ChatColor.YELLOW + "-" + level + "% " + ChatColor.GRAY + "Harvest requirement";
            case FAST_FARMER:
                return ChatColor.YELLOW + "Speed " + level + ChatColor.GRAY + " on crop break";
            case HARVEST_FESTIVAL:
                return ChatColor.YELLOW + "+" + (level * 50) + "% " + ChatColor.GRAY + "Haste II chance";
            case EXTRA_CROP_CHANCE_II:
                return ChatColor.YELLOW + "+" + (level * 16) + "% " + ChatColor.GRAY + "Extra Crop Chance";
            case UNRIVALED:
                return ChatColor.YELLOW + "+" + level + "% " + ChatColor.GRAY + "grow nearby crops";
            case REAPER_II:
                return ChatColor.YELLOW + "-" + level + "% " + ChatColor.GRAY + "Harvest requirement";
            case HYDRO_FARMER:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "extra irrigation growth";
            case FESTIVAL_BEES_I:
                return ChatColor.YELLOW + "+" + (level * 0.25) + "% " + ChatColor.GRAY + "Festival Bee chance";
            case EXTRA_CROP_CHANCE_III:
                return ChatColor.YELLOW + "+" + (level * 24) + "% " + ChatColor.GRAY + "Extra Crop Chance";
            case REAPER_III:
                return ChatColor.YELLOW + "-" + level + "% " + ChatColor.GRAY + "Harvest requirement";
            case HALLOWEEN:
                return ChatColor.YELLOW + "-" + level + " " + ChatColor.GRAY + "Scythe durability";
            case FESTIVAL_BEE_DURATION_I:
                return ChatColor.YELLOW + "+" + (level * 10) + "s Festival Bee Duration";
            case FESTIVAL_BEES_II:
                return ChatColor.YELLOW + "+" + (level * 0.25) + "% " + ChatColor.GRAY + "Festival Bee chance";
            case EXTRA_CROP_CHANCE_IV:
                return ChatColor.YELLOW + "+" + (level * 32) + "% " + ChatColor.GRAY + "Extra Crop Chance";
            case REAPER_IV:
                return ChatColor.YELLOW + "-" + level + "% " + ChatColor.GRAY + "Harvest requirement";
            case FERTILIZER_EFFICIENCY:
                return ChatColor.YELLOW + "+" + (level * 20) + "% " + ChatColor.GRAY + "double growth chance";
            case FESTIVAL_BEE_DURATION_II:
                return ChatColor.YELLOW + "+" + (level * 10) + "s Festival Bee Duration";
            case FESTIVAL_BEES_III:
                return ChatColor.YELLOW + "+" + (level * 0.25) + "% " + ChatColor.GRAY + "Festival Bee chance";
            case EXTRA_CROP_CHANCE_V:
                return ChatColor.YELLOW + "+" + (level * 40) + "% " + ChatColor.GRAY + "Extra Crop Chance";
            case REAPER_V:
                return ChatColor.YELLOW + "-" + level + "% " + ChatColor.GRAY + "Harvest requirement";
            case FESTIVAL_BEES_IV:
                return ChatColor.YELLOW + "+" + (level * 0.25) + "% " + ChatColor.GRAY + "Festival Bee chance";
            case SWARM:
                return ChatColor.YELLOW + "+" + (level * 10) + "% " + ChatColor.GRAY + "double bee chance";
            case HIVEMIND:
                return ChatColor.YELLOW + "+" + (level * 25) + "% " + ChatColor.GRAY + "Festival Bee Duration";
            // =============================
            // Taming Talents
            // =============================
            case BONUS_PET_XP_I:
                return ChatColor.YELLOW + "+" + (level * 2) + "% " + ChatColor.GRAY + "Bonus Pet XP Chance";
            case LULLABY:
                return ChatColor.YELLOW + "+" + (level * 50) + "% Range";
            case FLIGHT:
                return ChatColor.YELLOW + "+" + (level * 0.1) + "km " + ChatColor.GRAY + "Flight Distance";
            case DIGGING_CLAWS:
                return ChatColor.YELLOW + "Double Haste Duration";
            case DEVOUR:
                return ChatColor.GREEN + "Double Food Gains";
            case ANGLER:
                return ChatColor.YELLOW + "+" + (level * 50) + "% " + ChatColor.AQUA + "Bonus Sea Creature Chance";
            case LEAP:
                return ChatColor.YELLOW + "+" + (level * 50) + "% " + ChatColor.GRAY + "Hungerless Leap Chance";
            case LUMBERJACK:
                return ChatColor.YELLOW + "+" + level + " Bonus Logs";

            case BONUS_PET_XP_II:
                return ChatColor.YELLOW + "+" + (level * 4) + "% " + ChatColor.GRAY + "Bonus Pet XP Chance";
            case ANTIDOTE:
                return ChatColor.YELLOW + "Removes cooldown";
            case GREEN_THUMB:
                return ChatColor.YELLOW + "-" + (level * 25) + "% Cooldown";
            case COLLECTOR:
                return ChatColor.YELLOW + "+" + (level * 50) + "% Range";
            case WALKING_FORTRESS:
                return ChatColor.YELLOW + "+" + (level * 10) + "% Damage Reduction";
            case SHOTCALLING:
                return ChatColor.RED + "+" + (level * 5) + "% Arrow Damage";
            case SPEED_BOOST:
                return ChatColor.YELLOW + "+" + (level * 10) + "% Walk Speed";

            case BONUS_PET_XP_III:
                return ChatColor.YELLOW + "+" + (level * 6) + "% " + ChatColor.GRAY + "Bonus Pet XP Chance";
            case WATERLOGGED:
                return ChatColor.YELLOW + "-" + level + "s Oxygen Recovery Cooldown";
            case ENDLESS_WARP:
                return ChatColor.YELLOW + "+" + (level * 100) + " Bonus Warp Stacks";
            case DECAY:
                return ChatColor.DARK_GRAY + "+" + (level * 5) + " Deteriorate Stacks";
            case ASPECT_OF_FROST:
                return ChatColor.YELLOW + "Double Slow Duration";
            case PRACTICE:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Bartering XP";

            case BONUS_PET_XP_IV:
                return ChatColor.YELLOW + "+" + (level * 8) + "% " + ChatColor.GRAY + "Bonus Pet XP Chance";
            case SECRET_LEGION:
                return ChatColor.YELLOW + "No Hunger Cost";
            case BLACKLUNG:
                return ChatColor.YELLOW + "No Overworld Oxygen Loss";
            case COMFORTABLE:
                return ChatColor.YELLOW + "Double Absorption Duration & Health";
            case SPLASH_POTION:
                return ChatColor.YELLOW + "-" + (level * 10) + "% Brewtime";
            case EXPERIMENTATION:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Potion Duration";
            case REVENANT:
                return ChatColor.YELLOW + "-" + level + "m Resurrection Time";

            case BONUS_PET_XP_V:
                return ChatColor.YELLOW + "+" + (level * 10) + "% " + ChatColor.GRAY + "Bonus Pet XP Chance";
            case COMPACT_STONE:
                return ChatColor.YELLOW + "-50% Stone Needed";
            case GROOT:
                return ChatColor.YELLOW + "-50% Wood Needed";
            case COMPOSTER:
                return ChatColor.YELLOW + "-50% Dirt Needed";
            case ELITE:
                return ChatColor.RED + "+" + (level * 10) + "% Damage";
            case HAGGLE:
                return ChatColor.YELLOW + "+" + (level * 5) + "% Discount";
            case QUIRKY:
                return ChatColor.YELLOW + "+" + (level * 20) + "% Trait Effect";
            case NATURAL_SELECTION:
                return ChatColor.YELLOW + "Removes lowest rarity";
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
        if (skill == Skill.PLAYER) {
            if (talent == Talent.HEALTH_I || talent == Talent.HEALTH_II ||
                    talent == Talent.HEALTH_III || talent == Talent.HEALTH_IV ||
                    talent == Talent.HEALTH_V) {
                HealthManager.getInstance(plugin).applyAndFill(player);
            }
            if (talent == Talent.STUDY_BREWING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.BREWING, 1);
            } else if (talent == Talent.STUDY_SMITHING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.SMITHING, 1);
            } else if (talent == Talent.STUDY_CULINARY) {
                addExtraTalentPoints(player.getUniqueId(), Skill.CULINARY, 1);
            } else if (talent == Talent.STUDY_BARTERING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.BARTERING, 1);
            } else if (talent == Talent.STUDY_FORESTRY) {
                addExtraTalentPoints(player.getUniqueId(), Skill.FORESTRY, 1);
            } else if (talent == Talent.STUDY_TAMING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.TAMING, 1);
            } else if (talent == Talent.STUDY_COMBAT) {
                addExtraTalentPoints(player.getUniqueId(), Skill.COMBAT, 1);
            } else if (talent == Talent.STUDY_TERRAFORMING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.TERRAFORMING, 1);
            } else if (talent == Talent.STUDY_MINING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.MINING, 1);
            } else if (talent == Talent.STUDY_FARMING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.FARMING, 1);
            } else if (talent == Talent.STUDY_FISHING) {
                addExtraTalentPoints(player.getUniqueId(), Skill.FISHING, 1);
            }
        }
        if (skill == Skill.FORESTRY &&
                (talent == Talent.REGROWTH_I || talent == Talent.REGROWTH_II || talent == Talent.REGROWTH_III)) {
            SaplingManager.getInstance(plugin).reduceCooldownDays(1);
        }
        openSkillTree(player, skill, page);
    }
}
