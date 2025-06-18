package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Mining implements Listener {
    private MinecraftNew plugin = MinecraftNew.getInstance();
    private XPManager xpManager = new XPManager(plugin);
    private Random random = new Random();
    private final OreCountManager oreCountManager = new OreCountManager(plugin);
    private static GemstoneUpgradeSystem upgradeSystemInstance;

    // List of ores to monitor
    public static List<Material> ores = Arrays.asList(
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.STONE,
            Material.DEEPSLATE,
            Material.AMETHYST_BLOCK,
            Material.GRANITE,
            Material.DIORITE,
            Material.ANDESITE,
            Material.TUFF,
            Material.BASALT,
            Material.SMOOTH_BASALT,
            Material.BLACKSTONE,
            Material.NETHERRACK,
            Material.END_STONE,
            Material.ANCIENT_DEBRIS
    );
    public static List<Material> onlyOres = Arrays.asList(
            Material.COAL_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS
    );
        public void compactStoneBlocks(Player player) {
            // Define the array of stone-based materials
            Material[] stoneBasedBlocks = {
                    Material.STONE, Material.GRANITE, Material.DIORITE, Material.ANDESITE,
                    Material.DEEPSLATE, Material.TUFF, Material.CALCITE, Material.GRAVEL,
                    Material.COBBLED_DEEPSLATE, Material.TUFF, Material.COBBLESTONE
            };

            // Get the active pet level if the player has one
            PetManager petManager = PetManager.getInstance(plugin);
            int petLevel = petManager.getActivePet(player) != null ? petManager.getActivePet(player).getLevel() : 1;

            // Calculate the required amount of materials based on pet level
            int requiredMaterials = Math.max(256 - (petLevel - 1) * (256 - 64) / 99, 64);

            // Count total stone-based blocks in the player's inventory
            int totalStoneCount = 0;
            for (Material material : stoneBasedBlocks) {
                totalStoneCount += countMaterialInInventory(player, material);
            }

            // Check if the player has enough blocks to compact
            if (totalStoneCount >= requiredMaterials) {
                // Remove the required amount of stone-based blocks from inventory
                removeMaterialsFromInventory(player, stoneBasedBlocks, requiredMaterials);

                // Give the player the custom Compact Stone item
                giveCompactStone(player);

                // Notify the player
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 1.0f, 1.0f);
            }
        }

    /**
     * Counts the total number of a specific material in a player's inventory.
     * @param player The player whose inventory is being checked.
     * @param material The material to count.
     * @return The count of the material in the inventory.
     */
    private int countMaterialInInventory(Player player, Material material) {
        ItemStack[] items = player.getInventory().getContents();
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Removes a specified number of stone-based blocks from the player's inventory.
     * @param player The player from whose inventory materials are removed.
     * @param materials The materials to remove.
     * @param count The total number of blocks to remove.
     */
    private void removeMaterialsFromInventory(Player player, Material[] materials, int count) {
        for (Material material : materials) {
            if (count <= 0) break;
            count = removeMaterialFromInventory(player, material, count);
        }
    }

    /**
     * Removes a specific amount of a material from the inventory.
     * @param player The player.
     * @param material The material to remove.
     * @param count The number to remove.
     * @return The remaining count to be removed.
     */
    private int removeMaterialFromInventory(Player player, Material material, int count) {
        ItemStack[] items = player.getInventory().getContents();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && item.getType() == material) {
                int inStack = item.getAmount();
                if (inStack > count) {
                    item.setAmount(inStack - count);
                    return 0;
                } else {
                    player.getInventory().clear(i);
                    count -= inStack;
                }
            }
        }
        return count;
    }

    /**
     * Give the compact stone item to the player.
     * @param player The player to receive the item.
     */
    private void giveCompactStone(Player player) {
        // Placeholder for giving the custom item to the player.
        ItemStack compactStone = ItemRegistry.getCompactStone();
        player.getInventory().addItem(compactStone);
    }
    @EventHandler
    public void onOreMine(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if(onlyOres.contains(block.getType())){
            oreCountManager.incrementOreCount(player);
        }
        if(ores.contains(block.getType())){

            PetManager petManager = PetManager.getInstance(plugin);
            if(petManager.getActivePet(player) != null && petManager.getActivePet(player).getPerks().contains(PetManager.PetPerk.ROCK_EATER)){
                compactStoneBlocks(player);
            }
        }
        if (ores.contains(block.getType())) {
            // Check if the player is using Silk Touch
            if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                return; // Silk Touch disables XP and gemstone drops
            }

            boolean isDiamondOre = block.getType() == Material.DIAMOND_ORE ||
                    block.getType() == Material.DEEPSLATE_DIAMOND_ORE;



            // Award XP for mining ores


            int xpAwarded = getXPAwarded(block.getType());

            MiningGemManager gemManager = new MiningGemManager();
            if (tool != null && tool.hasItemMeta()) {
                Set<MiningGemManager.MiningGem> appliedGems = gemManager.getGemsFromItem(tool);

                if (appliedGems.contains(MiningGemManager.MiningGem.LAPIS_GEM)) {
                    xpAwarded = (int) Math.round(xpAwarded * 1.25); // Increase XP by 25%
                }
            }

            xpManager.addXP(player, "Mining", xpAwarded);

            // Handle diamond ore drops manually to ignore Fortune
            if (isDiamondOre) {
                e.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND, 1));
            }

            // Apply haste effect based on Mining level
            int miningLevel = xpManager.getPlayerLevel(player, "Mining");
            double doubleDropChance = (double) miningLevel / 2;

            boolean hasDiamondGem = gemManager.getGemsFromItem(tool).contains(MiningGemManager.MiningGem.DIAMOND_GEM);
            double tripleDropChance = hasDiamondGem ? 10 : 0; // 10% chance for triple drops if Diamond Gem is applied

            double roll = random.nextInt(100) + 1;

            if (isDiamondOre) {
                if (roll <= tripleDropChance) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DIAMOND, 2));
                    player.playSound(player.getLocation(), Sound.BLOCK_NETHERRACK_BREAK, 10, 5);
                }
            } else {
                if (roll <= tripleDropChance) {
                    // Triple drop chance
                    dropAdditionalItems(block, player, 2); // Drop 2 additional stacks
                    player.playSound(player.getLocation(), Sound.BLOCK_NETHERRACK_BREAK, 10, 5);
                } else if (roll <= doubleDropChance) {
                    // Double drop chance
                    dropAdditionalItems(block, player, 1); // Drop 1 additional stack
                    player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_BRICKS_BREAK, 10, 5);
                }
            }

            // Apply haste effect based on Mining level
            grantHaste(player, "Mining", tool);

            // Handle gemstone drops from eligible ores
            if (isGemstoneEligibleOre(block.getType())) {
                handleGemstoneDrop(player, tool, block);
            }

            // Handle rare item drops (optional, unrelated to gemstones)
            if (block.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)) {
                int rollRareItem = random.nextInt(200) + 1;
                if (rollRareItem <= 1) { // 0.5% chance
                    player.getInventory().addItem(ItemRegistry.getPerfectDiamond());
                    player.sendMessage(ChatColor.AQUA + "You discovered a Perfect Diamond!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
                if (rollRareItem <= 2) { // Additional 1% chance
                    player.getInventory().addItem(ItemRegistry.getMithrilChunk());
                    player.sendMessage(ChatColor.GREEN + "You discovered a Mithril Chunk!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }

            // Chance to drop Shiny Emerald relic seed
            Material type = block.getType();
            if (type == Material.EMERALD_ORE || type == Material.DEEPSLATE_EMERALD_ORE) {
                if (random.nextInt(100) < 5) { // 5% chance
                    block.getWorld().dropItemNaturally(block.getLocation(), ItemRegistry.getVerdantRelicShinyEmeraldSeed());
                }
            } else if (type == Material.DIAMOND_ORE || type == Material.DEEPSLATE_DIAMOND_ORE ||
                    type == Material.LAPIS_ORE || type == Material.DEEPSLATE_LAPIS_ORE ||
                    type == Material.REDSTONE_ORE || type == Material.DEEPSLATE_REDSTONE_ORE) {
                if (random.nextInt(100) < 1) { // 1% chance
                    block.getWorld().dropItemNaturally(block.getLocation(), ItemRegistry.getVerdantRelicShinyEmeraldSeed());
                }
            }
        }
    }

    private void dropAdditionalItems(Block block, Player player, int additionalStacks) {
        Collection<ItemStack> drops = block.getDrops(player.getInventory().getItemInMainHand());
        for (int i = 0; i < additionalStacks; i++) {
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
            }
        }
    }
    public void grantHaste(Player player, String skill, ItemStack tool) {
        // Check for legacy gem system for compatibility
        MiningGemManager gemManager = new MiningGemManager();
        Set<MiningGemManager.MiningGem> appliedGems = gemManager.getGemsFromItem(tool);
        boolean hasEmeraldGem = appliedGems.contains(MiningGemManager.MiningGem.EMERALD_GEM);

        // Grant Night Vision if the player has the EMERALD_GEM applied (legacy system)
        if (hasEmeraldGem) {
            int nightVisionDuration = 400; // Night Vision duration (20 ticks per second = 20 seconds)
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisionDuration, 0, false));
        }

        // NEW GOLD FEVER SYSTEM: Check for gemstone upgrade system
        if (tool != null && tool.hasItemMeta() && tool.getItemMeta().hasLore()) {
            // Check if this is a diamond tool with gemstone power
            Material toolType = tool.getType();
            if (toolType == Material.DIAMOND_PICKAXE || toolType == Material.DIAMOND_AXE || 
                toolType == Material.DIAMOND_SHOVEL || toolType == Material.DIAMOND_HOE) {
                
                // Get the gemstone upgrade system instance from the plugin
                GemstoneUpgradeSystem upgradeSystem = getGemstoneUpgradeSystem();
                if (upgradeSystem != null) {
                    int[] goldFeverUpgrades = upgradeSystem.getGoldFeverUpgrades(tool);
                    int chanceBonus = goldFeverUpgrades[0]; // Bonus percentage chance
                    int durationBonus = goldFeverUpgrades[1]; // Bonus duration in seconds
                    int potencyBonus = goldFeverUpgrades[2]; // Bonus haste levels
                    
                    // Base Gold Fever: 5% chance, 15 seconds, Haste I
                    int totalChance = 5 + chanceBonus; // Base 5% + upgrades
                    int totalDuration = (15 + durationBonus) * 20; // Convert to ticks (20 ticks = 1 second)
                    int totalPotency = Math.min(0 + potencyBonus, 2); // Haste levels 0-2 (I-III), cap at 2
                    
                    int roll = random.nextInt(100) + 1; // Roll 1-100
                    
                    if (roll <= totalChance) {
                        // Apply Gold Fever effect
                        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, totalDuration, totalPotency, false));
                        player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
                    }
                    return; // Exit early if using new system
                }
            }
        }

        // LEGACY FALLBACK: Original haste system for tools without gemstone upgrades
        int level = xpManager.getPlayerLevel(player, skill);
        int roll = random.nextInt(100) + 1;
        boolean hasRedstoneGem = appliedGems.contains(MiningGemManager.MiningGem.REDSTONE_GEM);
        int hasteLevel = hasRedstoneGem ? 1 : 0;

        if (roll < 2) { // 2% chance for legacy system
            int duration = 200 + (level * 5);
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel, false));
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
        }
    }
    
    /**
     * Sets the GemstoneUpgradeSystem instance (called from MinecraftNew.onEnable)
     */
    public static void setUpgradeSystemInstance(GemstoneUpgradeSystem upgradeSystem) {
        upgradeSystemInstance = upgradeSystem;
    }
    
    /**
     * Gets the GemstoneUpgradeSystem instance
     */
    private GemstoneUpgradeSystem getGemstoneUpgradeSystem() {
        return upgradeSystemInstance;
    }
    
    /**
     * Converts haste level to roman numeral for display
     */
    private String getRomanNumeral(int level) {
        switch (level) {
            case 1: return "I";
            case 2: return "II"; 
            case 3: return "III";
            default: return String.valueOf(level);
        }
    }






    private boolean isGemstoneEligibleOre(Material ore) {
        return ore == Material.DIAMOND_ORE || ore == Material.DEEPSLATE_DIAMOND_ORE ||
               ore == Material.EMERALD_ORE || ore == Material.DEEPSLATE_EMERALD_ORE ||
               ore == Material.LAPIS_ORE || ore == Material.DEEPSLATE_LAPIS_ORE ||
                ore == Material.GOLD_ORE || ore == Material.DEEPSLATE_GOLD_ORE ||
                ore == Material.IRON_ORE || ore == Material.DEEPSLATE_IRON_ORE ||
                ore == Material.COAL_ORE || ore == Material.DEEPSLATE_COAL_ORE ||
               ore == Material.REDSTONE_ORE || ore == Material.DEEPSLATE_REDSTONE_ORE;
    }


    private int getXPAwarded(Material ore) {
        // Adjusted XP values to balance for approximately 6,000 ores to reach 100,000 XP
        switch (ore) {
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                return 4;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return 10;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return 4;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return 20;
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return 10;
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return 16;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return 100;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return 20;
            case NETHER_QUARTZ_ORE:
                return 6;
            case NETHER_GOLD_ORE:
                return 4;
            case AMETHYST_BLOCK:
                return 4;
            case STONE:
            case DEEPSLATE:
            case GRANITE:
            case DIORITE:
            case ANDESITE:
            case TUFF:
            case BASALT:
            case SMOOTH_BASALT:
            case BLACKSTONE:
            case NETHERRACK:
            case END_STONE:
                return 2; // Basic stones give minimal XP
            case ANCIENT_DEBRIS:
                return 850;
            default:
                return 2; // Default XP for any other blocks
        }
    }
    /**
     * Handles gemstone drops from eligible ores with upgrade bonuses
     */
    private void handleGemstoneDrop(Player player, ItemStack tool, Block block) {
        // Base 8% chance
        double baseChance = 2.0;
        double bonusChance = 0.0;
        
        // Check for Gemstone Yield upgrade (only on diamond tools with upgrade system)
        if (isDiamondTool(tool.getType()) && upgradeSystemInstance != null) {
            int gemstoneYieldLevel = upgradeSystemInstance.getUpgradeLevel(player, tool, GemstoneUpgradeSystem.UpgradeType.GEMSTONE_YIELD);
            // Each level adds 2% chance (max level 6 = +12%, total 20%)
            bonusChance = gemstoneYieldLevel * 2.0;
        }
        
        double totalChance = baseChance + bonusChance;
        double roll = random.nextDouble() * 100;
        
        if (roll <= totalChance) {
            // Create custom gemstone rarity system (separate from ItemRegistry drop rates)
            ItemStack gemstone = getRandomGemstoneByRarity();
            block.getWorld().dropItemNaturally(block.getLocation(), gemstone);
            PlayerMeritManager merit = PlayerMeritManager.getInstance(plugin);
            if (merit.hasPerk(player.getUniqueId(), "Double Gemstones") && random.nextDouble() < 0.5) {
                block.getWorld().dropItemNaturally(block.getLocation(), gemstone.clone());
            }
            
            // Play rarity-based sound effect and send discovery message
            String gemstoneName = ChatColor.stripColor(gemstone.getItemMeta().getDisplayName());
            String rarity = getGemstoneRarity(gemstoneName);
            playGemstoneDiscoverySound(player, gemstoneName);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ You discovered a " + rarity + ChatColor.LIGHT_PURPLE + " " + gemstoneName + "!");
        }
    }

    /**
     * Plays a rarity-based sound effect for gemstone discovery
     * @param player The player who discovered the gemstone
     * @param gemstoneName The name of the gemstone
     */
    private void playGemstoneDiscoverySound(Player player, String gemstoneName) {
        // Common gemstones - basic chime
        if (gemstoneName.equals("Quartz") || gemstoneName.equals("Hematite") || 
            gemstoneName.equals("Obsidian") || gemstoneName.equals("Agate")) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
        }
        // Uncommon gemstones - slightly better
        else if (gemstoneName.equals("Turquoise") || gemstoneName.equals("Amethyst") ||
                 gemstoneName.equals("Citrine") || gemstoneName.equals("Garnet")) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.0f, 1.4f);
        }
        // Rare gemstones - nice bell sound
        else if (gemstoneName.equals("Topaz") || gemstoneName.equals("Peridot") ||
                 gemstoneName.equals("Aquamarine") || gemstoneName.equals("Tanzanite")) {
            player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 1.8f);
        }
        // Epic gemstones - enchanting sound
        else if (gemstoneName.equals("Sapphire") || gemstoneName.equals("Ruby")) {
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
        }
        // Legendary gemstones - the best sound
        else if (gemstoneName.equals("Emerald") || gemstoneName.equals("Diamond")) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
        // Fallback
        else {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        }
    }

    /**
     * Gets the rarity tier of a gemstone by name
     * @param gemstoneName The name of the gemstone
     * @return Colored rarity string
     */
    private String getGemstoneRarity(String gemstoneName) {
        // Common gemstones
        if (gemstoneName.equals("Quartz") || gemstoneName.equals("Hematite") || 
            gemstoneName.equals("Obsidian") || gemstoneName.equals("Agate")) {
            return ChatColor.WHITE + "[Common]";
        }
        // Uncommon gemstones  
        if (gemstoneName.equals("Turquoise") || gemstoneName.equals("Amethyst") ||
            gemstoneName.equals("Citrine") || gemstoneName.equals("Garnet")) {
            return ChatColor.GREEN + "[Uncommon]";
        }
        // Rare gemstones
        if (gemstoneName.equals("Topaz") || gemstoneName.equals("Peridot") ||
            gemstoneName.equals("Aquamarine") || gemstoneName.equals("Tanzanite")) {
            return ChatColor.BLUE + "[Rare]";
        }
        // Epic gemstones
        if (gemstoneName.equals("Sapphire") || gemstoneName.equals("Ruby")) {
            return ChatColor.LIGHT_PURPLE + "[Epic]";
        }
        // Legendary gemstones
        if (gemstoneName.equals("Emerald") || gemstoneName.equals("Diamond")) {
            return ChatColor.GOLD + "[Legendary]";
        }
        return ChatColor.GRAY + "[Unknown]";
    }

    /**
     * Gets a random gemstone based on rarity distribution
     * @return A random gemstone ItemStack
     */
    private ItemStack getRandomGemstoneByRarity() {
        double rarityRoll = random.nextDouble() * 100;
        
        // Rarity distribution (independent of drop chance):
        // Common: 60% (Quartz, Hematite, Obsidian, Agate)
        // Uncommon: 25% (Turquoise, Amethyst, Citrine, Garnet)
        // Rare: 10% (Topaz, Peridot, Aquamarine, Tanzanite)
        // Epic: 4% (Sapphire, Ruby)
        // Legendary: 1% (Emerald, Diamond)
        
        if (rarityRoll < 60.0) {
            // Common gemstones (60%)
            ItemStack[] commonGems = {ItemRegistry.getQuartz(), ItemRegistry.getHematite()};
            return commonGems[random.nextInt(commonGems.length)];
        } else if (rarityRoll < 85.0) {
            // Uncommon gemstones (25%)
            ItemStack[] uncommonGems = {ItemRegistry.getTurquoise(), ItemRegistry.getAmethyst(), 
                                       ItemRegistry.getCitrine(), ItemRegistry.getGarnet()};
            return uncommonGems[random.nextInt(uncommonGems.length)];
        } else if (rarityRoll < 95.0) {
            // Rare gemstones (10%)
            ItemStack[] rareGems = {ItemRegistry.getTopaz(), ItemRegistry.getPeridot(), 
                                   ItemRegistry.getAquamarine(), ItemRegistry.getTanzanite()};
            return rareGems[random.nextInt(rareGems.length)];
        } else if (rarityRoll < 99.0) {
            // Epic gemstones (4%)
            ItemStack[] epicGems = {ItemRegistry.getSapphire(), ItemRegistry.getRuby()};
            return epicGems[random.nextInt(epicGems.length)];
        } else {
            // Legendary gemstones (1%)
            ItemStack[] legendaryGems = {ItemRegistry.getEmerald(), ItemRegistry.getDiamond()};
            return legendaryGems[random.nextInt(legendaryGems.length)];
        }
    }

    private boolean isDiamondTool(Material material) {
        return material == Material.DIAMOND_PICKAXE || material == Material.DIAMOND_AXE ||
                material == Material.DIAMOND_SHOVEL || material == Material.DIAMOND_HOE;
    }
}



