package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

/**
 * Utility methods for loading champion equipment from YAML files in the plugin resources.
 */
public class ChampionEquipmentUtil {

    private ChampionEquipmentUtil() { }

    /**
     * Loads armor contents from a YAML file bundled in the plugin resources and applies
     *
     * <p>The YAML file is expected to contain a list of {@link ItemStack} in the same
     * order returned by Bukkit's {@code PlayerInventory#getArmorContents()} (boots,
     * leggings, chestplate, helmet). The items will be mapped to Citizens inventory
     * slots as helmet (0), chestplate (1), leggings (2) and boots (3).</p>
     *
     * @param plugin       plugin instance used to access resources
     * @param npc          NPC whose armor should be modified
     * @param resourcePath path within the plugin JAR to the YAML file
     */
    public static void setArmorContentsFromFile(JavaPlugin plugin, NPC npc, String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            List<?> armorList = config.getList("armor");
            if (armorList == null || !npc.hasTrait(Equipment.class)) {
                return;
            }
            Equipment equipmentTrait = npc.getOrAddTrait(Equipment.class);
            if (equipmentTrait.getEquipment() == null) {
                return;
            }

            ItemStack[] armor = armorList.stream()
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .toArray(ItemStack[]::new);

            if (armor.length > 3) equipmentTrait.set(Equipment.EquipmentSlot.HELMET, armor[3]); // helmet
            if (armor.length > 2) equipmentTrait.set(Equipment.EquipmentSlot.CHESTPLATE, armor[2]); // chestplate
            if (armor.length > 1) equipmentTrait.set(Equipment.EquipmentSlot.LEGGINGS, armor[1]); // leggings
            if (armor.length > 0) equipmentTrait.set(Equipment.EquipmentSlot.BOOTS, armor[0]); // boots
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads armor contents from a YAML file in the plugin's data folder and applies
     * them to the provided player.
     *
     * <p>The YAML file must have the same structure produced by
     * {@link goat.minecraft.minecraftnew.utils.developercommands.SaveArmorContentsCommand}
     * so that the saved armor can be re-equipped.</p>
     *
     * @param plugin   plugin instance used to access the data folder
     * @param player   player whose armor should be modified
     * @param file     YAML file containing the armor contents
     */
    public static void setArmorContentsFromFile(JavaPlugin plugin, Player player, File file) {
        try {
            if (!file.exists() || player.getInventory() == null) {
                return;
            }
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<?> armorList = config.getList("armor");
            if (armorList == null) {
                return;
            }

            ItemStack[] armor = armorList.stream()
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .toArray(ItemStack[]::new);

            player.getInventory().setArmorContents(armor);
        } catch (Exception ignored) {
        }
    }

    /**
     * Loads a held item from a YAML file bundled in the plugin resources and places
     * it in the player's main hand.
     *
     * @param plugin       plugin instance used to access resources
     * @param player       player to modify
     * @param resourcePath path within the plugin JAR to the YAML file
     */
    public static void setHeldItemFromFile(JavaPlugin plugin, Player player, String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            ItemStack item = config.getItemStack("item");
            if (item != null && player.getEquipment() != null) {
                player.getEquipment().setItemInMainHand(item);
            }
        } catch (Exception ignored) {
        }
    }
    public static ItemStack getItemFromFile(JavaPlugin plugin, String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return null;

            try (InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
                ItemStack item = config.getItemStack("item");
                return (item != null) ? item.clone() : null; // clone so callers can't mutate the cached instance
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to load ItemStack from " + resourcePath, ex);
            return null;
        }
    }
}
