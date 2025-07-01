package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BlessingArmorStandListener implements Listener {

    private static final String PREFIX = ChatColor.YELLOW + "Blessing Artifact - ";

    private final Map<String, ChatColor> colorMap = new HashMap<>();
    private final Map<String, List<String>> bonusMap = new HashMap<>();

    public BlessingArmorStandListener() {
        colorMap.put("Feather", ChatColor.AQUA);
        colorMap.put("Shield", ChatColor.DARK_AQUA);
        colorMap.put("Speed", ChatColor.GOLD);
        colorMap.put("Nature's Wrath", ChatColor.DARK_GREEN);

        bonusMap.put("Feather", List.of(ChatColor.GRAY + "Full Set Bonus: Slow falling"));
        bonusMap.put("Shield", List.of(ChatColor.GRAY + "Full Set Bonus: Extra defense"));
        bonusMap.put("Speed", List.of(ChatColor.GRAY + "Full Set Bonus: Faster movement"));
        bonusMap.put("Nature's Wrath", List.of(ChatColor.GRAY + "Full Set Bonus: Increased damage"));
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand armorStand)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        String name = meta.getDisplayName();
        if (!name.startsWith(PREFIX)) {
            return;
        }
        String blessing = ChatColor.stripColor(name.substring(PREFIX.length()));

        ItemStack helmet = armorStand.getEquipment().getHelmet();
        ItemStack chest = armorStand.getEquipment().getChestplate();
        ItemStack legs = armorStand.getEquipment().getLeggings();
        ItemStack boots = armorStand.getEquipment().getBoots();

        if (!isFullNetherite(helmet, chest, legs, boots)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "The armor stand must wear a full netherite set.");
            return;
        }

        applyBlessing(helmet, blessing, "Helmet");
        applyBlessing(chest, blessing, "Chestplate");
        applyBlessing(legs, blessing, "Leggings");
        applyBlessing(boots, blessing, "Boots");

        armorStand.getEquipment().setHelmet(helmet);
        armorStand.getEquipment().setChestplate(chest);
        armorStand.getEquipment().setLeggings(legs);
        armorStand.getEquipment().setBoots(boots);

        decrement(item, player);
        event.setCancelled(true);
    }

    private boolean isFullNetherite(ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
        return helmet != null && helmet.getType() == Material.NETHERITE_HELMET &&
                chest != null && chest.getType() == Material.NETHERITE_CHESTPLATE &&
                legs != null && legs.getType() == Material.NETHERITE_LEGGINGS &&
                boots != null && boots.getType() == Material.NETHERITE_BOOTS;
    }

    private void applyBlessing(ItemStack piece, String blessing, String type) {
        if (piece == null) return;
        ItemMeta meta = piece.getItemMeta();
        if (meta == null) return;
        ChatColor color = colorMap.getOrDefault(blessing, ChatColor.GREEN);
        meta.setDisplayName(color + blessing + " " + type);
        List<String> lore = meta.hasLore() ? new ArrayList<>(Objects.requireNonNull(meta.getLore())) : new ArrayList<>();
        List<String> bonus = bonusMap.get(blessing);
        if (bonus != null) {
            lore.addAll(bonus);
        }
        meta.setLore(lore);
        piece.setItemMeta(meta);
    }

    private void decrement(ItemStack item, Player player) {
        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().removeItem(item);
        }
    }
}
