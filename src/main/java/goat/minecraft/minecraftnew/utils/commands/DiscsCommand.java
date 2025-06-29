package goat.minecraft.minecraftnew.utils.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DiscsCommand implements CommandExecutor, Listener {

    // A mapping from each music disc Material to its display data.
    private final Map<Material, DiscData> discDataMap;
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check if the inventory title is the one we set in our command
        if (event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Music Discs")) {
            event.setCancelled(true);
        }
    }
    public DiscsCommand() {
        discDataMap = new HashMap<>();
        // Populate disc data for each music disc
        discDataMap.put(Material.MUSIC_DISC_11, new DiscData(
                ChatColor.DARK_RED + "Music Disc 11",
                ChatColor.RED,
                Arrays.asList(
                        "Boosts monster hostility to Tier 20",
                        "Lasts 20 minutes, making mobs very aggressive"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_13, new DiscData(
                ChatColor.AQUA + "Music Disc 13",
                ChatColor.AQUA,
                Arrays.asList(
                        "Activates the BaroTrauma Virus for 3 minutes",
                        "Infected mobs glow and drop extra XP"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_BLOCKS, new DiscData(
                ChatColor.GREEN + "Music Disc Blocks",
                ChatColor.GREEN,
                Arrays.asList(
                        "Activates Recipe Writer feature",
                        "Gradually grants 32 random recipes over 345 seconds"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_CAT, new DiscData(
                ChatColor.LIGHT_PURPLE + "Music Disc Cat",
                ChatColor.LIGHT_PURPLE,
                Arrays.asList(
                        "Starts Harvest Frenzy for 3 minutes 5 seconds",
                        "Boosts crop growth with increased tick speed"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_CHIRP, new DiscData(
                ChatColor.YELLOW + "Music Disc Chirp",
                ChatColor.YELLOW,
                Arrays.asList(
                        "Enrages the Forest Spirits for 3 minutes 5 seconds",
                        "Chance to yield bonus logs and extra Forestry XP, as well as Tier 5 Forest Spirits"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_FAR, new DiscData(
                ChatColor.GOLD + "Music Disc Far",
                ChatColor.GOLD,
                Arrays.asList(
                        "Begins a Random Loot Crates event",
                        "Spawns 16 loot chests with themed drops"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_MALL, new DiscData(
                ChatColor.AQUA + "Music Disc Mall",
                ChatColor.AQUA,
                Arrays.asList(
                        "Starts a 10-minute rainstorm",
                        "Disables mob spawns and grants 40 minutes of Conduit Power"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_MELLOHI, new DiscData(
                ChatColor.DARK_GREEN + "Music Disc Mellohi",
                ChatColor.DARK_GREEN,
                Arrays.asList(
                        "Initiates a Zombie Apocalypse for 96 seconds",
                        "Transforms spawns into zombies with extra drops"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_STAL, new DiscData(
                ChatColor.DARK_PURPLE + "Music Disc Stal",
                ChatColor.DARK_PURPLE,
                Arrays.asList(
                        "Launches the Grand Auction Event",
                        "Displays auction items for purchase with emeralds"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_STRAD, new DiscData(
                ChatColor.LIGHT_PURPLE + "Music Disc Strad",
                ChatColor.LIGHT_PURPLE,
                Arrays.asList(
                        "Repairs your items gradually over 188 seconds",
                        "Restores durability for both inventory and armor"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_WAIT, new DiscData(
                ChatColor.BLUE + "Music Disc Wait",
                ChatColor.BLUE,
                Arrays.asList(
                        "Activates an Experience Surge event for 231 seconds",
                        "Randomly awards small amounts of XP to various skills"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_WARD, new DiscData(
                ChatColor.AQUA + "Music Disc Ward",
                ChatColor.AQUA,
                Arrays.asList(
                        "Rains XP near a jukebox for 251 seconds",
                        "Spawns XP orbs with accompanying visual effects"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_5, new DiscData(
                ChatColor.GRAY + "Music Disc 5",
                ChatColor.GRAY,
                Arrays.asList(
                        "Places regenerating emerald ore above a jukebox",
                        "Mine as many emeralds as you can before the song ends"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_OTHERSIDE, new DiscData(
                ChatColor.GOLD + "Music Disc Otherside",
                ChatColor.GOLD,
                Arrays.asList(
                        "Accelerates time for 195 seconds",
                        "Spawns parrots in day or fireworks at night"
                )
        ));
        discDataMap.put(Material.MUSIC_DISC_RELIC, new DiscData(
                ChatColor.DARK_AQUA + "Music Disc Relic",
                ChatColor.DARK_AQUA,
                Arrays.asList(
                        "Opens a teleportation session",
                        "Choose a target biome and teleport with optional return"
                )
        ));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure only players can run the command.
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        // Create a GUI inventory (27 slots is enough for our 16 discs)
        Inventory gui = player.getServer().createInventory(null, 27, ChatColor.DARK_AQUA + "Music Discs");

        int slot = 0;
        // Add each disc to the inventory with its display name and lore.
        for (Map.Entry<Material, DiscData> entry : discDataMap.entrySet()) {
            Material discMaterial = entry.getKey();
            DiscData data = entry.getValue();
            ItemStack discItem = new ItemStack(discMaterial);
            ItemMeta meta = discItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(data.getName());
                meta.setLore(data.getLore());
                discItem.setItemMeta(meta);
            }
            gui.setItem(slot, discItem);
            slot++;
        }
        // Open the GUI for the player.
        player.openInventory(gui);
        return true;
    }

    // A helper class to hold the display data for each music disc.
    private static class DiscData {
        private final String name;
        private final ChatColor color;
        private final List<String> lore;

        public DiscData(String name, ChatColor color, List<String> lore) {
            this.name = name;
            this.color = color;
            // Prepend a gray color to each lore line for uniformity.
            this.lore = new ArrayList<>();
            for (String line : lore) {
                this.lore.add(ChatColor.GRAY + line);
            }
        }

        public String getName() {
            return name;
        }

        public ChatColor getColor() {
            return color;
        }

        public List<String> getLore() {
            return lore;
        }
    }
}
