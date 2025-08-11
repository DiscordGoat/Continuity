package goat.minecraft.minecraftnew.other.arenas.champions;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Utility methods for loading champion equipment from YAML files in the plugin resources.
 */
public class ChampionEquipmentUtil {

    private ChampionEquipmentUtil() { }

    /**
     * Loads armor contents from a YAML file bundled in the plugin resources and applies
     * them to the player's armor slots.
     *
     * @param plugin      plugin instance used to access resources
     * @param player      player to modify
     * @param resourcePath path within the plugin JAR to the YAML file
     */
    public static void setArmorContentsFromFile(JavaPlugin plugin, Player player, String resourcePath) {
        try (InputStream in = plugin.getResource(resourcePath)) {
            if (in == null) return;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in));
            List<?> list = config.getList("armor");
            if (list != null) {
                ItemStack[] armor = list.stream()
                        .filter(ItemStack.class::isInstance)
                        .map(ItemStack.class::cast)
                        .toArray(ItemStack[]::new);
                player.getInventory().setArmorContents(armor);
            }
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
            if (item != null) {
                player.getInventory().setItemInMainHand(item);
            }
        } catch (Exception ignored) {
        }
    }
}
