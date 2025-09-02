package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentPreferences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * GUI command allowing players to toggle custom enchantment effects.
 */
public class ToggleCustomEnchantmentsCommand implements CommandExecutor, Listener {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Custom Enchantments";
    private final JavaPlugin plugin;

    // Basic descriptions for known enchantments
    private final Map<String, List<String>> descriptions = new HashMap<>();

    public ToggleCustomEnchantmentsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        populateDescriptions();
    }

    private void populateDescriptions() {
        descriptions.put("Alchemy", Arrays.asList(
                "4% * level chance to upgrade ores",
                "into their block form.",
                "Costs 1 durability per use."));
        descriptions.put("Feed", Arrays.asList(
                "Chance to restore hunger when",
                "damaging a mob.",
                "Consumes 1 durability."));
        descriptions.put("Rappel", Arrays.asList(
                "Right click to teleport to the",
                "highest block above you.",
                "Costs durability equal to level."));
        descriptions.put("Stun", Arrays.asList(
                "Arrows apply extreme slowness",
                "for 10 seconds.",
                "Costs 1 durability."));
        descriptions.put("Lethal Reaction", Arrays.asList(
                "Crossbow arrows become fireballs",
                "with power based on level.",
                "Costs durability."));
        descriptions.put("Aspect of the Journey", Arrays.asList(
                "Sneak right click to dash forward",
                "6 blocks. Overuse drains hunger",
                "or deals damage.",
                "Consumes durability."));
        descriptions.put("Preservation", Arrays.asList(
                "Prevents items from breaking",
                "and stores them safely."));
        descriptions.put("Composter", Arrays.asList(
                "4% * level chance to convert",
                "excess materials into Organic Soil."));
        descriptions.put("Forge", Arrays.asList(
                "20% * level chance to auto-smelt",
                "broken blocks."));
        descriptions.put("Accelerate", Arrays.asList(
                "Adds deterioration stacks equal",
                "to level * 5 when hitting mobs."));
        descriptions.put("Velocity", Arrays.asList(
                "Projectiles fly 25% faster per",
                "enchantment level."));
        descriptions.put("Defenestration", Arrays.asList(
                "Arrows shatter glass for a short",
                "time on impact."));
        descriptions.put("Merit", Arrays.asList(
                "10% chance to earn a merit point",
                "when mining diamond ore."));
        descriptions.put("Cleaver", Arrays.asList(
                "Small chance to decapitate mobs",
                "dropping their head.",
                "Consumes durability."));
        descriptions.put("Experience", Arrays.asList(
                "Drops extra XP when killing mobs,",
                "amount scales with level.",
                "Costs 1 durability."));
        descriptions.put("Water Aspect", Arrays.asList(
                "Deals bonus damage to certain",
                "mobs like guardians or endermen."));
        descriptions.put("Cornfield", Arrays.asList(
                "2% * level chance to",
                "double-count wheat (max 100%)."));
        descriptions.put("What's Up Doc", Arrays.asList(
                "2% * level chance to",
                "double-count carrots (max 100%)."));
        descriptions.put("Venerate", Arrays.asList(
                "2% * level chance to",
                "double-count beetroot (max 100%)."));
        descriptions.put("Legend", Arrays.asList(
                "2% * level chance to",
                "double-count potatoes (max 100%)."));
        descriptions.put("Clean Cut", Arrays.asList(
                "2% * level chance to",
                "double-count melons (max 100%)."));
        descriptions.put("Gourd", Arrays.asList(
                "2% * level chance to",
                "double-count pumpkins (max 100%)."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        openGUI(player);
        return true;
    }

    private void openGUI(Player player) {
        Map<String, Integer> enchants = CustomEnchantmentManager.getRegisteredEnchantmentMaxLevels();
        int size = ((enchants.size() / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);
        int slot = 0;
        for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
            String name = entry.getKey();
            int max = entry.getValue();
            boolean enabled = CustomEnchantmentPreferences.isEnabled(player, name);
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            List<String> desc = descriptions.getOrDefault(name, Collections.singletonList("No description available."));
            for (String line : desc) {
                lore.add(ChatColor.GRAY + line);
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "Max Level: " + max);
            lore.add("");
            if (enabled) {
                lore.add(ChatColor.GREEN + "✓ ENABLED");
                lore.add(ChatColor.GRAY + "Left click to disable");
            } else {
                lore.add(ChatColor.RED + "✗ DISABLED");
                lore.add(ChatColor.GRAY + "Left click to enable");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot++, item);
        }
        player.openInventory(gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (name.isEmpty()) return;
        CustomEnchantmentPreferences.toggle(player, name);
        openGUI(player);
    }
}
