package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.other.auras.Aura;
import goat.minecraft.minecraftnew.other.auras.AuraManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles activating auras when players wear a full blessed armor set.
 */
public class BlessedSetAuraListener implements Listener {
    private static final String META_KEY = "activeBlessing";

    private final JavaPlugin plugin;
    private final AuraManager auraManager;
    private final Map<String, Aura> blessingAuraMap = new HashMap<>();

    public BlessedSetAuraListener(JavaPlugin plugin, AuraManager auraManager) {
        this.plugin = plugin;
        this.auraManager = auraManager;
        blessingAuraMap.put("Lost Legion", Aura.LOST_LEGION);
        blessingAuraMap.put("Monolith", Aura.MONOLITH);
        blessingAuraMap.put("Scorchsteel", Aura.SCORCHSTEEL);
        blessingAuraMap.put("Dweller", Aura.DWELLER);
        blessingAuraMap.put("Pastureshade", Aura.PASTURESHADE);
        blessingAuraMap.put("Countershot", Aura.COUNTERSHOT);
        blessingAuraMap.put("Shadowstep", Aura.SHADOWSTEP);
        blessingAuraMap.put("Strider", Aura.STRIDER);
        blessingAuraMap.put("Slayer", Aura.SLAYER);
        blessingAuraMap.put("Duskblood", Aura.DUSKBLOOD);
        blessingAuraMap.put("Thunderforge", Aura.THUNDERFORGE);
        blessingAuraMap.put("Fathmic Iron", Aura.FATHMIC_IRON);
        blessingAuraMap.put("Nature's Wrath", Aura.NATURES_WRATH);

        // Reapply auras for online players after reload
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                checkPlayer(p);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Ensure aura tasks and metadata are cleared on quit to prevent leaks
        deactivate(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(player), 1L);
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof org.bukkit.entity.ArmorStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
        }
    }

    private void checkPlayer(Player player) {
        String blessing = getBlessing(player.getInventory().getHelmet());
        if (blessing == null) {
            deactivate(player);
            return;
        }
        if (!blessing.equals(getBlessing(player.getInventory().getChestplate()))) {
            deactivate(player);
            return;
        }
        if (!blessing.equals(getBlessing(player.getInventory().getLeggings()))) {
            deactivate(player);
            return;
        }
        if (!blessing.equals(getBlessing(player.getInventory().getBoots()))) {
            deactivate(player);
            return;
        }
        Aura aura = blessingAuraMap.get(blessing);
        if (aura == null) {
            deactivate(player);
            return;
        }
        String current = player.hasMetadata(META_KEY) ? player.getMetadata(META_KEY).get(0).asString() : null;
        if (!blessing.equals(current)) {
            auraManager.activateAura(player, aura);
            player.setMetadata(META_KEY, new FixedMetadataValue(plugin, blessing));
        }
    }

    private void deactivate(Player player) {
        if (player.hasMetadata(META_KEY)) {
            auraManager.deactivateAura(player);
            player.removeMetadata(META_KEY, plugin);
        }
    }

    private String getBlessing(ItemStack item) {
        if (item == null || item.getType() == org.bukkit.Material.AIR || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return null;
        }
        String name = ChatColor.stripColor(meta.getDisplayName());
        int idx = name.lastIndexOf(' ');
        return idx > 0 ? name.substring(0, idx) : null;
    }
}
