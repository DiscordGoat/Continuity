package goat.minecraft.minecraftnew.utils.devtools;

import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.utils.devtools.TalismanManager;
import goat.minecraft.minecraftnew.utils.stats.StrengthManager;
import goat.minecraft.minecraftnew.utils.stats.DefenseManager;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility class to normalize item lore ordering across the plugin.
 */
public class ItemLoreFormatter {
    private static final Pattern ROMAN = Pattern.compile("[IVXLCDM]+$");
    private static final Set<String> VANILLA_NAMES;

    static {
        Set<String> names = new HashSet<>();
        for (Enchantment e : Enchantment.values()) {
            names.add(capitalize(e.getKey().getKey().replace('_', ' ')));
        }
        VANILLA_NAMES = names;
    }

    private ItemLoreFormatter() {}

    /**
     * Reorders the lore of the given item to the standard format.
     */
    public static void formatLore(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        String power = null;
        String upgrades = null;
        String bar = null;
        String cap = null;
        String trim = null;
        String bless = null;
        String durability = null;
        List<String> vanilla = new ArrayList<>();
        List<String> custom = new ArrayList<>();
        List<String> talismans = new ArrayList<>();
        List<String> reforge = new ArrayList<>();
        List<String> other = new ArrayList<>();

        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.startsWith("Gemstone Power") || stripped.startsWith("Angler Energy") ||
                    stripped.startsWith("Soul Power") || stripped.startsWith("Spirit Energy")) {
                power = line;
            }else if (stripped.contains("Upgrades")) {
                upgrades = line;
            } else if (stripped.startsWith("Power Cap") || stripped.startsWith("Spirit Cap") || stripped.startsWith("Enhanced Power Cap") || stripped.startsWith("Soul Cap")) {
                cap = line;
            } else if (stripped.contains("[") && stripped.contains("|") && stripped.contains("]")) {
                bar = line;
            } else if (stripped.startsWith("Trim:")) {
                trim = line;
            } else if (stripped.startsWith("Talisman:")) {
                if (stripped.startsWith("Talisman: Damage") && !stripped.contains("Strength")) {
                    String newline = ChatColor.GOLD + "Talisman: Damage. "
                            + StrengthManager.COLOR + "+" + TalismanManager.DAMAGE_STRENGTH_BONUS
                            + " Strength" + StrengthManager.EMOJI;
                    talismans.add(newline);
                } else if (stripped.startsWith("Talisman: Armor") ||
                        (stripped.startsWith("Talisman: Defense") && !stripped.contains("+"))) {
                    String newline = ChatColor.GOLD + "Talisman: Defense. "
                            + DefenseManager.COLOR + "+" + TalismanManager.DEFENSE_DEFENSE_BONUS
                            + " Defense" + DefenseManager.EMOJI;
                    talismans.add(newline);
                } else {
                    talismans.add(line);
                }
            } else if (stripped.startsWith("Damage Increase")) {
                if (isSwordOrAxe(item)) {
                    // convert old sword reforge display to new Strength format
                    String[] parts = stripped.split(" ");
                    int amount = 0;
                    if (parts.length > 0) {
                        String last = parts[parts.length - 1];
                        if (last.endsWith("%")) {
                            last = last.substring(0, last.length() - 1);
                        }
                        try {
                            amount = Integer.parseInt(last);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    String newline = ChatColor.DARK_GRAY + "Strength: " + StrengthManager.COLOR + "+" +
                            amount + " " + StrengthManager.EMOJI;
                    reforge.add(newline);
                } else {
                    reforge.add(line);
                }
            } else if (stripped.startsWith("Strength:")) {
                reforge.add(line);
            } else if (stripped.startsWith("Damage Reduction") ||
                       stripped.startsWith("Defense") ||
                       stripped.startsWith("Chance to repair durability") || stripped.startsWith("Max Durability")) {
                reforge.add(line);
            } else if (stripped.startsWith("Durability:") || stripped.startsWith("Golden Durability:")) {
                durability = line;
            } else if (stripped.contains("Full Set Bonus")) {
                bless = line;
            } else if (isEnchantmentLine(stripped)) {
                String name = stripped.replaceAll(" [IVXLCDM]+$", "");
                if (VANILLA_NAMES.contains(name)) {
                    vanilla.add(line);
                } else {
                    custom.add(line);
                }
            } else {
                other.add(line);
            }
        }

        List<String> newLore = new ArrayList<>();
        // Keep other lore (like descriptions) before formatted sections
        newLore.addAll(other);



        if (isArmor(item) && trim != null) {
            newLore.add(trim);
        }

        newLore.addAll(vanilla);
        newLore.addAll(custom);
        if (power != null && !isSwordOrAxe(item)) {
            newLore.add(power);
            if (bar != null) newLore.add(bar);
            if(upgrades != null) newLore.add(upgrades);
            if (cap != null) newLore.add(cap);
        }
        newLore.addAll(talismans);
        newLore.addAll(reforge);
        if (isArmor(item) && bless != null) {
            newLore.add(bless);
        }

        if (durability != null) {
            newLore.add(durability);
        }

        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    private static boolean isEnchantmentLine(String stripped) {
        int lastSpace = stripped.lastIndexOf(' ');
        if (lastSpace == -1) return false;
        String numeral = stripped.substring(lastSpace + 1);
        return ROMAN.matcher(numeral).matches();
    }

    private static boolean isSwordOrAxe(ItemStack item) {
        if (item == null) return false;
        String n = item.getType().name();
        return n.endsWith("_SWORD") || n.endsWith("_AXE");
    }

    private static boolean isArmor(ItemStack item) {
        if (item == null) return false;
        String n = item.getType().name();
        return n.endsWith("_HELMET") || n.endsWith("_CHESTPLATE") || n.endsWith("_LEGGINGS") || n.endsWith("_BOOTS");
    }

    private static String capitalize(String text) {
        String[] parts = text.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            sb.append(Character.toUpperCase(parts[i].charAt(0)))
              .append(parts[i].substring(1).toLowerCase());
            if (i < parts.length - 1) sb.append(' ');
        }
        return sb.toString();
    }
}
