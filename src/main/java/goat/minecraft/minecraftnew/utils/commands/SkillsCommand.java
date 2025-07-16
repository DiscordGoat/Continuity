package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SkillsCommand implements CommandExecutor, Listener {

    private final XPManager xpManager;

    public SkillsCommand(XPManager xpManager) {
        this.xpManager = xpManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = ChatColor.stripColor(view.getTitle());

        // Check if the clicked inventory is the Skills GUI
        if (title.equals("Your Skills")) {
            event.setCancelled(true); // Prevent any interaction

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) {
                return;
            }
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            if (name.endsWith(" Skill")) {
                name = name.replace(" Skill", "");
                Skill skill = Skill.fromDisplay(name);
                if (skill != null) {
                    SkillTreeManager.getInstance().openSkillTree((Player) event.getWhoClicked(), skill);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Ensure the command is executed by a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        // Create the Skills GUI
        Inventory skillsInventory = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Your Skills");

        // Define the skills to display
        String[] skills = {"Fishing", "Farming", "Mining", "Terraforming", "Combat", "Player", "Taming", "Forestry", "Bartering", "Culinary", "Smithing", "Brewing"};

        // Define icons for each skill (customize as desired)
        Material[] icons = {
                Material.FISHING_ROD,    // Fishing
                Material.WHEAT,          // Farming
                Material.IRON_PICKAXE,   // Mining
                Material.GRASS_BLOCK,    // Terraforming
                Material.IRON_SWORD,     // Combat
                Material.PLAYER_HEAD,    // Player
                Material.LEAD,           // Taming
                Material.GOLDEN_AXE,     // Forestry
                Material.EMERALD,        // Bartering
                Material.FURNACE,        // Culinary
                Material.DAMAGED_ANVIL,  // Smithing
                Material.BREWING_STAND   // Brewing
        };

        // Populate the inventory with skill items
        for (int i = 0; i < skills.length; i++) {
            String skill = skills[i];
            Material icon = icons[i];
            int level = xpManager.getPlayerLevel(player, skill);

            ItemStack skillItem = new ItemStack(icon);
            ItemMeta meta = skillItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + skill + " Skill");
                // Pass the player so we can include a progress bar in the lore.
                meta.setLore(getSkillStatLore(player, skill, level));
                skillItem.setItemMeta(meta);
            }

            // Place the item in the inventory (spacing out items)
            skillsInventory.setItem(i * 2, skillItem);
        }

        // Optionally, fill the remaining slots with decorative items to prevent clicks
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }

        for (int i = 0; i < skillsInventory.getSize(); i++) {
            if (skillsInventory.getItem(i) == null) {
                skillsInventory.setItem(i, glassPane);
            }
        }

        // Open the inventory for the player
        player.openInventory(skillsInventory);

        return true;
    }

    /**
     * Generate dynamic lore for a skill based on its level and XP progress.
     */
    private List<String> getSkillStatLore(Player player, String skill, double level) {
        List<String> lore;
        double multiplier = 1 + (level * 0.01); // 1% per level
        switch (skill) {
            case "Smithing":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.WHITE + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.WHITE + "Repair Amount: " + (25 + level)
                ));
                break;
            case "Culinary":
                double additionalSaturation = Math.min(level * 0.05, 20.0);
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.YELLOW + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.YELLOW + "Extra Saturation: " + ChatColor.GREEN + String.format("%.2f", additionalSaturation) + " (Max 5)"
                ));
                break;
            case "Bartering":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.BLUE + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.BLUE + "Discount: " + ChatColor.GREEN + (level * 0.25) + "%"
                ));
                break;
            case "Fishing":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.BLUE + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.BLUE + "Sea Creature Chance: " + ChatColor.GREEN + (level / 4) + "%"
                ));
                break;
            case "Farming":
                double growthTimeDays = 10 - 5 * ((level - 1) / 99.0);
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.YELLOW + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.YELLOW + "Double Crops Chance: " + ChatColor.GREEN + (level / 2) + "%",
                        ChatColor.YELLOW + "Growth Time: " + ChatColor.GREEN + String.format("%.1f", growthTimeDays) + " days"
                ));
                break;

            case "Mining":
                double duration = 200 + (level * 4);
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.GRAY + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.GRAY + "Bonus Oxygen: +" + ChatColor.GREEN + (4 * level) + " seconds",
                        ChatColor.GRAY + "Double Drops Chance: " + ChatColor.GREEN + (level / 2) + "%",
                        ChatColor.GRAY + "Gold Fever: " + ChatColor.GREEN + "Haste " + 1 + " (" + duration / 20 + "s)"
                ));
                break;
            case "Terraforming":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.GREEN + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.GREEN + "Unbreaking Chance: " + level * 0.25 // simple descriptor
                ));
                break;
            case "Combat":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.RED + "Level: " + ChatColor.GREEN + (int) level
                ));
                break;
            case "Player":
                multiplier = Math.min(multiplier, 2.00); // Cap at 2.00x
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.AQUA + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.AQUA + "Health Boost: " + ChatColor.GREEN + String.format("%.2f", multiplier) + "x",
                        ChatColor.RED + "Max Hostility Tier: " + ChatColor.DARK_RED + xpManager.getTierFromLevel((int) level)
                ));
                break;
            case "Forestry":
                double forestryLevel = level;
                double doubleDropChance = forestryLevel;
                double hasteDuration = 200 + (forestryLevel * 5);
                int spiritSpawnChance = 1;
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.DARK_GREEN + "Level: " + ChatColor.GREEN + (int) forestryLevel,
                        ChatColor.DARK_GREEN + "Double Logs Chance: " + ChatColor.GREEN + doubleDropChance + "%",
                        ChatColor.DARK_GREEN + "Haste Duration: " + ChatColor.GREEN + (hasteDuration / 20) + " seconds",
                        ChatColor.DARK_GREEN + "Spirit Spawn Chance: " + ChatColor.GREEN + spiritSpawnChance + "%"
                ));
                break;
            case "Brewing":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.DARK_GREEN + "Level: " + ChatColor.GREEN + (int) level
                ));
                break;
            case "Taming":
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.DARK_GREEN + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.LIGHT_PURPLE + "Bonus Pet XP: " + (level) + "%"
                ));
                break;
            default:
                lore = new ArrayList<>(Arrays.asList(
                        ChatColor.GRAY + "Level: " + ChatColor.GREEN + (int) level,
                        ChatColor.GRAY + "No stats available."
                ));
                break;
        }
        // Append the progress bar line (shows XP progress: current/needed)
        lore.add(generateProgressBar(player, skill));
        return lore;
    }

    /**
     * Generates an advanced progress bar string with the current XP progress for a skill.
     * It displays a bar of 30 segments using '|' characters, followed immediately by a percentage (to two decimals),
     * then the "current/needed XP" text.
     */
    private String generateProgressBar(Player player, String skill) {
        int currentXP = xpManager.getXP(player, skill);
        int level = xpManager.calculateLevel(currentXP);
        int xpToNext = xpManager.getXPToNextLevel(player, skill);
        // If max level reached, show a message.
        if (xpToNext == 0) {
            return ChatColor.GOLD + "Max Level Reached!";
        }
        int levelStart = xpManager.getLevelStartXP(level);
        int levelEnd = xpManager.getLevelEndXP(level);
        int xpInLevel = currentXP - levelStart;
        int xpNeeded = levelEnd - levelStart;
        double progress = (double) xpInLevel / xpNeeded;

        int barLength = 30;
        int filled = (int) Math.round(progress * barLength);
        if (filled > barLength) {
            filled = barLength;
        }
        int empty = barLength - filled;

        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.GRAY).append("Progress: [");
        for (int i = 0; i < filled; i++) {
            bar.append(ChatColor.GREEN).append("|");
        }
        for (int i = 0; i < empty; i++) {
            bar.append(ChatColor.RED).append("|");
        }
        bar.append(ChatColor.GRAY).append("] ");
        bar.append(ChatColor.WHITE)
                .append("(").append(String.format("%.2f", progress * 100)).append("%) ");
        bar.append(ChatColor.YELLOW)
                .append(xpInLevel).append("/").append(xpNeeded).append(" XP");
        return bar.toString();
    }
}
