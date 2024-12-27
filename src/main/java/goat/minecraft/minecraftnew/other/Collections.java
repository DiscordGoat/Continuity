package goat.minecraft.minecraftnew.other;

import goat.minecraft.minecraftnew.utils.ItemRegistry;
import goat.minecraft.minecraftnew.other.ItemDisplayManager.ItemDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Material;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Collections implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final ItemDisplayManager displayManager;

    // We'll store our "collections" data here
    private final List<CollectionData> collections = new ArrayList<>();

    // A quick data model to hold info about each collection
    public static class CollectionData {
        public String name;
        public ItemStack iconItem; // Formerly iconMaterial
        public List<ItemStack> requiredItems;
        public List<ItemStack> rewardItems;
        public String rewardMessage;
        public Set<UUID> claimedPlayers = new HashSet<>();

        public CollectionData(String name,
                              ItemStack iconItem,
                              List<ItemStack> requiredItems,
                              List<ItemStack> rewardItems,
                              String rewardMessage) {
            this.name = name;
            this.iconItem = iconItem;
            this.requiredItems = requiredItems;
            this.rewardItems = rewardItems;
            this.rewardMessage = rewardMessage;
        }
    }

    private File configFile;
    private YamlConfiguration config;

    public Collections(JavaPlugin plugin, ItemDisplayManager displayManager) {
        this.plugin = plugin;
        this.displayManager = displayManager;

        // For instance, store data in "collections.yml"
        configFile = new File(plugin.getDataFolder(), "collections.yml");

        loadCollections();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     *  Main entry point to load all data from the configFile,
     *  or create a new file if it doesn't exist.
     */
    private void loadCollections() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("[Collections] Could not create collections.yml");
                e.printStackTrace();
            }
            config = YamlConfiguration.loadConfiguration(configFile);

            // Hardcode some defaults if no config
            // Example: "Swords" collection that uses a custom item from ItemRegistry
            Map<String, Object> swords = new HashMap<>();
            swords.put("name", "All Swords");
            ItemStack iconSword = new ItemStack(Material.DIAMOND_SWORD);
            swords.put("iconItem", iconSword.serialize());

            // For the required items, demonstrate mixing a custom item plus normal swords
            List<Map<String, Object>> defaultRequiredList = new ArrayList<>();
            // Add custom item
            ItemStack forbiddenBook = ItemRegistry.getForbiddenBook(); // Example custom item
            //defaultRequiredList.add(forbiddenBook.serialize());
            defaultRequiredList.add(new ItemStack(Material.WOODEN_SWORD).serialize());
            defaultRequiredList.add(new ItemStack(Material.STONE_SWORD).serialize());
            defaultRequiredList.add(new ItemStack(Material.IRON_SWORD).serialize());
            defaultRequiredList.add(new ItemStack(Material.GOLDEN_SWORD).serialize());
            defaultRequiredList.add(new ItemStack(Material.DIAMOND_SWORD).serialize());
            swords.put("requiredItems", defaultRequiredList);

            // For rewards
            List<Map<String, Object>> defaultRewardList = new ArrayList<>();
            defaultRewardList.add(new ItemStack(Material.EMERALD, 64).serialize());
            defaultRewardList.add(ItemRegistry.getExperienceArtifact().serialize());
            swords.put("rewardItems", defaultRewardList);

            swords.put("rewardMessage", "&aYou have collected all swords!");
            swords.put("claimed", new ArrayList<>()); // none claimed yet

            config.set("collections.default_swords", swords);

            // Another example: "Apples"
            Map<String, Object> apples = new HashMap<>();
            apples.put("name", "Apple Mania");

            // Apple icon
            ItemStack appleIcon = new ItemStack(Material.APPLE);
            apples.put("iconItem", appleIcon.serialize());

            // Required items: golden apple, enchanted golden apple
            List<Map<String, Object>> appleRequiredList = new ArrayList<>();
            appleRequiredList.add(new ItemStack(Material.GOLDEN_APPLE).serialize());
            appleRequiredList.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE).serialize());
            apples.put("requiredItems", appleRequiredList);

            // Reward items: gold ingot, xp bottle
            List<Map<String, Object>> appleRewardList = new ArrayList<>();
            appleRewardList.add(new ItemStack(Material.GOLD_INGOT, 2).serialize());
            appleRewardList.add(new ItemStack(Material.EXPERIENCE_BOTTLE, 10).serialize());
            apples.put("rewardItems", appleRewardList);

            apples.put("rewardMessage", "&aYou have collected all the apples!");
            apples.put("claimed", new ArrayList<>());

            config.set("collections.default_apples", apples);

            saveConfig();
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }

        // Load from config into memory
        loadIntoMemory();
    }

    /**
     * Reads raw config data and constructs our in-memory CollectionData objects.
     */
    private void loadIntoMemory() {
        collections.clear();
        if (config.contains("collections")) {
            ConfigurationSection section = config.getConfigurationSection("collections");
            if (section == null) return;

            for (String key : section.getKeys(false)) {
                ConfigurationSection collSec = section.getConfigurationSection(key);
                if (collSec == null) continue;

                String name = collSec.getString("name", "Unnamed Collection");

                // Icon item
                Map<String, Object> iconMap = collSec.getConfigurationSection("iconItem") != null
                        ? collSec.getConfigurationSection("iconItem").getValues(true)
                        : null;
                ItemStack iconItem;
                if (iconMap != null && !iconMap.isEmpty()) {
                    iconItem = ItemStack.deserialize(iconMap);
                } else {
                    // fallback
                    iconItem = new ItemStack(Material.CHEST);
                }

                // Required items
                List<Map<?, ?>> requiredMaps = collSec.getMapList("requiredItems");
                List<ItemStack> requiredItems = requiredMaps.stream()
                        .map(m -> ItemStack.deserialize((Map<String, Object>) m))
                        .collect(Collectors.toList());

                // Reward items
                List<Map<?, ?>> rewardMaps = collSec.getMapList("rewardItems");
                List<ItemStack> rewardItems = rewardMaps.stream()
                        .map(m -> ItemStack.deserialize((Map<String, Object>) m))
                        .collect(Collectors.toList());

                String rewardMsg = ChatColor.translateAlternateColorCodes('&',
                        collSec.getString("rewardMessage", "&aYou completed the collection!"));

                List<String> claimed = collSec.getStringList("claimed");
                Set<UUID> claimedPlayers = new HashSet<>();
                for (String uuidStr : claimed) {
                    try {
                        claimedPlayers.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException ignored) {}
                }

                // Build object
                CollectionData cData = new CollectionData(
                        name,
                        iconItem,
                        requiredItems,
                        rewardItems,
                        rewardMsg
                );
                cData.claimedPlayers = claimedPlayers;

                collections.add(cData);
            }
        }
    }

    /**
     * Saves current in-memory data into the config file with full serialization.
     */
    private void saveCollections() {
        // Clear existing
        config.set("collections", null);

        for (int i = 0; i < collections.size(); i++) {
            CollectionData c = collections.get(i);
            String path = "collections.coll_" + i;

            config.set(path + ".name", c.name);

            // Icon item
            config.createSection(path + ".iconItem", c.iconItem.serialize());

            // Required items
            List<Map<String, Object>> requiredList = new ArrayList<>();
            for (ItemStack is : c.requiredItems) {
                requiredList.add(is.serialize());
            }
            config.set(path + ".requiredItems", requiredList);

            // Reward items
            List<Map<String, Object>> rewardList = new ArrayList<>();
            for (ItemStack is : c.rewardItems) {
                rewardList.add(is.serialize());
            }
            config.set(path + ".rewardItems", rewardList);

            config.set(path + ".rewardMessage", c.rewardMessage);

            // Save claimed players
            List<String> claimedStrs = new ArrayList<>();
            for (UUID u : c.claimedPlayers) {
                claimedStrs.add(u.toString());
            }
            config.set(path + ".claimed", claimedStrs);
        }

        saveConfig();
    }

    /**
     * Helper to save the config file.
     */
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Collections] Could not save collections.yml");
            e.printStackTrace();
        }
    }

    // Register this in onEnable (or plugin startup) with:
    // getCommand("collection").setExecutor(new Collections(plugin, displayManagerInstance));
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run /collection");
            return true;
        }
        Player player = (Player) sender;

        openCollectionsGUI(player);
        return true;
    }

    /**
     * Create a GUI to display each collection, along with user progress.
     */
    private void openCollectionsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Your Collections");

        // Loop each collection, create an icon
        for (int i = 0; i < collections.size() && i < 54; i++) {
            CollectionData cData = collections.get(i);

            ItemStack icon = cData.iconItem.clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + cData.name);

                // Build lore
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Required items:");

                // Grab all displays that belong to the player
                List<ItemDisplay> userDisplays = displayManager.getDisplaysByPlayer(player.getUniqueId());

                // Convert user displays to a set of Materials the player has
                // (still just Material-based logic)
                Set<Material> itemsPlayerHas = new HashSet<>();
                for (ItemDisplay d : userDisplays) {
                    if (d.storedItem != null) {
                        itemsPlayerHas.add(d.storedItem.getType());
                    }
                }

                boolean complete = true;
                for (ItemStack req : cData.requiredItems) {
                    // Get custom name if available, else fallback to Material name
                    ItemMeta reqMeta = req.getItemMeta();
                    String reqName = (reqMeta != null && reqMeta.hasDisplayName())
                            ? reqMeta.getDisplayName()
                            : req.getType().name(); // Or req.getType().toString()

                    if (itemsPlayerHas.contains(req.getType())) {
                        lore.add(ChatColor.GREEN + " - " + reqName);
                    } else {
                        lore.add(ChatColor.RED + " - " + reqName);
                        complete = false;
                    }
                }

                if (complete) {
                    if (cData.claimedPlayers.contains(player.getUniqueId())) {
                        lore.add(ChatColor.YELLOW + "[Reward Already Claimed]");
                    } else {
                        lore.add(ChatColor.GREEN + "[Click to Claim Reward!]");
                    }
                } else {
                    lore.add(ChatColor.RED + "[Incomplete]");
                }

                meta.setLore(lore);
                icon.setItemMeta(meta);
            }
            inv.setItem(i, icon);
        }

        player.openInventory(inv);
    }

    /**
     * Handles clicks in the "Your Collections" GUI. Checks if the collection is complete
     * and, if so, grants the reward (unless already claimed).
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Make sure it's our Collections GUI
        if (!ChatColor.stripColor(event.getView().getTitle()).equals("Your Collections")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;
        if (slot >= collections.size()) return;

        CollectionData cData = collections.get(slot);

        // Check completion
        List<ItemDisplay> userDisplays = displayManager.getDisplaysByPlayer(player.getUniqueId());
        Set<Material> itemsPlayerHas = new HashSet<>();
        for (ItemDisplay d : userDisplays) {
            if (d.storedItem != null) {
                itemsPlayerHas.add(d.storedItem.getType());
            }
        }

        boolean complete = true;
        for (ItemStack req : cData.requiredItems) {
            if (!itemsPlayerHas.contains(req.getType())) {
                complete = false;
                break;
            }
        }

        if (!complete) {
            player.sendMessage(ChatColor.RED + "You haven't collected all items yet!");
            return;
        }

        // Check if already claimed
        if (cData.claimedPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You've already claimed this reward!");
            return;
        }

        // Grant the reward
        for (ItemStack reward : cData.rewardItems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(reward.clone());
            // If there's leftover, drop it at the player's location
            for (ItemStack l : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), l);
            }
        }

        String msg = ChatColor.translateAlternateColorCodes('&', cData.rewardMessage);
        player.sendMessage(msg);

        // Mark as claimed and save
        cData.claimedPlayers.add(player.getUniqueId());
        saveCollections();

        // Refresh the GUI
        openCollectionsGUI(player);
    }

    /*
     * If you want to truly compare display names/lore rather than just Material,
     * you must expand your item-check logic in onInventoryClick.
     * For example:
     *
     * private boolean hasRequiredItem(List<ItemDisplay> userDisplays, ItemStack required) {
     *     // return userDisplays.stream().anyMatch(d -> itemsAreEquivalent(d.storedItem, required));
     * }
     *
     * private boolean itemsAreEquivalent(ItemStack a, ItemStack b) {
     *     if (a == null || b == null) return false;
     *     if (a.getType() != b.getType()) return false;
     *     // Also compare display name, lore, enchantments, etc. for perfect matching.
     *     ...
     *     return true;
     * }
     */
}
