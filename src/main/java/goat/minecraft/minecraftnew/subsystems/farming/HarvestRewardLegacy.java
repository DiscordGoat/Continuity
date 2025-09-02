package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Reinstates the original harvest reward chances, items, and pet awards with messages.
 */
public class HarvestRewardLegacy {
    private static final Random random = new Random();

    public static void grant(Player player, Material blockType, Location dropLoc) {
        switch (blockType) {
            case WHEAT: {
                double roll = random.nextDouble();
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getWheatSeeder(); // common: 1 seeder
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getWheatSeeder(); // uncommon: 2 seeders
                    item.setAmount(2);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getEnchantedHayBale(); // rare: 1 enchanted
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedHayBale(); // epic: 4 enchanted
                    item.setAmount(4);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                    if (petManager.getPet(player, "Scarecrow") == null) {
                        new PetRegistry().addPetByName(player, "Scarecrow"); // legendary pet
                        notifyHarvest(player, ChatColor.GOLD + "Scarecrow pet", 1, true);
                    } else {
                        ItemStack legendary = ItemRegistry.getEnchantedHayBale(); // fallback: 8 enchanted
                        legendary.setAmount(8);
                        player.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
                break;
            }
            case CARROTS: {
                double roll = random.nextDouble();
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getCarrotSeeder();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getCarrotSeeder();
                    item.setAmount(2);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getEnchantedGoldenCarrot();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedGoldenCarrot(); // epic: 4 enchanted
                    item.setAmount(4);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                    if (petManager.getPet(player, "Killer Rabbit") == null) {
                        new PetRegistry().addPetByName(player, "Killer Rabbit");
                        notifyHarvest(player, ChatColor.GOLD + "Killer Rabbit pet", 1, true);
                    } else {
                        ItemStack legendary = ItemRegistry.getEnchantedGoldenCarrot(); // fallback: 8 enchanted
                        legendary.setAmount(8);
                        player.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
                break;
            }
            case BEETROOTS: {
                double roll = random.nextDouble();
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getBeetrootSeeder();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getBeetrootSeeder();
                    item.setAmount(2);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getHeartRoot();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getHeartRoot(); // epic: 4 enchanted
                    item.setAmount(4);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    ItemStack legendary = ItemRegistry.getHeartRoot(); // fallback: 8 enchanted
                    legendary.setAmount(8);
                    player.getWorld().dropItemNaturally(dropLoc, legendary);
                    notifyHarvest(player, legendary, true);
                }
                break;
            }
            case POTATOES: {
                double roll = random.nextDouble();
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getPotatoSeeder();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getPotatoSeeder();
                    item.setAmount(2);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getImmortalPotato();
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getImmortalPotato(); // epic: 4 enchanted
                    item.setAmount(4);
                    player.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    ItemStack legendary = ItemRegistry.getImmortalPotato(); // fallback: 8 enchanted
                    legendary.setAmount(8);
                    player.getWorld().dropItemNaturally(dropLoc, legendary);
                    notifyHarvest(player, legendary, true);
                }
                break;
            }
            case MELON:
            case PUMPKIN:
            default:
                break;
        }
    }

    private enum HarvestRarity {
        COMMON(org.bukkit.ChatColor.WHITE, "", 0.8f, 1.0f, 5, 40, 10),
        UNCOMMON(org.bukkit.ChatColor.GREEN, org.bukkit.ChatColor.ITALIC.toString(), 1.0f, 1.1f, 10, 60, 10),
        RARE(org.bukkit.ChatColor.BLUE, org.bukkit.ChatColor.BOLD.toString(), 1.2f, 1.2f, 15, 80, 15),
        EPIC(org.bukkit.ChatColor.DARK_PURPLE, org.bukkit.ChatColor.BOLD.toString() + org.bukkit.ChatColor.ITALIC, 1.4f, 1.3f, 20, 100, 20),
        LEGENDARY(org.bukkit.ChatColor.GOLD, org.bukkit.ChatColor.BOLD.toString() + org.bukkit.ChatColor.ITALIC + org.bukkit.ChatColor.UNDERLINE, 1.6f, 1.4f, 25, 120, 25);

        private final org.bukkit.ChatColor color;
        private final String styles;
        private final float volume;
        private final float pitch;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        HarvestRarity(org.bukkit.ChatColor color, String styles, float volume, float pitch, int fadeIn, int stay, int fadeOut) {
            this.color = color;
            this.styles = styles;
            this.volume = volume;
            this.pitch = pitch;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        public String getColor() { return color.toString(); }
        public String getStyles() { return styles; }
        public float getVolume() { return volume; }
        public float getPitch() { return pitch; }
        public int getFadeIn() { return fadeIn; }
        public int getStay() { return stay; }
        public int getFadeOut() { return fadeOut; }
    }

    private static void notifyHarvest(Player player, ItemStack item, boolean rareOrAbove) {
        String name;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            name = item.getItemMeta().getDisplayName();
        } else {
            name = org.bukkit.ChatColor.WHITE + formatMaterialName(item.getType());
        }
        notifyHarvest(player, name, item.getAmount(), rareOrAbove);
    }

    private static void notifyHarvest(Player player, String itemName, int amount, boolean rareOrAbove) {
        HarvestRarity rarity = detectRarity(itemName, rareOrAbove);
        String baseName = ChatColor.stripColor(itemName);
        String styledName = rarity.getColor() + rarity.getStyles() + baseName;
        String amountText = amount > 1 ? org.bukkit.ChatColor.YELLOW + "" + amount + "x " : "";
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Harvest Reward: " + amountText + styledName);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, rarity.getVolume(), rarity.getPitch());
        String subtitle = amount > 1 ? org.bukkit.ChatColor.YELLOW + "" + amount + "x" : "";
        player.sendTitle(styledName, subtitle, rarity.getFadeIn(), rarity.getStay(), rarity.getFadeOut());
    }

    private static HarvestRarity detectRarity(String name, boolean rareOrAbove) {
        if (name.startsWith(org.bukkit.ChatColor.GOLD.toString())) return HarvestRarity.LEGENDARY;
        if (name.startsWith(org.bukkit.ChatColor.DARK_PURPLE.toString())) return HarvestRarity.EPIC;
        if (name.startsWith(org.bukkit.ChatColor.BLUE.toString())) return HarvestRarity.RARE;
        if (name.startsWith(org.bukkit.ChatColor.GREEN.toString())) return HarvestRarity.UNCOMMON;
        return rareOrAbove ? HarvestRarity.RARE : HarvestRarity.COMMON;
    }

    private static String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }
}
