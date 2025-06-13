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

    private enum Category {
        COMBAT("Combat", Material.IRON_SWORD),
        SMITHING("Smithing", Material.ANVIL),
        FARMING("Farming", Material.WHEAT),
        BREWING("Brewing", Material.BREWING_STAND),
        FISHING("Fishing", Material.FISHING_ROD),
        VILLAGER("Villager", Material.EMERALD),
        UTILITY("Utility", Material.REDSTONE);

        private final String display;
        private final Material icon;

        Category(String display, Material icon) {
            this.display = display;
            this.icon = icon;
        }

        public String getDisplay() { return display; }
        public Material getIcon() { return icon; }
    }

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
            new Perk(ChatColor.DARK_GRAY + "Unbreaking II", 1, Material.OBSIDIAN,
                    Arrays.asList(
                            ChatColor.GRAY + "Stacks with Unbreaking for another 15% chance.",
                            ChatColor.BLUE + "On Lose Durability: " + ChatColor.GRAY + "Additional 15% refund chance."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Unbreaking III", 1, Material.OBSIDIAN,
                    Arrays.asList(
                            ChatColor.GRAY + "Further increases durability refund by 15%.",
                            ChatColor.BLUE + "On Lose Durability: " + ChatColor.GRAY + "Total 45% chance to refund."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Excavator", 1, Material.DIAMOND_SHOVEL,
                    Arrays.asList(
                            ChatColor.GRAY + "Shovels rarely wear down.",
                            ChatColor.BLUE + "On Shovel Use: " + ChatColor.GRAY + "90% chance durability is preserved."
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
            new Perk(ChatColor.DARK_GRAY + "Rebreather", 2, Material.TURTLE_HELMET,
                    Arrays.asList(
                            ChatColor.GRAY + "Regenerates breath underwater below Y=50.",
                            ChatColor.BLUE + "When Underwater Below Y=50: " + ChatColor.GRAY + "+1 Oxygen every 3s."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Keepinventory", 3, Material.CHEST,
                    Arrays.asList(
                            ChatColor.GRAY + "Keeps your items on death after login.",
                            ChatColor.BLUE + "On Login: " + ChatColor.GRAY + "Keep inventory enabled for that session."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Smith", 3, Material.ANVIL,
                    Arrays.asList(
                            ChatColor.GRAY + "Adds +50 durability when repairing with iron.",
                            ChatColor.BLUE + "On Anvil Repair With Iron: " + ChatColor.GRAY + "+50 durability."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Botanist", 3, Material.WHEAT_SEEDS,
                    Arrays.asList(
                            ChatColor.GRAY + "Verdant relics mature 50% faster.",
                            ChatColor.BLUE + "On Verdant Relic Growth: " + ChatColor.GRAY + "Time reduced by 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Brewer", 3, Material.BREWING_STAND,
                    Arrays.asList(
                            ChatColor.GRAY + "Brews potions 50% faster.",
                            ChatColor.BLUE + "On Brewing: " + ChatColor.GRAY + "Total time reduced by 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Chef", 3, Material.COOKED_BEEF,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to cook double culinary delights.",
                            ChatColor.BLUE + "On Finalize Recipe: " + ChatColor.GRAY + "50% chance for 2 items."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Thief", 3, Material.ENDER_EYE,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for rare monster drops to double.",
                            ChatColor.BLUE + "On Monster Kill: " + ChatColor.GRAY + "50% chance drop occurs twice."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Duelist", 3, Material.IRON_SWORD,
                    Arrays.asList(
                            ChatColor.GRAY + "20% chance to crit for +50% damage.",
                            ChatColor.BLUE + "On Melee Attack: " + ChatColor.GRAY + "20% chance to crit for +50% damage."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Angler", 3, Material.FISHING_ROD,
                    Arrays.asList(
                            ChatColor.GRAY + "+5% sea creature chance.",
                            ChatColor.BLUE + "On Fishing: " + ChatColor.GRAY + "+5% sea creature chance."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diplomat", 3, Material.WRITABLE_BOOK,
                    Arrays.asList(
                            ChatColor.GRAY + "Reduces notoriety gains by 60%.",
                            ChatColor.BLUE + "On Notoriety Gain: " + ChatColor.GRAY + "Reduce gain by 60%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Diffuser", 3, Material.CREEPER_HEAD,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance for creepers to drop a disc.",
                            ChatColor.BLUE + "On Creeper Death: " + ChatColor.GRAY + "50% chance for random music disc."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Trader", 3, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "5% chance for purchases to be free.",
                            ChatColor.BLUE + "On Purchase: " + ChatColor.GRAY + "5% chance to cost nothing."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Haggler", 5, Material.EMERALD,
                    Arrays.asList(
                            ChatColor.GRAY + "Grants 10% discount on villager trades.",
                            ChatColor.BLUE + "On Villager Trade: " + ChatColor.GRAY + "Prices reduced by 10%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Master Employer", 3, Material.BELL,
                    Arrays.asList(
                            ChatColor.GRAY + "50% chance to halve villager work timers.",
                            ChatColor.BLUE + "On Workcycle: " + ChatColor.GRAY + "50% chance next timer reduced 50%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Loyalty II", 3, Material.NAME_TAG,
                    Arrays.asList(
                            ChatColor.GRAY + "Loyal cooldown reduced to 1 second.",
                            ChatColor.BLUE + "On Use Loyal: " + ChatColor.GRAY + "Cooldown becomes 1 second."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Strong Digestion", 3, Material.GOLDEN_APPLE,
                    Arrays.asList(
                            ChatColor.GRAY + "Doubles potion duration.",
                            ChatColor.BLUE + "On Drink Potion: " + ChatColor.GRAY + "Duration increased by 100%."
                    )),
            new Perk(ChatColor.DARK_GRAY + "Icarus", 2, Material.ELYTRA,
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
                    )),
            new Perk(ChatColor.DARK_GRAY + "Tuxedo", 4, Material.BLACK_WOOL,
                    Arrays.asList(
                            ChatColor.GRAY + "Displays two extra auction items.",
                            ChatColor.BLUE + "On Auction: " + ChatColor.GRAY + "2 bonus items with rare chance"
                    ))
            );

    private static final java.util.Map<Category, java.util.List<Perk>> categoryMap = new java.util.EnumMap<>(Category.class);
    static {
        for(Category c : Category.values()) {
            categoryMap.put(c, new java.util.ArrayList<>());
        }
        int i = 0;
        // manually assign perks to categories
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // EnderMind
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Workbench
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Shulkl Box
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Motion Sensor
        categoryMap.get(Category.SMITHING).add(perks.get(i++)); // Unbreaking
        categoryMap.get(Category.SMITHING).add(perks.get(i++)); // Unbreaking II
        categoryMap.get(Category.SMITHING).add(perks.get(i++)); // Unbreaking III
        categoryMap.get(Category.SMITHING).add(perks.get(i++)); // Excavator
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Berserkers Rage
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Tactical Retreat
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Vampiric Strike
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Lord of Thunder
        categoryMap.get(Category.FISHING).add(perks.get(i++)); // Deep Hook
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Instant Transmission
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Deep Breath
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Trainer
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // QuickSwap
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Restock
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Rebreather
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Keepinventory
        categoryMap.get(Category.SMITHING).add(perks.get(i++)); // Master Smith
        categoryMap.get(Category.FARMING).add(perks.get(i++)); // Master Botanist
        categoryMap.get(Category.BREWING).add(perks.get(i++)); // Master Brewer
        categoryMap.get(Category.BREWING).add(perks.get(i++)); // Master Chef
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Master Thief
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Master Duelist
        categoryMap.get(Category.FISHING).add(perks.get(i++)); // Master Angler
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Master Diplomat
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Master Diffuser
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Master Trader
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Haggler
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Master Employer
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Loyalty II
        categoryMap.get(Category.BREWING).add(perks.get(i++)); // Strong Digestion
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // Icarus
        categoryMap.get(Category.UTILITY).add(perks.get(i++)); // AutoStrad
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Resurrection
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Resurrection Charge 2
        categoryMap.get(Category.COMBAT).add(perks.get(i++)); // Resurrection Charge 3
        categoryMap.get(Category.VILLAGER).add(perks.get(i++)); // Tuxedo
    }


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
        openMeritGUI(player, Category.COMBAT);
        return true;
    }

    /**
     * Create and open the 54-slot "Merit" GUI for the player.
     */
    private void openMeritGUI(Player player) {
        openMeritGUI(player, Category.COMBAT);
    }

    private void openMeritGUI(Player player, Category category) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Merits: " + category.getDisplay());

        // === prepare a black‐pane template ===
        ItemStack blackGlass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta blackMeta = blackGlass.getItemMeta();
        blackMeta.setDisplayName(ChatColor.BLACK + "");
        blackGlass.setItemMeta(blackMeta);

        // === fill top row (0–8) with black panes ===
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, blackGlass.clone());
        }

        // === fill left column (slots 9,18,27,36,45) with black panes ===
        for (int row = 0; row < 5; row++) {
            inv.setItem(9 + row * 9, blackGlass.clone());
        }

        // === place category tabs only in the top row ===
        int tab = 0;
        for (Category c : Category.values()) {
            if (tab >= 9) break;
            ItemStack icon = new ItemStack(c.getIcon());
            ItemMeta im = icon.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + c.getDisplay());
            icon.setItemMeta(im);
            inv.setItem(tab, icon);
            tab++;
        }

        // === diamond in slot 8 to show available points ===
        int pts = playerData.getMeritPoints(player.getUniqueId());
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta dmeta = diamond.getItemMeta();
        dmeta.setDisplayName(ChatColor.AQUA + "Available Merit Points: " + pts);
        diamond.setItemMeta(dmeta);
        inv.setItem(8, diamond);

        // === populate perks starting just below the top row ===
        int slotIndex = 9;  // first slot of second row
        for (Perk perk : categoryMap.get(category)) {
            // skip the left‐column pane
            if (slotIndex % 9 == 0) slotIndex++;
            if (slotIndex >= 54) break;

            ItemStack perkItem = new ItemStack(perk.getIcon());
            ItemMeta pm = perkItem.getItemMeta();
            String name = ChatColor.translateAlternateColorCodes('&', perk.getName());
            pm.setDisplayName(name);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Cost: " + perk.getCost());
            lore.addAll(perk.getDescription());
            pm.setLore(lore);

            // add glow if already purchased
            String stripped = ChatColor.stripColor(name);
            if (playerData.hasPerk(player.getUniqueId(), stripped)) {
                pm.addEnchant(Enchantment.DURABILITY, 1, true);
                pm.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            perkItem.setItemMeta(pm);
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
        if (event.getView().getTitle().startsWith(ChatColor.DARK_GREEN + "Merits")) {
            event.setCancelled(true);

            String title = ChatColor.stripColor(event.getView().getTitle());
            Category current = Category.COMBAT;
            for (Category c : Category.values()) {
                if (title.endsWith(c.getDisplay())) {
                    current = c;
                    break;
                }
            }

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

            String name = ChatColor.stripColor(meta.getDisplayName());
            for (Category c : Category.values()) {
                if (name.equals(c.getDisplay())) {
                    openMeritGUI(player, c);
                    return;
                }
            }

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
            openMeritGUI(player, current);
        }
    }
}
