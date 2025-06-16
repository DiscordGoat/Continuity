package goat.minecraft.minecraftnew.subsystems.enchanting;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.forestry.EffigyApplicationSystem;
import goat.minecraft.minecraftnew.subsystems.forestry.EffigyUpgradeSystem;
import goat.minecraft.minecraftnew.subsystems.fishing.BaitApplicationSystem;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingUpgradeSystem;
import goat.minecraft.minecraftnew.subsystems.combat.SoulApplicationSystem;
import goat.minecraft.minecraftnew.subsystems.combat.SoulUpgradeSystem;
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

import goat.minecraft.minecraftnew.subsystems.enchanting.EnchantmentUtils;

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

        // 2) Melee
        registerEnchantmentForType("Ultimate: Warp", 1, false, MELEE.toArray(new Material[0]));
        // Simple ghost sword enchantment
        registerEnchantmentForType("Ultimate: Shred", 1, false, MELEE.toArray(new Material[0]));
        // New Enchantment for thrown sword
        registerEnchantmentForType("Ultimate: Loyal", 1, false, MELEE.toArray(new Material[0]));

        // 3) Tools
        registerEnchantmentForType("Ultimate: Hammer", 1, false, TOOLS.toArray(new Material[0]));
        registerEnchantmentForType("Ultimate: Treecapitator", 1, false, TOOLS.toArray(new Material[0]));
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
        int[] iconSlots = {12, 13, 14, 21, 23, 30, 31, 32};
        int enchantCount = Math.min(relevantEnchants.size(), 8);

        for (int i = 0; i < enchantCount && i < iconSlots.length; i++) {
            String enchantName = relevantEnchants.get(i);
            inv.setItem(iconSlots[i], createCustomIcon(enchantName));
        }

        if (isEffigyAxe(heldItem.getType())) {
            inv.setItem(53, createEffigyUpgradeButton(heldItem));
        } else if (isSoulWeapon(heldItem.getType())) {
            inv.setItem(53, createSoulUpgradeButton(heldItem));
        } else if (isFishingRod(heldItem.getType())) {
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
            requiredEnchantment = Enchantment.DAMAGE_ALL;
            upgradeName = "Sharpness";
            numTiers = 5;
            isUpgradeApplicable = true;
        } else if (TOOLS.contains(heldItem.getType())) {
            // For tools: efficiency (using DIG_SPEED)
            requiredEnchantment = Enchantment.DIG_SPEED;
            upgradeName = "Efficiency";
            numTiers = 5;
            isUpgradeApplicable = true;
        } else if (ARMOR.contains(heldItem.getType())) {
            // For armor: protection (using PROTECTION_ENVIRONMENTAL)
            requiredEnchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
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
    private ItemStack createEffigyUpgradeButton(ItemStack axe) {
        ItemStack button = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "Effigy Upgrades");
            List<String> lore = new ArrayList<>();
            int energy = EffigyApplicationSystem.getAxeSpiritEnergy(axe);
            if (energy > 0) {
                lore.add(ChatColor.GRAY + "Spirit Energy: " + ChatColor.WHITE + energy + "%");
                lore.add(ChatColor.YELLOW + "Click to open upgrade tree!");
            } else {
                lore.add(ChatColor.RED + "No Spirit Energy detected.");
                lore.add(ChatColor.GRAY + "Apply effigies to this axe first.");
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

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
     * Creates a soul upgrade button for weapons with Soul Power.
     */
    private ItemStack createSoulUpgradeButton(ItemStack weapon) {
        ItemStack button = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = button.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_AQUA + "Soul Upgrades");
            List<String> lore = new ArrayList<>();
            int power = SoulApplicationSystem.getWeaponSoulPower(weapon);
            if (power > 0) {
                lore.add(ChatColor.GRAY + "Soul Power: " + ChatColor.WHITE + power + "%");
                lore.add(ChatColor.YELLOW + "Click to open upgrade tree!");
            } else {
                lore.add(ChatColor.RED + "No Soul Power detected.");
                lore.add(ChatColor.GRAY + "Apply soul items first.");
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }
        return button;
    }

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
     * Returns true if the material is a diamond or netherite axe.
     */
    private boolean isEffigyAxe(Material material) {
        return material == Material.DIAMOND_AXE || material == Material.NETHERITE_AXE;
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
    private boolean isSoulWeapon(Material material) {
        return material.toString().endsWith("_SWORD");
    }

    /**
     * Gets the total gemstone power from a tool.
     */
    private int getGemstonePower(ItemStack item) {
        return goat.minecraft.minecraftnew.subsystems.mining.GemstoneApplicationSystem.getToolGemstonePower(item);
    }

    /**
     * Handles inventory clicks in the Ultimate Enchantment GUI.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof UltimateEnchantInventoryHolder)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Create a single XPManager instance for this event
        XPManager xpManager = new XPManager(MinecraftNew.getInstance());

        // Allow the player to click the held item (slot 22) without interference
        if (event.getSlot() == 22) {
            event.setCancelled(true);
            return;
        }

        // -------------
        // Handle Upgrade Pane Clicks (slots 47–51)
        // -------------
        if (event.getSlot() >= 47 && event.getSlot() <= 51) {
            // Determine the upgrade details based on held item type
            Enchantment requiredEnchantment = null;
            String upgradeName = "";
            int numTiers = 0;
            boolean isUpgradeApplicable = false;
            if (MELEE.contains(handItem.getType()) && !isBow(handItem)) {
                requiredEnchantment = Enchantment.DAMAGE_ALL;
                upgradeName = "Sharpness";
                numTiers = 5;
                isUpgradeApplicable = true;
            } else if (TOOLS.contains(handItem.getType())) {
                requiredEnchantment = Enchantment.DIG_SPEED;
                upgradeName = "Efficiency";
                numTiers = 5;
                isUpgradeApplicable = true;
            } else if (ARMOR.contains(handItem.getType())) {
                requiredEnchantment = Enchantment.PROTECTION_ENVIRONMENTAL;
                upgradeName = "Protection";
                numTiers = 4;
                isUpgradeApplicable = true;
            }
            if (!isUpgradeApplicable || requiredEnchantment == null) return;

            // Map slot to the tier (slot 47 -> tier 1, etc.)
            int clickedTier = event.getSlot() - 46; // so slot 47 is tier 1, 48 is tier 2, etc.
            if (clickedTier > numTiers) return; // In case of armor, if slot 51 is clicked

            int currentLevel = handItem.getEnchantmentLevel(requiredEnchantment);
            // Ensure that the player is purchasing the next tier in sequence
            if (currentLevel != clickedTier - 1) {
                player.sendMessage(ChatColor.RED + "You must purchase previous tiers first!");
                return;
            }
            // Check if the player has enough Lapis Lazuli
            if (!playerHasEnoughMaterial(player, Material.LAPIS_LAZULI, UPGRADE_ENCHANT_COST_LAPIS)) {
                player.sendMessage(ChatColor.RED + "Insufficient Lapis Lazuli for upgrade!");
                return;
            }
            // NEW: Check if the player has enough Forbidden Books for this tier upgrade.
            if (!playerHasEnoughForbiddenBooks(player, clickedTier)) {
                player.sendMessage(ChatColor.RED + "Insufficient Forbidden Books for upgrade! You need "
                        + clickedTier + " Forbidden Book" + (clickedTier > 1 ? "s" : "") + "!");
                return;
            }
            // Subtract both costs: lapis and the required number of forbidden books.
            removeMaterialFromPlayer(player, Material.LAPIS_LAZULI, UPGRADE_ENCHANT_COST_LAPIS);
            removeForbiddenBooksFromPlayer(player, clickedTier);
            // Use EnchantmentUtils to increment the enchantment.
            EnchantmentUtils.incrementEnchantment(player, handItem, null, requiredEnchantment);
            // Award XP for a successful upgrade.
            xpManager.addXP(player, "Smithing", 100);
            player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP,   // nice chime
                    1.0f,                          // volume
                    1.0f                           // pitch
            );

// ─── NEW: spawn enchantment particles ──────────────────────────────────────────
// this will create a swirl of enchantment particles around the player
            player.getWorld().spawnParticle(
                    Particle.END_ROD,   // Minecraft’s enchant‐table swirl
                    player.getLocation().add(0, 1, 0), // center at head height
                    30,                            // count
                    0.5, 1.0, 0.5                  // x/y/z offsets for spread
            );

            player.sendMessage(ChatColor.GREEN + upgradeName + " upgraded to tier " + clickedTier + "!");
            player.closeInventory();
            openUltimateEnchantmentGUI(player);
            return;
        }

        // Handle Upgrade Button Click (slot 53)
        if (event.getSlot() == 53) {
            if (clickedItem.getType() == Material.SOUL_TORCH && isEffigyAxe(handItem.getType())) {
                if (EffigyApplicationSystem.getAxeSpiritEnergy(handItem) > 0) {
                    EffigyUpgradeSystem effigyUpgradeSystem =
                            new EffigyUpgradeSystem(MinecraftNew.getInstance());
                    effigyUpgradeSystem.openUpgradeGUI(player, handItem);
                }
                return;
            }
            if (clickedItem.getType() == Material.DIAMOND_SWORD && isSoulWeapon(handItem.getType())) {
                SoulUpgradeSystem upgradeSystem = new SoulUpgradeSystem(MinecraftNew.getInstance());
                upgradeSystem.openUpgradeGUI(player, handItem);
                return;
            }
            if (clickedItem.getType() == Material.PRISMARINE_CRYSTALS && isFishingRod(handItem.getType())) {
                FishingUpgradeSystem fishingUpgradeSystem =
                        new FishingUpgradeSystem(MinecraftNew.getInstance());
                fishingUpgradeSystem.openUpgradeGUI(player, handItem);
                return;
            }
            if (clickedItem.getType() == Material.EMERALD && isDiamondTool(handItem.getType())) {
                goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem upgradeSystem =
                        new goat.minecraft.minecraftnew.subsystems.mining.GemstoneUpgradeSystem(MinecraftNew.getInstance());
                upgradeSystem.openUpgradeGUIFromExternal(player, handItem);
                return;
            }
        }


        // -------------
        // Handle Ultimate Enchantment Clicks (icons in other slots)
        // -------------
        // Check if the player has enough materials for ultimate enchants
        if (!playerHasEnoughMaterial(player, Material.LAPIS_LAZULI, ULTIMATE_ENCHANT_COST_LAPIS) ||
                !playerHasEnoughForbiddenBooks(player, FORBIDDEN_BOOK_COST)) {
            player.sendMessage(ChatColor.RED + "You need 64 Lapis Lazuli and 4 Forbidden Books to apply this enchantment!");
            return;
        }

        // Process ultimate enchantments and award bonus XP after applying them.
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Warp")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Warp", 1);
            xpManager.addXP(player, "Smithing", 500);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Shred")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Shred", 1);
            xpManager.addXP(player, "Smithing", 500);
        }
        // New check for Loyal enchantment
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Loyal")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Loyal", 1);
            xpManager.addXP(player, "Smithing", 500);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Treecapitator")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Treecapitator", 1);
            xpManager.addXP(player, "Smithing", 500);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Hammer")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Hammer", 1);
            xpManager.addXP(player, "Smithing", 500);
        }
        if(clickedItem.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Ultimate: Headshot")){
            CustomEnchantmentManager.addUltimateEnchantment(player, null, handItem, "Ultimate: Headshot", 1);
            xpManager.addXP(player, "Smithing", 500);
        }

        // Remove cost materials for ultimate enchantments
        removeMaterialFromPlayer(player, Material.LAPIS_LAZULI, ULTIMATE_ENCHANT_COST_LAPIS);
        removeForbiddenBooksFromPlayer(player, FORBIDDEN_BOOK_COST);
    }

    /**
     * Returns true if the given ItemStack is a bow (but not a crossbow).
     */
    public boolean isBow(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String name = item.getType().toString().toUpperCase();
        return name.endsWith("BOW") && !name.endsWith("CROSSBOW");
    }

    /**
     * Creates the filler item for the GUI.
     */
    private ItemStack createGuiFiller() {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLACK + "");
            filler.setItemMeta(meta);
        }
        return filler;
    }

    /**
     * Simple InventoryHolder to distinguish this GUI.
     */
    private static class UltimateEnchantInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    /**
     * Checks if a player has at least 'amount' of the given material in their inventory.
     */
    private boolean playerHasEnoughMaterial(Player player, Material mat, int amount) {
        int count = 0;
        for (ItemStack is : player.getInventory()) {
            if (is != null && is.getType() == mat) {
                count += is.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    /**
     * Removes 'amount' of 'mat' from the player's inventory.
     */
    private void removeMaterialFromPlayer(Player player, Material mat, int amount) {
        int toRemove = amount;
        for (ItemStack is : player.getInventory()) {
            if (is == null || is.getType() != mat) continue;

            if (is.getAmount() > toRemove) {
                is.setAmount(is.getAmount() - toRemove);
                return;
            } else {
                toRemove -= is.getAmount();
                is.setAmount(0);
            }
            if (toRemove <= 0) break;
        }
    }

    /**
     * Checks if a player has at least 'amount' forbidden books.
     */
    private boolean playerHasEnoughForbiddenBooks(Player player, int amount) {
        int count = 0;
        for (ItemStack is : player.getInventory()) {
            if (is != null && is.isSimilar(ItemRegistry.getForbiddenBook())) {
                count += is.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    /**
     * Removes 'amount' forbidden books from the player's inventory.
     */
    private void removeForbiddenBooksFromPlayer(Player player, int amount) {
        int toRemove = amount;
        for (ItemStack is : player.getInventory()) {
            if (is == null || !is.isSimilar(ItemRegistry.getForbiddenBook())) continue;

            if (is.getAmount() > toRemove) {
                is.setAmount(is.getAmount() - toRemove);
                return;
            } else {
                toRemove -= is.getAmount();
                is.setAmount(0);
            }
            if (toRemove <= 0) break;
        }
    }
}
