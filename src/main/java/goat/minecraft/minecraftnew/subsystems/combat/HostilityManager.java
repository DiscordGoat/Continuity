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

    public static synchronized HostilityManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new HostilityManager(plugin);
        }
        return instance;
    }

    /**
     * Returns the current HostilityManager instance without creating a new one.
     * Used during shutdown to avoid registering events when the plugin is
     * already disabled.
     *
     * @return the existing HostilityManager instance, or null if not initialized
     */
    public static synchronized HostilityManager getExistingInstance() {
        return instance;
    }

    public int getPlayerDifficultyTier(Player player) {
        if(player == null){
            return 1;
        }
        UUID uuid = player.getUniqueId();
        String path = CONFIG_KEY + "." + uuid.toString();
        return hostilityConfig.getInt(path, 1); // Default tier is 1
    }

    public void saveConfig() {
        try {
            hostilityConfig.save(hostilityFile);
            plugin.getLogger().info("[HostilityManager] Configuration saved.");
        } catch (IOException e) {
            plugin.getLogger().severe("[HostilityManager] Could not save hostility.yml!");
            e.printStackTrace();
        }
    }

    public void setPlayerTier(Player player, int tier) {
        UUID uuid = player.getUniqueId();
        String path = CONFIG_KEY + "." + uuid.toString();
        hostilityConfig.set(path, tier);
        saveConfig();
    }

    /**
     * Checks if the given tier is unlocked for a player's current level.
     * Tier 1 = level >= 0
     * Tier 2 = level >= 10
     * Tier 3 = level >= 20
     * ...
     * Tier 10 = level >= 90
     */
    private boolean isTierUnlocked(Player player, int tier) {
        int requiredLevel = (tier - 1) * 10;
        return player.getLevel() >= requiredLevel;
    }

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
            if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE
                    || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String displayName = ChatColor.stripColor(meta.getDisplayName());
                    if (displayName.startsWith("Tier ")) {
                        try {
                            int tier = Integer.parseInt(displayName.substring(5).trim());
                            // Check if tier is unlocked
                            if (!isTierUnlocked(player, tier)) {
                                player.sendMessage(ChatColor.RED + "You haven't unlocked Tier " + tier + " yet!");
                                player.closeInventory();
                                return;
                            }
                            // If it is unlocked, set it
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
         * Opens the improved Hostility GUI for the player,
         * showing only the tiers they've unlocked in red,
         * and locked tiers in gray.
         */
        public void openHostilityGUI(Player player) {
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
                    gui.setItem(i, borderItem);
                }
            }

            // Slots where tiers go
            int[] tierSlots = {11, 13, 15, 20, 22, 24, 29, 31, 33, 40};
            // Tier 1 -> 10
            for (int i = 0; i < 10; i++) {
                int tierNum = i + 1;
                Material mat;
                String display;
                String lore;

                if (isTierUnlocked(player, tierNum)) {
                    mat = Material.RED_STAINED_GLASS_PANE;
                    display = ChatColor.BOLD + "Tier " + tierNum;
                    lore = ChatColor.GRAY + "Click to select Tier " + tierNum;
                } else {
                    // locked
                    mat = Material.GRAY_STAINED_GLASS_PANE;
                    display = ChatColor.BOLD + "Tier " + tierNum;
                    lore = ChatColor.RED + "Locked until level " + ((tierNum - 1) * 10);
                }

                ItemStack tierItem = new ItemStack(mat);
                ItemMeta meta = tierItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(display);
                    meta.setLore(Collections.singletonList(lore));
                    tierItem.setItemMeta(meta);
                }
                gui.setItem(tierSlots[i], tierItem);
            }

            // Add "Close" button at the bottom center
            ItemStack closeItem = new ItemStack(Material.BARRIER);
            ItemMeta closeMeta = closeItem.getItemMeta();
            if (closeMeta != null) {
                closeMeta.setDisplayName(ChatColor.RED + "Close");
                closeItem.setItemMeta(closeMeta);
            }
            gui.setItem(49, closeItem);

            player.openInventory(gui);
        }
    }
}
