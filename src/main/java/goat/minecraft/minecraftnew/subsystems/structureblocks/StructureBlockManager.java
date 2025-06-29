package goat.minecraft.minecraftnew.subsystems.structureblocks;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;

import java.util.*;

public class StructureBlockManager implements Listener {

    private static StructureBlockManager instance;
    private final JavaPlugin plugin;


    private final NamespacedKey powerKey;

    private StructureBlockManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.powerKey = new NamespacedKey(plugin, "sb_power");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new StructureBlockManager(plugin);
        }
    }

    public static StructureBlockManager getInstance() {
        return instance;
    }

    // Create a new Structure Block charm with starting power 0
    public ItemStack createStructureBlock() {
        ItemStack item = new ItemStack(Material.STRUCTURE_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Structure Block Charm");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.AQUA + "Power: " + ChatColor.YELLOW + "0");
        meta.setLore(lore);

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(powerKey, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Determines if the given ItemStack is a Structure Block Charm.
     */
    public boolean isStructureBlock(ItemStack item) {
        if (item == null || item.getType() != Material.STRUCTURE_BLOCK) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return name.equals("Structure Block Charm");
    }

    /**
     * Sets the power of a Structure Block Charm.
     */
    public void setStructureBlockPower(ItemStack item, int power) {
        if (item == null) return;
        setPower(item, power);
    }

    /**
     * Returns the current power stored in a Structure Block Charm.
     */
    public int getStructureBlockPower(ItemStack item) {
        return getPower(item);
    }

    private int getPower(ItemStack item) {
        if (item == null || item.getType() != Material.STRUCTURE_BLOCK) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer val = meta.getPersistentDataContainer().get(powerKey, PersistentDataType.INTEGER);
        return val == null ? 0 : val;
    }

    private void setPower(ItemStack item, int power) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(powerKey, PersistentDataType.INTEGER, power);
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < lore.size(); i++) {
            String s = ChatColor.stripColor(lore.get(i));
            if (s.startsWith("Power:")) {
                lore.set(i, ChatColor.AQUA + "Power: " + ChatColor.YELLOW + power);
                found = true;
                break;
            }
        }
        if (!found) {
            lore.add(0, ChatColor.AQUA + "Power: " + ChatColor.YELLOW + power);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private boolean isReplaceable(Material m) {
        return m == Material.AIR || m == Material.CAVE_AIR || m == Material.VOID_AIR ||
               m == Material.WATER || m == Material.LAVA ||
               m == Material.GRASS || m == Material.TALL_GRASS ||
               m == Material.SNOW || m == Material.SNOW_BLOCK;
    }

    private void expandFace(Block origin, BlockFace face, Player player, ItemStack item) {
        int power = getPower(item);
        if (power <= 0) {
            player.sendMessage(ChatColor.RED + "Structure Block is out of power!");
            return;
        }

        Material mat = origin.getType();
        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        Block place = null;
        while (!queue.isEmpty()) {
            Block current = queue.poll();
            Block target = current.getRelative(face);
            if (isReplaceable(target.getType())) {
                place = target;
                break;
            }

            for (BlockFace dir : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
                Block nb = current.getRelative(dir);
                if (!visited.contains(nb) && nb.getType() == mat) {
                    visited.add(nb);
                    queue.add(nb);
                }
            }
        }

        if (place != null) {
            place.setType(mat);
            setPower(item, power - 1);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
            player.sendMessage(ChatColor.GREEN + "Placed 1 block using Structure Block.");
        } else {
            player.sendMessage(ChatColor.RED + "No space to place blocks.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isStructureBlock(item)) return;

        event.setCancelled(true);
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;
        expandFace(clicked, event.getBlockFace(), player, item);
    }






    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack cursor = event.getCursor();
        ItemStack clicked = event.getCurrentItem();
        if (cursor == null || clicked == null) return;
        if (clicked.getType() != Material.STRUCTURE_BLOCK) return;
        if (!clicked.hasItemMeta() || !ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).equals("Structure Block Charm")) return;
        if (!cursor.getType().isBlock()) return;
        // Only handle top inventory or player inventory? Follows beacon logic.
        int add = cursor.getType() == Material.OBSIDIAN ? 10 : 1;
        int amount = cursor.getAmount();
        int current = getPower(clicked);
        int newVal = Math.min(current + add * amount, 10000);
        if (newVal == current) {
            player.sendMessage(ChatColor.RED + "This Structure Block is already at maximum power (10,000)!");
            return;
        }
        setPower(clicked, newVal);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.2f);
        player.sendMessage(ChatColor.GOLD + "Added " + ChatColor.YELLOW + amount + "x " + cursor.getType().name().toLowerCase().replace('_',' ') + ChatColor.GOLD + " (+" + (add*amount) + " Power)" );
        event.setCursor(null);
        event.setCancelled(true);
    }

}

