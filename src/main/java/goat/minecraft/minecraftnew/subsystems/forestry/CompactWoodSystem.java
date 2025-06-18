package goat.minecraft.minecraftnew.subsystems.forestry;

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

public class CompactWoodSystem implements Listener {
    private final MinecraftNew plugin;
    private final Map<UUID, Integer> activeGui = new HashMap<>();
    private static final List<Material> VARIANTS = List.of(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,
            Material.CHERRY_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            Material.OAK_PLANKS,
            Material.SPRUCE_PLANKS,
            Material.BIRCH_PLANKS,
            Material.JUNGLE_PLANKS,
            Material.ACACIA_PLANKS,
            Material.DARK_OAK_PLANKS,
            Material.MANGROVE_PLANKS,
            Material.CHERRY_PLANKS,
            Material.CRIMSON_PLANKS,
            Material.WARPED_PLANKS
    );

    public CompactWoodSystem(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    private boolean isPlainCompactWood(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).equals("Compact Wood");
    }

    private boolean isVariantCompactWood(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return ChatColor.stripColor(meta.getDisplayName()).startsWith("Compact Wood: ");
    }

    @EventHandler
    public void onFletchingTableUse(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Block clicked = event.getClickedBlock();
        if (clicked == null || clicked.getType() != Material.FLETCHING_TABLE) return;
        ItemStack inHand = event.getItem();
        if (!isPlainCompactWood(inHand)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        Inventory gui = Bukkit.createInventory(null, ((VARIANTS.size() - 1) / 9 + 1) * 9, ChatColor.GOLD + "Select Wood");
        for (int i = 0; i < VARIANTS.size(); i++) {
            gui.setItem(i, new ItemStack(VARIANTS.get(i)));
        }
        activeGui.put(player.getUniqueId(), player.getInventory().getHeldItemSlot());
        player.openInventory(gui);
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.GOLD + "Select Wood")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        Integer slot = activeGui.remove(player.getUniqueId());
        if (slot == null) return;
        ItemStack stack = player.getInventory().getItem(slot);
        if (!isPlainCompactWood(stack)) return;
        ItemMeta meta = stack.getItemMeta();
        String materialName = clicked.getType().name().toLowerCase();
        meta.setDisplayName(ChatColor.YELLOW + "Compact Wood: " + materialName);
        stack.setItemMeta(meta);
        player.getInventory().setItem(slot, stack);
        player.closeInventory();
    }

    @EventHandler
    public void onGuiClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals(ChatColor.GOLD + "Select Wood")) {
            activeGui.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onCompactWoodPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!isVariantCompactWood(item)) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        String materialName = name.substring("Compact Wood: ".length()).toUpperCase();
        Material mat = Material.matchMaterial(materialName);
        if (mat == null) return;
        BlockFace face = event.getBlockAgainst().getFace(event.getBlockPlaced());
        BlockFace direction = face.getOppositeFace();
        Block current = event.getBlockAgainst().getRelative(direction);
        int placed = 0;
        for (int i = 0; i < 64; i++) {
            Material type = current.getType();
            if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.WATER || type == Material.LAVA) {
                current.setType(mat);
                current = current.getRelative(direction);
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
