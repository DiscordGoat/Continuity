package goat.minecraft.minecraftnew.other.professions.bartender;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.culinary.CulinarySubsystem;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerTradeManager.TradeItem;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import goat.minecraft.minecraftnew.other.additionalfunctionality.PlayerTabListUpdater;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BartenderVillagerManager implements Listener {
    private final JavaPlugin plugin;
    private final Map<Player, Villager> interactionMap = new HashMap<>();
    private final List<TradeItem> bartenderTrades = new ArrayList<>();

    public BartenderVillagerManager(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // 1) Add each oceanic recipe paper exactly once
        for (CulinarySubsystem.CulinaryRecipe r : CulinarySubsystem.oceanicRecipes) {
            ItemStack paper = CulinarySubsystem
                    .getInstance(plugin)
                    .getRecipeItemByName(r.getName());
            if (paper != null) {
                // recipe paper: quantity 1, cost 16 emeralds, level 1
                bartenderTrades.add(new TradeItem(paper, 16, 1, 1));
            }
        }

        // 2) Then add the three signature ingredients exactly once each
        //    (quantity, cost, and level as needed—here: qty 1, cost 16, lvl 1)
        ItemStack banana   = ItemRegistry.getBanana();
        ItemStack chocolate= ItemRegistry.getChocolate();
        ItemStack calamari = ItemRegistry.getCalamari();
        ItemStack pineapple = ItemRegistry.getPineapple();
        ItemStack coconut = ItemRegistry.getCoconut();
        ItemStack lime = ItemRegistry.getLime();
        ItemStack hireme = ItemRegistry.getHireBartender();

        bartenderTrades.add(new TradeItem(banana,    64, 4, 1));
        bartenderTrades.add(new TradeItem(chocolate, 64, 4, 1));
        bartenderTrades.add(new TradeItem(calamari,  4, 4, 1));
        bartenderTrades.add(new TradeItem(pineapple,  64, 4, 1));
        bartenderTrades.add(new TradeItem(coconut,  64, 4, 1));
        bartenderTrades.add(new TradeItem(lime,  64, 4, 1));
        bartenderTrades.add(new TradeItem(hireme,  512, 1, 1));
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Villager v)) return;
        if (!ChatColor.stripColor(Objects.toString(v.getCustomName(), ""))
                .equals("Bartender")) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        interactionMap.put(p, v);
        openBartenderMenu(p);
    }

    private void openBartenderMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Bartender Trading");
        Villager villager = interactionMap.get(player);

        // 1) Compute villager level from days played exactly as your other GUI:
        int days = new PlayerTabListUpdater(MinecraftNew.getInstance(),
                new XPManager(plugin))
                .getDaysPlayed(player);
        int lvl = days >=200?5:
                days >=150?4:
                        days >=100?3:
                                days >= 50?2:1;
        villager.setVillagerLevel(lvl);

        // 2) Divider down the middle:
        ItemStack divider = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta dm = divider.getItemMeta();
        dm.setDisplayName(ChatColor.DARK_GRAY + " ");
        divider.setItemMeta(dm);
        for (int i = 0; i < 6; i++) {
            gui.setItem(i * 9 + 4, divider);
        }

        // 3) Populate **only** purchases on columns 0–3:
        int idx = 0;
        for (TradeItem ti : bartenderTrades) {
            ItemStack disp;
            if (lvl >= ti.getRequiredLevel()) {
                disp = ti.getItem().clone();
                ItemMeta m = disp.getItemMeta();
                int base = ti.getEmeraldValue();
                int price = VillagerTradeManager
                        .getInstance((JavaPlugin)MinecraftNew.getInstance())
                        .calculateDiscountedPrice(player, base);

                m.setLore(List.of(
                        ChatColor.RED   + "Original Price: "   + base   + " emerald(s)",
                        ChatColor.GREEN + "Discounted Price: " + price  + " emerald(s)",
                        ChatColor.YELLOW+ "Click to purchase " + ti.getQuantity() + " item(s)"
                ));
                disp.setItemMeta(m);
            } else {
                disp = createLockedTradeItem();
            }
            int row = idx / 4, col = idx % 4;
            gui.setItem(row * 9 + col, disp);
            idx++;
        }

        // 4) Pet button in slot 49
        addVillagerPetButton(player, gui);

        player.openInventory(gui);
    }

    private ItemStack createLockedTradeItem() {
        ItemStack b = new ItemStack(Material.BARRIER);
        ItemMeta m = b.getItemMeta();
        m.setDisplayName(ChatColor.RED + "Locked");
        m.setLore(List.of(
                ChatColor.GRAY + "This trade is locked.",
                ChatColor.GRAY + "Villager needs to level up."));
        b.setItemMeta(m);
        return b;
    }

    private void addVillagerPetButton(Player player, Inventory gui) {
        PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
        PetManager.Pet pet = pm.getPet(player, "Villager");
        ItemStack button;
        if (pet != null) {
            button = new ItemStack(Material.VILLAGER_SPAWN_EGG);
            var m = button.getItemMeta();
            m.setDisplayName(ChatColor.GOLD + "Summon Villager Pet");
            m.setLore(List.of(ChatColor.YELLOW + "Click to summon your Villager pet."));
            button.setItemMeta(m);
        } else {
            button = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            var m = button.getItemMeta();
            m.setDisplayName(ChatColor.RED + "No Villager Pet Found");
            button.setItemMeta(m);
        }
        gui.setItem(49, button);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(ChatColor.GOLD + "Bartender Trading")) return;
        e.setCancelled(true);
        Player p = (Player)e.getWhoClicked();
        Villager v = interactionMap.get(p);
        int slot = e.getSlot();

        // Pet‐button
        if (slot == 49) {
            if (e.getCurrentItem().getType() == Material.VILLAGER_SPAWN_EGG) {
                PetManager pm = PetManager.getInstance(MinecraftNew.getInstance());
                PetManager.Pet old = pm.getActivePet(p);
                p.setMetadata("previousPet",
                        new FixedMetadataValue(plugin, old==null?null:old.getName()));
                pm.summonPet(p, "Villager");
                p.sendMessage(ChatColor.GREEN + "Villager Pet summoned while trading!");
                openBartenderMenu(p);
            }
            return;
        }

        // Purchases (cols 0–3)
        int col = slot % 9;
        if (col <= 3) {
            int purchaseIndex = (slot / 9) * 4 + col;
            if (purchaseIndex < bartenderTrades.size()) {
                TradeItem ti = bartenderTrades.get(purchaseIndex);
                // delegate to your VillagerTradeManager helper:
                VillagerTradeManager.getInstance((JavaPlugin)MinecraftNew.getInstance())
                        .processPurchase(p, v, ti);
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent ev) {
        if (!ev.getView().getTitle().equals(ChatColor.GOLD + "Bartender Trading")) return;
        Player p = (Player)ev.getPlayer();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.getOpenInventory().getTitle().equals(ChatColor.GOLD + "Bartender Trading")) {
                if (p.hasMetadata("previousPet")) {
                    String prev = p.getMetadata("previousPet").get(0).asString();
                    p.removeMetadata("previousPet", plugin);
                    var pm = PetManager.getInstance(MinecraftNew.getInstance());
                    if (!prev.isEmpty()) pm.summonPet(p, prev);
                    else           pm.despawnPet(p);
                    p.playSound(p.getLocation(),
                            Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
                }
            }
        }, 20L);
    }
}
