package goat.minecraft.minecraftnew.subsystems.brewing;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CancelBrewing implements Listener {
    private final Map<UUID, Inventory> brewingStandInventories = new HashMap<>();
    private final File inventoriesFile;
    private final FileConfiguration inventoriesConfig;
    private final MinecraftNew plugin;
    private final int[] playerEditableSlots = {0, 18, 19, 20, 21, 22, 23, 24, 25, 26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53};

    public CancelBrewing(MinecraftNew plugin) {
        this.plugin = plugin;

        // Initialize the inventories file
        inventoriesFile = new File(plugin.getDataFolder(), "brewing_inventories.yml");
        if (!inventoriesFile.exists()) {
            try {
                inventoriesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        inventoriesConfig = YamlConfiguration.loadConfiguration(inventoriesFile);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        XPManager xpManager = new XPManager(plugin);
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && clickedBlock != null && clickedBlock.getType() == Material.BREWING_STAND) {
            event.setCancelled(true);
            // Get or load the player's brewing stand inventory
            Inventory brewingStandInventory = brewingStandInventories.get(player.getUniqueId());
            if (brewingStandInventory == null) {
                brewingStandInventory = loadInventory(player.getUniqueId());
                brewingStandInventories.put(player.getUniqueId(), brewingStandInventory);
            }

            player.openInventory(brewingStandInventory);

        }
    }

    /**
     * Creates a brewing stand inventory with the custom GUI setup.
     *
     * @return The custom brewing stand inventory.
     */
    private Inventory createBrewingStandInventory() {
        Inventory brewingStandInventory = Bukkit.createInventory(null, 54, "Brewing Stand");

        // Define GUI panes and items
        ItemStack blackPane = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "Place Ingredients to brew potions");
        ItemStack whitePane = createGuiItem(Material.WHITE_STAINED_GLASS_PANE, ChatColor.WHITE + "<--- Place bottles here!");
        ItemStack potionPlaceholder = createGuiItem(Material.BREWING_STAND, ChatColor.DARK_PURPLE + "Click to Brew Potion");
        ItemStack purplePane = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.GOLD + "Active Ingredients" + ChatColor.LIGHT_PURPLE + " between these!");
        ItemStack redPane = createGuiItem(Material.RED_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.RED + "Duration Boost Items" + ChatColor.LIGHT_PURPLE + " here!");
        ItemStack yellowPane = createGuiItem(Material.YELLOW_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.YELLOW + "Potency Boost Items" + ChatColor.LIGHT_PURPLE + " here!");
        ItemStack modifierPane = createGuiItem(Material.LIME_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place Modifier Item Here");
        ItemStack greyPane = createGuiItem(Material.GRAY_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.GRAY + "Gunpowder" + ChatColor.LIGHT_PURPLE + " here for splash potions");
        ItemStack cyanPane = createGuiItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.AQUA + "Dye" + ChatColor.LIGHT_PURPLE + " here to change potion color");
        ItemStack enderPane = createGuiItem(Material.PURPLE_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.DARK_PURPLE + "Ender Pearls" + ChatColor.LIGHT_PURPLE + " here for teleportation effect");
        ItemStack pumpkinPane = createGuiItem(Material.ORANGE_STAINED_GLASS_PANE, ChatColor.LIGHT_PURPLE + "Place " + ChatColor.GOLD + "Pumpkin" + ChatColor.LIGHT_PURPLE + " here for 'Tasty' effect");

        // Set up the GUI layout
        for (int i = 0; i < 36; i++) {
            brewingStandInventory.setItem(i, blackPane);
        }

        // Place specific GUI items
        brewingStandInventory.setItem(0, null); // Bottle slot
        brewingStandInventory.setItem(1, whitePane);

        brewingStandInventory.setItem(4, potionPlaceholder);

        brewingStandInventory.setItem(9, purplePane);
        brewingStandInventory.setItem(10, purplePane);
        brewingStandInventory.setItem(18, null); // Ingredient 1
        brewingStandInventory.setItem(19, null); // Ingredient 2

        brewingStandInventory.setItem(11, redPane);
        brewingStandInventory.setItem(20, null); // Duration item

        brewingStandInventory.setItem(12, yellowPane);
        brewingStandInventory.setItem(21, null); // Potency item

        brewingStandInventory.setItem(13, blackPane);
        brewingStandInventory.setItem(22, blackPane); // Modifier item

        brewingStandInventory.setItem(14, greyPane);
        brewingStandInventory.setItem(23, null); // Gunpowder slot

        brewingStandInventory.setItem(15, cyanPane);
        brewingStandInventory.setItem(24, null); // Dye slot

        brewingStandInventory.setItem(16, blackPane);
        brewingStandInventory.setItem(25, blackPane); // Ender Pearl slot

        brewingStandInventory.setItem(17, blackPane);
        brewingStandInventory.setItem(26, blackPane); // Pumpkin slot

        return brewingStandInventory;
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a custom potion based on the ingredients and modifiers in the inventory.
     *
     * @param inv The brewing inventory.
     * @return The brewed potion.
     */
    public static ItemStack createCustomPotion(Inventory inv) {
        ItemStack emptyBottle = inv.getItem(0);
        if (emptyBottle != null && emptyBottle.getType() == Material.GLASS_BOTTLE) {
            ItemStack ingredient1 = inv.getItem(18);
            ItemStack ingredient2 = inv.getItem(19);
            ItemStack durationItem = inv.getItem(20);
            ItemStack potencyItem = inv.getItem(21);
            ItemStack modifierItem = inv.getItem(22); // Modifier item slot
            ItemStack gunpowder = inv.getItem(23);
            ItemStack dyeItem = inv.getItem(24);

            PotionEffectType effectType = getPotionEffectType(ingredient1, ingredient2);
            if (effectType == null) {
                HumanEntity player = inv.getViewers().get(0);
                player.sendMessage(ChatColor.RED + "Invalid ingredients!");
                return null;
            }

            int duration = calculateDuration(durationItem);
            int potency = calculatePotency(potencyItem);

            boolean isSplash = (gunpowder != null && (gunpowder.getType() == Material.GUNPOWDER));
            ItemStack potion = new ItemStack(isSplash ? Material.SPLASH_POTION : Material.POTION);
            PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

            // Set potion color based on dye
            if (dyeItem != null && dyeItem.getType().toString().endsWith("_DYE")) {
                Color color = getColorFromDye(dyeItem.getType());
                potionMeta.setColor(color);
            } else {
                potionMeta.setColor(Color.YELLOW);
            }

            // Add main effect
            PotionEffect mainEffect = new PotionEffect(effectType, duration, potency);
            potionMeta.addCustomEffect(mainEffect, true);

            // Add modifier effect if present
            if (modifierItem != null && modifierItem.getType() != Material.AIR) {
                String modifierName = modifierItem.getType().name();
                int modifierPotency = calculatePotency(modifierItem);
                int modifierDuration = calculateDuration(modifierItem);
                addModifierEffect(potionMeta, modifierName, modifierPotency, modifierDuration);
            }

            // Handle special lore
            List<String> lore = new ArrayList<>();
            potionMeta.setLore(lore);

            potion.setItemMeta(potionMeta);

            // Consume items
            consumeItem(inv, 0); // Bottle
            consumeItem(inv, 18); // Ingredient 1
            consumeItem(inv, 19); // Ingredient 2
            consumeItem(inv, 20); // Duration item
            consumeItem(inv, 21); // Potency item
            consumeItem(inv, 22); // Modifier item
            consumeItem(inv, 23); // Gunpowder
            consumeItem(inv, 24); // Dye
            consumeItem(inv, 25); // Ender Pearl
            consumeItem(inv, 26); // Pumpkin

            // Play sound and particles

            potion.setAmount(3);

            HumanEntity player = inv.getViewers().get(0);
            Location loc = player.getLocation();
            World world = loc.getWorld();
            world.playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, 1.0f, 1.0f);
            world.spawnParticle(Particle.SPELL_WITCH, loc, 30, 0.5, 0.5, 0.5, 0.1);

            return potion;
        } else {
            HumanEntity player = inv.getViewers().get(0);
            player.sendMessage(ChatColor.RED + "You need a glass bottle to brew a potion!");
            return null;
        }
    }

    private static void consumeItem(Inventory inv, int slot) {
        ItemStack item = inv.getItem(slot);
        if (item != null) {
            item.setAmount(item.getAmount() - 1);
            if (item.getAmount() <= 0) {
                inv.setItem(slot, null);
            } else {
                inv.setItem(slot, item);
            }
        }
    }

    private static void addModifierEffect(PotionMeta potionMeta, String modifierName, int potency, int duration) {
        PotionEffectType modifierEffectType = PotionEffectType.getByName(modifierName);
        if (modifierEffectType != null) {
            PotionEffect modifierEffect = new PotionEffect(modifierEffectType, duration, potency);
            potionMeta.addCustomEffect(modifierEffect, true);
        }
    }

    private static Color getColorFromDye(Material dyeMaterial) {
        switch (dyeMaterial) {
            case RED_DYE:
                return Color.RED;
            case BLUE_DYE:
                return Color.BLUE;
            case GREEN_DYE:
                return Color.GREEN;
            case YELLOW_DYE:
                return Color.YELLOW;
            case ORANGE_DYE:
                return Color.ORANGE;
            case PURPLE_DYE:
                return Color.PURPLE;
            case CYAN_DYE:
                return Color.TEAL;
            case BLACK_DYE:
                return Color.BLACK;
            case WHITE_DYE:
                return Color.WHITE;
            // Add more colors as needed
            default:
                return Color.WHITE;
        }
    }

    private static PotionEffectType getPotionEffectType(ItemStack ingredient1, ItemStack ingredient2) {
        if (ingredient1 != null && ingredient2 != null) {
            // Define your custom recipes here
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.SUGAR) {
                return PotionEffectType.SPEED;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.SLIME_BALL) {
                return PotionEffectType.SLOW;
            }
            if (ingredient1.getType() == Material.HONEYCOMB && ingredient2.getType() == Material.GOLD_INGOT) {
                return PotionEffectType.FAST_DIGGING;
            }
            if (ingredient1.getType() == Material.HONEYCOMB && ingredient2.getType() == Material.CHARCOAL) {
                return PotionEffectType.SLOW_DIGGING;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.BLAZE_POWDER) {
                return PotionEffectType.INCREASE_DAMAGE;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.IRON_NUGGET) {
                return PotionEffectType.WEAKNESS;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.PUFFERFISH) {
                return PotionEffectType.WATER_BREATHING;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.SPIDER_EYE) {
                return PotionEffectType.POISON;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.GOLDEN_APPLE) {
                return PotionEffectType.ABSORPTION;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.PHANTOM_MEMBRANE) {
                return PotionEffectType.SLOW_FALLING;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.ROTTEN_FLESH) {
                return PotionEffectType.HUNGER;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.NAUTILUS_SHELL) {
                return PotionEffectType.DOLPHINS_GRACE;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.RABBIT_FOOT) {
                return PotionEffectType.LUCK;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.BONE) {
                return PotionEffectType.UNLUCK;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.CROSSBOW) {
                return PotionEffectType.BAD_OMEN;
            }
            if (ingredient1.getType() == Material.HONEYCOMB && ingredient2.getType() == Material.EMERALD_BLOCK) {
                return PotionEffectType.HERO_OF_THE_VILLAGE;
            }
            // Add more recipes as needed
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.GLASS) {
                return PotionEffectType.INVISIBILITY;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.RABBIT_HIDE) {
                return PotionEffectType.JUMP;
            }
            if (ingredient1.getType() == Material.HONEYCOMB && ingredient2.getType() == Material.SLIME_BALL) {
                return PotionEffectType.LEVITATION;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.MAGMA_CREAM) {
                return PotionEffectType.FIRE_RESISTANCE;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.GHAST_TEAR) {
                return PotionEffectType.REGENERATION;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getType() == Material.GOLDEN_CARROT) {
                return PotionEffectType.NIGHT_VISION;
            }
            if (ingredient1.getType() == Material.NETHER_WART && ingredient2.getItemMeta().getDisplayName()
                    .equals(ChatColor.LIGHT_PURPLE + "Luminescent Ink")) {
                return PotionEffectType.GLOWING;
            }
        }
        return null;
    }

    private static int calculateDuration(ItemStack durationItem) {
        if (durationItem != null) {
            if (durationItem.getType() == Material.REDSTONE) {
                return 7200; // 6 minutes
            } else if (durationItem.getType() == Material.REDSTONE_BLOCK) {
                return 14400; // 12 minutes
            }else if(durationItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Abyssal Venom")){
                return 28800*2;
            }
        }
        return 1200; // Default duration (3 minutes)
    }

    private static int calculatePotency(ItemStack potencyItem) {
        if (potencyItem != null) {
            if (potencyItem.getType() == Material.GLOWSTONE_DUST) {
                return 1; // Potency level 1
            } else if (potencyItem.getType() == Material.GLOWSTONE) {
                return 2; // Potency level 2
            }else if(potencyItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Abyssal Ink")){
                return 6;
            }
        }
        return 0; // Default potency level
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Brewing Stand")) {
            Player player = (Player) event.getWhoClicked();
            Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory == null) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            // Prevent moving GUI items
            if (isGuiItem(clickedItem)) {
                event.setCancelled(true);
                return;
            }

            // Handle brewing when clicking the brew button
            if (event.getSlot() == 4 && clickedItem.getType() == Material.BREWING_STAND) {
                ItemStack potion = createCustomPotion(event.getInventory());
                if (potion != null) {
                    player.getInventory().addItem(potion);
                    player.sendMessage(ChatColor.GREEN + "You have brewed a potion!");
                }
                event.setCancelled(true);
            }
        }
    }

    private boolean isGuiItem(ItemStack item) {
        if (item.getItemMeta() == null) return false;
        String displayName = item.getItemMeta().getDisplayName();
        return displayName != null && displayName.contains("Place");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("Brewing Stand")) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();
            brewingStandInventories.put(player.getUniqueId(), inventory);
            saveInventory(player.getUniqueId(), inventory);
        }
    }

    /**
     * Saves the player's brewing inventory to the file.
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
     * Loads the player's brewing inventory from the file.
     *
     * @param playerUUID The player's UUID.
     * @return The loaded inventory.
     */
    public Inventory loadInventory(UUID playerUUID) {
        Inventory inventory = createBrewingStandInventory();
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
     * Saves all brewing stand inventories to the file.
     * Call this method when the plugin is disabled to ensure all data is saved.
     */
    public void saveAllInventories() {
        for (Map.Entry<UUID, Inventory> entry : brewingStandInventories.entrySet()) {
            saveInventory(entry.getKey(), entry.getValue());
        }
    }
}
