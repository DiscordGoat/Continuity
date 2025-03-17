package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
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

            // Handle gemstone drops
            switch (block.getType()) {
                case DEEPSLATE_DIAMOND_ORE:
                    if (random.nextInt(100) < 1) { // 4% chance
                        player.getInventory().addItem(ItemRegistry.getDiamondGemstone());
                        player.sendMessage(ChatColor.AQUA + "You discovered a Diamond Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case EMERALD_ORE:
                    if (random.nextInt(100) < 10) { // 10% chance
                        player.getInventory().addItem(ItemRegistry.getEmeraldGemstone());
                        player.sendMessage(ChatColor.GREEN + "You discovered an Emerald Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case DEEPSLATE_LAPIS_ORE:
                    if (random.nextInt(100) < 0.2) { // 2% chance
                        player.getInventory().addItem(ItemRegistry.getLapisGemstone());
                        player.sendMessage(ChatColor.BLUE + "You discovered a Lapis Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case DEEPSLATE_REDSTONE_ORE:
                    if (random.nextInt(100) < 0.1) { // 1% chance
                        player.getInventory().addItem(ItemRegistry.getRedstoneGemstone());
                        player.sendMessage(ChatColor.RED + "You discovered a Redstone Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                default:
                    break;
            }

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


            // Apply haste effect based on Mining level
            int miningLevel = xpManager.getPlayerLevel(player, "Mining");
            double doubleDropChance = (double) miningLevel / 2;

            boolean hasDiamondGem = gemManager.getGemsFromItem(tool).contains(MiningGemManager.MiningGem.DIAMOND_GEM);
            double tripleDropChance = hasDiamondGem ? 10 : 0; // 10% chance for triple drops if Diamond Gem is applied

            double roll = random.nextInt(100) + 1;

            if (roll <= tripleDropChance) {
                // Triple drop chance
                dropAdditionalItems(block, player, 2); // Drop 2 additional stacks
                player.playSound(player.getLocation(), Sound.BLOCK_NETHERRACK_BREAK, 10, 5);
            } else if (roll <= doubleDropChance) {
                // Double drop chance
                dropAdditionalItems(block, player, 1); // Drop 1 additional stack
                player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_BRICKS_BREAK, 10, 5);
            }

            // Apply haste effect based on Mining level
            grantHaste(player, "Mining", tool);

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

        int level = xpManager.getPlayerLevel(player, skill); // Get the player's current mining level
        int roll = random.nextInt(100) + 1; // Roll a random number between 1 and 100

        // Check if the tool has the REDSTONE_GEM or EMERALD_GEM applied using lore
        MiningGemManager gemManager = new MiningGemManager();
        Set<MiningGemManager.MiningGem> appliedGems = gemManager.getGemsFromItem(tool);

        // Check for specific gemstones
        boolean hasRedstoneGem = appliedGems.contains(MiningGemManager.MiningGem.REDSTONE_GEM);
        boolean hasEmeraldGem = appliedGems.contains(MiningGemManager.MiningGem.EMERALD_GEM);

        // Determine the Haste level
        int hasteLevel = hasRedstoneGem ? 1 : 0; // Default to Haste I (level 0), upgrade to Haste II (level 1) if REDSTONE_GEM is applied

        if (roll < 2) { // 11% chance to grant Haste
            int duration = 200 + (level * 5); // Duration increases with level

            // Apply or extend Haste effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel, false));
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
        }

        // Grant Night Vision if the player has the EMERALD_GEM applied
        if (hasEmeraldGem) {
            int nightVisionDuration = 400; // Night Vision duration (20 ticks per second = 20 seconds)
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisionDuration, 0, false));
        }
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
                return 160;
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
}
