package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.subsystems.auras.AuraManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Applies per-piece armor set stats and handles full set bonuses.
 */
public class ArmorSetBonusListener implements Listener {

    private final JavaPlugin plugin;
    private final AuraManager auraManager;
    private final FlowManager flowManager;
    private final Map<UUID, ArmorSet> activeSets = new HashMap<>();

    private static final UUID HEALTH_UUID = UUID.fromString("f3b46b64-96e7-4b96-a13a-9ff35a5ad9f1");
    private static final UUID DAMAGE_UUID = UUID.fromString("653685b7-888b-4ef2-8dd1-4cb014a2a7f8");
    private static final UUID SPEED_UUID = UUID.fromString("d082aa4a-dce9-4939-915a-402499697b22");

    public ArmorSetBonusListener(JavaPlugin plugin, AuraManager auraManager, FlowManager flowManager) {
        this.plugin = plugin;
        this.auraManager = auraManager;
        this.flowManager = flowManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> update(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> update(player), 1L);
        }
    }

    @EventHandler
    public void onArmorStand(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> update(event.getPlayer()), 1L);
        }
    }

    private void update(Player player) {
        ItemStack helm = player.getInventory().getHelmet();
        ItemStack chest = player.getInventory().getChestplate();
        ItemStack legs = player.getInventory().getLeggings();
        ItemStack boots = player.getInventory().getBoots();

        ArmorSet hSet = ArmorSet.fromItem(helm);
        ArmorSet cSet = ArmorSet.fromItem(chest);
        ArmorSet lSet = ArmorSet.fromItem(legs);
        ArmorSet bSet = ArmorSet.fromItem(boots);

        double health = 0;
        double damage = 0;
        double speed = 0;

        if (hSet != null) { health += hSet.stats().health(); damage += hSet.stats().attackDamage(); speed += hSet.stats().speed(); }
        if (cSet != null) { health += cSet.stats().health(); damage += cSet.stats().attackDamage(); speed += cSet.stats().speed(); }
        if (lSet != null) { health += lSet.stats().health(); damage += lSet.stats().attackDamage(); speed += lSet.stats().speed(); }
        if (bSet != null) { health += bSet.stats().health(); damage += bSet.stats().attackDamage(); speed += bSet.stats().speed(); }

        apply(player, Attribute.GENERIC_MAX_HEALTH, HEALTH_UUID, health);
        apply(player, Attribute.GENERIC_ATTACK_DAMAGE, DAMAGE_UUID, damage);
        apply(player, Attribute.GENERIC_MOVEMENT_SPEED, SPEED_UUID, speed);

        ArmorSet full = null;
        if (hSet != null && hSet == cSet && hSet == lSet && hSet == bSet) {
            full = hSet;
        }
        ArmorSet current = activeSets.get(player.getUniqueId());
        if (full != null && full != current) {
            auraManager.activateAura(player, full.aura());
            activeSets.put(player.getUniqueId(), full);
            int flow = flowManager.getFlow(player);
            if (flow > 0) {
                player.sendMessage(ChatColor.GREEN + full.fullSetBonus() + " (Flow " + flow + ")");
            } else {
                player.sendMessage(ChatColor.GREEN + full.fullSetBonus());
            }
        } else if (full == null && current != null) {
            auraManager.deactivateAura(player);
            activeSets.remove(player.getUniqueId());
        }
    }

    private void apply(Player player, Attribute attr, UUID id, double value) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        for (AttributeModifier mod : new ArrayList<>(inst.getModifiers())) {
            if (mod.getUniqueId().equals(id)) {
                inst.removeModifier(mod);
            }
        }
        if (Math.abs(value) > 0.0001) {
            inst.addModifier(new AttributeModifier(id, "armor-set", value, AttributeModifier.Operation.ADD_NUMBER));
        }
    }
}
