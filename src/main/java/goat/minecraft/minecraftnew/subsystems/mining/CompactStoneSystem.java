package goat.minecraft.minecraftnew.subsystems.mining;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CompactStoneSystem implements Listener {
    private final MinecraftNew plugin;
    private final Map<UUID, Integer> activeGui = new HashMap<>();
    private static final List<Material> VARIANTS = List.of(
            Material.STONE,
            Material.SMOOTH_STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE,
            Material.GRANITE,
            Material.POLISHED_GRANITE,
            Material.DIORITE,
            Material.POLISHED_DIORITE,
            Material.ANDESITE,
            Material.POLISHED_ANDESITE,
            Material.STONE_BRICKS,
            Material.MOSSY_STONE_BRICKS,
            Material.CRACKED_STONE_BRICKS,
            Material.CHISELED_STONE_BRICKS,
            Material.DEEPSLATE,
            Material.COBBLED_DEEPSLATE,
            Material.POLISHED_DEEPSLATE,
            Material.DEEPSLATE_BRICKS,
            Material.CRACKED_DEEPSLATE_BRICKS,
            Material.DEEPSLATE_TILES,
            Material.CRACKED_DEEPSLATE_TILES,
            Material.BLACKSTONE,
            Material.POLISHED_BLACKSTONE,
            Material.POLISHED_BLACKSTONE_BRICKS,
            Material.CRACKED_POLISHED_BLACKSTONE_BRICKS,
            Material.CHISELED_POLISHED_BLACKSTONE,
            Material.BASALT,
            Material.SMOOTH_BASALT,
            Material.POLISHED_BASALT,
            Material.END_STONE,
            Material.END_STONE_BRICKS,
            Material.NETHER_BRICKS,
            Material.CRACKED_NETHER_BRICKS,
            Material.RED_NETHER_BRICKS
    );

    public CompactStoneSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    private boolean isPlainCompactStone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equals("Compact Stone");
    }

    private boolean isVariantCompactStone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).startsWith("Compact Stone: ");
    }

    @EventHandler
    public void onStonecutterUse(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.STONECUTTER) return;
        ItemStack inHand = event.getItem();
        if (!isPlainCompactStone(inHand)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        Inventory gui = Bukkit.createInventory(null, ((VARIANTS.size() - 1) / 9 + 1) * 9, ChatColor.GOLD + "Select Material");
        for (int i = 0; i < VARIANTS.size(); i++) {
            gui.setItem(i, new ItemStack(VARIANTS.get(i)));
        }
        activeGui.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());
        player.openInventory(gui);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Select Material")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        Integer slot = activeGui.remove(player.getUniqueId());
        if (slot == null) return;
        ItemStack stack = player.getInventory().getItem(slot);
        if (!isPlainCompactStone(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        String materialName = clicked.getType().name().toLowerCase();
        meta.setDisplayName(ChatColor.YELLOW + "Compact Stone: " + materialName);
        stack.setItemMeta(meta);
        player.getInventory().setItem(slot, stack);
        player.closeInventory();
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Select Material")) {
            activeGui.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onCompactStonePlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isVariantCompactStone(item)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        String materialName = name.substring("Compact Stone: ".length()).toUpperCase();
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) return;
        BlockFace face = event.getBlockAgainst().getFace(event.getBlockPlaced());
        Block current = event.getBlockAgainst().getRelative(face);
        int placed = 0;
        for (int i = 0; i < 64; i++) {
            Material type = current.getType();
            if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.WATER || type == Material.LAVA) {
                current.setType(mat);
                current = current.getRelative(face);
                placed++;
            } else {
                break;
            }
        }
        if (placed > 0) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
        }
    }
}

