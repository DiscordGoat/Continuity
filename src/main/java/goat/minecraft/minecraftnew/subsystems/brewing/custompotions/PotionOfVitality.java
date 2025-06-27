package goat.minecraft.minecraftnew.subsystems.brewing.custompotions;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Potion of Vitality - grants 20 bonus max health while active.
 */
public class PotionOfVitality implements Listener {

    private static final Map<UUID, Boolean> applied = new HashMap<>();
    private static final Map<UUID, Double> baseHealth = new HashMap<>();

    public PotionOfVitality() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : MinecraftNew.getInstance().getServer().getOnlinePlayers()) {
                    if (PotionManager.isActive("Potion of Vitality", player)) {
                        applyBonusHealth(player);
                    } else {
                        removeBonusHealth(player);
                    }
                }
            }
        }.runTaskTimer(MinecraftNew.getInstance(), 0L, 20L);
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            if (displayName.equals("Potion of Vitality")) {
                Player player = event.getPlayer();
                XPManager xpManager = new XPManager(MinecraftNew.getInstance());
                int brewingLevel = xpManager.getPlayerLevel(player, "Brewing");
                int duration = (60 * 3) + (brewingLevel * 10);
                PotionManager.addCustomPotionEffect("Potion of Vitality", player, duration);
                player.sendMessage(ChatColor.RED + "Potion of Vitality effect activated for " + duration + " seconds!");
                xpManager.addXP(player, "Brewing", 100);
                applyBonusHealth(player);
            }
        }
    }

    private void applyBonusHealth(Player player) {
        UUID id = player.getUniqueId();
        if (applied.getOrDefault(id, false)) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr == null) return;
        baseHealth.put(id, attr.getBaseValue());
        double newMax = attr.getBaseValue() + 20.0;
        attr.setBaseValue(newMax);
        player.setHealth(Math.min(player.getHealth() + 20.0, newMax));
        applied.put(id, true);
    }

    private void removeBonusHealth(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) return;
        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (attr != null) {
            double orig = baseHealth.getOrDefault(id, attr.getBaseValue() - 20.0);
            attr.setBaseValue(orig);
            if (player.getHealth() > orig) {
                player.setHealth(orig);
            }
        }
        applied.put(id, false);
        baseHealth.remove(id);
    }
}
