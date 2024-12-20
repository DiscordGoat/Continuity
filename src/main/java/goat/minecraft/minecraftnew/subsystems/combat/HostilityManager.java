package goat.minecraft.minecraftnew.subsystems.combat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

public class HostilityManager implements Listener {

    private static HostilityManager instance; // Singleton instance
    private final JavaPlugin plugin;
    private final File hostilityFile;
    private final YamlConfiguration hostilityConfig;

    private static final String CONFIG_KEY = "playerTiers";

    /**
     * Private constructor to enforce Singleton pattern.
     */
    public HostilityManager(JavaPlugin plugin) {
        this.plugin = plugin;

        // Initialize the configuration file
        hostilityFile = new File(plugin.getDataFolder(), "hostility.yml");
        if (!hostilityFile.exists()) {
            try {
                hostilityFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[HostilityManager] Could not create hostility.yml!");
                e.printStackTrace();
            }
        }
        hostilityConfig = YamlConfiguration.loadConfiguration(hostilityFile);

        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("[HostilityManager] Initialized and registered.");
    }

    /**
     * Returns the singleton instance of HostilityManager.
     * If it doesn't exist, creates it.
     *
     * @param plugin The main plugin instance.
     * @return The HostilityManager instance.
     */
    public static synchronized HostilityManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new HostilityManager(plugin);
        }
        return instance;
    }

    /**
     * Method to get a player's difficulty tier.
     * Defaults to 1 if not set.
     *
     * @param player The player whose tier is to be retrieved.
     * @return The difficulty tier as an integer.
     */
    public int getPlayerDifficultyTier(Player player) {
        UUID uuid = player.getUniqueId();
        String path = CONFIG_KEY + "." + uuid.toString();
        return hostilityConfig.getInt(path, 1); // Default tier is 1
    }

    /**
     * Saves the current configuration to the file.
     */
    public void saveConfig() {
        try {
            hostilityConfig.save(hostilityFile);
            plugin.getLogger().info("[HostilityManager] Configuration saved.");
        } catch (IOException e) {
            plugin.getLogger().severe("[HostilityManager] Could not save hostility.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Sets the player's difficulty tier and saves it to the configuration.
     *
     * @param player The player whose tier is to be set.
     * @param tier   The tier to set.
     */
    public void setPlayerTier(Player player, int tier) {
        UUID uuid = player.getUniqueId();
        String path = CONFIG_KEY + "." + uuid.toString();
        hostilityConfig.set(path, tier);
        saveConfig();
    }

    /**
     * Handles clicks within the Hostility GUI.
     *
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_RED + "Select Hostility Tier")) {
            event.setCancelled(true); // Prevent taking items

            if (!(event.getWhoClicked() instanceof Player)) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // Handle "Close" button
            if (clickedItem.getType() == Material.BARRIER) {
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Hostility selection closed.");
                return;
            }

            // Handle tier selection
            if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = ChatColor.stripColor(meta.getDisplayName());
                    if (displayName.startsWith("Tier ")) {
                        try {
                            int tier = Integer.parseInt(displayName.substring(5).trim());
                            if (tier >= 1 && tier <= 10) {
                                setPlayerTier(player, tier);
                                player.sendMessage(ChatColor.GREEN + "Your hostility tier has been set to Tier " + tier + "!");
                                player.closeInventory();
                            } else {
                                player.sendMessage(ChatColor.RED + "Invalid tier selected.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid tier format.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Inner class to handle the /hostility command.
     * Note: Do not register the command here.
     * Register it in your main plugin class like this:
     * getCommand("hostility").setExecutor(HostilityManager.getInstance(this).new HostilityCommand());
     */
    public class HostilityCommand implements org.bukkit.command.CommandExecutor {

        @Override
        public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;
            openHostilityGUI(player);
            return true;
        }

        /**
         * Opens the improved Hostility GUI for the player.
         *
         * @param player The player to open the GUI for.
         */
        public void openHostilityGUI(Player player) {
            // Create a 6-row (54-slot) inventory GUI
            Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Select Hostility Tier");

            // Add decorative borders
            ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta borderMeta = borderItem.getItemMeta();
            if (borderMeta != null) {
                borderMeta.setDisplayName(ChatColor.GRAY + "Decoration");
                borderItem.setItemMeta(borderMeta);
            }
            for (int i = 0; i < 54; i++) {
                if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                    gui.setItem(i, borderItem); // Place borders in top, bottom, and side slots
                }
            }

            // Add tier items (10 tiers distributed evenly)
            int[] tierSlots = {11, 13, 15, 20, 22, 24, 29, 31, 33, 40}; // Centered slots for tiers
            for (int i = 0; i < 10; i++) {
                ItemStack tierItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                ItemMeta meta = tierItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.BOLD + "Tier " + (i + 1));
                    meta.setLore(Collections.singletonList(ChatColor.GRAY + "Click to select Tier " + (i + 1)));
                    tierItem.setItemMeta(meta);
                }
                gui.setItem(tierSlots[i], tierItem); // Place the tier item in the designated slot
            }

            // Add "Close" button at the bottom center
            ItemStack closeItem = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = closeItem.getItemMeta();
            if (closeMeta != null) {
                closeMeta.setDisplayName(ChatColor.RED + "Close");
                closeItem.setItemMeta(closeMeta);
            }
            gui.setItem(49, closeItem); // Bottom center slot

            // Open the inventory for the player
            player.openInventory(gui);
        }
    }
}
