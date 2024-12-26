package goat.minecraft.minecraftnew.utils.chocolatemisc;

import goat.minecraft.minecraftnew.utils.chocolatemisc.ItemDisplayManager;
import goat.minecraft.minecraftnew.utils.chocolatemisc.ItemDisplayManager.ItemDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Collections implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final ItemDisplayManager displayManager;

    // We'll store our "collections" data here
    private final List<CollectionData> collections = new ArrayList<>();

    // A quick data model to hold info about each collection
    public static class CollectionData {
        public String name;
        public ItemStack iconMaterial;
        public List<ItemStack> requiredItems; // items the player must place
        public List<ItemStack> rewardItems;   // items given on completion
        public String rewardMessage;
        public Set<UUID> claimedPlayers = new HashSet<>(); // track who claimed

        public CollectionData(String name, ItemStack iconMaterial, List<ItemStack> requiredItems,
                              List<ItemStack> rewardItems, String rewardMessage) {
            this.name = name;
            this.iconMaterial = iconMaterial;
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
            // E.g. "Swords" collection
            Map<String, Object> swords = new HashMap<>();
            swords.put("name", "All Swords");
            swords.put("iconMaterial", Material.DIAMOND_SWORD.toString());
            swords.put("requiredItems", Arrays.asList(
                    Material.WOODEN_SWORD.toString(),
                    Material.STONE_SWORD.toString(),
                    Material.IRON_SWORD.toString(),
                    Material.GOLDEN_SWORD.toString(),
                    Material.DIAMOND_SWORD.toString(),
                    Material.NETHERITE_SWORD.toString()
            ));
            swords.put("rewardItems", Arrays.asList(
                    Material.GOLD_INGOT.toString(),
                    Material.EXPERIENCE_BOTTLE.toString()
            ));
            swords.put("rewardMessage", "&aYou have collected all swords!");
            swords.put("claimed", new ArrayList<>()); // none claimed yet

            config.set("collections.default_swords", swords);

            // Another example "Apples" (regular + golden + enchanted)
            Map<String, Object> apples = new HashMap<>();
            apples.put("name", "Apple Mania");
            apples.put("iconMaterial", Material.APPLE.toString());
            apples.put("requiredItems", Arrays.asList(
                    Material.APPLE.toString(),
                    Material.GOLDEN_APPLE.toString(),
                    Material.ENCHANTED_GOLDEN_APPLE.toString()
            ));
            apples.put("rewardItems", Arrays.asList(
                    Material.GOLD_INGOT.toString(),
                    Material.EXPERIENCE_BOTTLE.toString()
            ));
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

    private void loadIntoMemory() {
        collections.clear();
        if (config.contains("collections")) {
            ConfigurationSection section = config.getConfigurationSection("collections");
            for (String key : section.getKeys(false)) {
                ConfigurationSection collSec = section.getConfigurationSection(key);
                if (collSec == null) continue;

                String name = collSec.getString("name", "Unnamed Collection");
                Material iconMat = Material.matchMaterial(collSec.getString("iconMaterial", "CHEST"));
                List<String> requiredList = collSec.getStringList("requiredItems");
                List<String> rewardList   = collSec.getStringList("rewardItems");

                // Convert required items to actual ItemStacks (here just basic, no custom meta)
                List<ItemStack> requiredItems = new ArrayList<>();
                for (String matString : requiredList) {
                    Material m = Material.matchMaterial(matString);
                    if (m != null) {
                        requiredItems.add(new ItemStack(m));
                    }
                }

                // Convert reward items
                List<ItemStack> rewardItems = new ArrayList<>();
                for (String matString : rewardList) {
                    Material m = Material.matchMaterial(matString);
                    if (m != null) {
                        rewardItems.add(new ItemStack(m));
                    }
                }

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
                        new ItemStack(iconMat != null ? iconMat : Material.CHEST),
                        requiredItems,
                        rewardItems,
                        rewardMsg
                );
                cData.claimedPlayers = claimedPlayers;

                collections.add(cData);
            }
        }
    }

    private void saveCollections() {
        // Wipe existing data
        config.set("collections", null);

        // Save each collection back
        for (int i = 0; i < collections.size(); i++) {
            CollectionData c = collections.get(i);
            String path = "collections.coll_" + i;
            config.set(path + ".name", c.name);
            config.set(path + ".iconMaterial", c.iconMaterial.getType().toString());
            List<String> requiredList = new ArrayList<>();
            for (ItemStack is : c.requiredItems) {
                requiredList.add(is.getType().toString());
            }
            config.set(path + ".requiredItems", requiredList);

            List<String> rewardList = new ArrayList<>();
            for (ItemStack is : c.rewardItems) {
                rewardList.add(is.getType().toString());
            }
            config.set(path + ".rewardItems", rewardList);

            config.set(path + ".rewardMessage", c.rewardMessage);

            // Save who has claimed
            List<String> claimedStrs = new ArrayList<>();
            for (UUID u : c.claimedPlayers) {
                claimedStrs.add(u.toString());
            }
            config.set(path + ".claimed", claimedStrs);
        }

        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("[Collections] Could not save collections.yml");
            e.printStackTrace();
        }
    }

    // Register this in onEnable or wherever you set commands:
    // getCommand("collection").setExecutor(new Collections(plugin, yourItemDisplayManagerInstance));
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

    // Build the GUI with each collection as an icon
    private void openCollectionsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Your Collections");

        // Loop each collection, create an icon
        for (int i = 0; i < collections.size() && i < 54; i++) {
            CollectionData cData = collections.get(i);

            ItemStack icon = cData.iconMaterial.clone();
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + cData.name);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Required items:");

            // We need to check if the user has each required item in an item display
            // Grab all displays that belong to the player
            List<ItemDisplay> userDisplays = displayManager.getDisplaysByPlayer(player.getUniqueId());

            // Create a quick set of Materials that the player has in displays
            // If you need to support custom item meta, expand this to compare more than just type
            Set<String> itemsPlayerHas = new HashSet<>();
            for (ItemDisplay d : userDisplays) {
                if (d.storedItem != null) {
                    itemsPlayerHas.add(d.storedItem.getType().toString());
                }
            }

            boolean complete = true;
            for (ItemStack req : cData.requiredItems) {
                String reqMat = req.getType().toString();
                if (itemsPlayerHas.contains(reqMat)) {
                    lore.add(ChatColor.GREEN + " - " + reqMat);
                } else {
                    lore.add(ChatColor.RED + " - " + reqMat);
                    complete = false;
                }
            }

            // If the player has everything, check if they've claimed
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
            inv.setItem(i, icon);
        }

        player.openInventory(inv);
    }

    // Listen for player clicks in that GUI
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!ChatColor.stripColor(event.getView().getTitle()).equals("Your Collections")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) return;
        if (slot >= collections.size()) return;

        CollectionData cData = collections.get(slot);

        // Check if user complete
        List<ItemDisplay> userDisplays = displayManager.getDisplaysByPlayer(player.getUniqueId());
        Set<String> itemsPlayerHas = new HashSet<>();
        for (ItemDisplay d : userDisplays) {
            if (d.storedItem != null) {
                itemsPlayerHas.add(d.storedItem.getType().toString());
            }
        }

        boolean complete = true;
        for (ItemStack req : cData.requiredItems) {
            if (!itemsPlayerHas.contains(req.getType().toString())) {
                complete = false;
                break;
            }
        }

        if (!complete) {
            player.sendMessage(ChatColor.RED + "You haven't collected all items yet!");
            return;
        }

        // If complete, check if claimed
        if (cData.claimedPlayers.contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "You've already claimed this reward!");
            return;
        }

        // Otherwise, give them the reward
        for (ItemStack reward : cData.rewardItems) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(reward.clone());
            // If there's leftover, drop it
            for (ItemStack l : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), l);
            }
        }
        String msg = ChatColor.translateAlternateColorCodes('&', cData.rewardMessage);
        player.sendMessage(msg);

        // Mark as claimed
        cData.claimedPlayers.add(player.getUniqueId());
        saveCollections(); // so it persists

        // Refresh the GUI
        openCollectionsGUI(player);
    }
}
