package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager.PetPerk;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.subsystems.farming.CropCountManager;
import goat.minecraft.minecraftnew.subsystems.farming.HarvestProgressTracker;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoComposter implements Listener {

    private final MinecraftNew plugin;

    /**
     * Crops eligible for auto-composting.
     */
    private final Set<Material> AUTO_COMPOSTER_ELIGIBLE = EnumSet.of(
            Material.POTATO,
            Material.CARROT,
            Material.WHEAT,
            Material.WHEAT_SEEDS,
            Material.BEETROOT_SEEDS,
            Material.POISONOUS_POTATO,
            Material.MELON_SLICE,
            Material.MELON,
            Material.PUMPKIN,
            Material.BEETROOT,
            Material.PUMPKIN_SEEDS
    );

    private final Map<UUID, Location> playerLastLocations = new HashMap<>();
    private final Map<UUID, Integer> composterTally = new HashMap<>();
    private final Map<UUID, Long> lastHarvestFestivalTime = new HashMap<>();
    private final Map<UUID, Long> lastComposterTime = new HashMap<>();
    private final Map<UUID, Integer> harvestFestivalTally = new HashMap<>();
    private final Map<UUID, Material> lastDominant = new HashMap<>();
    private final Map<UUID, Map<Material, Integer>> cachedCounts = new HashMap<>();
    private final Map<UUID, Long> lastReconcile = new HashMap<>();
    private final Map<UUID, Long> lastMoveActivation = new HashMap<>();

    public AutoComposter(MinecraftNew plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        // Migrate to movement-based activation like Collector; scheduled task no longer used.
    }


    /**
     * Determines which auto-compost perk the player's active pet has, if any.
     */
    private PetPerk getAutoPerk(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null) {
            return null;
        }
        if (activePet.hasPerk(PetPerk.COMPOSTER) || activePet.hasUniqueTraitPerk(PetPerk.COMPOSTER)) {
            return PetPerk.COMPOSTER;
        }
        if (activePet.hasPerk(PetPerk.HARVEST_FESTIVAL) || activePet.hasUniqueTraitPerk(PetPerk.HARVEST_FESTIVAL)) {
            return PetPerk.HARVEST_FESTIVAL;
        }
        return null;
    }

    /**
     * Gets the pet's level from the player's active pet.
     */
    private int getPetLevel(Player player) {
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        return activePet.getLevel();
    }

    /**
     * Performs the crop conversion for the player, using the
     * dynamic "required materials" formula:
     *
     *  requiredMaterialsOrganic = max(256 - (level - 1)*(256 - 64)/99, 64)
     */
    private void performConversion(Player player, PetPerk perk) {
        long nowTs = System.currentTimeMillis();
        Long lastTs = lastComposterTime.get(player.getUniqueId());
        if (lastTs != null && (nowTs - lastTs) < 500) {
            return; // 0.5s cooldown
        }
        // Set cooldown immediately to throttle scans even if nothing converts
        lastComposterTime.put(player.getUniqueId(), nowTs);
        int level = getPetLevel(player);

        // 2) Calculate how many crops are required for 1 organic soil
        int requiredMaterialsOrganic = Math.max(
                256 - (level - 1) * (256 - 64) / 99,
                64
        );
        if (SkillTreeManager.getInstance() != null) {
            int lvl = SkillTreeManager.getInstance()
                    .getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.COMPOSTER);
            if (lvl > 0) {
                requiredMaterialsOrganic = requiredMaterialsOrganic / 2;
            }
        }

        // 3) Count how many of each eligible crop the player holds
        Map<Material, Integer> playerCropCounts = getCachedCounts(player);

        // 4) For each eligible crop, see how many conversions we can do
        Map<Material, Integer> toRemove = new HashMap<>();
        int totalConversions = 0;
        for (Material cropMat : AUTO_COMPOSTER_ELIGIBLE) {
            int playerCropCount = playerCropCounts.getOrDefault(cropMat, 0);
            // Nerf pumpkins and melon slices: reduce weight from 8 to 2 (~4x nerf)
            int weight = (cropMat == Material.PUMPKIN || cropMat == Material.MELON || cropMat == Material.MELON_SLICE) ? 2 : 1;

            int effective = playerCropCount * weight;
            if (effective >= requiredMaterialsOrganic) {
                int conversions = effective / requiredMaterialsOrganic;
                int effectiveUsed = conversions * requiredMaterialsOrganic;
                int itemsToRemove = (int) Math.ceil(effectiveUsed / (double) weight);
                toRemove.put(cropMat, toRemove.getOrDefault(cropMat, 0) + itemsToRemove);
                totalConversions += conversions;
            }
        }

        if (totalConversions > 0) {
            // Apply removals in a single inventory update and update cache
            batchSubtract(player, toRemove);
            if (perk == PetPerk.COMPOSTER) {
                addOrganicSoil(player, totalConversions);
            } else {
                addFertilizer(player, totalConversions);
            }
        }
    }

    /**
     * Retrieves the count of each eligible crop in the player's inventory.
     */
    private Map<Material, Integer> getPlayerCropCountsScan(Player player) {
        Map<Material, Integer> cropCounts = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && AUTO_COMPOSTER_ELIGIBLE.contains(item.getType())) {
                if (item.hasItemMeta() && (item.getItemMeta().hasDisplayName() || !item.getEnchantments().isEmpty())) continue;
                cropCounts.put(item.getType(), cropCounts.getOrDefault(item.getType(), 0) + item.getAmount());
            }
        }
        return cropCounts;
    }

    private Map<Material, Integer> getCachedCounts(Player player) {
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Map<Material, Integer> cached = cachedCounts.get(id);
        Long last = lastReconcile.get(id);
        if (cached == null || last == null || now - last > 30000) { // periodic reconcile every 30s
            Map<Material, Integer> scan = getPlayerCropCountsScan(player);
            cachedCounts.put(id, scan);
            lastReconcile.put(id, now);
            return new HashMap<>(scan);
        }
        return new HashMap<>(cached);
    }

    /**
     * Subtracts the specified amount of a given crop from the player's inventory.
     */
    private void batchSubtract(Player player, Map<Material, Integer> removeMap) {
        if (removeMap.isEmpty()) return;
        UUID id = player.getUniqueId();
        Inventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        Map<Material, Integer> removed = new HashMap<>();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it == null) continue;
            Material m = it.getType();
            if (!AUTO_COMPOSTER_ELIGIBLE.contains(m)) continue;
            if (it.hasItemMeta() && (it.getItemMeta().hasDisplayName() || !it.getEnchantments().isEmpty())) continue;
            int need = removeMap.getOrDefault(m, 0) - removed.getOrDefault(m, 0);
            if (need <= 0) continue;
            int take = Math.min(need, it.getAmount());
            int remain = it.getAmount() - take;
            if (remain > 0) {
                it.setAmount(remain);
            } else {
                contents[i] = null;
            }
            removed.put(m, removed.getOrDefault(m, 0) + take);
        }
        inv.setContents(contents);
        // update cache by subtracting removed amounts
        Map<Material, Integer> cache = cachedCounts.computeIfAbsent(id, k -> new HashMap<>());
        for (Map.Entry<Material, Integer> e : removed.entrySet()) {
            cache.put(e.getKey(), Math.max(0, cache.getOrDefault(e.getKey(), 0) - e.getValue()));
        }
        lastReconcile.put(id, System.currentTimeMillis());
    }

    private void performHarvestFestival(Player player) {
        long nowTs = System.currentTimeMillis();
        Long lastTs = lastHarvestFestivalTime.get(player.getUniqueId());
        if (lastTs != null && (nowTs - lastTs) < 500) {
            return; // 0.5s cooldown
        }
        // Remove all eligible crops instantly and count per material
        Inventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        Map<Material, Integer> removedByMat = new HashMap<>();
        int removedTotal = 0;
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null) continue;
            if (!AUTO_COMPOSTER_ELIGIBLE.contains(item.getType())) continue;
            if (item.hasItemMeta() && (item.getItemMeta().hasDisplayName() || !item.getEnchantments().isEmpty())) continue;
            removedByMat.put(item.getType(), removedByMat.getOrDefault(item.getType(), 0) + item.getAmount());
            removedTotal += item.getAmount();
            contents[i] = null;
        }
        if (removedTotal > 0) {
            inv.setContents(contents);
            // zero out cache for removed mats
            Map<Material, Integer> cache = cachedCounts.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
            for (Map.Entry<Material, Integer> e : removedByMat.entrySet()) {
                cache.put(e.getKey(), Math.max(0, cache.getOrDefault(e.getKey(), 0) - e.getValue()));
            }
            lastReconcile.put(player.getUniqueId(), System.currentTimeMillis());

            // Determine dominant crop for this activation (normalized to CropCountManager keys)
            Map<Material, Integer> normalized = new HashMap<>();
            for (Map.Entry<Material, Integer> e : removedByMat.entrySet()) {
                Material key = normalizeToCrop(e.getKey());
                if (key == null) continue; // skip unsupported materials
                normalized.put(key, normalized.getOrDefault(key, 0) + e.getValue());
            }
            Material dominant = null;
            int max = 0;
            for (Map.Entry<Material, Integer> e : normalized.entrySet()) {
                if (e.getValue() > max) { max = e.getValue(); dominant = e.getKey(); }
            }
            if (dominant == null) dominant = lastDominant.getOrDefault(player.getUniqueId(), Material.WHEAT);
            lastDominant.put(player.getUniqueId(), dominant);

            // Accumulate tally and award +100 per 2000 removed
            int tally = harvestFestivalTally.getOrDefault(player.getUniqueId(), 0) + removedTotal;
            while (tally >= 2000) {
                tally -= 2000;
                CropCountManager.getInstance(plugin).bulkIncrement(player, dominant, 100);
                int total = CropCountManager.getInstance(plugin).getCount(player, dominant);
                int req = CropCountManager.getInstance(plugin).getRequirement(player);
                int current = total % Math.max(1, req);
                if (current == 0 && total > 0) current = req; // avoid showing 0 right after threshold
                HarvestProgressTracker.set(player.getUniqueId(), dominant, current, req);
                // Subtle, short title to notify +100 progress
                String main = ChatColor.GREEN + "+100 Harvest";
                String sub = ChatColor.YELLOW + formatMaterialName(dominant);
                player.sendTitle(main, sub, 5, 20, 5);
                player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
            }
            harvestFestivalTally.put(player.getUniqueId(), tally);
        }
        // set cooldown regardless of whether anything was removed to avoid rapid scanning
        lastHarvestFestivalTime.put(player.getUniqueId(), nowTs);
    }

    /**
     * Map inventory materials to the canonical crop materials used by CropCountManager.
     */
    private Material normalizeToCrop(Material m) {
        switch (m) {
            case WHEAT:
            case WHEAT_SEEDS:
                return Material.WHEAT; // count keys support WHEAT and WHEAT_SEEDS; prefer WHEAT here
            case CARROT:
            case CARROTS:
                return Material.CARROTS;
            case POTATO:
            case POTATOES:
                return Material.POTATOES;
            case BEETROOT:
            case BEETROOTS:
            case BEETROOT_SEEDS:
                return Material.BEETROOTS;
            case MELON:
            case MELON_SLICE:
                return Material.MELON;
            case PUMPKIN:
            case PUMPKIN_SEEDS:
                return Material.PUMPKIN;
            default:
                return null;
        }
    }

    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

    /**
     * Adds the given number of Organic Soil items to the player's inventory.
     * If full, drops them at the player's feet.
     */
    private void addOrganicSoil(Player player, int quantity) {
        giveStacked(player, ItemRegistry.getOrganicSoil(), quantity, ChatColor.RED + "Your inventory is full! Organic Soil has been dropped on the ground.");
    }

    private void addFertilizer(Player player, int quantity) {
        giveStacked(player, ItemRegistry.getFertilizer(), quantity, ChatColor.RED + "Your inventory is full! Fertilizer has been dropped on the ground.");
    }

    private void giveStacked(Player player, ItemStack base, int total, String fullMessage) {
        while (total > 0) {
            ItemStack stack = base.clone();
            int give = Math.min(total, stack.getMaxStackSize());
            stack.setAmount(give);
            Map<Integer, ItemStack> overflow = player.getInventory().addItem(stack);
            if (!overflow.isEmpty()) {
                for (ItemStack left : overflow.values()) player.getWorld().dropItemNaturally(player.getLocation(), left);
                player.sendMessage(fullMessage);
            }
            total -= give;
        }
    }

    // Cache maintenance: increment counts on pickup
    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        Item item = event.getItem();
        ItemStack stack = item.getItemStack();
        if (!AUTO_COMPOSTER_ELIGIBLE.contains(stack.getType())) return;
        if (stack.hasItemMeta() && (stack.getItemMeta().hasDisplayName() || !stack.getEnchantments().isEmpty())) return;
        UUID id = ((Player) entity).getUniqueId();
        Map<Material, Integer> cache = cachedCounts.computeIfAbsent(id, k -> new HashMap<>());
        cache.put(stack.getType(), cache.getOrDefault(stack.getType(), 0) + stack.getAmount());
        lastReconcile.put(id, System.currentTimeMillis());
    }

    // Movement-based activation like Collector: run once per second while moving
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastMoveActivation.get(player.getUniqueId());
        if (last != null && (now - last) < 500) {
            return; // throttle to once per second
        }
        lastMoveActivation.put(player.getUniqueId(), now);

        PetPerk perk = getAutoPerk(player);
        if (perk == PetPerk.COMPOSTER) {
            performConversion(player, perk);
        } else if (perk == PetPerk.HARVEST_FESTIVAL) {
            performHarvestFestival(player);
        }
    }
}
