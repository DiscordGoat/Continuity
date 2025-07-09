package goat.minecraft.minecraftnew.other.beacon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class BeaconCatalystsGUI implements Listener {

    private final JavaPlugin plugin;
    private final ItemStack beacon;
    private final String guiTitle;

    public BeaconCatalystsGUI(JavaPlugin plugin, ItemStack beacon) {
        this.plugin = plugin;
        this.beacon = beacon;
        this.guiTitle = ChatColor.BLUE + "Beacon Catalysts";
    }

    public void openGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, guiTitle); // 5 rows for catalysts + navigation

        // Create catalyst selection buttons (2x4 grid in center)
        int[] catalystSlots = {11, 12, 13, 20, 21, 22};
        
        // Get the current held beacon instead of relying on constructor parameter
        ItemStack heldBeacon = getHeldBeacon(player);
        if (heldBeacon == null) {
            player.sendMessage(ChatColor.RED + "You must be holding a Beacon Charm to view catalysts!");
            return;
        }
        
        int tier = BeaconManager.getBeaconTier(heldBeacon);
        int range = BeaconManager.getBeaconRange(heldBeacon);
        int duration = BeaconManager.getBeaconDuration(heldBeacon);
        
        // Catalyst of Power
        int powerDamage = 25 + (tier * 5);
        ItemStack powerCatalyst = createCatalystButton(
            Material.REDSTONE_BLOCK,
            ChatColor.RED + "Catalyst of Power",
            true,
            "Grants increased damage to all",
            "players within range.",
            "",
            ChatColor.GOLD + "Total Damage Bonus: " + ChatColor.YELLOW + "+" + powerDamage + "%",
            ChatColor.GRAY + "(Base: +25% + " + tier + " × 5%)",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[0], powerCatalyst);

        // Catalyst of Flight
        ItemStack flightCatalyst = createCatalystButton(
            Material.FEATHER,
            ChatColor.AQUA + "Catalyst of Flight",
            true,
            "Enables creative flight for all",
            "players within range.",
            "",
            ChatColor.GOLD + "Effect: " + ChatColor.YELLOW + "Creative Flight",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[1], flightCatalyst);


        // Catalyst of Depth
        int depthSeaCreature = 5 + tier;
        int depthTreasure = 5 + tier;
        ItemStack depthCatalyst = createCatalystButton(
            Material.PRISMARINE,
            ChatColor.DARK_AQUA + "Catalyst of Depth",
            true,
            "Increases sea creature and treasure",
            "chances for all players within range.",
            "",
            ChatColor.GOLD + "Total Sea Creature Chance: " + ChatColor.YELLOW + "+" + depthSeaCreature + "%",
            ChatColor.GRAY + "(Base: +5% + " + tier + " × 1%)",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[2], depthCatalyst);


        // Catalyst of Rejuvenation
        ItemStack rejuvenationCatalyst = createCatalystButton(
            Material.GHAST_TEAR,
            ChatColor.GOLD + "Catalyst of Rejuvenation",
            true,
            "Slowly heals players and repairs",
            "gear while they remain nearby.",
            "",
            ChatColor.GOLD + "Effect: " + ChatColor.YELLOW + "Regeneration",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[3], rejuvenationCatalyst);

        // Catalyst of Insanity
        int insanitySpirit = 5 + tier;
        int insanityDamageReduction = 50 + (tier * 5);
        ItemStack insanityCatalyst = createCatalystButton(
            Material.SOUL_SAND,
            ChatColor.DARK_PURPLE + "Catalyst of Insanity",
            true,
            "Increases spirit chance and reduces",
            "damage from spirits for all",
            "players within range.",
            "",
            ChatColor.GOLD + "Total Spirit Chance: " + ChatColor.YELLOW + "+" + insanitySpirit + "%",
            ChatColor.GOLD + "Total Spirit Damage Reduction: " + ChatColor.YELLOW + "+" + insanityDamageReduction + "%",
            ChatColor.GRAY + "(Base: +5%/+50% + " + tier + " × 1%/5%)",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[4], insanityCatalyst);

        // Catalyst of Prosperity
        int prosperityTriple = 40 + (tier * 10);
        int prosperityXP = 20 + (tier * 5);
        int prosperityRare = 20 + (tier * 5);
        ItemStack prosperityCatalyst = createCatalystButton(
            Material.EMERALD_BLOCK,
            ChatColor.GREEN + "Catalyst of Prosperity",
            true,
            "Increases resource yields and",
            "skill experience for players within",
            "range.",
            "",
            ChatColor.GOLD + "Triple Drop Chance: " + ChatColor.YELLOW + "+" + prosperityTriple + "%",
            ChatColor.GOLD + "Skill XP Bonus: " + ChatColor.YELLOW + "+" + prosperityXP + "%",
            ChatColor.GOLD + "Rare Drop Bonus: " + ChatColor.YELLOW + "+" + prosperityRare + "%",
            ChatColor.BLUE + "Range: " + ChatColor.WHITE + range + " blocks",
            ChatColor.GREEN + "Duration: " + ChatColor.WHITE + duration + " seconds"
        );
        gui.setItem(catalystSlots[5], prosperityCatalyst);


        // Fill empty slots
        fillEmptySlots(gui);

        player.openInventory(gui);
    }

    private ItemStack createCatalystButton(Material material, String name, boolean available, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        loreList.add("");
        
        if (available) {
            loreList.add(ChatColor.GREEN + "✓ Available");
            loreList.add(ChatColor.YELLOW + "Click to select!");
        } else {
            loreList.add(ChatColor.RED + "✗ Not Available");
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        List<String> loreList = new ArrayList<>();
        loreList.add("");
        for (String line : lore) {
            loreList.add(ChatColor.GRAY + line);
        }
        loreList.add("");
        loreList.add(ChatColor.YELLOW + "Click to use!");

        meta.setLore(loreList);
        item.setItemMeta(meta);

        return item;
    }

    private void fillEmptySlots(Inventory gui) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.setDisplayName(" ");
        filler.setItemMeta(meta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                gui.setItem(i, filler);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!event.getView().getTitle().equals(guiTitle)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        switch (itemName) {
            case "← Back":
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.closeInventory();
                ItemStack heldBeacon = getHeldBeacon(player);
                if (heldBeacon != null) {
                    BeaconCharmGUI mainGUI = new BeaconCharmGUI(plugin, heldBeacon);
                    mainGUI.openGUI(player);
                } else {
                    player.sendMessage(ChatColor.RED + "You must be holding a Beacon Charm!");
                }
                break;
                
            case "Catalyst of Power":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Power")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.RED + "Catalyst of Power");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;
                
            case "Catalyst of Flight":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Flight")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.AQUA + "Catalyst of Flight");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;
                
                
            case "Catalyst of Depth":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Depth")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.DARK_AQUA + "Catalyst of Depth");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;


            case "Catalyst of Rejuvenation":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Rejuvenation")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.GOLD + "Catalyst of Rejuvenation");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;

            case "Catalyst of Insanity":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Insanity")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.DARK_PURPLE + "Catalyst of Insanity");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;

            case "Catalyst of Prosperity":
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);
                if (BeaconManager.updateBeaconInInventory(player, "Catalyst of Prosperity")) {
                    player.sendMessage(ChatColor.GREEN + "Selected: " + ChatColor.GREEN + "Catalyst of Prosperity");
                } else {
                    player.sendMessage(ChatColor.RED + "Could not find beacon in inventory!");
                }
                break;
        }
    }
    
    private ItemStack getHeldBeacon(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        
        if (isBeaconCharm(mainHand)) {
            return mainHand;
        } else if (isBeaconCharm(offHand)) {
            return offHand;
        }
        return null;
    }
    
    private boolean isBeaconCharm(ItemStack item) {
        if (item == null || item.getType() != Material.BEACON) return false;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return false;
        
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        return displayName.equals("Beacon Charm");
    }
}