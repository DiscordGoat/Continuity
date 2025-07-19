package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.subsystems.brewing.PotionBrewingSubsystem;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionEffectPreferences;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionBrewingSubsystem.PotionRecipe;
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
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI command allowing players to toggle custom potion effect benefits.
 */
public class TogglePotionEffectsCommand implements CommandExecutor, Listener {

    private static final String GUI_TITLE = ChatColor.DARK_PURPLE + "Custom Potion Effects";
    private final JavaPlugin plugin;

    public TogglePotionEffectsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
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
        List<PotionRecipe> recipes = PotionBrewingSubsystem.getPotionRecipes();
        int size = ((recipes.size() / 9) + 1) * 9;
        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);
        int slot = 0;
        for (PotionRecipe recipe : recipes) {
            String name = recipe.getName();
            boolean enabled = PotionEffectPreferences.isEnabled(player, name);

            ItemStack item = new ItemStack(Material.POTION);
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            meta.setDisplayName((enabled ? ChatColor.GREEN : ChatColor.RED) + name);
            meta.setColor(recipe.getFinalColor());
            meta.setBasePotionData(new PotionData(PotionType.WATER));

            List<String> lore = new ArrayList<>();
            for (String line : recipe.getEffectLore()) {
                lore.add(ChatColor.GRAY + line);
            }
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
        PotionEffectPreferences.toggle(player, name);
        openGUI(player);
    }
}

