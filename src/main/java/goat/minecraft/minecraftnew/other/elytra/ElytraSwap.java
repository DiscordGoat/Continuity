package goat.minecraft.minecraftnew.other.elytra;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElytraSwap implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Long> lastSneak = new HashMap<>();

    public ElytraSwap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) {
            return;
        }
        Player player = event.getPlayer();
        if (player.isGliding()) {
            return; // Don't swap while gliding
        }

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastSneak.getOrDefault(id, 0L);
        lastSneak.put(id, now);
        if (now - last > 1000) {
            return; // Not a double shift
        }

        PlayerInventory inv = player.getInventory();
        ItemStack chest = inv.getChestplate();
        if (chest == null || chest.getType() != Material.ELYTRA) {
            if (swapToElytra(player, inv)) {
                Bukkit.getScheduler().runTask(plugin, () -> player.setGliding(true));
            }
        } else {
            swapToChestplate(player, inv, chest);
        }
    }

    private boolean swapToElytra(Player player, PlayerInventory inv) {
        ItemStack chest = inv.getChestplate();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() == Material.ELYTRA) {
                inv.setItem(i, chest);
                inv.setChestplate(stack);
                return true;
            }
        }
        CustomBundleGUI gui = CustomBundleGUI.getInstance();
        if (gui != null) {
            for (int i = 0; i < 54; i++) {
                ItemStack stack = gui.getBackpackItem(player, i);
                if (stack != null && stack.getType() == Material.ELYTRA) {
                    gui.setBackpackItem(player, i, chest);
                    inv.setChestplate(stack);
                    return true;
                }
            }
        }
        return false;
    }

    private void swapToChestplate(Player player, PlayerInventory inv, ItemStack elytra) {
        int slot = findBestChestplateSlot(inv);
        if (slot != -1) {
            ItemStack chestplate = inv.getItem(slot);
            inv.setItem(slot, elytra);
            inv.setChestplate(chestplate);
            return;
        }
        CustomBundleGUI gui = CustomBundleGUI.getInstance();
        if (gui != null) {
            slot = findBestBackpackChestplateSlot(player);
            if (slot != -1) {
                ItemStack chestplate = gui.getBackpackItem(player, slot);
                gui.setBackpackItem(player, slot, elytra);
                inv.setChestplate(chestplate);
            }
        }
    }

    private int findBestChestplateSlot(PlayerInventory inv) {
        Material[] priorities = {
                Material.NETHERITE_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE,
                Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE,
                Material.CHAINMAIL_CHESTPLATE,
                Material.LEATHER_CHESTPLATE
        };
        for (Material mat : priorities) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack stack = inv.getItem(i);
                if (stack != null && stack.getType() == mat) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findBestBackpackChestplateSlot(Player player) {
        CustomBundleGUI gui = CustomBundleGUI.getInstance();
        if (gui == null) return -1;
        Material[] priorities = {
                Material.NETHERITE_CHESTPLATE,
                Material.DIAMOND_CHESTPLATE,
                Material.IRON_CHESTPLATE,
                Material.GOLDEN_CHESTPLATE,
                Material.CHAINMAIL_CHESTPLATE,
                Material.LEATHER_CHESTPLATE
        };
        for (Material mat : priorities) {
            for (int i = 0; i < 54; i++) {
                ItemStack stack = gui.getBackpackItem(player, i);
                if (stack != null && stack.getType() == mat) {
                    return i;
                }
            }
        }
        return -1;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastSneak.remove(event.getPlayer().getUniqueId());
    }
}
