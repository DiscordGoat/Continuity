package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.trims.CustomTrimEffects;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies a selected Blessing to armor sets placed on an ArmorStand.
 */
public class BlessingApplyListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand stand)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String stripped = ChatColor.stripColor(meta.getDisplayName());
        if (stripped == null || !stripped.toLowerCase().startsWith("blessing")) return;

        String[] parts = stripped.split(":", 2);
        if (parts.length < 2) {
            player.sendMessage(ChatColor.RED + "Select a blessing first.");
            return;
        }
        String setName = parts[1].trim();
        FlowType type;
        try {
            type = FlowType.valueOf(setName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown blessing: " + setName);
            return;
        }

        ItemStack[] armor = stand.getEquipment().getArmorContents();
        if (armor.length < 4) {
            player.sendMessage(ChatColor.RED + "The armor stand must hold a full set.");
            return;
        }

        String trim = null;
        for (ItemStack piece : armor) {
            if (piece == null || piece.getType() == Material.AIR) {
                player.sendMessage(ChatColor.RED + "The armor stand is missing pieces.");
                return;
            }
            if (!piece.getType().name().startsWith("NETHERITE_")) {
                player.sendMessage(ChatColor.RED + "All pieces must be Netherite.");
                return;
            }
            String mat = CustomTrimEffects.getTrimMaterial(piece);
            if (mat == null) {
                player.sendMessage(ChatColor.RED + "Each piece must have the same trim.");
                return;
            }
            if (trim == null) {
                trim = mat;
            } else if (!trim.equalsIgnoreCase(mat)) {
                player.sendMessage(ChatColor.RED + "All trims must match.");
                return;
            }
        }

        ChatColor color = CustomTrimEffects.getTrimColor(trim);
        for (ItemStack piece : armor) {
            ItemMeta im = piece.getItemMeta();
            if (im == null) continue;
            im.setDisplayName(color + setName);
            List<String> lore = im.hasLore() ? new ArrayList<>(im.getLore()) : new ArrayList<>();
            lore.add(ChatColor.GRAY + "2pc Bonus: TBD");
            lore.add(ChatColor.GRAY + "4pc Bonus: TBD");
            im.setLore(lore);
            piece.setItemMeta(im);
        }

        // consume artifact
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1f, 1f);
        player.sendMessage(ChatColor.GREEN + "Blessing applied to armor set!");
        event.setCancelled(true);
    }
}
