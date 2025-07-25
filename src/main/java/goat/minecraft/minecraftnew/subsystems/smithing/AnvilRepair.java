package goat.minecraft.minecraftnew.subsystems.smithing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.other.durability.CustomDurabilityManager;
import goat.minecraft.minecraftnew.subsystems.mining.MiningGemManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.other.enchanting.EnchantmentUtils;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.devtools.TalismanManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemLoreFormatter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;


import static goat.minecraft.minecraftnew.other.enchanting.EnchantmentUtils.*;

public class AnvilRepair implements Listener {
    // Mapping of player UUIDs to their custom anvil inventories
    private final Map<UUID, Inventory> anvilInventories = new HashMap<>();

    // File to persist anvil inventories
    private final File inventoriesFile;
    private final FileConfiguration inventoriesConfig;
    private final XPManager xpManager = new XPManager(MinecraftNew.getInstance()); // Add this line
    // Reference to the main plugin class
    private final MinecraftNew plugin;

    // Slots that players can edit: slot 10 (item to repair) and slot 16 (repair material)
    private final int[] playerEditableSlots = {10, 13};

    /**
     * Constructor initializes the anvil repair system and loads existing inventories.
     *
     * @param plugin The main plugin instance.
     */
    public AnvilRepair(MinecraftNew plugin) {
        this.plugin = plugin;

        // Initialize the inventories file
        inventoriesFile = new File(plugin.getDataFolder(), "anvil_inventories.yml");
        if (!inventoriesFile.exists()) {
            try {
                inventoriesFile.createNewFile();
                // Optionally, you can add default configurations here
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inventoriesConfig = YamlConfiguration.loadConfiguration(inventoriesFile);
    }

    /**
     * Handles player interactions with the anvil block.
     *
     * @param event The player interaction event.
     */

    EnchantmentUtils enchantmentUtils = new EnchantmentUtils();
    public static final Set<Material> ANVILS = EnumSet.of(Material.ANVIL, Material.CHIPPED_ANVIL,
            Material.DAMAGED_ANVIL);
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();

        // Check if the player right-clicked on an anvil

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && ANVILS.contains(clickedBlock.getType())) {
            event.setCancelled(true); // Prevent the default anvil GUI from opening

            // Get or load the player's custom anvil inventory
            Inventory anvilInventory = anvilInventories.get(player.getUniqueId());
            if (anvilInventory == null) {
                anvilInventory = loadInventory(player.getUniqueId());
                anvilInventories.put(player.getUniqueId(), anvilInventory);
            }

            // Open the custom anvil inventory for the player
            player.openInventory(anvilInventory);
        }
    }

    /**
     * Opens the Anvil Repair GUI for the given player without requiring an anvil block.
     * This is used by certain trinkets to access the interface remotely.
     *
     * @param player The player for whom to open the GUI.
     */
    public void openAnvilGui(Player player) {
        Inventory anvilInventory = anvilInventories.get(player.getUniqueId());
        if (anvilInventory == null) {
            anvilInventory = loadInventory(player.getUniqueId());
            anvilInventories.put(player.getUniqueId(), anvilInventory);
        }
        player.openInventory(anvilInventory);
    }

    /**
     * Creates a custom anvil inventory with predefined GUI elements.
     *
     * @return The custom anvil inventory.
     */
    private Inventory createAnvilInventory() {
        // Create an inventory with 27 slots (3 rows) titled "Anvil Repair"
        Inventory anvilInventory = Bukkit.createInventory(null, 27, "Anvil Repair");

        // Define GUI panes and items for decoration and instructions
        ItemStack blackPane = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "");
        ItemStack resultPane = createGuiItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Click to repair");
        // Create lore for the "Sharpen" button
        List<String> sharpnessLore = new ArrayList<>();
        sharpnessLore.add(ChatColor.GRAY + "+1 Sharpness Level");
        sharpnessLore.add(ChatColor.GRAY + "Cost: 2 Diamonds");


        // Set up the GUI layout with decorative panes
        for (int i = 0; i < 27; i++) {
            anvilInventory.setItem(i, blackPane);
        }
        // Place specific GUI items and interactive slots
        anvilInventory.setItem(10, null); // Slot for the item to repair
        anvilInventory.setItem(15, resultPane); // Slot for the repair material
        anvilInventory.setItem(13, null); // Output slot with "Click to repair" pane
        return anvilInventory;
    }
    private void handleFailure(Block anvilBlock, ItemStack repairee, Inventory inventory, int materialCount, Material material, Player player) {
        // Consume the required resources
        inventory.removeItem(new ItemStack(material, materialCount));

        // Damage the item by 50% of max durability
        if (repairee != null && repairee.getItemMeta() instanceof Damageable) {
            Damageable damageable = (Damageable) repairee.getItemMeta();
            int maxDurability = repairee.getType().getMaxDurability();
            int currentDamage = damageable.getDamage();
            damageable.setDamage(Math.min(currentDamage + maxDurability / 2, maxDurability - 1));
            repairee.setItemMeta((ItemMeta) damageable);
        }

        // Downgrade the anvil
        if (anvilBlock != null && (anvilBlock.getType() == Material.ANVIL || anvilBlock.getType() == Material.CHIPPED_ANVIL || anvilBlock.getType() == Material.DAMAGED_ANVIL)) {
            switch (anvilBlock.getType()) {
                case ANVIL:
                    anvilBlock.setType(Material.CHIPPED_ANVIL);
                    break;
                case CHIPPED_ANVIL:
                    anvilBlock.setType(Material.DAMAGED_ANVIL);
                    break;
                case DAMAGED_ANVIL:
                    anvilBlock.setType(Material.AIR);
                    break;
            }
        }

        // Play a terrifying sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 0.5f);
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.5f);

        // Send failure message
        player.sendMessage(ChatColor.RED + "The enchantment process failed!");
    }

    /**
     * Utility method to create a GUI item with a specific material and display name.
     *
     * @param material The material of the item.
     * @param name     The display name of the item.
     * @return The created ItemStack.
     */
    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "")); // Empty lore to prevent stacking
            item.setItemMeta(meta);
        }
        return item;
    }

//    public boolean isArmor(ItemStack item) {
//        if (item == null || item.getType() == Material.AIR) {
//            return false; // Check for null or empty items
//        }
//
//        String type = item.getType().toString().toUpperCase();
//        return type.endsWith("CHESTPLATE") || type.endsWith("HELMET") ||
//                type.endsWith("LEGGINGS") || type.endsWith("BOOTS");
//    }
    public boolean isFishingRod(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        String type = item.getType().toString().toUpperCase();
        return item.getType().toString().toUpperCase().endsWith("ROD");
    }

    public boolean isSword(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("SWORD");
    }

    public boolean isPickaxe(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("PICKAXE");
    }
    public boolean isShovel(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("SHOVEL");
    }

    public boolean isBow(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("BOW") &&
               !item.getType().toString().toUpperCase().endsWith("CROSSBOW");
    }
    public boolean isCrossbow(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("CROSSBOW");
    }
    public boolean isTrident(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        return item.getType().toString().toUpperCase().endsWith("TRIDENT");
    }

    public boolean isDurable(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false; // Check for null or empty items
        }

        // Get the item's meta data and check if it supports durability
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.isUnbreakable() == false && item.getType().getMaxDurability() > 0;
    }
    public static final Set<Material> MELEE = EnumSet.of(Material.WOODEN_SWORD, Material.STONE_SWORD,
            Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.TRIDENT);
    public static final Set<Material> TOOLS = EnumSet.of(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE,
            Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE,
            Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL,
            Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE,
            Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.SHEARS, Material.FISHING_ROD, Material.FLINT_AND_STEEL);
    public static final Set<Material> ARMOR = EnumSet.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE,
            Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE,
            Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE,
            Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
    );
    /**
     * Finds the nearest anvil block around the player within a given radius.
     *
     * @param player The player to search around.
     * @param radius The radius to search for an anvil.
     * @return The nearest anvil block, or null if none found.
     */
    private Block getNearestAnvil(Player player, int radius) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        Block nearestAnvil = null;
        double nearestDistanceSquared = Double.MAX_VALUE;

        // Iterate over a cubic area around the player's location
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(playerLocation.clone().add(x, y, z));
                    if (block != null && isAnvil(block)) {
                        double distanceSquared = playerLocation.distanceSquared(block.getLocation());
                        if (distanceSquared < nearestDistanceSquared) {
                            nearestAnvil = block;
                            nearestDistanceSquared = distanceSquared;
                        }
                    }
                }
            }
        }
        return nearestAnvil;
    }

    /**
     * Checks if a block is an anvil (any stage: normal, chipped, or damaged).
     *
     * @param block The block to check.
     * @return True if the block is an anvil, false otherwise.
     */
    private boolean isAnvil(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        return type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the click is within the "Anvil Repair" inventory
        if (event.getView().getTitle().equals("Anvil Repair")) {
            Player player = (Player) event.getWhoClicked();
            Inventory clickedInventory = event.getClickedInventory();
            //Bukkit.broadcastMessage("Clicked in anvil");
            if (clickedInventory == null) return; // Clicked outside the inventory

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedInventory == null || clickedInventory != event.getView().getTopInventory()) {
                return; // Allow clicks in the player's inventory
            }
            int slot = event.getRawSlot();

            // Prevent moving GUI items (decorative panes and info book)
            if (isGuiItem(clickedItem) && slot < clickedInventory.getSize()) {
                event.setCancelled(true);
            }

            // Allow interaction with repairee slot (slot 10) and bill slot (slot 16)
            if (slot == 10 || slot == 13) {
                return;
            }
            // Handle Protection (slot 26)
            Random random = new Random(); // Add at the beginning of your class

// Handle Protection (slot 26)
            if (event.getView().getTopInventory() != null && slot == 26) {
                Inventory inventory = player.getInventory();
                event.setCancelled(true);

                // Count obsidian blocks
                int obsidianCount = 0;
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == Material.OBSIDIAN) {
                        obsidianCount += item.getAmount();
                    }
                }

                if (obsidianCount >= 2) {
                    ItemStack repairee = event.getClickedInventory().getItem(10);

                    if (repairee != null && ARMOR.contains(repairee.getType())) {
                        int smithingLevel = xpManager.getPlayerLevel(player, "Smithing");
                        double failureChance = 0.5 * (1 - (smithingLevel / 100.0)); // Scales from 50% to 0%
                        if (random.nextDouble() < failureChance) {
                            handleFailure(getNearestAnvil(player, 10), repairee, inventory, 2, Material.OBSIDIAN, player);
                            return;
                        }

                        if (repairee.getEnchantmentLevel(Enchantment.PROTECTION) == 4) {
                            return;
                        }
                        EnchantmentUtils.incrementEnchantment(player, repairee, null, Enchantment.PROTECTION);
                        inventory.removeItem(new ItemStack(Material.OBSIDIAN, 2));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
                        player.sendMessage(ChatColor.AQUA + "Your item has been enchanted with Protection!");
                        xpManager.addXP(player, "Smithing", 200.0); // Add XP here

                    } else {
                        player.sendMessage("Please place armor to enchant!");
                    }
                } else {
                    player.sendMessage("You need at least 2 obsidian blocks to enchant with Protection!");
                }
            }

// Handle Efficiency (slot 17)
            if (event.getView().getTopInventory() != null && slot == 17) {
                Inventory inventory = player.getInventory();
                event.setCancelled(true);

                // Count gold blocks
                int goldBlockCount = 0;
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == Material.GOLD_BLOCK) {
                        goldBlockCount += item.getAmount();
                    }
                }

                if (goldBlockCount >= 1) {
                    ItemStack repairee = event.getClickedInventory().getItem(10);

                    if (repairee != null && TOOLS.contains(repairee.getType())) {
                        int smithingLevel = xpManager.getPlayerLevel(player, "Smithing");
                        double failureChance = 0.5 * (1 - (smithingLevel / 100.0)); // Scales from 50% to 0%
                        if (random.nextDouble() < failureChance) {
                            handleFailure(getNearestAnvil(player, 10), repairee, inventory, 2, Material.GOLD_BLOCK, player);
                            return;
                        }

                        if (repairee.getEnchantmentLevel(Enchantment.EFFICIENCY) == 5) {
                            return;
                        }
                        EnchantmentUtils.incrementEnchantment(player, repairee, null, Enchantment.EFFICIENCY);
                        inventory.removeItem(new ItemStack(Material.GOLD_BLOCK, 2));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
                        player.sendMessage(ChatColor.AQUA + "Your item has been enchanted with Efficiency!");
                        xpManager.addXP(player, "Smithing", 200.0); // Add XP here

                    } else {
                        player.sendMessage("Please place a tool to enchant!");
                    }
                } else {
                    player.sendMessage("You need at least 1 gold block to enchant with Efficiency!");
                }
            }

// Handle sharpen click event (slot 8)
            if (event.getView().getTopInventory() != null && slot == 8) {
                Inventory inventory = player.getInventory();
                event.setCancelled(true);
                int diamondCount = 0;
                for (ItemStack item : inventory.getContents()) {
                    if (item != null && item.getType() == Material.DIAMOND) {
                        diamondCount += item.getAmount();
                    }
                }
                if (diamondCount >= 2) {
                    ItemStack repairee = event.getClickedInventory().getItem(10);
                    ItemStack billItem = event.getClickedInventory().getItem(13);

                    if (repairee != null && MELEE.contains(repairee.getType())) {
                        int smithingLevel = xpManager.getPlayerLevel(player, "Smithing");
                        double failureChance = 0.5 * (1 - (smithingLevel / 100.0)); // Scales from 50% to 0%
                        if (random.nextDouble() < failureChance) {
                            handleFailure(getNearestAnvil(player, 10), repairee, inventory, 2, Material.DIAMOND, player);
                            return;
                        }

                        if (repairee.getEnchantmentLevel(Enchantment.SHARPNESS) == 5) {
                            return;
                        }
                        EnchantmentUtils.incrementEnchantment(player, repairee, billItem, Enchantment.SHARPNESS);
                        inventory.removeItem(new ItemStack(Material.DIAMOND, 2));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
                        player.sendMessage(ChatColor.AQUA + "Your item has been sharpened!");
                        xpManager.addXP(player, "Smithing", 200.0); // Add XP here

                    } else {
                        player.sendMessage("Please place a sword to be sharpened!");
                    }
                } else {
                    player.sendMessage("You need at least 2 diamonds to sharpen your item!");
                }
            }


            if (slot == 15) {
                //Bukkit.broadcastMessage("clicked repair");
                ItemStack repairee = clickedInventory.getItem(10);
                ItemStack billItem = clickedInventory.getItem(13);
                if (repairee != null && billItem != null) {
                    // Perform the repair
                    event.setCancelled(true); // Prevent taking the pane

                    List<Material> billableItems = new ArrayList<>();
                    billableItems.add(Material.IRON_INGOT);
                    billableItems.add(Material.GOLD_INGOT);
                    billableItems.add(Material.DIAMOND);
                    billableItems.add(Material.EMERALD);
                    billableItems.add(Material.COCOA_BEANS);

                    billableItems.add(Material.YELLOW_DYE);
                    billableItems.add(Material.TURTLE_HELMET);
                    billableItems.add(Material.ENDER_CHEST);
                    billableItems.add(Material.TURTLE_SCUTE);
                    billableItems.add(Material.CYAN_DYE);

                    billableItems.add(Material.LIGHT_BLUE_DYE);
                    billableItems.add(Material.STRING);
                    billableItems.add(Material.ARROW);



                    billableItems.add(Material.OAK_PLANKS);
                    billableItems.add(Material.BIRCH_PLANKS);
                    billableItems.add(Material.SPRUCE_PLANKS);
                    billableItems.add(Material.ACACIA_PLANKS);
                    billableItems.add(Material.DARK_OAK_PLANKS);


                    billableItems.add(Material.NETHER_WART);
                    billableItems.add(Material.GOAT_HORN);
                    billableItems.add(Material.CRIMSON_HYPHAE);
                    billableItems.add(Material.PINK_TULIP);
                    billableItems.add(Material.BEETROOT_SEEDS);
                    billableItems.add(Material.HONEY_BOTTLE);
                    billableItems.add(Material.JUNGLE_LOG);
                    billableItems.add(Material.PAPER);
                    billableItems.add(Material.SPRUCE_SAPLING);
                    billableItems.add(Material.OAK_WOOD);

                    billableItems.add(Material.ROTTEN_FLESH);
                    billableItems.add(Material.OAK_WOOD);
                    billableItems.add(Material.SPIDER_EYE);
                    billableItems.add(Material.FIRE_CHARGE);
                    billableItems.add(Material.ENCHANTED_BOOK);
                    billableItems.add(Material.ICE);
                    billableItems.add(Material.SLIME_BALL);
                    billableItems.add(Material.AMETHYST_SHARD);
                    billableItems.add(Material.SOUL_SOIL);
                    billableItems.add(Material.LEATHER_BOOTS);
                    billableItems.add(Material.TURTLE_EGG);
                    billableItems.add(Material.COMPOSTER);
                    billableItems.add(Material.GLASS);
                    billableItems.add(Material.LEATHER_LEGGINGS);
                    billableItems.add(Material.BONE);
                    billableItems.add(Material.IRON_SWORD);
                    billableItems.add(Material.BOW);
                    billableItems.add(Material.RABBIT_STEW);
                    billableItems.add(Material.GOLDEN_AXE);
                    billableItems.add(Material.COD);
                    billableItems.add(Material.EXPERIENCE_BOTTLE);
                    billableItems.add(Material.GLASS_BOTTLE);
                    billableItems.add(Material.TORCH);
                    billableItems.add(Material.OAK_BUTTON);
                    billableItems.add(Material.GOLD_NUGGET);
                    billableItems.add(Material.SHEARS);
                    billableItems.add(Material.ENDER_PEARL);
                    billableItems.add(Material.ANVIL);
                    billableItems.add(Material.IRON_BLOCK);
                    billableItems.add(Material.GOLD_NUGGET);
                    billableItems.add(Material.FLINT);
                    billableItems.add(Material.ROTTEN_FLESH);
                    billableItems.add(Material.SUGAR_CANE);
                    billableItems.add(Material.ENDER_EYE);
                    billableItems.add(Material.WHITE_DYE);
                    billableItems.add(Material.IRON_SHOVEL);
                    billableItems.add(Material.LEATHER);
                    billableItems.add(Material.RED_DYE);
                    billableItems.add(Material.MOJANG_BANNER_PATTERN);
                    billableItems.add(Material.LIGHTNING_ROD);
                    billableItems.add(Material.CONDUIT);
                    billableItems.add(Material.GOLDEN_PICKAXE);
                    billableItems.add(Material.OBSIDIAN);
                    billableItems.add(Material.GOLDEN_SWORD);
                    billableItems.add(Material.WHEAT);
                    billableItems.add(Material.SUGAR);
                    billableItems.add(Material.GOLD_INGOT);
                    billableItems.add(Material.SLIME_BLOCK);
                    billableItems.add(Material.FIRE_CHARGE);
                    billableItems.add(Material.BONE);
                    billableItems.add(Material.FERMENTED_SPIDER_EYE);
                    billableItems.add(Material.BRAIN_CORAL_BLOCK);
                    billableItems.add(Material.STICK);
                    billableItems.add(Material.GOLDEN_HELMET);
                    billableItems.add(Material.GOLDEN_CHESTPLATE);
                    billableItems.add(Material.CACTUS);
                    billableItems.add(Material.FEATHER);
                    billableItems.add(Material.LIME_DYE);
                    billableItems.add(Material.BLUE_DYE);
                    billableItems.add(Material.MAGENTA_DYE);
                    billableItems.add(Material.YELLOW_DYE);
                    billableItems.add(Material.FILLED_MAP);

                    billableItems.add(Material.WHITE_STAINED_GLASS);
                    billableItems.add(Material.LIME_STAINED_GLASS);
                    billableItems.add(Material.BLUE_STAINED_GLASS);
                    billableItems.add(Material.MAGENTA_STAINED_GLASS);
                    billableItems.add(Material.YELLOW_STAINED_GLASS);

                    billableItems.add(Material.WHITE_STAINED_GLASS_PANE);
                    billableItems.add(Material.LIME_STAINED_GLASS_PANE);
                    billableItems.add(Material.BLUE_STAINED_GLASS_PANE);
                    billableItems.add(Material.MAGENTA_STAINED_GLASS_PANE);
                    billableItems.add(Material.YELLOW_STAINED_GLASS_PANE);
                    billableItems.add(Material.IRON_NUGGET);
                    billableItems.add(Material.SOUL_LANTERN);
                    billableItems.add(Material.REDSTONE);
                    billableItems.add(Material.LAPIS_LAZULI);

                    billableItems.add(Material.GOLD_BLOCK);
                    billableItems.add(Material.BEDROCK);
                    billableItems.add(Material.GOLDEN_BOOTS);
                    billableItems.add(Material.COBWEB);
                    billableItems.add(Material.HEART_OF_THE_SEA);
                    billableItems.add(Material.WITHER_SKELETON_SKULL);
                    billableItems.add(Material.CHAIN);
                    billableItems.add(Material.PURPLE_DYE);





                    if (billableItems.contains(billItem.getType())) {

                        repairItem(event.getInventory(), player);
                    }else {
                        player.sendMessage(ChatColor.RED + "You must use Valid materials!");
                    }
                } else {
                    event.setCancelled(true); // Prevent taking the pane
                }
                return;
            }

            // Prevent moving items in other slots of the GUI
            if (slot < clickedInventory.getSize()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void formatAfterClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Anvil Repair")) {
            ItemStack stack = event.getInventory().getItem(10);
            if (stack != null) {
                ItemLoreFormatter.formatLore(stack);
            }
        }
    }


    /**
     * Repairs the item in slot 10 using the material in slot 16.
     *
     * - Checks for null items.
     * - Determines if the repair material is Iron Ingot, Diamond, or a custom item.
     * - Adds 100 durability for Iron Ingots and 300 durability for Diamonds.
     * - Prioritizes custom repair materials over basic ones.
     * - Subtracts one from the repair material's stack upon successful repair.
     * - Sends feedback messages and plays a sound to the player.
     *
     * @param inventory The custom anvil inventory.
     * @param player    The player performing the repair.
     */
    /**
     * Repairs the item in slot 10 using the material in slot 13.
     *
     * - Checks if both items are present.
     * - Determines if the repair material is Iron Ingot or Diamond.
     * - Adds 100 durability for Iron Ingots and 300 durability for Diamonds.
     * - Subtracts one from the repair material's stack upon successful repair.
     * - Sends feedback messages and plays a sound to inform the player.
     *
     * @param inventory The custom anvil inventory.
     * @param player    The player performing the repair.
     */
    ReforgeManager reforgeManager = new ReforgeManager();
    private void repairItem(Inventory inventory, Player player) {
        ItemStack repairee = inventory.getItem(10);
        ItemStack billItem = inventory.getItem(13);

        List<Material> ironWhitelist = new ArrayList<>();
        //iron tool support
        ironWhitelist.add(Material.IRON_SWORD);
        ironWhitelist.add(Material.IRON_PICKAXE);
        ironWhitelist.add(Material.IRON_AXE);
        ironWhitelist.add(Material.IRON_SHOVEL);
        ironWhitelist.add(Material.SHIELD);
        //iron armor support
        ironWhitelist.add(Material.IRON_HELMET);
        ironWhitelist.add(Material.IRON_CHESTPLATE);
        ironWhitelist.add(Material.IRON_LEGGINGS);
        ironWhitelist.add(Material.IRON_BOOTS);

        if (repairee == null || billItem == null) {
            player.sendMessage(ChatColor.RED + "Both the item to repair and repair material must be present!");
            return;
        }

        // Check if the item to repair is damageable (has durability)
        if (!(repairee.getItemMeta() instanceof Damageable)) {
            player.sendMessage(ChatColor.RED + "This item cannot be repaired!");
            return;
        }

        Damageable damageable = (Damageable) repairee.getItemMeta();
        int currentDamage = damageable.getDamage();

        // Check if the item is already at full durability

        // Determine the repair amount based on the repair material
        int repairAmount = 25;
        float anvilPitch = 1.0f;
        SkillTreeManager mgr = SkillTreeManager.getInstance();
        if (mgr != null) {
            UUID uid = player.getUniqueId();
            repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_ONE);
            repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_TWO) * 2;
            repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_THREE) * 3;
            repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_FOUR) * 4;
        }
        if(ironWhitelist.contains(repairee.getType())){
            repairAmount = repairAmount + 150;
        }
        // Determine the type of repair material and set the repair amount accordingly
        if (billItem.getType() == Material.IRON_INGOT) {
            int quality = getRepairQuality(player);
            int roll = quality;
            if (repairAmount > quality) {
                roll = quality + new Random().nextInt(repairAmount - quality + 1);
            }
            repairAmount = roll;
            xpManager.addXP(player, "Smithing", roll);
            anvilPitch = getAnvilPitch(roll);

            CustomDurabilityManager durMgr = CustomDurabilityManager.getInstance();
            if (durMgr != null) {
                durMgr.addMaxDurabilityBonus(repairee, 1);
            }
        } else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Shallow Shell")){
            repairAmount = 100;
            xpManager.addXP(player, "Smithing", 100.0);
            anvilPitch = getAnvilPitch(0); // minimal
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Shell")){
            repairAmount = 200;
            xpManager.addXP(player, "Smithing", 100.0);
            anvilPitch = getAnvilPitch(30); // fair
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Deep Shell")){
            repairAmount = 400;
            xpManager.addXP(player, "Smithing", 100.0);
            anvilPitch = getAnvilPitch(70); // great
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Abyssal Shell")){
            repairAmount = 800;
            xpManager.addXP(player, "Smithing", 100.0);
            anvilPitch = getAnvilPitch(90); // legendary
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Mithril Chunk") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.UNBREAKING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Perfect Diamond")&& TOOLS.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.FORTUNE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Silk Worm") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.SILK_TOUCH);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Secrets of Infinity") && isBow(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.INFINITY);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Petrified Log") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.UNBREAKING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Pinecone") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.BLAST_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Birch Strip") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.PROJECTILE_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Humid Bark") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.FEATHER_FALLING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Acacia Gum") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.FIRE_PROTECTION);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Acorn") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.BLAST_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Cherry Blossom") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.UNBREAKING);
            incrementEnchantment(player, repairee, billItem,Enchantment.UNBREAKING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Maple Bark") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.FIRE_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Blue Nether Wart") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.BLAST_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Beating Heart") && isSword(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.SMITE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "SpiderBane") && isSword(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.BANE_OF_ARTHROPODS);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fire Ball") && isBow(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.FLAME);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Mending") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.MENDING);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Frost Heart") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.FROST_WALKER);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "KB Ball") && isDurable(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.KNOCKBACK);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "High Caliber Arrow") && isBow(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.PIERCING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Grains of Soul") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.SOUL_SPEED);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Gold Bar") && isSword(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.LOOTING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fins") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.DEPTH_STRIDER);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Turtle Tactics") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.AQUA_AFFINITY);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Diving Helmet") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.RESPIRATION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Swim Trunks") && ARMOR.contains(repairee.getType())){
            incrementEnchantment(player, repairee, billItem,Enchantment.SWIFT_SNEAK);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fish Bait") && isFishingRod(repairee)) {
            incrementEnchantment(player, repairee, billItem, Enchantment.LURE);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;

        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Loyal Declaration") && isTrident(repairee)) {
            incrementEnchantment(player, repairee, billItem, Enchantment.LOYALTY);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lucky") && isFishingRod(repairee)) {
            incrementEnchantment(player, repairee, billItem, Enchantment.LUCK_OF_THE_SEA);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Narwhal Tusk") && isSword(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.IMPALING);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Sweeping Edge") && isSword(repairee)){
            incrementEnchantment(player, repairee, billItem,Enchantment.SWEEPING_EDGE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Bowstring") && isBow(repairee)){
            incrementEnchantment(player, repairee, billItem, Enchantment.POWER);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lightning Bolt") && isTrident(repairee)){
            incrementEnchantment(player, repairee, billItem, Enchantment.CHANNELING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Anaklusmos") && isTrident(repairee)){
            incrementEnchantment(player, repairee, billItem, Enchantment.RIPTIDE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }

        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Well Balanced Meal") && MELEE.contains(repairee.getType())){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Feed", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Feed") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Brutal Tactics") && isSword(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Cleaver", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Cleaver") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Call of the Void") && isFishingRod(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Call of the Void", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Call of the Void") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Savant") && isDurable(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Savant", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Savant") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Oxygen Tank")&& ARMOR.contains(repairee.getType())){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Ventilation", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Ventilation") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Everflame")&& TOOLS.contains(repairee.getType())) {
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Forge", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Forge") + 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Qualification")&& TOOLS.contains(repairee.getType())) {
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Merit", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Merit") + 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Contingency Plan") && (ARMOR.contains(repairee.getType()) || TOOLS.contains(repairee.getType()) || MELEE.contains(repairee.getType()) || isBow(repairee) || isCrossbow(repairee))) {
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Preservation", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Preservation") + 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Climbing Rope")&& TOOLS.contains(repairee.getType())){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Rappel", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Rappel") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Golden Hook")&& isFishingRod(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Piracy", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Piracy") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Laceration")&& MELEE.contains(repairee.getType())){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Shear", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Shear") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Hide")&& ARMOR.contains(repairee.getType())){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Physical Protection", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Physical Protection") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Alchemical Bundle")&& isPickaxe(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Alchemy", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Alchemy") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fast Travel")&& isSword(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Aspect of the Journey", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Aspect of the Journey") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Stun Coating")&& isBow(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Stun", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Stun") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Velocity")&& isBow(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Velocity", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Velocity") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Explosive Bolts")&& isCrossbow(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Lethal Reaction", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Lethal Reaction") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Composter")&& isShovel(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Composter", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Composter") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lynch")&& isShovel(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Lynch", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Lynch") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);

        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Defenestration") && isBow(repairee)) {
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Defenestration", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Defenestration") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Soul Lantern")&& isSword(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Experience", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Experience") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Water Aspect") && isSword(repairee)){
            CustomEnchantmentManager.addEnchantment(player, billItem, repairee, "Water Aspect", CustomEnchantmentManager.getEnchantmentLevel(repairee, "Water Aspect") +1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "The Law of Gravity") && TOOLS.contains(repairee.getType())){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Treecapitator", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Unstoppable vs Immovable") && TOOLS.contains(repairee.getType())){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Hammer", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Draupnir") && MELEE.contains(repairee.getType())){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Shred", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Quantum Physics") && MELEE.contains(repairee.getType())){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Warp", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Evisceration") && isShovel(repairee)){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Mulch", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Revenant") && MELEE.contains(repairee.getType())){
            CustomEnchantmentManager.addUltimateEnchantment(player, billItem, repairee, "Ultimate: Loyal", 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }


        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Armor Talisman")&& ARMOR.contains(repairee.getType())){
            TalismanManager.applyReforgeLore(repairee, "Armor");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Armor Toughness Talisman")&& ARMOR.contains(repairee.getType())){
            TalismanManager.applyReforgeLore(repairee, "Armor Toughness");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Attack Damage Talisman")&& MELEE.contains(repairee.getType())){
            TalismanManager.applyReforgeLore(repairee, "Damage");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Sea Creature Talisman")&& isFishingRod(repairee)){
            TalismanManager.applyReforgeLore(repairee, "Sea Creature Chance");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Durability Talisman")&& isDurable(repairee)){
            TalismanManager.applyReforgeLore(repairee, "Durability");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Swift Blade Talisman")&& MELEE.contains(repairee.getType())){
            TalismanManager.applyReforgeLore(repairee, "Swift Blade");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }






        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Emerald Gemstone")&& TOOLS.contains(repairee.getType())) {
            MiningGemManager gemManager = new MiningGemManager();
            xpManager.addXP(player, "Smithing", 100.0);
            gemManager.applyGem(repairee, MiningGemManager.MiningGem.EMERALD_GEM);
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Diamond Gemstone")&& TOOLS.contains(repairee.getType())) {
            MiningGemManager gemManager = new MiningGemManager();
            xpManager.addXP(player, "Smithing", 100.0);
            gemManager.applyGem(repairee, MiningGemManager.MiningGem.DIAMOND_GEM);
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Lapis Gemstone")&& TOOLS.contains(repairee.getType())) {
            MiningGemManager gemManager = new MiningGemManager();
            xpManager.addXP(player, "Smithing", 100.0);
            gemManager.applyGem(repairee, MiningGemManager.MiningGem.LAPIS_GEM);
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Redstone Gemstone")&& TOOLS.contains(repairee.getType())) {
            MiningGemManager gemManager = new MiningGemManager();
            xpManager.addXP(player, "Smithing", 100.0);
            gemManager.applyGem(repairee, MiningGemManager.MiningGem.REDSTONE_GEM);
        }








        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Masterwork Ingot")&& isDurable(repairee)){
            CustomDurabilityManager.getInstance().addMaxDurabilityBonus(repairee, 10);
            CustomDurabilityManager.getInstance().repairFully(repairee);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }

        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Efficiency Expertise")&& TOOLS.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.EFFICIENCY);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Unbreaking Expertise")&& isDurable(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.UNBREAKING);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Sharpness Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.SHARPNESS);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Sweeping Edge Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.SWEEPING_EDGE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Looting Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.LOOTING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Knockback Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.KNOCKBACK);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fire Aspect Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.FIRE_ASPECT);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Smite Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.SMITE);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Bane of Anthropods Expertise")&& isSword(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.BANE_OF_ARTHROPODS);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Lure Expertise")&& isFishingRod(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.LURE);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Luck of the Sea Expertise")&& isFishingRod(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.LUCK_OF_THE_SEA);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Protection Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Respiration Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.RESPIRATION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Thorns Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.THORNS);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Feather Falling Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.FEATHER_FALLING);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Blast Protection Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.BLAST_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Fire Protection Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.FIRE_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Projectile Protection Expertise")&& ARMOR.contains(repairee.getType())){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.PROJECTILE_PROTECTION);

            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Power Expertise")&& isBow(repairee)){
            incrementEnchantmentUnsafely(player, repairee, billItem, Enchantment.POWER);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }


        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Common Sword Reforge")&& isSword(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_1);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Uncommon Sword Reforge")&& isSword(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_2);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Rare Sword Reforge")&& isSword(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_3);
            xpManager.addXP(player, "Smithing", 100.0);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            billItem.setAmount(billItem.getAmount() - 1);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Epic Sword Reforge")&& isSword(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_4);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Legendary Sword Reforge")&& isSword(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.WHITE + "Oak Bow")&& isBow(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_1);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Birch Bow")&& isBow(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_2);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Spruce Bow")&& isBow(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_3);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "Acacia Bow")&& isBow(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_4);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Dark Oak Bow")&& isBow(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }

        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Common Armor Reforge")&& ARMOR.contains(repairee.getType())){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_1);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Uncommon Armor Reforge")&& ARMOR.contains(repairee.getType())){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_2);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Rare Armor Reforge")&& ARMOR.contains(repairee.getType())){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_3);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Epic Armor Reforge")&& ARMOR.contains(repairee.getType())){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_4);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Legendary Armor Reforge")&& ARMOR.contains(repairee.getType())){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }




        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Common Tool Reforge")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_1);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Uncommon Tool Reforge")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_2);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Rare Tool Reforge")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_3);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Epic Tool Reforge")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_4);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.YELLOW + "Legendary Tool Reforge")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.BLUE + "Singularity")&& isDurable(repairee)){
            reforgeManager.applyReforge(repairee, ReforgeManager.ReforgeTier.TIER_1);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }


        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Midas Gold") && MELEE.contains(repairee.getType())){
            incrementInfernalEnchantment(repairee, billItem, Enchantment.LOOTING);
            xpManager.addXP(player, "Smithing", 100.0);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Unbreakable") && isDurable(repairee)){
            setEnchantment(repairee, Enchantment.UNBREAKING, 5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "LavaStride") && ARMOR.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.DEPTH_STRIDER, 5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Howl") && isFishingRod(repairee)){
            setEnchantment(repairee, Enchantment.LURE, 5);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Cure") && MELEE.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.SMITE, 7);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Shrapnel") && MELEE.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.SHARPNESS, 7);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Hellfire") && MELEE.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.FIRE_ASPECT, 4);
            xpManager.addXP(player, "Smithing", 100.0);


            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Weak Spot") && TOOLS.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.EFFICIENCY, 6);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }
        else if(billItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Extinction") && MELEE.contains(repairee.getType())){
            setEnchantment(repairee, Enchantment.BANE_OF_ARTHROPODS, 7);
            xpManager.addXP(player, "Smithing", 100.0);
            billItem.setAmount(billItem.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 10);
            return;
        }


        else {
            // Unsupported repair material
            return;
        }

        // Apply the repair by reducing the damage
        CustomDurabilityManager customDurabilityManager = CustomDurabilityManager.getInstance();
        customDurabilityManager.setCustomDurability(repairee, customDurabilityManager.getCurrentDurability(repairee) + repairAmount, customDurabilityManager.getMaxDurability(repairee));

        // Subtract one from the repair material's stack
        billItem.setAmount(billItem.getAmount() - 1);
        if (billItem.getAmount() <= 0) {
            inventory.setItem(13, null); // Remove the item if stack is empty
        } else {
            inventory.setItem(13, billItem); // Update the stack size
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.1f, anvilPitch);

    }




    /**
     * Updates the result slot based on the items placed in the repairee and bill slots.
     *
     * @param inventory The anvil inventory.
     * @param player    The player performing the repair.
     */
    private void updateResultSlot(Inventory inventory, Player player) {
        ItemStack repaireeItem = inventory.getItem(10);
        ItemStack billItem = inventory.getItem(16);

        // Check if both slots have items
        if (repaireeItem != null && billItem != null) {
            // Check if the item to repair is damageable
            if (repaireeItem.getItemMeta() instanceof Damageable) {
                Damageable damageable = (Damageable) repaireeItem.getItemMeta();
                int currentDamage = damageable.getDamage();
                int maxDurability = repaireeItem.getType().getMaxDurability();

                // Check if the item is damaged
                if (currentDamage > 0) {
                    // Determine the repair amount based on the repair material
                    int repairAmount = calculateRepairAmount(billItem, repaireeItem.getType());
                    SkillTreeManager mgr = SkillTreeManager.getInstance();
                    if (mgr != null) {
                        UUID uid = player.getUniqueId();
                        repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_ONE);
                        repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_TWO) * 2;
                        repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_THREE) * 3;
                        repairAmount += mgr.getTalentLevel(uid, Skill.SMITHING, Talent.REPAIR_FOUR) * 4;
                    }
                    List<Material> ironWhitelist = new ArrayList<>();
                    ironWhitelist.add(Material.IRON_SWORD);
                    ironWhitelist.add(Material.IRON_PICKAXE);
                    ironWhitelist.add(Material.IRON_AXE);
                    ironWhitelist.add(Material.IRON_SHOVEL);
                    ironWhitelist.add(Material.SHIELD);
                    ironWhitelist.add(Material.IRON_HELMET);
                    ironWhitelist.add(Material.IRON_CHESTPLATE);
                    ironWhitelist.add(Material.IRON_LEGGINGS);
                    ironWhitelist.add(Material.IRON_BOOTS);
                    if (ironWhitelist.contains(repaireeItem.getType())) {
                        repairAmount += 150;
                    }

                    if (repairAmount > 0) {
                        // Create a copy of the item with increased durability
                        ItemStack repairedItem = repaireeItem.clone();
                        Damageable repairedMeta = (Damageable) repairedItem.getItemMeta();
                        int newDamage = currentDamage - repairAmount;

                        // Ensure that durability does not exceed the maximum
                        if (newDamage < 0) newDamage = 0;
                        repairedMeta.setDamage(newDamage);
                        repairedItem.setItemMeta((ItemMeta) repairedMeta);

                        // Set the repaired item in the result slot
                        inventory.setItem(13, repairedItem);
                        return;
                    } else {
                        // Invalid repair material used
                        inventory.setItem(13, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Click to repair"));
                        player.sendMessage(ChatColor.RED + "Invalid repair material!");
                        return;
                    }
                } else {
                    // Item is already at full durability
                    inventory.setItem(13, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Click to repair"));
                    return;
                }
            } else {
                // The item to repair is not damageable
                inventory.setItem(13, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Click to repair"));
                player.sendMessage(ChatColor.RED + "This item cannot be repaired!");
                return;
            }
        }

        // If either slot is empty, reset the result slot to default
        inventory.setItem(13, createGuiItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "Click to repair"));
    }

    /**
     * Calculates the amount of durability to repair based on the repair material.
     *
     * @param billItem     The repair material ItemStack.
     * @param repaireeType The Material of the item to repair.
     * @return The amount of durability to repair.
     */
    private int calculateRepairAmount(ItemStack billItem, Material repaireeType) {
        // Check for null to prevent NullPointerException
        if (billItem == null || billItem.getType() == Material.AIR) {
            return 0;
        }
        // Retrieve the display name of the repair material, stripping any color codes
        ItemMeta billMeta = billItem.getItemMeta();
        String repairMaterialName = "";
        if (billMeta != null && billMeta.hasDisplayName()) {
            repairMaterialName = ChatColor.stripColor(billMeta.getDisplayName()).toLowerCase();
        } else {
            // If no display name, use the material name
            repairMaterialName = billItem.getType().toString().toLowerCase();
        }
        // Priority Check: Custom items first
        // Define custom repair materials by their display names
        // Example: "Custom Repair Gem" repairs more durability or has special properties
        switch (repairMaterialName) {
            case "shallow shell":
                return 500; // Example: Custom repair material repairs 500 durability
            // Add more custom repair materials here
        }

        // Basic Materials Repair Amounts
        switch (billItem.getItemMeta().getDisplayName()) {
            case "Iron Ingot":
                return 100; // Iron Ingot repairs 100 durability
            case "Diamond":
                return 300; // Diamond repairs 300 durability
            case "Shallow Shell":
                return 3000;
            default:
                return 0; // Unsupported materials do not repair
        }
    }

    /**
     * Retrieves the player's current Repair Quality. This value represents
     * the minimum amount of durability restored when using basic repair
     * materials such as Iron Ingots.
     *
     * <p>Currently there are no mechanics to increase this value so it
     * simply returns {@code 0}.</p>
     */
    private int getRepairQuality(Player player) {
        return 0; // Placeholder for future expansion
    }

    /**
     * Determines the anvil sound pitch based on the amount of durability
     * restored.
     */
    private float getAnvilPitch(int amount) {
        if (amount < 20) {
            return 2.0f; // minimal repair
        } else if (amount < 40) {
            return 1.2f; // fair repair
        } else if (amount < 60) {
            return 1.0f; // good repair
        } else if (amount < 80) {
            return 0.8f; // great repair
        }
        return 0.5f; // legendary repair
    }

    /**
     * Determines if the clicked item is a GUI decorative item.
     *
     * @param item The clicked ItemStack.
     * @return True if it's a GUI item, false otherwise.
     */
    private boolean isGuiItem(ItemStack item) {
        if (item == null || item.getItemMeta() == null) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return displayName != null && (displayName.contains("Place") || displayName.contains("Click") || displayName.equals("Repair Instructions"));
    }

    /**
     * Handles the closing of the Anvil Repair inventory by saving the current state.
     *
     * @param event The inventory close event.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Anvil Repair")) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            anvilInventories.put(player.getUniqueId(), inventory);
            saveInventory(player.getUniqueId(), inventory);
        }
    }

    /**
     * Saves the player's anvil inventory to the YAML configuration file.
     *
     * @param playerUUID The player's UUID.
     * @param inventory  The inventory to save.
     */
    public void saveInventory(UUID playerUUID, Inventory inventory) {
        for (int slot : playerEditableSlots) {
            ItemStack item = inventory.getItem(slot);
            inventoriesConfig.set("players." + playerUUID.toString() + ".slot" + slot, item);
        }
        try {
            inventoriesConfig.save(inventoriesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the player's anvil inventory from the YAML configuration file.
     *
     * @param playerUUID The player's UUID.
     * @return The loaded inventory.
     */
    public Inventory loadInventory(UUID playerUUID) {
        Inventory inventory = createAnvilInventory();
        for (int slot : playerEditableSlots) {
            String path = "players." + playerUUID.toString() + ".slot" + slot;
            if (inventoriesConfig.contains(path)) {
                ItemStack item = inventoriesConfig.getItemStack(path);
                inventory.setItem(slot, item);
            }
        }
        return inventory;
    }

    /**
     * Saves all anvil inventories to the YAML configuration file.
     * Call this method when the plugin is disabled to ensure all data is saved.
     */
    public void saveAllInventories() {
        for (Map.Entry<UUID, Inventory> entry : anvilInventories.entrySet()) {
            saveInventory(entry.getKey(), entry.getValue());
        }
    }
}
