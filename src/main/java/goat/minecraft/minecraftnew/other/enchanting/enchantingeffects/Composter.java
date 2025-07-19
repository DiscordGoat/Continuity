package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Composter implements Listener {

    private static final Set<Material> ELIGIBLE_MATERIALS = EnumSet.of(
            Material.DIRT,
            Material.GRAVEL,
            Material.SAND,
            Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.MELON_SEEDS,
            Material.PUMPKIN_SEEDS,
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY
    );

    private final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!CustomEnchantmentManager.isEnchantmentActive(player, tool, "Composter")) {
            return;
        }
        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Composter");
        if (level <= 0) return;

        double chance = 0.04 * level; // 4% per level
        if (random.nextDouble() > chance) {
            return;
        }

        Location dropLoc = event.getBlock().getLocation();
        autoCompost(player, dropLoc);
    }

    private void autoCompost(Player player, Location dropLoc) {
        for (Material mat : ELIGIBLE_MATERIALS) {
            int count = countItem(player, mat);
            if (count >= 256) {
                removeItem(player, mat, 64);
                dropLoc.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getOrganicSoil());
                return;
            }
        }
    }

    private int countItem(Player player, Material mat) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() != mat) continue;
            if (item.hasItemMeta() && (item.getItemMeta().hasDisplayName() || !item.getEnchantments().isEmpty())) {
                continue; // skip custom named or enchanted items
            }
            total += item.getAmount();
        }
        return total;
    }

    private void removeItem(Player player, Material mat, int amount) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && amount > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != mat) continue;
            if (item.hasItemMeta() && (item.getItemMeta().hasDisplayName() || !item.getEnchantments().isEmpty())) {
                continue;
            }
            int take = Math.min(item.getAmount(), amount);
            item.setAmount(item.getAmount() - take);
            if (item.getAmount() <= 0) {
                contents[i] = null;
            }
            amount -= take;
        }
        player.getInventory().setContents(contents);
    }
}
