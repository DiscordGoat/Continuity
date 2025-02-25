package goat.minecraft.minecraftnew.utils.devtools;

import goat.minecraft.minecraftnew.subsystems.forestry.CustomItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Provides methods to retrieve random reforge items.
 */
public class ReforgeItemProvider {

    private final List<ItemStack> allReforges;
    private final Random random;

    // Define all Sword Reforges
    private final ItemStack commonSwordReforge = CustomItemManager.createCustomItem(
            Material.WHITE_DYE,
            ChatColor.YELLOW + "Common Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack uncommonSwordReforge = CustomItemManager.createCustomItem(
            Material.LIME_DYE,
            ChatColor.YELLOW + "Uncommon Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack rareSwordReforge = CustomItemManager.createCustomItem(
            Material.BLUE_DYE,
            ChatColor.YELLOW + "Rare Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack epicSwordReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_DYE,
            ChatColor.YELLOW + "Epic Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack legendarySwordReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_DYE,
            ChatColor.YELLOW + "Legendary Sword Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges a sword to deal more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );

    // Define all Armor Reforges
    private final ItemStack commonArmorReforge = CustomItemManager.createCustomItem(
            Material.WHITE_STAINED_GLASS,
            ChatColor.YELLOW + "Common Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack uncommonArmorReforge = CustomItemManager.createCustomItem(
            Material.LIME_STAINED_GLASS,
            ChatColor.YELLOW + "Uncommon Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack rareArmorReforge = CustomItemManager.createCustomItem(
            Material.BLUE_STAINED_GLASS,
            ChatColor.YELLOW + "Rare Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack epicArmorReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_STAINED_GLASS,
            ChatColor.YELLOW + "Epic Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack legendaryArmorReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_STAINED_GLASS,
            ChatColor.YELLOW + "Legendary Armor Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges armor to absorb more damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );

    // Define all Tool Reforges
    private final ItemStack commonToolReforge = CustomItemManager.createCustomItem(
            Material.WHITE_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Common Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack uncommonToolReforge = CustomItemManager.createCustomItem(
            Material.LIME_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Uncommon Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack rareToolReforge = CustomItemManager.createCustomItem(
            Material.BLUE_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Rare Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack epicToolReforge = CustomItemManager.createCustomItem(
            Material.MAGENTA_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Epic Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );
    private final ItemStack legendaryToolReforge = CustomItemManager.createCustomItem(
            Material.YELLOW_STAINED_GLASS_PANE,
            ChatColor.YELLOW + "Legendary Tool Reforge",
            Arrays.asList(
                    ChatColor.GRAY + "Reforges tools to take less damage",
                    ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Refines base stats beyond normal levels.",
                    ChatColor.DARK_PURPLE + "Smithing Item"
            ),
            1,
            false,
            true
    );

    /**
     * Initializes the ReforgeItemProvider by organizing all reforge items into a single list.
     */
    public ReforgeItemProvider() {
        this.random = new Random();

        // Combine all reforges into a single unmodifiable list
        this.allReforges = Collections.unmodifiableList(Arrays.asList(
                commonSwordReforge,
                uncommonSwordReforge,
                rareSwordReforge,
                epicSwordReforge,
                legendarySwordReforge,
                commonArmorReforge,
                uncommonArmorReforge,
                rareArmorReforge,
                epicArmorReforge,
                legendaryArmorReforge,
                commonToolReforge,
                uncommonToolReforge,
                rareToolReforge,
                epicToolReforge,
                legendaryToolReforge
        ));
    }

    /**
     * Returns a random reforge item from all categories (Sword, Armor, Tool).
     *
     * @return A randomly selected reforge ItemStack.
     */
    public ItemStack getRandomReforge() {
        return allReforges.get(random.nextInt(allReforges.size()));
    }
}
