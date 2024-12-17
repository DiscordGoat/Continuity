package goat.minecraft.minecraftnew.utils;

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

import java.util.Arrays;
import java.util.List;

public class SkillsCommand implements CommandExecutor {

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
        String[] skills = {"Fishing", "Farming", "Mining", "Combat", "Player", "Forestry", "Bartering", "Culinary"};

        // Define icons for each skill (you can customize these as desired)
        Material[] icons = {
                Material.FISHING_ROD,   // Fishing
                Material.WHEAT,         // Farming
                Material.IRON_PICKAXE,  // Mining
                Material.IRON_SWORD,    // Combat
                Material.PLAYER_HEAD,   // Player
                Material.GOLDEN_AXE,     // Forestry
                Material.EMERALD,     // Bartering
                Material.FURNACE     // Bartering
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
                meta.setLore(getSkillStatLore(skill, level)); // Use the list of lore
                skillItem.setItemMeta(meta);
            }

            // Place the item in the inventory (spacing out items)
            skillsInventory.setItem(i * 2, skillItem);
        }

        // Optionally, fill the remaining slots with decorative items or leave them empty
        // Example: Filling empty slots with stained glass panes to prevent clicks
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
     * Helper method to format the XP required for the next level.
     * Returns "Max Level Reached" if the player is at the highest level.
     */
    private String getXPToNextLevelFormatted(Player player, String skill) {
        int xpToNext = xpManager.getXPToNextLevel(player, skill);
        if (xpToNext == -1) {
            return ChatColor.RED + "Max Level";
        }
        return ChatColor.YELLOW + "" + xpToNext + " XP";
    }

    /**
     * Generate dynamic lore for a skill based on its level.
     *
     * @param skill The name of the skill.
     * @param level The player's current level in that skill.
     * @return A formatted string showing the multipliers for the skill.
     */
    private List<String> getSkillStatLore(String skill, int level) {
        double multiplier = 1 + (level * 0.02); // 2% per level
        switch (skill) {
            case "Culinary":
                double additionalSaturation = Math.min(level * 0.05, 20.0); // Max 20 saturation at level 100
                return Arrays.asList(
                        ChatColor.YELLOW + "Level: " + ChatColor.GREEN + level,
                        ChatColor.YELLOW + "Extra Saturation: " + ChatColor.GREEN + String.format("%.2f", additionalSaturation) + " (Max 5)"
                );
            case "Bartering":
                return Arrays.asList(
                        ChatColor.BLUE + "Level: " + ChatColor.GREEN + level,
                        ChatColor.BLUE + "Discount: " + ChatColor.GREEN +  + level * 0.1 + "%"
                );
            case "Fishing":
                return Arrays.asList(
                        ChatColor.BLUE + "Level: " + ChatColor.GREEN + level,
                        ChatColor.BLUE + "Sea Creature Chance: " + ChatColor.GREEN +  + level /2 + "%"
                );
            case "Farming":
                return Arrays.asList(
                        ChatColor.YELLOW + "Level: " + ChatColor.GREEN + level,
                        ChatColor.YELLOW + "Double Crops Chance: " + ChatColor.GREEN + (level / 2) + "%"
                );
            case "Mining":
                int duration = 200 + (level * 5); // Duration increases with level
                return Arrays.asList(
                        ChatColor.GRAY + "Level: " + ChatColor.GREEN + level,
                        ChatColor.GRAY + "Bonus Oxygen: +" + ChatColor.GREEN + (5 * level) + " seconds",
                        ChatColor.GRAY + "Double Drops Chance: " + ChatColor.GREEN + (level / 2) + "%",
                        ChatColor.GRAY + "Gold Fever: " + ChatColor.GREEN + "Haste " + (1) + " (" + duration / 20 + "s)"
                );
            case "Combat":
                return Arrays.asList(
                        ChatColor.RED + "Level: " + ChatColor.GREEN + level,
                        ChatColor.RED + "Damage Multiplier: " + ChatColor.GREEN + String.format("%.2f", (1 + level * 0.01)) + "x"
                );
            case "Player":
                multiplier = Math.min(multiplier, 2.00); // Cap multiplier at 2.00x
                return Arrays.asList(
                        ChatColor.AQUA + "Level: " + ChatColor.GREEN + level,
                        ChatColor.AQUA + "Health Boost: " + ChatColor.GREEN + String.format("%.2f", multiplier) + "x"
                );
            case "Forestry":
                int forestryLevel = level; // Forestry level
                int doubleDropChance = forestryLevel; // 1% chance per level
                double hasteDuration = 200 + (forestryLevel * 5); // Haste duration in ticks
                int spiritSpawnChance = 1; // 1% chance for spirit spawn

                return Arrays.asList(
                        ChatColor.DARK_GREEN + "Level: " + ChatColor.GREEN + forestryLevel,
                        ChatColor.DARK_GREEN + "Double Logs Chance: " + ChatColor.GREEN + doubleDropChance + "%",
                        ChatColor.DARK_GREEN + "Haste Duration: " + ChatColor.GREEN + (hasteDuration / 20) + " seconds",
                        ChatColor.DARK_GREEN + "Spirit Spawn Chance: " + ChatColor.GREEN + spiritSpawnChance + "%"
                );

            default:
                return Arrays.asList(
                        ChatColor.GRAY + "Level: " + ChatColor.GREEN + level,
                        ChatColor.GRAY + "No stats available."
                );
        }
    }


}
