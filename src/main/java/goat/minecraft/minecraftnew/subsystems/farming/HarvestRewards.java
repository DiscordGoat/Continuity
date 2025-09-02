package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Simplified reward generator used by batched grants when a harvest threshold is crossed.
 * Mirrors the existing per-crop reward tables at a high level.
 */
public class HarvestRewards {
    private static final Random random = new Random();

    public static ItemStack simpleRoll(Player player, Material cropType) {
        switch (cropType) {
            case WHEAT:
            case WHEAT_SEEDS: {
                double roll = random.nextDouble();
                if (roll < 0.50) return ItemRegistry.getWheatSeeder();
                if (roll < 0.80) { ItemStack i = ItemRegistry.getWheatSeeder(); i.setAmount(2); return i; }
                if (roll < 0.90) return ItemRegistry.getEnchantedHayBale();
                if (roll < 0.975) { ItemStack i = ItemRegistry.getEnchantedHayBale(); i.setAmount(2); return i; }
                // Legendary case: pet or fallback
                PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                if (petManager.getPet(player, "Scarecrow") == null) {
                    new PetRegistry().addPetByName(player, "Scarecrow");
                    player.sendMessage(ChatColor.GOLD + "Scarecrow pet");
                    return null;
                } else {
                    ItemStack legendary = ItemRegistry.getEnchantedHayBale();
                    legendary.setAmount(16);
                    return legendary;
                }
            }
            case CARROTS: {
                double roll = random.nextDouble();
                if (roll < 0.50) return ItemRegistry.getCarrotSeeder();
                if (roll < 0.80) { ItemStack i = ItemRegistry.getCarrotSeeder(); i.setAmount(2); return i; }
                if (roll < 0.90) return ItemRegistry.getEnchantedGoldenCarrot();
                if (roll < 0.975) { ItemStack i = ItemRegistry.getEnchantedGoldenCarrot(); i.setAmount(2); return i; }
                PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
                if (petManager.getPet(player, "Killer Rabbit") == null) {
                    new PetRegistry().addPetByName(player, "Killer Rabbit");
                    player.sendMessage(ChatColor.GOLD + "Killer Rabbit pet");
                    return null;
                } else {
                    ItemStack legendary = ItemRegistry.getEnchantedGoldenCarrot();
                    legendary.setAmount(16);
                    return legendary;
                }
            }
            case BEETROOTS: {
                double roll = random.nextDouble();
                if (roll < 0.50) return ItemRegistry.getBeetrootSeeder();
                if (roll < 0.80) { ItemStack i = ItemRegistry.getBeetrootSeeder(); i.setAmount(2); return i; }
                if (roll < 0.90) return ItemRegistry.getHeartRoot();
                if (roll < 0.975) { ItemStack i = ItemRegistry.getHeartRoot(); i.setAmount(2); return i; }
                ItemStack legendary = ItemRegistry.getHeartRoot();
                legendary.setAmount(16);
                return legendary;
            }
            case POTATOES: {
                double roll = random.nextDouble();
                if (roll < 0.50) return ItemRegistry.getPotatoSeeder();
                if (roll < 0.80) { ItemStack i = ItemRegistry.getPotatoSeeder(); i.setAmount(2); return i; }
                if (roll < 0.90) return ItemRegistry.getImmortalPotato();
                if (roll < 0.975) { ItemStack i = ItemRegistry.getImmortalPotato(); i.setAmount(2); return i; }
                ItemStack legendary = ItemRegistry.getImmortalPotato();
                legendary.setAmount(16);
                return legendary;
            }
            default:
                return null;
        }
    }
}
