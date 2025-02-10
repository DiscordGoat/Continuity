package goat.minecraft.minecraftnew.subsystems.enchanting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GamblingTable implements Listener {

    private final Plugin plugin;

    /**
     * Maps the names in SPINNER_CONTENTS to actual Materials
     * so we can show the real item icon during animation/final result.
     */
    private static final Map<String, Material> ITEM_MAP = new LinkedHashMap<>();
    static {
        ITEM_MAP.put("16 Emeralds",    Material.COAL_BLOCK);
        ITEM_MAP.put("32 Emeralds",    Material.IRON_BLOCK);
        ITEM_MAP.put("64 Emeralds",    Material.GOLD_BLOCK);
        ITEM_MAP.put("64 Emerald Blocks",    Material.DIAMOND_BLOCK);
    }

    // Spinner contents: must match the ITEM_MAP keys above for correct icons.
    private static final List<String> SPINNER_CONTENTS = new ArrayList<>(ITEM_MAP.keySet());

    // The 5 "centered" slots for spinners in a 54-slot inventory.
    private static final int[] SPINNER_SLOTS = {20, 21, 22, 23, 24};

    // "Spin!" button slot (near bottom-center of a 54-slot inventory).
    private static final int SPIN_BUTTON_SLOT = 49;

    // Maps each player to the relevant data
    private final Map<UUID, Inventory> playerGUIMap         = new HashMap<>();
    private final Map<UUID, boolean[]> spinnerActiveMap      = new HashMap<>();
    private final Map<UUID, Integer>   activeSpinnerCountMap = new HashMap<>();

    // Cost per spinner
    private static final int LAPIS_BLOCK_COST = 9;

    // Title of the GUI
    private static final String GUI_TITLE = ChatColor.DARK_BLUE + "Gambling Table";

    public GamblingTable(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Listen for the player left-clicking an enchanting table (while holding Lapis).
     * Adjust to RIGHT_CLICK_BLOCK if that’s your intended trigger.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (event.getClickedBlock().getType() == Material.ENCHANTING_TABLE) {
                Player player = event.getPlayer();
                // (Optional) Check the item in hand if you only want it to open when holding Lapis:
                // ItemStack itemInHand = player.getInventory().getItemInMainHand();
                // if (itemInHand.getType() == Material.LAPIS_LAZULI) { ... }

                openGamblingGUI(player);
                event.setCancelled(true); // prevent normal enchanting usage
            }
        }
    }

    /**
     * Create and open a 54-slot "Gambling Table" GUI for the player.
     */
    public void openGamblingGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Initialize spinner states
        boolean[] spinnerStates = new boolean[5]; // all false initially
        spinnerActiveMap.put(player.getUniqueId(), spinnerStates);
        activeSpinnerCountMap.put(player.getUniqueId(), 0);

        // Place placeholders for spinners in the center slots
        for (int i = 0; i < SPINNER_SLOTS.length; i++) {
            int slot = SPINNER_SLOTS[i];
            inv.setItem(slot, createGuiItem(
                    ChatColor.YELLOW + "Spinner " + (i + 1) + " [Inactive]",
                    Material.WHITE_STAINED_GLASS_PANE,
                    1,
                    Collections.singletonList(ChatColor.GRAY + "Click to activate! "
                            + ChatColor.BLUE + "9x Lapis Blocks")
            ));
        }

        // Place the "Spin!" button
        inv.setItem(SPIN_BUTTON_SLOT, createGuiItem(
                ChatColor.GREEN + "SPIN!",
                Material.NETHERITE_SWORD,
                1,
                Collections.singletonList(ChatColor.GRAY + "Click to spin all active spinners!")
        ));

        // Fill other slots with filler
        ItemStack filler = createGuiItem("", Material.BLACK_STAINED_GLASS_PANE, 1, null);
        for (int slot = 0; slot < 54; slot++) {
            if (!isSpinnerSlot(slot) && slot != SPIN_BUTTON_SLOT) {
                inv.setItem(slot, filler);
            }
        }

        playerGUIMap.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(GUI_TITLE)) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            Inventory inv = event.getInventory();
            int slot = event.getRawSlot();

            // Click outside top inventory? Ignore.
            if (slot < 0 || slot >= inv.getSize()) {
                return;
            }

            // Ensure this is the player's gambling GUI
            if (!playerGUIMap.containsKey(player.getUniqueId())) {
                return;
            }

            // If clicked on a spinner slot, try to activate
            Integer spinnerIndex = getSpinnerIndex(slot);
            if (spinnerIndex != null) {
                activateSpinner(player, spinnerIndex);
                return;
            }

            // If clicked on the Spin button, spin all active spinners
            if (slot == SPIN_BUTTON_SLOT) {
                spinAllActiveSpinners(player);
            }
        }
    }

    /**
     * Activates a spinner (costs 9 Lapis Blocks) if not already active.
     */
    private void activateSpinner(Player player, int spinnerIndex) {
        boolean[] spinnerStates = spinnerActiveMap.get(player.getUniqueId());
        if (spinnerStates == null) return;

        if (spinnerStates[spinnerIndex]) {
            player.sendMessage(ChatColor.RED + "This spinner is already active!");
            return;
        }

        if (!hasEnoughLapisBlocks(player, LAPIS_BLOCK_COST)) {
            player.sendMessage(ChatColor.RED + "You don't have " + LAPIS_BLOCK_COST + " Lapis Blocks!");
            return;
        }

        removeLapisBlocks(player, LAPIS_BLOCK_COST);

        spinnerStates[spinnerIndex] = true;
        int activeCount = activeSpinnerCountMap.getOrDefault(player.getUniqueId(), 0) + 1;
        activeSpinnerCountMap.put(player.getUniqueId(), activeCount);

        Inventory inv = playerGUIMap.get(player.getUniqueId());
        if (inv != null) {
            int slot = SPINNER_SLOTS[spinnerIndex];
            inv.setItem(slot, createGuiItem(
                    ChatColor.GOLD + "Spinner " + (spinnerIndex + 1) + " [Active]",
                    Material.YELLOW_STAINED_GLASS_PANE,
                    1,
                    Collections.singletonList(ChatColor.GRAY + "Ready to Spin!")
            ));
        }

        player.sendMessage(ChatColor.GREEN + "Spinner " + (spinnerIndex + 1) + " activated!");
    }

    /**
     * Spin all active spinners for the player, with an animation and final result.
     */
    private void spinAllActiveSpinners(Player player) {
        boolean[] spinnerStates = spinnerActiveMap.get(player.getUniqueId());
        if (spinnerStates == null) return;

        int activeCount = activeSpinnerCountMap.get(player.getUniqueId());
        if (activeCount == 0) {
            player.sendMessage(ChatColor.RED + "You haven't activated any spinners!");
            return;
        }

        for (int i = 0; i < spinnerStates.length; i++) {
            if (spinnerStates[i]) {
                animateSpinner(player, i);
            }
        }
    }

    /**
     * Animate a single spinner, cycling through SPINNER_CONTENTS, then finalize.
     */
    private void animateSpinner(Player player, int spinnerIndex) {
        Inventory inv = playerGUIMap.get(player.getUniqueId());
        if (inv == null) return;

        final int slot = SPINNER_SLOTS[spinnerIndex];

        new BukkitRunnable() {
            int cycle = 0;
            final int maxCycles = 20; // how many 'ticks' of cycling

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (cycle < maxCycles) {
                    String itemKey = SPINNER_CONTENTS.get(cycle % SPINNER_CONTENTS.size());
                    Material mat = ITEM_MAP.getOrDefault(itemKey, Material.BARRIER);

                    // Show the item while "spinning"
                    inv.setItem(slot, createGuiItem(
                            ChatColor.YELLOW + itemKey + " ...",
                            mat,
                            1,
                            null
                    ));

                    // Tick sound
                    player.playSound(player.getLocation(), Sound.UI_STONECUTTER_SELECT_RECIPE, 1f, 1.2f);
                    cycle++;
                } else {
                    cancel();
                    finalizeSpinnerResult(player, spinnerIndex);
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    /**
     * Pick a random item, display it with lore and name, then call the snippet method.
     */
    private void finalizeSpinnerResult(Player player, int spinnerIndex) {
        boolean[] spinnerStates = spinnerActiveMap.get(player.getUniqueId());
        if (spinnerStates == null) return;

        Inventory inv = playerGUIMap.get(player.getUniqueId());
        if (inv == null) return;

        Random rand = new Random();
        String finalItem = SPINNER_CONTENTS.get(rand.nextInt(SPINNER_CONTENTS.size()));
        Material mat = ITEM_MAP.getOrDefault(finalItem, Material.BARRIER);

        int slot = SPINNER_SLOTS[spinnerIndex];

        // Prepare lore explaining the functionality (placeholders for you to edit)
        List<String> finalLore = new ArrayList<>();
        finalLore.add(ChatColor.GRAY + "Reward: " + finalItem);
        finalLore.add(ChatColor.GRAY + "--------------------------------");
        finalLore.add(ChatColor.YELLOW + "Functionality: (Add your code!)");

        // Show the final result in the GUI
        inv.setItem(slot, createGuiItem(
                ChatColor.GREEN + finalItem + " [Result]",
                mat,
                1,
                finalLore
        ));

        // Call the snippet method you can fill
        runRewardSnippet(player, finalItem);

        spinnerStates[spinnerIndex] = false;
        int activeCount = activeSpinnerCountMap.getOrDefault(player.getUniqueId(), 1) - 1;
        activeSpinnerCountMap.put(player.getUniqueId(), Math.max(activeCount, 0));

        // Optional: "win" sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    /**
     * This method calls the relevant snippet for the final item.
     * All are empty placeholders you can fill in as you like.
     */
    private void runRewardSnippet(Player player, String finalItem) {
        switch (finalItem) {
            case "Emerald Reward":
                snippetEmeraldBlock(player);
                break;
            default:
                // If you add more items, handle them or do nothing
                break;
        }
    }

    // -----------------------------------------------------------------------
    //   SNIPPET METHODS: fill with your custom functionality
    // -----------------------------------------------------------------------

    private void snippetEmeraldBlock(Player player) {
        // TODO: Add your code for emerald_block reward
        player.getInventory().addItem(new ItemStack(Material.EMERALD, 16));
    }

    // ===================================================
    // Utility Methods
    // ===================================================

    /**
     * Creates a named ItemStack with lore (if provided).
     */
    private ItemStack createGuiItem(String displayName, Material material, int amount, List<String> lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(displayName);
            }
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private boolean hasEnoughLapisBlocks(Player player, int required) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == Material.LAPIS_BLOCK) {
                count += stack.getAmount();
                if (count >= required) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeLapisBlocks(Player player, int required) {
        int toRemove = required;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == Material.LAPIS_BLOCK) {
                if (stack.getAmount() > toRemove) {
                    stack.setAmount(stack.getAmount() - toRemove);
                    break;
                } else {
                    toRemove -= stack.getAmount();
                    contents[i] = null;
                    if (toRemove <= 0) {
                        break;
                    }
                }
            }
        }
        player.getInventory().setContents(contents);
    }

    /**
     * Checks if a slot is a spinner slot.
     */
    private boolean isSpinnerSlot(int slot) {
        for (int s : SPINNER_SLOTS) {
            if (s == slot) return true;
        }
        return false;
    }

    /**
     * Returns the spinner index (0..4) for a slot, or null if not a spinner slot.
     */
    private Integer getSpinnerIndex(int slot) {
        for (int i = 0; i < SPINNER_SLOTS.length; i++) {
            if (SPINNER_SLOTS[i] == slot) {
                return i;
            }
        }
        return null;
    }
}
