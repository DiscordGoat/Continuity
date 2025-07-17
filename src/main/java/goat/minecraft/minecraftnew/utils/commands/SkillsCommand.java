package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import java.util.ArrayList;
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
        List<String> lore = new ArrayList<>();

        ChatColor color;
        switch (skill) {
            case "Smithing":
                color = ChatColor.WHITE;
                break;
            case "Culinary":
            case "Farming":
                color = ChatColor.YELLOW;
                break;
            case "Bartering":
            case "Fishing":
                color = ChatColor.BLUE;
                break;
            case "Mining":
                color = ChatColor.GRAY;
                break;
            case "Terraforming":
                color = ChatColor.GREEN;
                break;
            case "Combat":
                color = ChatColor.RED;
                break;
            case "Player":
                color = ChatColor.AQUA;
                break;
            case "Forestry":
            case "Brewing":
            case "Taming":
                color = ChatColor.DARK_GREEN;
                break;
            default:
                color = ChatColor.GRAY;
                break;
        }

        lore.add(color + "Level: " + ChatColor.GREEN + (int) level);

        Skill enumSkill = Skill.fromDisplay(skill);
        int points = 0;
        if (enumSkill != null) {
            points = SkillTreeManager.getInstance().getAvailableTalentPoints(player, enumSkill);
        }
        lore.add(ChatColor.AQUA + "Talent Points: " + ChatColor.GREEN + points);

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
