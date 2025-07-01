package goat.minecraft.minecraftnew.other.trims;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

public class CustomTrimEffects implements Listener {
    private final Random random = new Random();

    private static final String[] MATERIALS = {
            "Diamond", "Iron", "Gold", "Emerald",
            "Lapis", "Redstone", "Quartz",
            "Amethyst", "Copper", "Netherite"
    };

    /**
     * Extract the trim material from an armor piece based on its lore.
     * This method is public so other systems can query trim data.
     */
    public static String getTrimMaterial(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return null;
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped == null) continue;
            for (String mat : MATERIALS) {
                if (stripped.toLowerCase().contains(mat.toLowerCase() + " material")) {
                    return mat;
                }
            }
        }
        return null;
    }

    public static String getFullTrimMaterial(Player player) {
        String found = null;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            String mat = getTrimMaterial(piece);
            if (mat == null) return null;
            if (found == null) {
                found = mat;
            } else if (!found.equalsIgnoreCase(mat)) {
                return null;
            }
        }
        return found;
    }

    /**
     * Maps trim material names to display colors.
     */
    public static ChatColor getTrimColor(String material) {
        if (material == null) return ChatColor.WHITE;
        return switch (material.toLowerCase()) {
            case "diamond" -> ChatColor.AQUA;
            case "iron" -> ChatColor.GRAY;
            case "gold" -> ChatColor.GOLD;
            case "emerald" -> ChatColor.GREEN;
            case "lapis" -> ChatColor.BLUE;
            case "redstone" -> ChatColor.RED;
            case "quartz" -> ChatColor.WHITE;
            case "amethyst" -> ChatColor.LIGHT_PURPLE;
            case "copper" -> ChatColor.GOLD;
            case "netherite" -> ChatColor.DARK_GRAY;
            default -> ChatColor.WHITE;
        };
    }

    /* Diamond: 15% damage reduction */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String material = getFullTrimMaterial(player);
        if (material == null) return;
        if (material.equalsIgnoreCase("Diamond")) {
            event.setDamage(event.getDamage() * 0.85);
            Bukkit.getLogger().info("[Trims] Diamond reduction triggered for " + player.getName());
        }
        if (material.equalsIgnoreCase("Emerald") && event.getCause() == DamageCause.FALL) {
            event.setCancelled(true);
            Bukkit.getLogger().info("[Trims] Emerald fall immunity triggered for " + player.getName());
        }
        if (material.equalsIgnoreCase("Gold")) {
            if (random.nextDouble() < 0.10) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
                Bukkit.getLogger().info("[Trims] Gold regeneration triggered for " + player.getName());
            }
        }
        if (material.equalsIgnoreCase("Netherite")) {
            if (event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK
                    || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.HOT_FLOOR) {
                player.setFireTicks(0);
                Bukkit.getLogger().info("[Trims] Netherite fire protection triggered for " + player.getName());
            }
        }
    }

    /* Redstone: 25% more damage */
    @EventHandler
    public void onPlayerDealDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Redstone")) {
            event.setDamage(event.getDamage() * 1.25);
            Bukkit.getLogger().info("[Trims] Redstone damage buff triggered for " + player.getName());
        }
    }

    /* Quartz: arrows bounce off of you */
    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Quartz")) {
            event.setCancelled(true);
            arrow.setVelocity(arrow.getVelocity().multiply(-1));
            Bukkit.getLogger().info("[Trims] Quartz arrow bounce triggered for " + player.getName());
        }
    }

    /* Iron: 50% chance to repair armor when taking durability */
    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Iron")) {
            if (random.nextDouble() < 0.5) {
                ItemStack item = event.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable dmg) {
                    int current = dmg.getDamage();
                    if (current > 0) {
                        dmg.setDamage(current - 1);
                        item.setItemMeta(meta);
                        Bukkit.getLogger().info("[Trims] Iron auto-repair triggered for " + player.getName());
                    }
                }
            }
        }
    }

    /* Amethyst: no hunger loss */
    @EventHandler
    public void onFoodChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Amethyst")) {
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
                Bukkit.getLogger().info("[Trims] Amethyst hunger prevention triggered for " + player.getName());
            }
        }
    }

    /* Copper: restore air when losing it */
    @EventHandler
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Copper")) {
            if (event.getAmount() < player.getRemainingAir()) {
                int newAir = Math.min(player.getMaximumAir(), event.getAmount() + 1);
                event.setAmount(newAir);
                Bukkit.getLogger().info("[Trims] Copper air restoration triggered for " + player.getName());
            }
        }
    }

    /* Netherite: prevent being set on fire */
    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String material = getFullTrimMaterial(player);
        if (material != null && material.equalsIgnoreCase("Netherite")) {
            event.setCancelled(true);
            player.setFireTicks(0);
            Bukkit.getLogger().info("[Trims] Netherite fire immunity triggered for " + player.getName());
        }
    }
}
