package goat.minecraft.minecraftnew.other.enchanting.enchantingeffects;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Merit implements Listener {

    private final Random random = new Random();
    private final PlayerMeritManager meritManager;

    public Merit(PlayerMeritManager meritManager) {
        this.meritManager = meritManager;
    }

    @EventHandler
    public void onDiamondMine(BlockBreakEvent event) {
        Block block = event.getBlock();
        ArrayList<Material> validOres = new ArrayList<Material>();
        validOres.add(Material.DIAMOND_ORE);
        validOres.add(Material.DEEPSLATE_DIAMOND_ORE);

        // Only apply to diamond ore
        if (!validOres.contains(block.getType())) {
            return;
        }
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) {
            return;
        }
        // Do nothing if the tool has Silk Touch
        if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
            return;
        }
        // With 10% chance, award 1 merit point
        if (random.nextDouble() < 0.10) {
            UUID playerId = player.getUniqueId();
            meritManager.setMeritPoints(playerId, (meritManager.getMeritPoints(playerId) + 1));
            player.sendMessage(ChatColor.GOLD + "You earned a merit point!");
        }
    }
}
