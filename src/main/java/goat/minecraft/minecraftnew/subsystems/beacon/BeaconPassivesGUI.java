package goat.minecraft.minecraftnew.subsystems.beacon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconPassivesGUI implements Listener {

    private final JavaPlugin plugin;
    private final ItemStack beacon;
    private final String guiTitle;

    // Store passive selections per player
    private static final Map<UUID, Map<String, Boolean>> playerPassives = new HashMap<>();

    // Persistence
    private static File passivesFile;
    private static FileConfiguration passivesConfig;

    public static void init(JavaPlugin plugin) {
        passivesFile = new File(plugin.getDataFolder(), "beacon_passives.yml");
        if (!passivesFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                passivesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        passivesConfig = YamlConfiguration.loadConfiguration(passivesFile);

        for (String key : passivesConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                ConfigurationSection section = passivesConfig.getConfigurationSection(key);
                if (section != null) {
                    Map<String, Boolean> map = new HashMap<>();
                    for (String passive : section.getKeys(false)) {
                        map.put(passive, section.getBoolean(passive, false));
                    }
                    playerPassives.put(uuid, map);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public BeaconPassivesGUI(JavaPlugin plugin, ItemStack beacon) {
        this.plugin = plugin;
        this.beacon = beacon;
        this.guiTitle = ChatColor.GREEN + "Beacon Passives";
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);
        
        // Get player's current passive selections
        Map<String, Boolean> passives = getPlayerPassives(player);
        
        // Create passive buttons
        ItemStack mendingButton = createPassiveButton(Material.GOLDEN_APPLE, 
            "Mending", "+1 row of hearts", passives.getOrDefault("mending", false));
        
        ItemStack sturdyButton = createPassiveButton(Material.SHIELD,
            "Sturdy", "+15% damage reduction, knockback resistance",
            passives.getOrDefault("sturdy", false));
        
        ItemStack swiftButton = createPassiveButton(Material.FEATHER, 
            "Swift", "+20% walk speed, -50% fall damage", passives.getOrDefault("swift", false));
        
        ItemStack powerButton = createPassiveButton(Material.DIAMOND_SWORD, 
            "Power", "+15% damage", passives.getOrDefault("power", false));
        
        // Place passive buttons in the GUI
        gui.setItem(10, mendingButton);
        gui.setItem(12, sturdyButton);
        gui.setItem(14, swiftButton);
        gui.setItem(16, powerButton);
        
        // Info button about single passive limitation
        ItemStack infoButton = createButton(Material.BOOK, 
            ChatColor.YELLOW + "Info", 
            "Only one passive can be",
            "active at a time.");
        gui.setItem(4, infoButton);
        
        // Back button
        ItemStack backButton = createButton(Material.ARROW, 
            ChatColor.RED + "Back", 
            "Return to main beacon menu");
        gui.setItem(22, backButton);
        
        // Fill empty slots with glass panes
        fillEmptySlots(gui);
        
        player.openInventory(gui);
    }

    private ItemStack createPassiveButton(Material material, String name, String description, boolean enabled) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        String displayName = (enabled ? ChatColor.GREEN : ChatColor.GRAY) + name;
        meta.setDisplayName(displayName);
        
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.YELLOW + description);
        lore.add("");
        if (enabled) {
            lore.add(ChatColor.GREEN + "✓ ENABLED");
            lore.add(ChatColor.GRAY + "Click to disable");
        } else {
            lore.add(ChatColor.RED + "✗ DISABLED");
            lore.add(ChatColor.GRAY + "Click to enable");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        loreList.add("");
        loreList.add(ChatColor.YELLOW + "Click to select!");
        
        meta.setLore(loreList);
        item.setItemMeta(meta);
        
        return item;
    }

    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);
        
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        
        if (!event.getView().getTitle().equals(guiTitle)) return;
        
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        
        switch (itemName) {
            case "Mending":
                togglePassive(player, "mending");
                openGUI(player);
                break;
                
            case "Sturdy":
                togglePassive(player, "sturdy");
                openGUI(player);
                break;
                
            case "Swift":
                togglePassive(player, "swift");
                openGUI(player);
                break;
                
            case "Power":
                togglePassive(player, "power");
                openGUI(player);
                break;
                
            case "Info":
                // Do nothing - just an informational button
                break;
                
            case "Back":
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                BeaconCharmGUI mainGUI = new BeaconCharmGUI(plugin, beacon);
                mainGUI.openGUI(player);
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!playerPassives.containsKey(uuid)) {
            loadPlayerPassives(uuid);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        savePlayerPassives(event.getPlayer().getUniqueId());
    }

    private void togglePassive(Player player, String passiveName) {
        Map<String, Boolean> passives = getPlayerPassives(player);
        boolean currentState = passives.getOrDefault(passiveName, false);
        
        if (currentState) {
            // Disable the current passive
            passives.put(passiveName, false);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(ChatColor.RED + "Disabled " + passiveName + " passive!");
        } else {
            // Disable all other passives first (only one passive allowed at a time)
            passives.clear();
            passives.put(passiveName, true);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.2f);
            player.sendMessage(ChatColor.GREEN + "Enabled " + passiveName + " passive!");
        }
        savePlayerPassives(player.getUniqueId());
    }

    private Map<String, Boolean> getPlayerPassives(Player player) {
        return playerPassives.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
    }

    private static void savePlayerPassives(UUID uuid) {
        if (passivesConfig == null) return;
        Map<String, Boolean> passives = playerPassives.get(uuid);
        if (passives == null) return;

        // Clear existing entries to prevent multiple passives being stored
        passivesConfig.set(uuid.toString(), null);

        for (Map.Entry<String, Boolean> entry : passives.entrySet()) {
            passivesConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
        }
        try {
            passivesConfig.save(passivesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadPlayerPassives(UUID uuid) {
        if (passivesConfig == null) return;
        ConfigurationSection section = passivesConfig.getConfigurationSection(uuid.toString());
        if (section != null) {
            Map<String, Boolean> map = new HashMap<>();
            for (String key : section.getKeys(false)) {
                map.put(key, section.getBoolean(key, false));
            }
            // Ensure only one passive is active
            String active = null;
            for (Map.Entry<String, Boolean> entry : map.entrySet()) {
                if (entry.getValue()) {
                    if (active == null) {
                        active = entry.getKey();
                    } else {
                        entry.setValue(false);
                    }
                }
            }
            playerPassives.put(uuid, map);
        }
    }

    public static void saveAllPassives() {
        if (passivesConfig == null) return;
        for (UUID uuid : playerPassives.keySet()) {
            Map<String, Boolean> passives = playerPassives.get(uuid);
            // Remove old entries
            passivesConfig.set(uuid.toString(), null);
            for (Map.Entry<String, Boolean> entry : passives.entrySet()) {
                passivesConfig.set(uuid.toString() + "." + entry.getKey(), entry.getValue());
            }
        }
        try {
            passivesConfig.save(passivesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if a player has a specific passive enabled
     * @param player The player to check
     * @param passiveName The passive name (mending, sturdy, swift, power)
     * @return True if the passive is enabled
     */
    public static boolean hasPassiveEnabled(Player player, String passiveName) {
        Map<String, Boolean> passives = playerPassives.get(player.getUniqueId());
        if (passives == null) return false;
        return passives.getOrDefault(passiveName, false);
    }

    /**
     * Check if a player has any beacon passives active and has a beacon in inventory
     * @param player The player to check
     * @return True if player has beacon with passives
     */
    public static boolean hasBeaconPassives(Player player) {
        // Check if player has a beacon in inventory
        boolean hasBeacon = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BEACON) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && 
                    ChatColor.stripColor(meta.getDisplayName()).equals("Beacon Charm")) {
                    hasBeacon = true;
                    break;
                }
            }
        }
        
        if (!hasBeacon) return false;
        
        // Check if player has any passives enabled
        Map<String, Boolean> passives = playerPassives.get(player.getUniqueId());
        if (passives == null) return false;
        
        return passives.values().stream().anyMatch(Boolean::booleanValue);
    }
}