package goat.minecraft.minecraftnew.other.arenas.champions;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.InventoryTrait;
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
     * them to the NPC's armor slots via the Citizens {@link InventoryTrait}.
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
            if (armorList == null || !npc.hasTrait(InventoryTrait.class)) {
                return;
            }
            InventoryTrait inventory = npc.getTrait(InventoryTrait.class);
            if (inventory.getInventory() == null) {
                return;
            }

            ItemStack[] armor = armorList.stream()
                    .filter(ItemStack.class::isInstance)
                    .map(ItemStack.class::cast)
                    .toArray(ItemStack[]::new);

            if (armor.length > 3) inventory.setItem(0, armor[3]); // helmet
            if (armor.length > 2) inventory.setItem(1, armor[2]); // chestplate
            if (armor.length > 1) inventory.setItem(2, armor[1]); // leggings
            if (armor.length > 0) inventory.setItem(3, armor[0]); // boots
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
}
