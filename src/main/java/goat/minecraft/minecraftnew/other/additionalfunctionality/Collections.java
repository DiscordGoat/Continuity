package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.other.qol.ItemDisplayManager;
import goat.minecraft.minecraftnew.other.recipes.LockedRecipeManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.other.qol.ItemDisplayManager.ItemDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
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
    private LockedRecipeManager lockedRecipeManager;

    /** Let the main plugin inject the locked recipe manager. */
    public void setLockedRecipeManager(LockedRecipeManager lockedRecipeManager) {
        this.lockedRecipeManager = lockedRecipeManager;
    }
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
        public List<String> recipeKeysToUnlock = new ArrayList<>();

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
    public List<CollectionData> getAllCollections() {
        return this.collections;
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
// NEW example: "Redstone Collection"
            Map<String, Object> redstone = new HashMap<>();
            redstone.put("name", "Redstone Collection");

            // Use a Redstone Block as the icon
            ItemStack redstoneIcon = new ItemStack(Material.REDSTONE_BLOCK);
            redstone.put("iconItem", redstoneIcon.serialize());

            // Required items: 1 Redstone Block + 1 Redstone Dust
            List<Map<String, Object>> redstoneRequiredList = new ArrayList<>();
            redstoneRequiredList.add(new ItemStack(Material.REDSTONE_BLOCK).serialize());
            redstoneRequiredList.add(new ItemStack(Material.REDSTONE).serialize());
            redstoneRequiredList.add(new ItemStack(Material.REDSTONE_TORCH).serialize());
            redstoneRequiredList.add(new ItemStack(Material.REDSTONE_LAMP).serialize());
            redstoneRequiredList.add(new ItemStack(Material.REPEATER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.OBSERVER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.PISTON).serialize());
            redstoneRequiredList.add(new ItemStack(Material.COMPARATOR).serialize());
            redstoneRequiredList.add(new ItemStack(Material.NOTE_BLOCK).serialize());
            redstoneRequiredList.add(new ItemStack(Material.LEVER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.HOPPER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.DISPENSER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.TNT).serialize());
            redstoneRequiredList.add(new ItemStack(Material.DROPPER).serialize());
            redstoneRequiredList.add(new ItemStack(Material.DAYLIGHT_DETECTOR).serialize());
            redstone.put("requiredItems", redstoneRequiredList);

            // Reward items: 1 Redstone Torch (example)
            List<Map<String, Object>> redstoneRewardList = new ArrayList<>();
            redstoneRewardList.add(new ItemStack(Material.REDSTONE_BLOCK, 64).serialize());
            redstoneRewardList.add(new ItemStack(Material.REDSTONE, 64*4).serialize());
            redstoneRewardList.add(new ItemStack(Material.EMERALD_BLOCK, 16).serialize());
            redstone.put("rewardItems", redstoneRewardList);

            redstone.put("rewardMessage", "&aYou have unlocked Engineering Degree Recipe! Use /recipes to view recipes");
            redstone.put("claimed", new ArrayList<>()); // no one has claimed yet

            // If you're using recipe unlocking, list the recipe keys to unlock
            // (assuming "minecraftnew:engineering_profession" is your recipe key)
            redstone.put("recipeKeysToUnlock", new ArrayList<>());

            config.set("collections.default_redstone", redstone);


            // Another example: "Shells"
            Map<String, Object> shells = new HashMap<>();
            shells.put("name", "Turtle Shells");

            // Apple icon
            ItemStack shellsIcon = new ItemStack(Material.TURTLE_HELMET);
            shells.put("iconItem", shellsIcon.serialize());

            // Required items: golden apple, enchanted golden apple
            List<Map<String, Object>> shellsRequiredList = new ArrayList<>();
            shellsRequiredList.add(ItemRegistry.getShallowShell().serialize());
            shellsRequiredList.add(ItemRegistry.getShell().serialize());
            shellsRequiredList.add(ItemRegistry.getDeepShell().serialize());
            shellsRequiredList.add(ItemRegistry.getAbyssalShell().serialize());

            shells.put("requiredItems", shellsRequiredList);

            // Reward items: gold ingot, xp bottle
            List<Map<String, Object>> shellsRewardList = new ArrayList<>();
            shellsRewardList.add(new ItemStack(Material.EMERALD_BLOCK, 16).serialize());
            ItemStack shellStack = ItemRegistry.getShell().clone(); // Clone to avoid modifying the original
            shellStack.setAmount(64);
            shellsRewardList.add(shellStack.serialize());
            shells.put("rewardItems", shellsRewardList);

            shells.put("rewardMessage", "&aYou have collected all the shells!");
            shells.put("claimed", new ArrayList<>());

            config.set("collections.default_shells", shells);

            saveConfig();
// Another example: "Skulls"
            Map<String, Object> skulls = new HashMap<>();
            skulls.put("name", "Collector of Skulls");

// Skull icon
            ItemStack skullsIcon = new ItemStack(Material.WITHER_SKELETON_SKULL);
            skulls.put("iconItem", skullsIcon.serialize());

// Required items: Different types of skulls
            List<Map<String, Object>> skullsRequiredList = new ArrayList<>();
            skullsRequiredList.add(new ItemStack(Material.WITHER_SKELETON_SKULL).serialize());
            skullsRequiredList.add(new ItemStack(Material.SKELETON_SKULL).serialize());
            skullsRequiredList.add(new ItemStack(Material.ZOMBIE_HEAD).serialize());
            skullsRequiredList.add(new ItemStack(Material.CREEPER_HEAD).serialize());

            skulls.put("requiredItems", skullsRequiredList);

// Reward items: Diamonds and a stack of skeleton skulls
            List<Map<String, Object>> skullsRewardList = new ArrayList<>();
            skullsRewardList.add(new ItemStack(Material.EMERALD_BLOCK, 16).serialize());
            skullsRewardList.add(ItemRegistry.getUndeadDrop().serialize());
            skullsRewardList.add(ItemRegistry.getSpiderDrop().serialize());
            skullsRewardList.add(ItemRegistry.getSkeletonDrop().serialize());
            skullsRewardList.add(ItemRegistry.getBlazeDrop().serialize());
            skullsRewardList.add(ItemRegistry.getCreeperDrop().serialize());
            skullsRewardList.add(ItemRegistry.getDrownedDrop().serialize());
            skulls.put("rewardItems", skullsRewardList);

            skulls.put("rewardMessage", "&aYou have unlocked Custom Disc Recipe! Use /recipes to view recipes");
            skulls.put("claimed", new ArrayList<>());

            skulls.put("recipeKeysToUnlock", Arrays.asList("minecraftnew:custom_music_disc_recipe"));

            config.set("collections.default_skulls", skulls);

            saveConfig();


            // Another example: "Apples"
            Map<String, Object> apples = new HashMap<>();
            apples.put("name", "Apple Mania");

            // Apple icon
            ItemStack appleIcon = new ItemStack(Material.APPLE);
            apples.put("iconItem", appleIcon.serialize());

            // Required items: golden apple, enchanted golden apple
            List<Map<String, Object>> appleRequiredList = new ArrayList<>();
            appleRequiredList.add(new ItemStack(Material.APPLE).serialize());
            appleRequiredList.add(new ItemStack(Material.GOLDEN_APPLE).serialize());
            appleRequiredList.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE).serialize());
            apples.put("requiredItems", appleRequiredList);

            // Reward items: gold ingot, xp bottle
            List<Map<String, Object>> appleRewardList = new ArrayList<>();
            appleRewardList.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4).serialize());
            appleRewardList.add(new ItemStack(Material.GOLDEN_CARROT, 64).serialize());
            apples.put("rewardItems", appleRewardList);

            apples.put("rewardMessage", "&aYou have collected all the apples! Unlocked Enchanted Golden Apple recipe!");
            apples.put("claimed", new ArrayList<>());

            apples.put("recipeKeysToUnlock", Arrays.asList("minecraftnew:notch_apple_recipe"));

            config.set("collections.default_apples", apples);

            saveConfig();





            //minerals
            Map<String, Object> minerals = new HashMap<>();
            minerals.put("name", "Minerals");

            // Apple icon
            ItemStack mineralsIcon = new ItemStack(Material.DIAMOND);
            minerals.put("iconItem", mineralsIcon.serialize());

            // Required items: golden apple, enchanted golden apple
            List<Map<String, Object>> mineralsRequiredList = new ArrayList<>();
            mineralsRequiredList.add(new ItemStack(Material.COAL).serialize());
            mineralsRequiredList.add(new ItemStack(Material.COPPER_INGOT).serialize());
            mineralsRequiredList.add(new ItemStack(Material.IRON_INGOT).serialize());
            mineralsRequiredList.add(new ItemStack(Material.GOLD_INGOT).serialize());
            mineralsRequiredList.add(new ItemStack(Material.LAPIS_LAZULI).serialize());
            mineralsRequiredList.add(new ItemStack(Material.REDSTONE).serialize());
            mineralsRequiredList.add(new ItemStack(Material.DIAMOND).serialize());
            mineralsRequiredList.add(new ItemStack(Material.EMERALD).serialize());


            minerals.put("requiredItems", mineralsRequiredList);

            // Reward items: gold ingot, xp bottle
            List<Map<String, Object>> mineralsRewardList = new ArrayList<>();
            mineralsRewardList.add(new ItemStack(Material.NETHERITE_INGOT, 1).serialize());
            minerals.put("rewardItems", mineralsRewardList);

            minerals.put("rewardMessage", "&aYou have collected all the minerals! Unlocked Pet Training recipe!");
            minerals.put("claimed", new ArrayList<>());

            minerals.put("recipeKeysToUnlock", Arrays.asList("minecraftnew:pet_training_recipe"));

            config.set("collections.default_minterals", minerals);

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
        if (lockedRecipeManager != null) {
            for (String recipeKeyString : cData.recipeKeysToUnlock) {
                // Convert the string to a NamespacedKey
                NamespacedKey recipeKey = NamespacedKey.fromString(recipeKeyString, plugin);
                lockedRecipeManager.discoverRecipeForPlayer(player, recipeKey);
            }
        }
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
