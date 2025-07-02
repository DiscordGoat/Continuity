package goat.minecraft.minecraftnew.subsystems.armorsets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.BlessingUtils;
import goat.minecraft.minecraftnew.subsystems.farming.VerdantRelicsSubsystem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Applies the Pastureshade full set bonus while the player is wearing the full set.
 * Adds a bonus crop when harvesting fully grown crops and doubles relic yield.
 */
public class PastureshadeSetBonus implements Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, Boolean> applied = new HashMap<>();
    
    // Crops that can be harvested
    private static final Set<Material> CROPS = Set.of(
        Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
        Material.COCOA, Material.NETHER_WART
    );

    public PastureshadeSetBonus(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Reapply bonus for players already online (e.g. during reload)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkPlayer(player);
            }
        }, 1L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(player), 1L);
        }
    }

    @EventHandler
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> checkPlayer(event.getPlayer()), 1L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        
        if (!applied.getOrDefault(player.getUniqueId(), false)) {
            return;
        }

        // Check if it's a fully grown crop
        if (CROPS.contains(block.getType()) && block.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                // Activate Flow on crop breaks
                FlowManager flowManager = FlowManager.getInstance(plugin);
                flowManager.addFlowStacks(player, 1);
                
                // Add bonus crop drop
                block.getDrops(player.getInventory().getItemInMainHand()).forEach(drop -> {
                    if (drop.getType() != Material.WHEAT_SEEDS && 
                        drop.getType() != Material.CARROT &&
                        drop.getType() != Material.POTATO &&
                        drop.getType() != Material.BEETROOT_SEEDS) {
                        // Drop one additional crop item (not seeds)
                        ItemStack bonusCrop = drop.clone();
                        bonusCrop.setAmount(1);
                        block.getWorld().dropItemNaturally(block.getLocation(), bonusCrop);
                    }
                });
            }
        }
    }

    /**
     * Returns the relic yield multiplier for the given player.
     * Should be called by the VerdantRelicsSubsystem when calculating relic drops.
     */
    public static int getRelicYieldBonus(Player player) {
        PastureshadeSetBonus instance = getInstance();
        if (instance != null && instance.applied.getOrDefault(player.getUniqueId(), false)) {
            return 1; // +1 additional relic
        }
        return 0;
    }

    private static PastureshadeSetBonus instance;
    
    private static PastureshadeSetBonus getInstance() {
        return instance;
    }

    private void checkPlayer(Player player) {
        if (BlessingUtils.hasFullSetBonus(player, "Pastureshade")) {
            applyBonus(player);
        } else {
            removeBonus(player);
        }
    }

    private void applyBonus(Player player) {
        UUID id = player.getUniqueId();
        if (applied.getOrDefault(id, false)) {
            return;
        }
        applied.put(id, true);
        instance = this; // Set static instance for external access
    }

    private void removeBonus(Player player) {
        UUID id = player.getUniqueId();
        if (!applied.getOrDefault(id, false)) {
            return;
        }
        applied.put(id, false);
    }

    /**
     * Removes all active bonuses. Called on plugin disable to clean up.
     */
    public void removeAllBonuses() {
        applied.clear();
        instance = null;
    }
}