package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class Mining implements Listener {
    private MinecraftNew plugin = MinecraftNew.getInstance();
    private XPManager xpManager = new XPManager(plugin);
    private Random random = new Random();

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

    public ItemStack createCustomItem(Material material, String name, List<String> lore, int amount, boolean unbreakable, boolean addEnchantmentShimmer) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        // Set custom name
        if (name != null) {
            meta.setDisplayName(name);
        }

        // Set lore
        if (lore != null && !lore.isEmpty()) {
            meta.setLore(lore);
        }

        // Add enchantment shimmer if specified
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS); // This hides the enchantments from the item's tooltip
        item.setItemMeta(meta);
        if (addEnchantmentShimmer) {
            // Add a dummy enchantment to create the shimmer effect
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }
        return item;
    }

    public ItemStack mithrilChunk() {
        return createCustomItem(
                Material.LIGHT_BLUE_DYE,
                ChatColor.BLUE + "Mithril Chunk",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock the secrets of Unbreaking.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }

    public ItemStack diamondGemstone() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.DARK_PURPLE + "Diamond Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock triple drop chance.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack lapisGemstone() {
        return createCustomItem(
                Material.LAPIS_LAZULI,
                ChatColor.DARK_PURPLE + "Lapis Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to enrich mining XP gains.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack redstoneGemstone() {
        return createCustomItem(
                Material.REDSTONE,
                ChatColor.DARK_PURPLE + "Redstone Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to enrich Gold Fever.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack emeraldGemstone() {
        return createCustomItem(
                Material.EMERALD,
                ChatColor.DARK_PURPLE + "Emerald Gemstone",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to equipment to unlock night vision chance.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }
    public ItemStack perfectDiamond() {
        return createCustomItem(
                Material.DIAMOND,
                ChatColor.BLUE + "Perfect Diamond",
                List.of(ChatColor.GRAY + "A rare mineral.",
                        "Apply it to a pickaxe to unlock the secrets of Fortune.",
                        "Smithing Item"),
                1,
                false,
                true
        );
    }

    @EventHandler
    public void onOreMine(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (ores.contains(block.getType())) {
            // Check if the player is using Silk Touch
            if (tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                return; // Silk Touch disables XP and gemstone drops
            }

            // Handle gemstone drops
            switch (block.getType()) {
                case DEEPSLATE_DIAMOND_ORE:
                    if (random.nextInt(100) < 1) { // 4% chance
                        player.getInventory().addItem(diamondGemstone());
                        player.sendMessage(ChatColor.AQUA + "You discovered a Diamond Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case EMERALD_ORE:
                    if (random.nextInt(100) < 10) { // 10% chance
                        player.getInventory().addItem(emeraldGemstone());
                        player.sendMessage(ChatColor.GREEN + "You discovered an Emerald Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case DEEPSLATE_LAPIS_ORE:
                    if (random.nextInt(100) < 0.2) { // 2% chance
                        player.getInventory().addItem(lapisGemstone());
                        player.sendMessage(ChatColor.BLUE + "You discovered a Lapis Gemstone!");
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                    break;

                case DEEPSLATE_REDSTONE_ORE:
                    if (random.nextInt(100) < 0.1) { // 1% chance
                        player.getInventory().addItem(redstoneGemstone());
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
            int doubleDropChance = miningLevel / 2;

            boolean hasDiamondGem = gemManager.getGemsFromItem(tool).contains(MiningGemManager.MiningGem.DIAMOND_GEM);
            int tripleDropChance = hasDiamondGem ? 10 : 0; // 10% chance for triple drops if Diamond Gem is applied

            int roll = random.nextInt(100) + 1;

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
                    player.getInventory().addItem(perfectDiamond());
                    player.sendMessage(ChatColor.AQUA + "You discovered a Perfect Diamond!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
                if (rollRareItem <= 2) { // Additional 1% chance
                    player.getInventory().addItem(mithrilChunk());
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

        if (roll >= 90) { // 11% chance to grant Haste
            int duration = 200 + (level * 5); // Duration increases with level

            // Apply or extend Haste effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel, false));
            player.sendMessage(ChatColor.YELLOW + "Gold Fever! Haste Level: " + (hasRedstoneGem ? "II" : "I"));
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
                return 2;
            case IRON_ORE:
            case DEEPSLATE_IRON_ORE:
                return 5;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                return 2;
            case GOLD_ORE:
            case DEEPSLATE_GOLD_ORE:
                return 10;
            case REDSTONE_ORE:
            case DEEPSLATE_REDSTONE_ORE:
                return 5;
            case LAPIS_ORE:
            case DEEPSLATE_LAPIS_ORE:
                return 8;
            case EMERALD_ORE:
            case DEEPSLATE_EMERALD_ORE:
                return 50;
            case DIAMOND_ORE:
            case DEEPSLATE_DIAMOND_ORE:
                return 80;
            case NETHER_QUARTZ_ORE:
                return 3;
            case NETHER_GOLD_ORE:
                return 2;
            case AMETHYST_BLOCK:
                return 2;
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
                return 1; // Basic stones give minimal XP
            case ANCIENT_DEBRIS:
                return 850;
            default:
                return 2; // Default XP for any other blocks
        }
    }
}
