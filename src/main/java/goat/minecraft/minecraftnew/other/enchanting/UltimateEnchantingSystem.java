package goat.minecraft.minecraftnew.other.enchanting;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.fishing.BaitApplicationSystem;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingUpgradeSystem;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class UltimateEnchantingSystem implements Listener {

    // Hard-coded costs for ultimate enchants
    private static final int ULTIMATE_ENCHANT_COST_LAPIS = 64;
    private static final int FORBIDDEN_BOOK_COST = 4;
    // Predefined lapis cost for upgrading a normal enchant (sharpness/efficiency/protection)
    private static final int UPGRADE_ENCHANT_COST_LAPIS = 16;

    /**
     * Mapping from Material -> List of Custom Enchant names.
     */
    private static final Map<Material, List<String>> CUSTOM_ENCHANTS_BY_TYPE = new HashMap<>();

    // Define the MELEE and TOOLS sets (provided in your original code)
    public static final Set<Material> MELEE = EnumSet.of(
            Material.WOODEN_SWORD,  Material.STONE_SWORD,
            Material.IRON_SWORD,    Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD, Material.NETHERITE_SWORD,
            Material.TRIDENT
    );

    public static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE,  Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,    Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE,      Material.STONE_AXE,
            Material.IRON_AXE,        Material.GOLDEN_AXE,
            Material.DIAMOND_AXE,     Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL,   Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,     Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL,  Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE,      Material.STONE_HOE,
            Material.IRON_HOE,        Material.GOLDEN_HOE,
            Material.DIAMOND_HOE,     Material.NETHERITE_HOE,
            Material.SHEARS,          Material.FISHING_ROD,
            Material.FLINT_AND_STEEL
    );

    // Subset of shovels for the Mulch ultimate enchantment
    public static final Set<Material> SHOVELS = EnumSet.of(
            Material.WOODEN_SHOVEL,  Material.STONE_SHOVEL,
            Material.IRON_SHOVEL,    Material.GOLDEN_SHOVEL,
            Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
    );

    // Subset of hoes for the Scythe ultimate enchantment
    public static final Set<Material> HOES = EnumSet.of(
            Material.WOODEN_HOE, Material.STONE_HOE,
            Material.IRON_HOE, Material.GOLDEN_HOE,
            Material.DIAMOND_HOE, Material.NETHERITE_HOE
    );

    // Define a set for armor pieces
    public static final Set<Material> ARMOR = EnumSet.of(
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS
    );

    /**
     * Registers custom enchantments and populates the mapping.
     */
    public void registerCustomEnchants() {
        // 1) Bows
        registerEnchantmentForType("Ultimate: Headshot", 1, false, Material.BOW, Material.CROSSBOW);
        registerEnchantmentForType("Ultimate: Rebound", 5, false, Material.BOW, Material.CROSSBOW);

        // 2) Melee
        registerEnchantmentForType("Ultimate: Warp", 1, false, MELEE.toArray(new Material[0]));
        // Simple ghost sword enchantment
        registerEnchantmentForType("Ultimate: Shred", 1, false, MELEE.toArray(new Material[0]));
        // New Enchantment for thrown sword
        registerEnchantmentForType("Ultimate: Loyal", 1, false, MELEE.toArray(new Material[0]));

        // 3) Tools
        registerEnchantmentForType("Ultimate: Hammer", 1, false, TOOLS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Treecapitator", 1, false, TOOLS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Mulch", 1, false, SHOVELS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Scythe", 1, false, HOES.toArray(new Material[0]));
    }

    /**
     *
     * Helper to register enchantments with the CustomEnchantmentManager and populate the mapping.
     */
    private void registerEnchantmentForType(String enchantName, int maxLevel, boolean isTreasure, Material... types) {
        // Register with your CustomEnchantmentManager
        CustomEnchantmentManager.registerEnchantment(enchantName, maxLevel, isTreasure);

        // Populate the mapping for each material
        for (Material mat : types) {
            CUSTOM_ENCHANTS_BY_TYPE.computeIfAbsent(mat, k -> new ArrayList<>()).add(enchantName);
        }
    }

    @EventHandler
    public void onPlayerRightClickEnchantmentTable(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE &&
                event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
                openUltimateEnchantmentGUI(event.getPlayer());
        }
    }

    public void openUltimateEnchantmentGUI(Player player) {
        // Get the held item from the player's main hand
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Validate that the item is a sword, tool, bow, or armor piece
        if (!isBow(heldItem) && !MELEE.contains(heldItem.getType()) &&
                !TOOLS.contains(heldItem.getType()) && !ARMOR.contains(heldItem.getType())) {
            player.sendMessage(ChatColor.RED + "You must be holding a sword, tool, bow, or armor piece to use this!");
            return;
        }

        // Get relevant ultimate enchantments for this item type (if any)
        List<String> relevantEnchants = CUSTOM_ENCHANTS_BY_TYPE
                .getOrDefault(heldItem.getType(), Collections.emptyList());

        // Create a 6x9 GUI
        Inventory inv = Bukkit.createInventory(
                new UltimateEnchantInventoryHolder(),
                54,
                ChatColor.DARK_PURPLE + "Ultimate Enchantment"
        );

        // Put the held item in the center (slot 22)
        int editableSlot = 22;
        inv.setItem(editableSlot, heldItem.clone());

        // Fill the rest of the GUI with filler glass
        for (int i = 0; i < 54; i++) {
            if (i == editableSlot) continue;
            inv.setItem(i, createGuiFiller());
        }

        // Place icons for up to 8 ultimate enchants in specific slots
        if (isFishingRod(heldItem.getType())) {
            inv.setItem(53, createAnglerUpgradeButton(heldItem));
        } else if (isDiamondTool(heldItem.getType())) {
            inv.setItem(53, createGemstoneUpgradeButton(heldItem));
        }


        // ----------------------------
        // Add the Upgrade Segment (slots 47–51)
        // ----------------------------
        // Determine which upgrade applies based on the held item type
        Enchantment requiredEnchantment = null;
        String upgradeName = "";
        int numTiers = 0;
        boolean isUpgradeApplicable = false;
        if (MELEE.contains(heldItem.getType()) && !isBow(heldItem)) {
            // For swords: sharpness (using vanilla DAMAGE_ALL)
            requiredEnchantment = Enchantment.SHARPNESS;
            upgradeName = "Sharpness";
            numTiers = 5;
            isUpgradeApplicable = true;
        } else if (TOOLS.contains(heldItem.getType())) {
            // For tools: efficiency (using DIG_SPEED)
            requiredEnchantment = Enchantment.EFFICIENCY;
            upgradeName = "Efficiency";
            numTiers = 5;
            isUpgradeApplicable = true;
        } else if (ARMOR.contains(heldItem.getType())) {
            // For armor: protection (using PROTECTION_ENVIRONMENTAL)
            requiredEnchantment = Enchantment.PROTECTION;
            upgradeName = "Protection";
            numTiers = 4;
            isUpgradeApplicable = true;
        }
        if (isUpgradeApplicable && requiredEnchantment != null) {
            int currentLevel = heldItem.getEnchantmentLevel(requiredEnchantment);
            // For each tier, assign a pane in slots starting at 47
            for (int i = 0; i < numTiers; i++) {
                int tier = i + 1;
                int slot = 47 + i;
                ItemStack pane;
                if (currentLevel >= tier) {
                    // Already acquired: green pane with an enchanted look
                    pane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                } else {
                    // Not yet acquired: red pane
                    pane = new ItemStack(Material.RED_STAINED_GLASS_PANE);
                }
                ItemMeta meta = pane.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + upgradeName + " " + tier);
                    List<String> lore = new ArrayList<>();
                    if (currentLevel >= tier) {
                        lore.add(ChatColor.GREEN + "Tier " + tier + " acquired.");
                    } else if (currentLevel == tier - 1) {
                        lore.add(ChatColor.BLUE + "Cost: " + UPGRADE_ENCHANT_COST_LAPIS + " Lapis Lazuli");
                        // Show the forbidden book cost dynamically for this tier upgrade.
                        lore.add(ChatColor.BLUE + "Plus: " + tier + " Forbidden Book" + (tier > 1 ? "s" : ""));
                        lore.add(ChatColor.YELLOW + "Click to purchase.");
                    } else {
                        lore.add(ChatColor.RED + "Locked. Purchase previous tiers first.");
                    }
                    meta.setLore(lore);
                    pane.setItemMeta(meta);
                }
                inv.setItem(slot, pane);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Creates an ItemStack representing an ultimate enchantment icon.
     */
    private ItemStack createCustomIcon(String enchantName) {
        ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + enchantName);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.BLUE + "Cost: 64 Lapis & 4 Forbidden Books.");
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            icon.setItemMeta(meta);
        }
        return icon;
    }

    /**
     * Creates a gemstone upgrade button for diamond tools.
     */
    private ItemStack createGemstoneUpgradeButton(ItemStack tool) {
        ItemStack button = new ItemStack(Material.EMERALD);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Gemstone Upgrades");
            List<String> lore = new ArrayList<>();
            
            // Check if tool has gemstone power
            int totalPower = getGemstonePower(tool);
            if (totalPower > 0) {
                lore.add(ChatColor.GRAY + "Total Power: " + ChatColor.WHITE + totalPower);
                lore.add(ChatColor.YELLOW + "Click to open upgrade tree!");
            } else {
                lore.add(ChatColor.RED + "No gemstone power detected.");
                lore.add(ChatColor.GRAY + "Apply gemstones to this tool first.");
            }
            
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }


    /**
     * Creates an effigy upgrade button for Spirit Energy axes.
     */

    /**
     * Creates an angler upgrade button for fishing rods.
     */
    private ItemStack createAnglerUpgradeButton(ItemStack rod) {
        ItemStack button = new ItemStack(Material.PRISMARINE_CRYSTALS);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Angler Upgrades");
            List<String> lore = new ArrayList<>();
            int energy = BaitApplicationSystem.getRodAnglerEnergyStatic(rod);
            if (energy > 0) {
                lore.add(ChatColor.GRAY + "Angler Energy: " + ChatColor.WHITE + energy + "%");
                lore.add(ChatColor.YELLOW + "Click to open upgrade tree!");
            } else {
                lore.add(ChatColor.RED + "No Angler Energy detected.");
                lore.add(ChatColor.GRAY + "Apply bait to this rod first.");
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

    /**

    /**
     * Checks if the item is a diamond tool.
     */
    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_AXE ||
         material == Material.NETHERITE_PICKAXE ||
               material == Material.DIAMOND_SHOVEL || material == Material.DIAMOND_HOE ||
               material == Material.DIAMOND_SWORD;
    }

    /**
     * Returns true if the material is a fishing rod.
     */
    private boolean isFishingRod(Material material) {
        return material == Material.FISHING_ROD;
    }

    /**
     * Returns true if the material is a sword for soul power.
     */
