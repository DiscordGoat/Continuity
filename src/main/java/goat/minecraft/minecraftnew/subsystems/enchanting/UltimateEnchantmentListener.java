package goat.minecraft.minecraftnew.subsystems.enchanting;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestSpiritManager;
import goat.minecraft.minecraftnew.subsystems.forestry.Forestry;
import goat.minecraft.minecraftnew.subsystems.forestry.ForestryPetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.*;

import static goat.minecraft.minecraftnew.subsystems.enchanting.UltimateEnchantmentListener.OreUtils.isOreBlock;


public class UltimateEnchantmentListener implements Listener {

    private final JavaPlugin plugin;

    // Maps: playerUUID -> Map<ultimateEnchantName, nextUsableTimestampInMillis>
    private final Map<UUID, Map<String, Long>> ultimateCooldowns = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> activeEnchantments = new HashMap<>();
    private final Map<UUID, Boolean> discSeekerActive = new HashMap<>();
    // Removed hammerActive and treecapitatorActive

    // Removed activateTreecapitator(...) and activateHammer(...)
    private static Map<UUID, LoyalSwordData> loyalSwordDataMap = new HashMap<>();
    // Track available shred swords per player
    private static final int MAX_SHRED_SWORDS = 30;
    private final Map<UUID, Integer> shredCharges = new HashMap<>();
    // Removed Leviathan ultimate enchantment

    private static class LoyalSwordData {
        double damageMultiplier = 1.0; // 1.0 = 100%
        long lastUsage = System.currentTimeMillis();
    }
    public UltimateEnchantmentListener(JavaPlugin plugin) {
        this.plugin = plugin;
        loadCooldowns();
    }

    /**
     * Checks whether a block is considered an ore.
     * This example just checks if the Material name ends with "_ORE".
     * Customize as needed for deepslate ores, ancient debris, etc.
     */

    /**
     * Example check for whether the player's item is "Hammer" enchanted.
     * If you store your custom enchant differently, adjust the logic accordingly.
     */
    private boolean hasHammerEnchant(ItemStack item) {
        // Simplest check: item’s display name or lore contains “Hammer”
        // Or you could check for a custom enchant in your system.
        if (item == null) return false;
        // Example check:
        return item.getItemMeta() != null && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().stream()
                .anyMatch(l -> l.toLowerCase().contains("hammer"));
    }

    /**
     * Example check for whether the player's item is “Treecapitator” enchanted.
     */
    private boolean hasTreecapEnchant(ItemStack item) {
        if (item == null) return false;
        return item.getItemMeta() != null && item.getItemMeta().hasLore()
                && item.getItemMeta().getLore().stream()
                .anyMatch(l -> l.toLowerCase().contains("treecapitator"));
    }

    /**
     * Break a single block and drop its loot.
     * If it's not an ore, we’ll also apply tool durability usage (so Hammer costs durability).
     */
    private void breakBlock(Player player, Block block, boolean consumeDurabilityIfNotOre) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        // Drop the block's natural drops
        for (ItemStack drop : block.getDrops(tool)) {
            if (drop != null && drop.getType() != Material.AIR) { // Check for null or air
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }
        // Set the block to air
        if(isLeafBlock(block.getType())){
            block.setType(Material.AIR);
            return;
        }
        block.setType(Material.AIR);

        // If it's not an ore, consume 1 durability (Hammer usage).
        // (Check if the tool can lose durability)

        if (consumeDurabilityIfNotOre && tool != null && tool.getType().getMaxDurability() > 0) {
            short currentDamage = tool.getDurability();
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY); // Get Unbreaking level
            double chanceToNotUseDurability = unbreakingLevel * 0.15; // 15% chance per level
            if (Math.random() > chanceToNotUseDurability) { // Spend durability only if random value exceeds the chance
                tool.setDurability((short) (currentDamage + 1));

                // Optional: If the tool breaks, remove it
                if (tool.getDurability() >= tool.getType().getMaxDurability()) {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                }
            }
        }
    }
    private void breakBlocksGradually(Player player, List<Block> blocks, boolean consumeDurabilityIfNotOre) {
        final ItemStack tool = player.getInventory().getItemInMainHand();

        // 1) Calculate total durability usage instantly if we want to consume it.
        if (consumeDurabilityIfNotOre
                && tool != null
                && tool.getType().getMaxDurability() > 0) {

            // Gather unbreaking level (e.g., Unbreaking VI => 6 => 90% skip chance).
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.DURABILITY);
            double skipChance = 0.15 * unbreakingLevel;  // 15% per Unbreaking level

            // We will accumulate how many times durability is actually used
            int totalDurabilityUsed = 0;

            for (Block block : blocks) {
                // If the block is NOT ore, we run the random skip check
                if (!isOreBlock(block) && !isLeafBlock(block.getType())) {
                    // If random > skipChance => consume durability
                    // (i.e. the "worst-case" scenario for the player)
                    if (Math.random() > skipChance) {
                        totalDurabilityUsed++;
                    }
                }
            }

            // Now apply all that durability at once
            short currentDamage = tool.getDurability();
            short newDamage = (short) (currentDamage + totalDurabilityUsed);
            tool.setDurability(newDamage);

            // If the tool breaks, play break sound (and optionally remove the item)
            if (tool.getDurability() >= tool.getType().getMaxDurability()) {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                // Optional: remove item from player’s hand
                // player.getInventory().setItemInMainHand(null);
            }
        }

        // 2) Break the blocks gradually, but SKIP any additional durability usage,
        //    because we've already applied it instantly above.

        // Decide how many blocks we break per tick (aim for ~20 seconds total).
        // For 200 blocks, we have 400 ticks in 20 seconds => ~0.5 blocks/tick -> round up to 1.
        // Tweak logic/variables as desired.
        int totalTicks = 10 * 20; // 10 seconds * 20 = 200 ticks (adjust to your liking)
        double rawBpt = (double) blocks.size() / (double) totalTicks;
        int blocksPerTick = (int) Math.ceil(rawBpt);

        // Cap between 1 and 10
        if (blocksPerTick < 1) {
            blocksPerTick = 1;
        }
        if (blocksPerTick > 10) {
            blocksPerTick = 10;
        }

        int finalBlocksPerTick = blocksPerTick;

        // Now schedule the actual block-breaking over time
        new BukkitRunnable() {
            int index = 0; // current block index

            @Override
            public void run() {
                for (int i = 0; i < finalBlocksPerTick && index < blocks.size(); i++, index++) {
                    Block block = blocks.get(index);

                    // Drop the block's natural drops
                    // (No more durability check here, we've already handled it.)
                    for (ItemStack drop : block.getDrops(tool)) {
                        if (drop != null && drop.getType() != Material.AIR) {
                            block.getWorld().dropItemNaturally(block.getLocation(), drop);
                        }
                    }

                    // Set block to AIR
                    block.setType(Material.AIR);
                }

                // Stop the task if we've processed all blocks
                if (index >= blocks.size()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Run every tick
    }



    /**
     * Break surrounding 3×3 blocks ignoring ores, applying durability for each broken block.
     * @param centerBlock  The block the player actually broke.
     * @param vertical     If true, break in x-y-z with y as vertical axis. Otherwise horizontally.
     */
    private void breakSurroundingBlocks(Player player, Block centerBlock, boolean vertical) {
        int range = 1; // 1 in each direction -> 3×3 area
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    // Skip the center block itself
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block relativeBlock;
                    if (vertical) {
                        relativeBlock = centerBlock.getRelative(x, y, z);
                    } else {
                        relativeBlock = centerBlock.getRelative(x, z, y);
                    }

                    if (!relativeBlock.getType().isAir() && relativeBlock.getType().isSolid()) {
                        // Check if the block is an ore
                        boolean isOre = isOreBlock(relativeBlock);

                        // Debug messages

                        // Skip ores
                        if (isOre) {
                            continue;
                        }

                        // Break the block if it's not an ore
                        breakBlock(player, relativeBlock, true);
                        XPManager xpManager = new XPManager(plugin);
                        xpManager.addXP(player, "Mining", 1);
                    }
                }
            }
        }
    }


    /**
     * Determine if a material is a log or wood block (for Treecapitator).
     */
    private boolean isWoodBlock(Material material) {
        return material == Material.OAK_LOG || material == Material.BIRCH_LOG ||
                material == Material.SPRUCE_LOG || material == Material.JUNGLE_LOG ||
                material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG ||
                material == Material.CRIMSON_STEM || material == Material.WARPED_STEM;
    }

    private boolean isLeafBlock(Material material) {
        return material == Material.OAK_LEAVES || material == Material.BIRCH_LEAVES ||
                material == Material.SPRUCE_LEAVES || material == Material.JUNGLE_LEAVES ||
                material == Material.ACACIA_LEAVES || material == Material.DARK_OAK_LEAVES ||
                material == Material.NETHER_WART_BLOCK || material == Material.WARPED_WART_BLOCK;
    }

    /**
     * Treecapitator logic: BFS to break connected logs and nearby leaves.
     */
    private void breakConnectedWoodAndLeaves(Player player, Block startBlock) {
        boolean spiritSpawned = false;

        // BFS to find connected logs
        Queue<Block> queue = new ArrayDeque<>();
        Set<Block> visitedLogs = new HashSet<>();
        int maxBlocks = 150; // limit so players don’t accidentally chop entire forest

        // For leaves around logs
        Set<Block> leavesToBreak = new HashSet<>();
        int leavesRange = 5;

        // Prepare BFS
        queue.add(startBlock);
        visitedLogs.add(startBlock);

        while (!queue.isEmpty() && visitedLogs.size() <= maxBlocks) {
            Block currentBlock = queue.poll();

            // Make sure this block is actually wood before we branch out
            if (!isWoodBlock(currentBlock.getType())) {
                continue;
            }

            // ---- Original BFS logic: If it’s wood, increment forestry count, 1% spirit spawn, etc. ----
            Forestry forestry = Forestry.getInstance(MinecraftNew.getInstance());
            XPManager xpManager = new XPManager(plugin);
            ForestryPetManager forestryPetManager = MinecraftNew.getInstance().getForestryManager();
            forestryPetManager.incrementForestryCount(player);
            forestry.processPerfectAppleChance(player, currentBlock, xpManager.getPlayerLevel(player, "Forestry"));
            forestry.processDoubleDropChance(player, currentBlock, xpManager.getPlayerLevel(player, "Forestry"));

            if (visitedLogs.size() % 4 == 0) {
                forestry.incrementNotoriety(player);
            }

            // 1% chance to summon a Forest Spirit if the block is wood

            ForestSpiritManager spiritMgr = ForestSpiritManager.getInstance(MinecraftNew.getInstance());
            if (!spiritSpawned) {
                if (spiritMgr.attemptSpiritSpawn(0.0001, currentBlock.getLocation(), currentBlock, player)) {
                    spiritSpawned = true;
                }
            }

            // Check all neighbors within a 1-block radius for more wood
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        // Skip (0,0,0)
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block adjacent = currentBlock.getRelative(x, y, z);
                        if (!visitedLogs.contains(adjacent) && isWoodBlock(adjacent.getType())) {
                            visitedLogs.add(adjacent);
                            queue.add(adjacent);
                        }
                    }
                }
            }
        }

        // ---- Now we have a set of all connected logs (visitedLogs). ----
        // BFS is complete. Next, gather leaves around those logs:
        for (Block log : visitedLogs) {
            for (int x = -leavesRange; x <= leavesRange; x++) {
                for (int y = -leavesRange; y <= leavesRange; y++) {
                    for (int z = -leavesRange; z <= leavesRange; z++) {
                        Block leafBlock = log.getRelative(x, y, z);
                        if (isLeafBlock(leafBlock.getType())) {
                            leavesToBreak.add(leafBlock);
                        }
                    }
                }
            }
        }

        // ---- Give XP for each wood block (similar to your BFS code). ----
        // (Alternatively, you can do this inside BFS itself. Up to you.)
        XPManager xpManager = new XPManager(plugin);
        for (Block woodBlock : visitedLogs) {
            xpManager.addXP(player, "Forestry", 1);
        }

        // ---- Finally, break them all gradually! ----
        // 1) Break logs with durability usage, because they’re not ores.
        //    We also want them to drop items.
        //    This means "consumeDurabilityIfNotOre=true".
        breakBlocksGradually(player, new ArrayList<>(visitedLogs), true);

        // 2) Break leaves. Typically we do not give XP for leaves, nor skip tool durability,
        //    but you can set it how you like.
        //    For most “treecapitator” tools, you do use durability on leaves.
        //    If you do *not* want that, set consumeDurabilityIfNotOre = false.
        breakBlocksGradually(player, new ArrayList<>(leavesToBreak), true);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block brokenBlock = event.getBlock();
        ItemStack tool = player.getInventory().getItemInMainHand();

        // If the player is sneaking (holding Shift) and the tool has "Treecapitator" ...

        // ---------------------------
        // Treecapitator branch
        // ---------------------------
        if (player.isSneaking() && hasTreecapEnchant(tool)) {
            // Check tool durability: if damage is at or above 90% of max, abort enchantment
            if (tool != null && tool.getType().getMaxDurability() > 0 &&
                    tool.getDurability() >= tool.getType().getMaxDurability() * 0.9) {
                player.sendMessage(ChatColor.RED + "Your tool is too damaged to use Treecapitator!");
                return;
            }
            // If the broken block is wood, cancel normal drop and process connected wood
            if (isWoodBlock(brokenBlock.getType())) {
                event.setCancelled(true);
                breakConnectedWoodAndLeaves(player, brokenBlock);
            }
        }

        // If the player is sneaking (holding Shift) and the tool has "Hammer"
        if (player.isSneaking() && hasHammerEnchant(tool)) {
            // Check tool durability: if damage is at or above 90% of max, abort enchantment
            if (tool != null && tool.getType().getMaxDurability() > 0 &&
                    tool.getDurability() >= tool.getType().getMaxDurability() * 0.9) {
                player.sendMessage(ChatColor.RED + "Your tool is too damaged to use Hammer enchant!");
                return;
            }
            // Break the center block normally (with durability consumption)
            boolean isOre = false;
            breakBlock(player, brokenBlock, !isOre);

            // Determine break orientation and break surrounding 3×3 blocks
            BlockFace blockFace = event.getBlock().getFace(brokenBlock);
            boolean isVerticalBreak = (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN);
            breakSurroundingBlocks(player, brokenBlock, isVerticalBreak);
        }
    }

    // -------------------------------------------------------------------
    // Disc Seeker, Inferno Blade, etc. remain unchanged below...
    // -------------------------------------------------------------------
    public void activateDiscSeeker(Player player) {
        UUID playerUUID = player.getUniqueId();
        discSeekerActive.put(playerUUID, true);
        player.sendMessage(ChatColor.GREEN + "Disc Seeker activated! The next Creeper you kill will drop a random music disc.");
    }
    private ItemStack getRandomMusicDisc() {
        Material[] musicDiscs = {
                Material.MUSIC_DISC_13,
                Material.MUSIC_DISC_CAT,
                Material.MUSIC_DISC_BLOCKS,
                Material.MUSIC_DISC_CHIRP,
                Material.MUSIC_DISC_FAR,
                Material.MUSIC_DISC_MALL,
                Material.MUSIC_DISC_MELLOHI,
                Material.MUSIC_DISC_STAL,
                Material.MUSIC_DISC_STRAD,
                Material.MUSIC_DISC_WARD,
                Material.MUSIC_DISC_11,
                Material.MUSIC_DISC_WAIT
        };

        Random random = new Random();
        return new ItemStack(musicDiscs[random.nextInt(musicDiscs.length)]);
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            addShredCharges(killer, 2);
        }

        if (event.getEntity() instanceof Creeper) {
            Creeper creeper = (Creeper) event.getEntity();
            if (killer != null) {
                UUID playerUUID = killer.getUniqueId();
                if (discSeekerActive.containsKey(playerUUID) && discSeekerActive.get(playerUUID)) {
                    ItemStack randomMusicDisc = getRandomMusicDisc();
                    creeper.getWorld().dropItemNaturally(creeper.getLocation(), randomMusicDisc);
                    discSeekerActive.remove(playerUUID);
                    killer.sendMessage(ChatColor.GOLD + "You got a music disc from the Creeper!");
                }
            }
        }
    }

    private final Map<UUID, Boolean> defenseActive = new HashMap<>();
    public void activateDefenseMechanism(Player player) {
        UUID playerUUID = player.getUniqueId();
        defenseActive.put(playerUUID, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                defenseActive.remove(playerUUID);
            }
        }.runTaskLater(plugin, 10L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Defense parry logic, Inferno Blade, etc. remain as-is
        if (event.getEntity() instanceof Player &&
                (event.getDamager() instanceof Monster || event.getDamager() instanceof Projectile)) {
            Player player = (Player) event.getEntity();
            UUID playerUUID = player.getUniqueId();

            if (defenseActive.containsKey(playerUUID) && defenseActive.get(playerUUID)) {
                event.setCancelled(true);
                defenseActive.remove(playerUUID);

                if (event.getDamager() instanceof Monster) {
                    Monster attacker = (Monster) event.getDamager();
                    attacker.setHealth(attacker.getHealth() / 2);
                    Vector knockbackDirection = attacker.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
                    knockbackDirection.multiply(0.5);
                    knockbackDirection.setY(0.3);
                    attacker.setVelocity(knockbackDirection);
                    attacker.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120, 1));
                }
                player.sendMessage(ChatColor.GREEN + "You parried!");
            }
        }

        // Check if the damager is a player
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        UUID playerUUID = player.getUniqueId();

        if (!activeEnchantments.containsKey(playerUUID)) return;
        Map<String, Integer> playerEnchants = activeEnchantments.get(playerUUID);
    }


    // The rest of your ultimate enchant code for arrows, parry, snowstorm, etc. remain as-is...
    // Note that we removed the "hammer" and "treecapitator" cases from onPlayerRightClick below:
    private boolean isUltimateEnchantment(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        List<String> lore = item.getItemMeta().getLore();
        if (lore == null) return false;

        for (String line : lore) {
            if (line.contains("Ultimate:")) {
                return true;
            }
        }
        return false;
    }
    private void activateLoyalSword(Player player, ItemStack sword) {
        // Manage the damage multiplier data.
        LoyalSwordData data = loyalSwordDataMap.get(player.getUniqueId());
        if (data == null) {
            data = new LoyalSwordData();
            loyalSwordDataMap.put(player.getUniqueId(), data);
        } else if (System.currentTimeMillis() - data.lastUsage > 60000) {
            // Reset after 60 seconds without use.
            data.damageMultiplier = 1.0;
        }
        data.lastUsage = System.currentTimeMillis();
        // Every use drops the multiplier by 4%
        data.damageMultiplier = Math.max(data.damageMultiplier - 0.04, 0.0);

        // Remove the sword from the player's inventory.
        player.getInventory().remove(sword);

        // Spawn the armorstand 1 block lower than before (for a thrown sword effect).
        Location spawnLoc = player.getLocation().add(0, 0.5, 0);
        ArmorStand armorStand = player.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setVisible(false);
            // Remove marker flag to let it visibly move.
            // stand.setMarker(true);
            // Equip the armorstand with a copy of the sword.
            stand.setItemInHand(sword.clone());
        });

        // Play a goat jump sound.
        player.playSound(player.getLocation(), Sound.ENTITY_GOAT_LONG_JUMP, 1.0f, 1.0f);

        // Launch the armorstand in the direction the player is facing.
        Vector direction = player.getLocation().getDirection().normalize();
        armorStand.setVelocity(direction.multiply(1)); // Adjust speed if needed.

        // Start the task that handles flight, collision, and return.
        new LoyalSwordTask(player, armorStand, sword).runTaskTimer(plugin, 0L, 1L);
    }


    private boolean activateShred(Player player, ItemStack sword){
        if(!consumeShredCharge(player)) {
            return false;
        }
        if(sword.getType().getMaxDurability() > 0){
            // Shred only costs 5 durability on activation
            short dmg = (short)(sword.getDurability() + 2);
            if(dmg > sword.getType().getMaxDurability()) dmg = sword.getType().getMaxDurability();
            sword.setDurability(dmg);
        }

        Location spawnLoc = player.getLocation().add(0, 0.5, 0);
        ArmorStand stand = player.getWorld().spawn(spawnLoc, ArmorStand.class, s -> {
            s.setGravity(false);
            s.setVisible(false);
            s.setMarker(true);
            s.setItemInHand(sword.clone());
        });
        Vector dir = player.getLocation().getDirection().normalize();
        Set<UUID> hit = new HashSet<>();
        new BukkitRunnable(){
            int tick=0;
            double spin=0;
            @Override
            public void run(){
                if(!stand.isValid()){ cancel(); return; }
                stand.teleport(stand.getLocation().add(dir));
                // Spin the sword end over end while flying
                spin += Math.toRadians(20);
                stand.setRightArmPose(new EulerAngle(spin, 0, 0));
                for(Entity e : stand.getNearbyEntities(0.5,0.5,0.5)){
                    if(e instanceof LivingEntity && e!=player){
                        LivingEntity le=(LivingEntity)e;
                        if(hit.contains(le.getUniqueId())) continue;
                        XPManager xp = new XPManager(plugin);
                        int combat = xp.getPlayerLevel(player, "Combat");
                        // Deal damage and immediately reset the entity's no damage
                        // ticks so multiple hits can register without waiting for
                        // the usual invulnerability window.
                        le.damage(combat/16.0, player);
                        le.setNoDamageTicks(0);
                        // Play a sound when the shred strikes an enemy
                        player.getWorld().playSound(le.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                        if(sword.getType().getMaxDurability()>0 && sword.getDurability()>0){
                            sword.setDurability((short)(sword.getDurability()-1));
                        }
                        hit.add(le.getUniqueId());
                    }
                }
                if(++tick>=20){
                    stand.remove();
                    cancel();
                }
            }
        }.runTaskTimer(plugin,0L,1L);
        return true;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        if (player.isSneaking()) {
            return;
        }
        CustomEnchantmentManager.UltimateEnchantmentData ueData = CustomEnchantmentManager.getUltimateEnchantment(item);
        if (ueData == null) return;

        String enchantName = ueData.getName().toLowerCase();
        long cooldownMs = 0;

        // Example: left-click logic, etc. (Hammer/Treecap removed)
        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                return;
            }

            switch (enchantName) {
                case "warp":
                    PlayerMeritManager playerMeritManager = PlayerMeritManager.getInstance(plugin);
                    Vector direction = player.getLocation().getDirection().normalize();
                    Vector offset = direction.multiply(8);
                    player.teleport(player.getLocation().add(offset));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0f);
                    cooldownMs = 1_000L;
                    if(playerMeritManager.hasPerk(player.getUniqueId(), "Instant Transmission")){
                        cooldownMs = 1;
                    }
                    break;
                case "homing arrows":
                    fireHomingArrows(player);
                    cooldownMs = 15_000L;
                    break;
                case "leg shot":
                    fireLeapingArrowWithSlowness(player);
                    cooldownMs = 5_000L;
                    break;
                case "headshot":
                    fireDamageArrow(player);
                    cooldownMs = 15_000L;
                    break;

                default:
                    cooldownMs = 1L;
                    break;
            }
        }

        // Right-click logic, with Hammer/Treecap removed
        if(event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (isOnCooldown(player.getUniqueId(), ueData.getName())) {
                long timeLeft = getCooldownTimeLeft(player.getUniqueId(), ueData.getName());
                player.sendMessage(ChatColor.RED + "This Ultimate Enchantment is on cooldown for "
                        + (timeLeft / 1000) + "s!");
                return;
            }
            switch (enchantName) {
                case "loyal":
                    // Activate the loyal enchantment effect.
                    activateLoyalSword(player, item);
                    cooldownMs = 5000L;
                    PlayerMeritManager loyaltyManager = PlayerMeritManager.getInstance(plugin);
                    if (loyaltyManager.hasPerk(player.getUniqueId(), "Loyalty II")) {
                        cooldownMs = 1_000L;
                    }
                    break;
                case "shred":
                    if (activateShred(player, item)) {
                        cooldownMs = 100L;
                    } else {
                        cooldownMs = 1L;
                    }
                    break;
                // Hammer/Treecapitator removed. No cooldown for them.
                case "excavate":
                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20 * 40, 4));
                    cooldownMs = 120_000L;
                    break;
                case "leap":
                    player.playSound(player.getLocation(), Sound.ENTITY_GOAT_LONG_JUMP, 1.0f, 1.0f);
                    launchPlayerForward(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 4, 255));
                    cooldownMs = 5_000L;
                    break;
                case "parry":
                    player.playSound(player.getLocation(), Sound.BLOCK_POWDER_SNOW_STEP, 1.0f, 1.0f);
                    activateDefenseMechanism(player);
                    cooldownMs = 1_000L;
                    break;
                case "rage mode":
                    player.sendMessage(ChatColor.GREEN + "You become enraged!");
                    player.playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 1)); // 30s
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 600, 1));
                    cooldownMs = 120_000L;
                    break;
                case "disc seeker":
                    activateDiscSeeker(player);
                    cooldownMs = 120_000L;
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 10);
                    break;
                case "snowstorm":
                    activateSnowstorm(player, 1);
                    cooldownMs = 35_000L;
                    setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
                    saveCooldowns();
                    break;
                default:
                    cooldownMs = 1L;
                    break;
            }
        }

        // Put the player on cooldown for everything else except hammer/treecap
        setPlayerCooldown(player.getUniqueId(), ueData.getName(), cooldownMs);
        saveCooldowns();
    }

    private void activateSnowstorm(Player player, int level) {
        int radius = 16 + level;
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity instanceof Monster && !(entity instanceof Boss)) {
                LivingEntity monster = (LivingEntity) entity;
                monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 100));
                monster.setHealth(monster.getHealth() / 2);
                monster.setFreezeTicks(2000);
                monster.getWorld().spawnParticle(Particle.SNOWBALL, monster.getLocation().add(0, 1, 0),
                        50, 0.5, 0.5, 0.5, 0.1);
            }
        }
        player.getWorld().spawnParticle(Particle.SNOWBALL, player.getLocation(),
                100, 0, 1, 0, 0.1);
        player.playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        player.sendMessage(ChatColor.AQUA + "Snowstorm activated! Nearby monsters are frozen!");
    }


    // Homing arrows etc. remain unchanged
    public void fireHomingArrows(Player player) {
        double spreadAngle = 5.0;
        new BukkitRunnable() {
            int arrowCount = 0;
            @Override
            public void run() {
                if (arrowCount >= 5) {
                    this.cancel();
                    return;
                }
                double angleOffset = (arrowCount - 2) * spreadAngle;
                Vector direction = player.getLocation().getDirection().clone();
                Vector rotatedDirection = rotateVectorAroundYAxis(direction, angleOffset);
                Arrow arrow = player.launchProjectile(Arrow.class);
                arrow.setVelocity(rotatedDirection.multiply(1.5));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (arrow.isDead() || arrow.isOnGround()) {
                            this.cancel();
                            return;
                        }
                        List<Entity> nearbyEntities = arrow.getNearbyEntities(20, 20, 20);
                        LivingEntity target = null;
                        double closestDistance = Double.MAX_VALUE;
                        for (Entity e : nearbyEntities) {
                            if (e instanceof LivingEntity && e != player) {
                                double distance = e.getLocation().distance(arrow.getLocation());
                                if (distance < closestDistance) {
                                    closestDistance = distance;
                                    target = (LivingEntity) e;
                                }
                            }
                        }
                        if (target != null) {
                            Vector targetVec = target.getLocation().toVector().add(new Vector(0, 1, 0));
                            Vector dir = targetVec.subtract(arrow.getLocation().toVector()).normalize();
                            arrow.setVelocity(dir.multiply(3));
                        }
                    }
                }.runTaskLater(plugin, 4);
                arrowCount++;
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
    private Vector rotateVectorAroundYAxis(Vector vector, double angle) {
        double radians = Math.toRadians(angle);
        double x = vector.getX() * Math.cos(radians) - vector.getZ() * Math.sin(radians);
        double z = vector.getX() * Math.sin(radians) + vector.getZ() * Math.cos(radians);
        return new Vector(x, vector.getY(), z);
    }
    private void fireLeapingArrowWithSlowness(Player player) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        Vector direction = player.getLocation().getDirection().normalize();
        arrow.setVelocity(direction.multiply(2));
        PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOW, 20 * 60 * 30, 99);
        arrow.addCustomEffect(slownessEffect, true);
        PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP, 20 * 60 * 30, 254);
        arrow.addCustomEffect(jumpBoostEffect, true);
        player.sendMessage(ChatColor.GREEN + "You fired a legshot!");
    }
    private void fireDamageArrow(Player player) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setDamage(20);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_WORK_FLETCHER, 10.f, 1.0f);
    }
    public void launchPlayerForward(Player player) {
        Vector direction = player.getLocation().getDirection().normalize();
        Vector launchVector = direction.multiply(3);
        player.setVelocity(launchVector);
        player.sendMessage(ChatColor.GREEN + "You have been launched forward!");
    }

    // Cooldown / config saving logic (unchanged)
    private boolean isOnCooldown(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return false;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        return nextUseTime != null && System.currentTimeMillis() < nextUseTime;
    }
    private long getCooldownTimeLeft(UUID playerUUID, String ultimateName) {
        if (!ultimateCooldowns.containsKey(playerUUID)) {
            return 0;
        }
        Long nextUseTime = ultimateCooldowns.get(playerUUID).get(ultimateName.toLowerCase());
        if (nextUseTime == null) return 0;
        return nextUseTime - System.currentTimeMillis();
    }
    private void setPlayerCooldown(UUID playerUUID, String ultimateName, long cooldownMs) {
        ultimateCooldowns.putIfAbsent(playerUUID, new HashMap<>());
        long nextUseTime = System.currentTimeMillis() + cooldownMs;
        ultimateCooldowns.get(playerUUID).put(ultimateName.toLowerCase(), nextUseTime);
    }

    private int getShredCharges(UUID player) {
        return shredCharges.getOrDefault(player, MAX_SHRED_SWORDS);
    }

    private boolean consumeShredCharge(Player player) {
        UUID id = player.getUniqueId();
        int charges = shredCharges.getOrDefault(id, MAX_SHRED_SWORDS);
        if (charges <= 0) {
            return false;
        }
        shredCharges.put(id, charges - 1);
        return true;
    }

    private void addShredCharges(Player player, int amount) {
        UUID id = player.getUniqueId();
        int current = shredCharges.getOrDefault(id, MAX_SHRED_SWORDS);
        if (current >= MAX_SHRED_SWORDS) {
            player.sendMessage(ChatColor.GREEN + "Your Shredders are at maximum capacity!");
            return;
        }
        int newTotal = Math.min(MAX_SHRED_SWORDS, current + amount);
        shredCharges.put(id, newTotal);
        if (newTotal == MAX_SHRED_SWORDS) {
            player.sendMessage(ChatColor.GREEN + "Your Shredders are at maximum capacity!");
        }
    }
    private void loadCooldowns() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("ultimate-cooldowns")) {
            return;
        }
        for (String uuidStr : config.getConfigurationSection("ultimate-cooldowns").getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                continue;
            }
            Map<String, Long> map = new HashMap<>();
            for (String enchantName : config.getConfigurationSection("ultimate-cooldowns." + uuidStr).getKeys(false)) {
                long nextUseTime = config.getLong("ultimate-cooldowns." + uuidStr + "." + enchantName);
                map.put(enchantName, nextUseTime);
            }
            ultimateCooldowns.put(uuid, map);
        }
    }
    public void saveCooldowns() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("ultimate-cooldowns")) {
            config.set("ultimate-cooldowns", null);
        }
        for (Map.Entry<UUID, Map<String, Long>> entry : ultimateCooldowns.entrySet()) {
            String uuidStr = entry.getKey().toString();
            Map<String, Long> enchantMap = entry.getValue();
            for (Map.Entry<String, Long> enchantEntry : enchantMap.entrySet()) {
                String enchantName = enchantEntry.getKey();
                long nextUseTime = enchantEntry.getValue();
                config.set("ultimate-cooldowns." + uuidStr + "." + enchantName, nextUseTime);
            }
        }
        plugin.saveConfig();
    }
    public class OreUtils {

        // A predefined set of all known ores for reliable and fast lookups
        private static final Set<Material> ORES;

        static {
            ORES = new HashSet<>();
            // Overworld Ores
            ORES.add(Material.COAL_ORE);
            ORES.add(Material.IRON_ORE);
            ORES.add(Material.COPPER_ORE);
            ORES.add(Material.GOLD_ORE);
            ORES.add(Material.REDSTONE_ORE);
            ORES.add(Material.LAPIS_ORE);
            ORES.add(Material.DIAMOND_ORE);
            ORES.add(Material.EMERALD_ORE);

            // Deepslate Ores
            ORES.add(Material.DEEPSLATE_COAL_ORE);
            ORES.add(Material.DEEPSLATE_IRON_ORE);
            ORES.add(Material.DEEPSLATE_COPPER_ORE);
            ORES.add(Material.DEEPSLATE_GOLD_ORE);
            ORES.add(Material.DEEPSLATE_REDSTONE_ORE);
            ORES.add(Material.DEEPSLATE_LAPIS_ORE);
            ORES.add(Material.DEEPSLATE_DIAMOND_ORE);
            ORES.add(Material.DEEPSLATE_EMERALD_ORE);

            // Nether Ores
            ORES.add(Material.NETHER_QUARTZ_ORE);
            ORES.add(Material.NETHER_GOLD_ORE);

            // Ancient Debris
            ORES.add(Material.ANCIENT_DEBRIS);
        }

        /**
         * Checks if the given block is an ore.
         * This method uses both a predefined set of ores and a fallback check based on material names.
         *
         * @param block The block to check.
         * @return True if the block is an ore, false otherwise.
         */
        public static boolean isOreBlock(Block block) {
            if (block == null) {
                return false;
            }

            Material material = block.getType();

            // Check if the block is in the predefined set
            boolean inSet = ORES.contains(material);
            if (inSet) {
                return true;
            }

            // Fallback: Check material name
            String materialName = material.name();
            boolean isOre = materialName.contains("ORE") || materialName.equals("ANCIENT_DEBRIS");

            return isOre;
        }

    }
        private class LoyalSwordTask extends BukkitRunnable {
        private final Player player;
        private final ArmorStand armorStand;
        private final ItemStack sword;
        private final Vector flightVelocity; // Store the initial flight direction and speed
        private int ticks = 0;
        private int missCount = 0;
        private boolean returning = false;
        private double previousDistance = Double.MAX_VALUE;
        private final int MAX_LIFETIME_TICKS = 200; // 10 seconds (20 ticks per second)

        public LoyalSwordTask(Player player, ArmorStand armorStand, ItemStack sword) {
            this.player = player;
            this.armorStand = armorStand;
            this.sword = sword;
            // Adjust multiplier as needed for forward speed.
            this.flightVelocity = player.getLocation().getDirection().normalize().multiply(1);
        }

        @Override
        public void run() {
            // Cancel if player disconnects or armorstand becomes invalid.
            if (!player.isOnline() || !armorStand.isValid()) {
                if (armorStand.isValid()) {
                    armorStand.remove();
                }
                player.getInventory().addItem(sword);
                cancel();
                return;
            }

            // Force return after 10 seconds regardless of state
            if (ticks >= MAX_LIFETIME_TICKS) {
                forceReturnToPlayer();
                return;
            }

            ticks++;

            if (!returning) {
                // Move forward by teleporting along the flight vector.
                Location current = armorStand.getLocation();
                Location next = current.clone().add(flightVelocity);
                armorStand.teleport(next);

                // Check collision with a block.
                Block blockAt = next.getBlock();
                if (blockAt.getType() != Material.AIR && blockAt.getType().isSolid()) {
                    startReturn();
                }

                // Check for collision with an entity (other than the player).
                for (Entity e : armorStand.getNearbyEntities(0.5, 0.5, 0.5)) {
                    if (e instanceof Monster || e.hasMetadata("SEA_CREATURE")) {
                        LivingEntity target = (LivingEntity) e;
                        if(target.isDead()){
                            return;
                        }
                        // Retrieve loyal data.
                        LoyalSwordData data = loyalSwordDataMap.get(player.getUniqueId());
                        if (data == null) {
                            data = new LoyalSwordData();
                            loyalSwordDataMap.put(player.getUniqueId(), data);
                        }
                        // Get player's combat level (assumed method).
                        XPManager xpManager = new XPManager(plugin);
                        int combatLevel = xpManager.getPlayerLevel(player, "Combat");
                        double damage = combatLevel;
                        target.damage(damage, player);
                        // Increase damage multiplier by 4% on a successful melee hit (max 1.0).
                        data.damageMultiplier = Math.min(data.damageMultiplier + 0.04, 1.0);
                        // Display damage dealt in an action bar message.
                        sendActionBar(player, ChatColor.GREEN + "Hit for " + String.format("%.1f", damage) +
                                " damage (" + (int)(data.damageMultiplier * 100) + "%)");
                        // Play a clang sound.
                        armorStand.getWorld().playSound(armorStand.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.0f, 1.0f);
                        startReturn();
                        break;
                    }
                }

                // After 3 seconds (60 ticks), trigger return.
                if (ticks >= 60) {
                    startReturn();
                }
            } else {
                // Return phase: move armorstand toward the player.
                Location current = armorStand.getLocation();
                Location targetLoc = player.getLocation().add(0, 1, 0);
                Vector toPlayer = targetLoc.toVector().subtract(current.toVector());
                double distance = toPlayer.length();
                if (distance < 1.5) {
                    returnSwordToPlayer();
                    return;
                }

                toPlayer.normalize();
                // Teleport a short distance toward the player.
                Location next = current.clone().add(toPlayer.multiply(0.5));
                armorStand.teleport(next);

                // Instead of random yaw/pitch, update the right arm pose to create a windmill effect.
                EulerAngle currentArm = armorStand.getRightArmPose();
                double newX = currentArm.getX() + Math.toRadians(15); // Rotate 15° per tick
                EulerAngle newArmPose = new EulerAngle(newX, currentArm.getY(), currentArm.getZ());
                armorStand.setRightArmPose(newArmPose);

                // Spawn particle effects along the return path.
                armorStand.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, armorStand.getLocation(), 10, 0.2, 0.2, 0.2, 0.01);

                // Check if the distance isn't decreasing (indicating a "miss").
                if (distance >= previousDistance - 0.1) {
                    missCount++;
                    if (missCount >= 20) { // Increased threshold to give more time to navigate obstacles
                        forceReturnToPlayer();
                        return;
                    }
                } else {
                    missCount = 0;
                }
                previousDistance = distance;
            }
        }

        // Switch to the return phase.
        private void startReturn() {
            if (!returning) {
                returning = true;
                ticks = 0;
                previousDistance = armorStand.getLocation().distance(player.getLocation());
            }
        }

        // Force the sword to return directly to the player's inventory
        private void forceReturnToPlayer() {
            if (armorStand.isValid()) {
                armorStand.remove();
            }
            
            // Play return sound at player's location
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1000.0f, 1.0f);
            
            // Get the first item in the player's inventory
            ItemStack firstSlotItem = player.getInventory().getItem(0);

            // Always replace the first slot with the returning sword
            if (firstSlotItem != null && !firstSlotItem.isSimilar(sword)) {
                // Clone the first item and drop it at player's feet
                ItemStack droppedItem = firstSlotItem.clone();
                player.getWorld().dropItemNaturally(player.getLocation(), droppedItem);
            }
            
            // Set the first slot to the returning sword
            player.getInventory().setItem(0, sword);
            
            // Visual effect to indicate the sword has returned
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0.1);
            
            cancel();

        }

        // Normal return when the sword reaches the player
        private void returnSwordToPlayer() {
            // Play a trident return sound when the sword comes back
            armorStand.getWorld().playSound(armorStand.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.0f);
            armorStand.remove();

            // Get the first item in the player's inventory
            ItemStack firstSlotItem = player.getInventory().getItem(0);

            // Always replace the first slot with the returning sword
            if (firstSlotItem != null && !firstSlotItem.isSimilar(sword)) {
                // Clone the first item and drop it at player's feet
                ItemStack droppedItem = firstSlotItem.clone();
                player.getWorld().dropItemNaturally(player.getLocation(), droppedItem);
            }
            
            // Set the first slot to the returning sword
            player.getInventory().setItem(0, sword);
            
            cancel();
        }

        private void sendActionBar(Player player, String message) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new net.md_5.bungee.api.chat.TextComponent(message));
        }
    }




}
