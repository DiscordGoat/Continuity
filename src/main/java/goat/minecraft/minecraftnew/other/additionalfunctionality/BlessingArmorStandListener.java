package goat.minecraft.minecraftnew.other.additionalfunctionality;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.music.MusicDiscManager;
import goat.minecraft.minecraftnew.utils.stats.StrengthManager;
import org.bukkit.*;
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
        colorMap.put("Lost Legion", ChatColor.YELLOW);
        //
        colorMap.put("Monolith", ChatColor.RED);
        colorMap.put("Scorchsteel", ChatColor.GOLD);
        colorMap.put("Dweller", ChatColor.DARK_AQUA);
        colorMap.put("Pastureshade", ChatColor.YELLOW);
        colorMap.put("Countershot", ChatColor.DARK_GRAY);
        colorMap.put("Shadowstep", ChatColor.LIGHT_PURPLE);
        colorMap.put("Strider", ChatColor.WHITE);
        colorMap.put("Slayer", ChatColor.DARK_RED);
        colorMap.put("Duskblood", ChatColor.DARK_PURPLE);
        colorMap.put("Thunderforge", ChatColor.BLUE);
        colorMap.put("Fathmic Iron", ChatColor.AQUA);
        colorMap.put("Nature's Wrath", ChatColor.DARK_GREEN);

        bonusMap.put("Lost Legion", List.of(ChatColor.GRAY + "Full Set Bonus: +25% Arrow Damage"));
        bonusMap.put("Monolith", List.of(ChatColor.GRAY + "Full Set Bonus: +20 Health, +20% Defense"));
        bonusMap.put("Scorchsteel", List.of(ChatColor.GRAY + "Full Set Bonus: +20 Fire Stacks, +40% Nether Monster Damage Reduction"));
        bonusMap.put("Nature's Wrath", List.of(ChatColor.GRAY + "Full Set Bonus: +4% Spirit Chance, +25% Spirit Defense, +25% Spirit Damage"));
        bonusMap.put("Dweller", List.of(ChatColor.GRAY + "Full Set Bonus: +25% Ore Yield, +500 Oxygen"));
        bonusMap.put("Pastureshade", List.of(ChatColor.GRAY + "Full Set Bonus: +100% Crop Yield, +1 Relic Yield"));
        bonusMap.put("Countershot", List.of(ChatColor.GRAY + "Full Set Bonus: Arrow Deflection"));
        bonusMap.put("Shadowstep", List.of(ChatColor.GRAY + "Full Set Bonus: +40% Dodge Chance"));
        bonusMap.put("Slayer", List.of(ChatColor.GRAY + "Full Set Bonus: "
                + StrengthManager.COLOR + "+20 " + StrengthManager.DISPLAY_NAME));
        bonusMap.put("Duskblood", List.of(ChatColor.GRAY + "Full Set Bonus: +60 Warp Stacks"));
        bonusMap.put("Thunderforge", List.of(ChatColor.GRAY + "Full Set Bonus: +15% Fury Chance"));
        bonusMap.put("Fathmic Iron", List.of(ChatColor.GRAY + "Full Set Bonus: Removes Common/Uncommon Sea Creatures, -20% Sea Creature Chance."));
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

        if (isAlreadyBlessed(helmet) || isAlreadyBlessed(chest) || isAlreadyBlessed(legs) || isAlreadyBlessed(boots)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "This armor set has already been blessed.");
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
        // Celebration effects
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        World world = armorStand.getWorld();
        Location effectLoc = armorStand.getLocation().add(0, 1, 0);
        world.playSound(effectLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        world.strikeLightningEffect(armorStand.getLocation());
        ChatColor chatColor = colorMap.getOrDefault(blessing, ChatColor.GREEN);
        world.spawnParticle(
                Particle.DRIPPING_HONEY,
                effectLoc,
                200,
                0.5, 1, 0.5, 0,
                new Particle.DustOptions(chatColorToColor(chatColor), 2f)
        );
        new MusicDiscManager(MinecraftNew.getInstance()).handleMusicDiscOtherside(player);
        event.setCancelled(true);
    }

    private boolean isFullNetherite(ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
        return helmet != null && helmet.getType() == Material.NETHERITE_HELMET &&
                chest != null && chest.getType() == Material.NETHERITE_CHESTPLATE &&
                legs != null && legs.getType() == Material.NETHERITE_LEGGINGS &&
                boots != null && boots.getType() == Material.NETHERITE_BOOTS;
    }

    private boolean isAlreadyBlessed(ItemStack piece) {
        if (piece == null || !piece.hasItemMeta()) return false;
        ItemMeta meta = piece.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Full Set Bonus")) {
                return true;
            }
        }
        return false;
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

    private Color chatColorToColor(ChatColor chatColor) {
        return switch (chatColor) {
            case BLACK -> Color.BLACK;
            case DARK_RED -> Color.MAROON;
            case DARK_GREEN -> Color.GREEN;
            case DARK_AQUA -> Color.TEAL;
            case DARK_PURPLE, LIGHT_PURPLE -> Color.PURPLE;
            case GOLD -> Color.ORANGE;
            case GRAY, DARK_GRAY -> Color.GRAY;
            case BLUE -> Color.BLUE;
            case AQUA -> Color.AQUA;
            case YELLOW -> Color.YELLOW;
            case WHITE -> Color.WHITE;
            default -> Color.WHITE;
        };
    }
}
