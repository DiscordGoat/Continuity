package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeritCommand implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final PlayerMeritManager playerData;

    /**
     * Simple container for perk data: supports & color codes in name,
     * a cost, a custom Material icon, and a description as a list of lore lines.
     */
    private static class Perk {
        private final String name;     // Can include & color codes
        private final int cost;
        private final Material icon;
        private final List<String> description;

        public Perk(String name, int cost, Material icon, List<String> description) {
            this.name = name;
            this.cost = cost;
            this.icon = icon;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public int getCost() {
            return cost;
        }

        public Material getIcon() {
            return icon;
        }

        public List<String> getDescription() {
            return description;
        }
    }

    /**
     * 45 perks from the concept list.
     * Edit the text, chat colors, materials, and descriptions as needed.
     */
    private static final List<Perk> perks = Arrays.asList(
            new Perk(ChatColor.DARK_GRAY + "EnderMind", 1, Material.ENDER_PEARL,
                    Arrays.asList(
                            ChatColor.GRAY + "Nullifies Ender Pearl Damage by 100%",
                            ChatColor.BLUE + "On Pearl Land: " + ChatColor.GRAY + "+20 Hunger, +20 Saturation."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Workbench", 3, Material.CRAFTING_TABLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Backpack Crafting Table usage.",
                            ChatColor.BLUE + "On Right Click Crafting Table In Backpack: " + ChatColor.GRAY + "Opens Crafting Table."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Shulkl Box", 7, Material.SHULKER_SHELL,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Backpack Shulker Box usage.",
                            ChatColor.BLUE + "On Right Click Shulker Box In Backpack: " + ChatColor.GRAY + "Opens it."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Motion Sensor", 1, Material.OAK_DOOR,
                    Arrays.asList(
                            ChatColor.GRAY + "Enables Autoclose doors.",
                            ChatColor.BLUE + "On Left Click Door: " + ChatColor.GRAY + "Opens doors for 2 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Unbreaking", 1, Material.OBSIDIAN,
                    Arrays.asList(
                            ChatColor.GRAY + "Prevents 15% of durability losses.",
                            ChatColor.BLUE + "On Lose Durability: " + ChatColor.GRAY + "15% chance to refund."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Berserkers Rage", 1, Material.FERMENTED_SPIDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "Enrages you when struck by a competent foe.",
                            ChatColor.BLUE + "On Take High Damage (30%) In One Hit: " + ChatColor.GRAY + "Strength 5 for 20 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Tactical Retreat", 1, Material.FEATHER,
                    Arrays.asList(
                            ChatColor.GRAY + "Speeds your step you when struck by a competent foe.",
                            ChatColor.BLUE + "On Take High Damage (30%) In One Hit: " + ChatColor.GRAY + "Speed 2 and Invisibility for 20 seconds."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Vampiric Strike", 1, Material.GHAST_TEAR,
                    Arrays.asList(
                            ChatColor.GRAY + "Heals you and kills your foe.",
                            ChatColor.BLUE + "On Deal Damage Rarely (5%): " + ChatColor.GRAY + "Fully Heals you and kills your foe."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Lord of Thunder", 1, Material.LIGHTNING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "Smites your foe.",
                            ChatColor.BLUE + "On Deal Damage Rarely (5%): " + ChatColor.GRAY + "Strike your foe with lightning."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Deep Hook", 2, Material.FISHING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "Taunts Sea Creatures.",
                            ChatColor.BLUE + "Reel In Fish: " + ChatColor.GRAY + "+5 Sea Creature Chance."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Instant Transmission", 7, Material.ENDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "Breaks the rules of teleportation.",
                            ChatColor.BLUE + "On Use Ultimate: Warp: " + ChatColor.GRAY + "Removes cooldown."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Deep Breath", 1, Material.GLASS_BOTTLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Obviously grants more oxygen you idiot.",
                            ChatColor.BLUE + "On Regenerate Oxygen: " + ChatColor.GRAY + "+100 Oxygen."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Trainer", 3, Material.LEAD,
                    Arrays.asList(
                            ChatColor.GRAY + "Boosts pet XP gains.",
                            ChatColor.BLUE + "On Gain Experience: " + ChatColor.GRAY + "+1 Bonus Pet XP."
                    )),
            new Perk(ChatColor.DARK_GRAY + "QuickSwap", 3, Material.TRIPWIRE_HOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Replenishes your supply of blocks.",
                            ChatColor.BLUE + "On Place Block: " + ChatColor.GRAY + "Replenishes your block supply."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Restock", 1, Material.ARROW,
                    Arrays.asList(
                            ChatColor.GRAY + "Grants arrows when holding a bow with none left.",
                            ChatColor.BLUE + "On Out Of Arrows While Bow Equipped: " + ChatColor.GRAY + "Gives one arrow."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Rebreather", 1, Material.TURTLE_HELMET,
                    Arrays.asList(
                            ChatColor.GRAY + "Regenerates breath underwater below Y=50.",
                            ChatColor.BLUE + "When Underwater Below Y=50: " + ChatColor.GRAY + "+1 Oxygen every 3s."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Keepinventory", 1, Material.CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Keeps your items on death after login.",
                            ChatColor.BLUE + "On Login: " + ChatColor.GRAY + "Keep inventory enabled for that session."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Smith", 1, Material.ANVIL,
                    Arrays.asList(
                            ChatColor.GRAY + "Adds +50 durability when repairing with iron.",
                            ChatColor.BLUE + "On Anvil Repair With Iron: " + ChatColor.GRAY + "+50 durability."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Botanist", 1, Material.WHEAT_SEEDS,
                    Arrays.asList(
                            ChatColor.GRAY + "Verdant relics mature 50% faster.",
                            ChatColor.BLUE + "On Verdant Relic Growth: " + ChatColor.GRAY + "Time reduced by 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Brewer", 1, Material.BREWING_STAND,
                    Arrays.asList(
                            ChatColor.GRAY + "Brews potions 50% faster.",
                            ChatColor.BLUE + "On Brewing: " + ChatColor.GRAY + "Total time reduced by 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Chef", 1, Material.COOKED_BEEF,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to cook double culinary delights.",
                            ChatColor.BLUE + "On Finalize Recipe: " + ChatColor.GRAY + "50% chance for 2 items."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Thief", 1, Material.ENDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for rare monster drops to double.",
                            ChatColor.BLUE + "On Monster Kill: " + ChatColor.GRAY + "50% chance drop occurs twice."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Duelist", 1, Material.IRON_SWORD,
                    Arrays.asList(
                            ChatColor.GRAY + "20% chance to crit for +50% damage.",
                            ChatColor.BLUE + "On Melee Attack: " + ChatColor.GRAY + "20% chance to crit for +50% damage."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Angler", 1, Material.FISHING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "+5% sea creature chance.",
                            ChatColor.BLUE + "On Fishing: " + ChatColor.GRAY + "+5% sea creature chance."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diplomat", 1, Material.WRITABLE_BOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Reduces notoriety gains by 60%.",
                            ChatColor.BLUE + "On Notoriety Gain: " + ChatColor.GRAY + "Reduce gain by 60%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diffuser", 1, Material.CREEPER_HEAD,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for creepers to drop a disc.",
                            ChatColor.BLUE + "On Creeper Death: " + ChatColor.GRAY + "50% chance for random music disc."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Trader", 1, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "5% chance for purchases to be free.",
                            ChatColor.BLUE + "On Purchase: " + ChatColor.GRAY + "5% chance to cost nothing."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Employer", 1, Material.BELL,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to halve villager work timers.",
                            ChatColor.BLUE + "On Workcycle: " + ChatColor.GRAY + "50% chance next timer reduced 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Loyalty II", 1, Material.NAME_TAG,
                    Arrays.asList(
                            ChatColor.GRAY + "Loyal cooldown reduced to 1 second.",
                            ChatColor.BLUE + "On Use Loyal: " + ChatColor.GRAY + "Cooldown becomes 1 second."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Double Enderchest", 1, Material.ENDER_CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Ender chest storage doubled.",
                            ChatColor.BLUE + "On Open Ender Chest: " + ChatColor.GRAY + "Opens 54-slot inventory."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Strong Digestion", 1, Material.GOLDEN_APPLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles potion duration.",
                            ChatColor.BLUE + "On Drink Potion: " + ChatColor.GRAY + "Duration increased by 100%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Icarus", 1, Material.ELYTRA,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles flight pet distance.",
                            ChatColor.BLUE + "On Flight Pet Use: " + ChatColor.GRAY + "Flying limit doubled."
                    )),
            new Perk(ChatColor.DARK_GRAY + "AutoStrad", 20, Material.NETHERITE_INGOT,
                    Arrays.asList(
                            ChatColor.GRAY + "Repairs all gear after 10 minutes without damage.",
                            ChatColor.BLUE + "After 10m Without Damage: " + ChatColor.GRAY + "Fully repairs equipment."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Prevents death once, then must be repurchased.",
                            ChatColor.BLUE + "On Fatal Damage: " + ChatColor.GRAY + "Survive with buffs, perk consumed."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection Charge 2", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Allows a second resurrection charge.",
                            ChatColor.BLUE + "On Purchase: " + ChatColor.GRAY + "Adds one extra charge (max 2)."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection Charge 3", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Allows a third resurrection charge.",
                            ChatColor.BLUE + "On Purchase: " + ChatColor.GRAY + "Adds another charge (max 3)."
                            ChatColor.GRAY + "Grants arrows when holding a bow with none left."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Rebreather", 1, Material.TURTLE_HELMET,
                    Arrays.asList(
                            ChatColor.GRAY + "Regenerates breath underwater below Y=50."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Keepinventory", 1, Material.CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Keeps your items on death after login."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Smith", 1, Material.ANVIL,
                    Arrays.asList(
                            ChatColor.GRAY + "Adds +50 durability when repairing with iron." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Botanist", 1, Material.WHEAT_SEEDS,
                    Arrays.asList(
                            ChatColor.GRAY + "Verdant relics mature 50% faster." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Brewer", 1, Material.BREWING_STAND,
                    Arrays.asList(
                            ChatColor.GRAY + "Brews potions 50% faster." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Chef", 1, Material.COOKED_BEEF,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to cook double culinary delights." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Thief", 1, Material.ENDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for rare monster drops to double." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Duelist", 1, Material.IRON_SWORD,
                    Arrays.asList(
                            ChatColor.GRAY + "20% chance to crit for +50% damage." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Angler", 1, Material.FISHING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "+5% sea creature chance." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diplomat", 1, Material.WRITABLE_BOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Reduces notoriety gains by 60%." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diffuser", 1, Material.CREEPER_HEAD,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for creepers to drop a disc." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Trader", 1, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "5% chance for purchases to be free." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Employer", 1, Material.BELL,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to halve villager work timers." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Loyalty II", 1, Material.NAME_TAG,
                    Arrays.asList(
                            ChatColor.GRAY + "Loyal cooldown reduced to 1 second." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Double Enderchest", 1, Material.ENDER_CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Ender chest storage doubled." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Strong Digestion", 1, Material.GOLDEN_APPLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles potion duration." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Icarus", 1, Material.ELYTRA,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles flight pet distance." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "AutoStrad", 20, Material.NETHERITE_INGOT,
                    Arrays.asList(
                            ChatColor.GRAY + "Repairs all gear after 10 minutes without damage." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Prevents death once, then must be repurchased." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection Charge 2", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Allows a second resurrection charge." 
                    )),
            new Perk(ChatColor.DARK_GRAY + "Resurrection Charge 3", 1, Material.TOTEM_OF_UNDYING,
                    Arrays.asList(
                            ChatColor.GRAY + "Allows a third resurrection charge."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Unlooting", 0, Material.BARRIER,
                    Arrays.asList(
                            ChatColor.GRAY + "Destroys junk drops automatically.",
                            ChatColor.BLUE + "On Pickup: " + ChatColor.GRAY + "Rotten flesh and similar items vanish."
                    ))
            );


    public MeritCommand(JavaPlugin plugin, PlayerMeritManager playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by a player!");
            return true;
        }
        Player player = (Player) sender;
        openMeritGUI(player);
        return true;
    }

    /**
     * Create and open the 54-slot "Merit" GUI for the player.
     */
    private void openMeritGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Merits Menu");

        // === FIRST ROW (indices 0..8) ===
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackGlassMeta = blackGlass.getItemMeta();
        blackGlassMeta.setDisplayName(ChatColor.BLACK + "");
        blackGlass.setItemMeta(blackGlassMeta);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass.clone());
        }

        // Diamond in center (slot 4) shows available merit points
        int currentMeritPoints = playerData.getMeritPoints(player.getUniqueId());
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta diamondMeta = diamond.getItemMeta();
        diamondMeta.setDisplayName(ChatColor.AQUA + "Available Merit Points: " + currentMeritPoints);
        diamond.setItemMeta(diamondMeta);
        inv.setItem(4, diamond);

        // === REMAINING 45 SLOTS (indices 9..53) ===
        int slotIndex = 9;
        for (Perk perk : perks) {
            ItemStack perkItem = new ItemStack(perk.getIcon());
            ItemMeta perkMeta = perkItem.getItemMeta();

            // Translate & codes in the perk name for fancy titles
            String displayName = ChatColor.translateAlternateColorCodes('&', perk.getName());
            perkMeta.setDisplayName(displayName);

            // Set lore with cost and description from our List
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Cost: " + perk.getCost());
            lore.addAll(perk.getDescription());
            perkMeta.setLore(lore);

            // If the player has already purchased this perk, add an enchant glow
            String strippedName = ChatColor.stripColor(displayName);
            if (playerData.hasPerk(player.getUniqueId(), strippedName)) {
                perkMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                perkMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            perkItem.setItemMeta(perkMeta);
            inv.setItem(slotIndex, perkItem);
            slotIndex++;
        }

        player.openInventory(inv);
    }

    /**
     * Handles clicks within the Merit GUI.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();

        // Check if this is our custom inventory
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Merits Menu")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR)
                return;

            // If player clicks the diamond, show available points
            if (clickedItem.getType() == Material.DIAMOND) {
                player.sendMessage(ChatColor.GREEN + "You have "
                        + playerData.getMeritPoints(player.getUniqueId()) + " available merit points.");
                return;
            }

            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null)
                return;

            // Extract cost from lore
            int cost = 0;
            if (meta.getLore() != null) {
                for (String line : meta.getLore()) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.startsWith("Cost: ")) {
                        try {
                            cost = Integer.parseInt(stripped.replace("Cost: ", ""));
                        } catch (NumberFormatException ignored) { }
                        break;
                    }
                }
            }

            // Get the perk title (using stripped name)
            String perkTitle = ChatColor.stripColor(meta.getDisplayName());

            // If already purchased, inform the player
            if (playerData.hasPerk(player.getUniqueId(), perkTitle)) {
                player.sendMessage(ChatColor.RED + "You have already purchased this perk!");
                return;
            }

            // Check if the player can afford the perk
            int currentPoints = playerData.getMeritPoints(player.getUniqueId());
            if (currentPoints < cost) {
                player.sendMessage(ChatColor.RED + "You do not have enough merit points to purchase this perk.");
                return;
            }

            // Deduct points and mark the perk as purchased
            playerData.setMeritPoints(player.getUniqueId(), currentPoints - cost);
            playerData.addPerk(player.getUniqueId(), perkTitle);

            player.sendMessage(ChatColor.GREEN + "You purchased perk: " + perkTitle
                    + " for " + cost + " merit points!");

            // Refresh the GUI to update the available points and add the enchant glow to the purchased perk
            openMeritGUI(player);
        }
    }
}
