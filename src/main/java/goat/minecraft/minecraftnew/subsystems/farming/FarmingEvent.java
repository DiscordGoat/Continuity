package goat.minecraft.minecraftnew.subsystems.farming;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.beacon.Catalyst;
import goat.minecraft.minecraftnew.other.beacon.CatalystManager;
import goat.minecraft.minecraftnew.other.beacon.CatalystType;
import goat.minecraft.minecraftnew.utils.stats.StatsCalculator;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.farming.FestivalBeeManager;
import goat.minecraft.minecraftnew.subsystems.farming.CropCountManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetRegistry;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class FarmingEvent implements Listener {
    private static final Set<Material> RARE_DROP_CROPS = EnumSet.noneOf(Material.class);
    // Define rarity probabilities

    MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    Random random = new Random();
    private static final String PLAYER_PLACED_KEY = "player_placed";

    private static final Map<Material, Integer> cropXP = new HashMap<>();

    private static final long HARVEST_BALLAD_TICKS = 240L * 20L;
    private final Map<World, BukkitTask> harvestBalladLoops = new HashMap<>();
    private final Map<UUID, Integer> cropHarvestCounter = new HashMap<>();

    static {
        cropXP.put(Material.WHEAT, 3); // Common crops
        cropXP.put(Material.NETHER_WART, 5); // Common crops
        cropXP.put(Material.POTATOES, 3);
        cropXP.put(Material.CARROTS, 4);
        cropXP.put(Material.CARROT, 4);
        cropXP.put(Material.BEETROOTS, 4); // Slightly rarer crops
        cropXP.put(Material.MELON, 6); // Uncommon crops
        cropXP.put(Material.PUMPKIN, 6);
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.PUMPKIN || block.getType() == Material.MELON) {
            block.setMetadata(PLAYER_PLACED_KEY, new FixedMetadataValue(plugin, true));
            System.out.println("Metadata set for player-placed block: " + block.getType());
        }
    }

    // Tilling larger area with For The Streets talent
    @EventHandler
    public void onTill(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Material mat = block.getType();
        if (mat != Material.DIRT && mat != Material.GRASS_BLOCK && mat != Material.COARSE_DIRT && mat != Material.ROOTED_DIRT) return;
        ItemStack tool = event.getItem();
        if (tool == null || !tool.getType().name().endsWith("_HOE")) return;
        Player player = event.getPlayer();
        int lvl = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FOR_THE_STREETS);
        if (lvl > 0 && random.nextDouble() < lvl * 0.20) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    Block b = block.getRelative(dx, 0, dz);
                    Material t = b.getType();
                    if (t == Material.DIRT || t == Material.GRASS_BLOCK || t == Material.COARSE_DIRT || t == Material.ROOTED_DIRT) {
                        b.setType(Material.FARMLAND);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSeederCompost(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.COMPOSTER) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // grab the display name (strip color if you're using ChatColor)
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        String name = ChatColor.stripColor(meta.getDisplayName());

        ItemStack seeder = null;
        switch (name) {
            case "Enchanted Hay Bale":
                seeder = ItemRegistry.getWheatSeeder();
                break;
            case "Enchanted Golden Carrot":
                seeder = ItemRegistry.getCarrotSeeder();
                break;
            case "HeartRoot":
                seeder = ItemRegistry.getBeetrootSeeder();
                break;
            case "Immortal Potato":
                seeder = ItemRegistry.getPotatoSeeder();
                break;
            default:
                return;  // no matching seeder
        }

        // consume one of the clicked items
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            event.getPlayer().getInventory().setItem(event.getHand(), null);
        }

        // drop the seeder(s)
        int amount = 1;
        seeder.setAmount(amount);
        Location dropLoc = block.getLocation().add(0.5, 1.0, 0.5);
        block.getWorld().dropItemNaturally(dropLoc, seeder);

        // effects
        World world = block.getWorld();
        world.playSound(dropLoc, Sound.BLOCK_COMPOSTER_FILL_SUCCESS, 1.0f, 1.0f);
        world.spawnParticle(Particle.COMPOSTER, dropLoc, 10, 0.25, 0.25, 0.25, 0.01);

        event.setCancelled(true);
    }

    @EventHandler
    public void onCropHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Material blockType = block.getType();
        //System.out.println("Breaking block: " + blockType.name());

        // Check if the block is a recognized crop
        if (cropXP.containsKey(blockType)) {
            boolean harvest = false;
            // Check if the block was placed by a player
            if (block.hasMetadata(PLAYER_PLACED_KEY)) {
                if (block.getType() == Material.PUMPKIN || block.getType() == Material.MELON) {
                    System.out.println("player placed block broken: " + blockType.name());

                    return;
                }
            }

            // Handle crops that have growth stages (Ageable blocks)
            if (block.getBlockData() instanceof Ageable) {
                Ageable crop = (Ageable) block.getBlockData();

                // Only proceed if the crop is fully grown
                if (crop.getAge() != crop.getMaximumAge()) {
                    return; // Crop is not fully grown, ignore the event
                }
            }

            // Batch XP and side-effects into a 2s window
            int xp = cropXP.getOrDefault(blockType, 0);
            boolean hasUnrivaled = SkillTreeManager.getInstance() != null &&
                    (SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.UNRIVALED) > 0);

            // Enqueue this crop into the player's current batch
            HarvestBatchManager.addToBatch(player, blockType, xp, hasUnrivaled, true, false, null);

            // Maintain per-crop XP pickup sound feedback
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

            // Handle extra increments from custom enchantments as count-only contributions
            if (blockType == Material.WHEAT || blockType == Material.WHEAT_SEEDS || blockType == Material.CARROTS || blockType == Material.POTATOES ||
                    blockType == Material.BEETROOTS || blockType == Material.PUMPKIN || blockType == Material.MELON) {
                ItemStack tool = player.getInventory().getItemInMainHand();
                switch (blockType) {
                    case WHEAT, WHEAT_SEEDS -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Cornfield");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    case CARROTS -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "What's Up Doc");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    case POTATOES -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Legend");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    case BEETROOTS -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Venerate");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    case MELON -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Clean Cut");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    case PUMPKIN -> {
                        int level = CustomEnchantmentManager.getEnchantmentLevel(tool, "Gourd");
                        double chance = Math.min(level * 0.02, 1.0);
                        if (level > 0 && random.nextDouble() < chance) {
                            HarvestBatchManager.addToBatch(player, blockType, 0, false, false, false, null);
                        }
                    }
                    default -> {}
                }
            }

            StatsCalculator calc = StatsCalculator.getInstance(plugin);
            double chance = calc.getExtraCropChance(player);
            int extra = 0;
            while (chance >= 100.0) {
                extra++;
                chance -= 100.0;
            }
            if (random.nextDouble() < chance / 100.0) extra++;
            if (extra > 0) {
                Collection<ItemStack> drops = block.getDrops();
                for (ItemStack drop : drops) {
                    ItemStack extraDrop = drop.clone();
                    extraDrop.setAmount(drop.getAmount() * extra);
                    Objects.requireNonNull(e.getBlock().getLocation().getWorld()).dropItem(e.getBlock().getLocation(), extraDrop);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_ROOTED_DIRT_PLACE, 1.0f, 1.0f);
            }

            // Harvest Festival haste buff
            int hf = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.HARVEST_FESTIVAL);
            if (hf > 0 && random.nextDouble() < hf * 0.5) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(PotionEffectType.HASTE, 100, 1));
            }

            // Fertilizer handled in batch grant

            // Festival Bees handled in batch grant
            // Harvest reward handled in batch grant
        }
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Entity dead = e.getEntity();
        String name = dead.getCustomName();
        if (name != null && name.contains(ChatColor.GOLD + "Festival")) {
            World world = dead.getWorld();
            // Delay one tick to let the entity actually disappear
            Bukkit.getScheduler().runTask(plugin, () -> {
                FestivalBeeManager.getInstance(plugin).onFestivalBeeDeath(dead.getUniqueId());
                if (FestivalBeeManager.getInstance(plugin).getFestivalBeeCount() == 0) {
                    // no more bees → stop the music
                    stopAllHarvestBallads();
                }
            });
        }
    }
    private void startHarvestBalladLoop(World world) {
        if (harvestBalladLoops.containsKey(world)) return;

        Bukkit.getOnlinePlayers().forEach(p ->
                p.playSound(
                        p.getLocation(),
                        "custom.harvest_ballad",
                        SoundCategory.AMBIENT,
                        1.0f, 1.0f
                )
        );

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (FestivalBeeManager.getInstance(plugin).getFestivalBeeCount() > 0) {
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(
                                p.getLocation(),
                                "custom.harvest_ballad",
                                SoundCategory.AMBIENT,
                                1.0f, 1.0f
                        )
                );
            } else {
                BukkitTask t = harvestBalladLoops.remove(world);
                if (t != null) t.cancel();
            }
        }, HARVEST_BALLAD_TICKS, HARVEST_BALLAD_TICKS);

        harvestBalladLoops.put(world, task);
    }

    private void stopAllHarvestBallads() {
        Bukkit.getOnlinePlayers().forEach(Player::stopAllSounds);
        harvestBalladLoops.values().forEach(BukkitTask::cancel);
        harvestBalladLoops.clear();
    }
    private long countFestivalBees(World world) {
        return world.getEntities().stream()
                .filter(e -> {
                    String name = e.getCustomName();
                    return name != null
                            && name.contains(ChatColor.GOLD + "Festival");
                })
                .count();
    }

    private void handleHarvestRewards(Block block, Player player, Material blockType) {
        double roll = random.nextDouble();
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);

        switch (blockType) {
            case WHEAT -> {
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getWheatSeeder();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getWheatSeeder();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getEnchantedHayBale();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedHayBale();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(plugin);
                    if (petManager.getPet(player, "Scarecrow") == null) {
                        new PetRegistry().addPetByName(player, "Scarecrow");
                        notifyHarvest(player, ChatColor.GOLD + "Scarecrow pet", 1, true);
                    } else {
                        // Already owns the pet; award a Legendary-tier harvest instead of epic fallback
                        ItemStack legendary = ItemRegistry.getEnchantedHayBale();
                        legendary.setAmount(16);
                        block.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
            }
            case CARROTS -> {
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getCarrotSeeder();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getCarrotSeeder();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getEnchantedGoldenCarrot();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedGoldenCarrot();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(plugin);
                    if (petManager.getPet(player, "Killer Rabbit") == null) {
                        new PetRegistry().addPetByName(player, "Killer Rabbit");
                        notifyHarvest(player, ChatColor.GOLD + "Killer Rabbit pet", 1, true);
                    } else {
                        // Already owns the pet; award a Legendary-tier harvest instead of epic fallback
                        ItemStack legendary = ItemRegistry.getEnchantedGoldenCarrot();
                        legendary.setAmount(16);
                        block.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
            }
            case BEETROOTS -> {
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getBeetrootSeeder();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getBeetrootSeeder();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getHeartRoot();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getHeartRoot();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(plugin);
                    if (petManager.getPet(player, "Baron") == null) {
                        new PetRegistry().addPetByName(player, "Baron");
                        notifyHarvest(player, ChatColor.GOLD + "Baron pet", 1, true);
                    } else {
                        // Already owns the pet; award a Legendary-tier harvest instead of epic fallback
                        ItemStack legendary = ItemRegistry.getHeartRoot();
                        legendary.setAmount(16);
                        block.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
            }
            case POTATOES -> {
                if (roll < 0.50) {
                    ItemStack item = ItemRegistry.getPotatoSeeder();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getPotatoSeeder();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getImmortalPotato();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getImmortalPotato();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    PetManager petManager = PetManager.getInstance(plugin);
                    if (petManager.getPet(player, "Mole") == null) {
                        new PetRegistry().addPetByName(player, "Mole");
                        notifyHarvest(player, ChatColor.GOLD + "Mole pet", 1, true);
                    } else {
                        // Already owns the pet; award a Legendary-tier harvest instead of epic fallback
                        ItemStack legendary = ItemRegistry.getImmortalPotato();
                        legendary.setAmount(16);
                        block.getWorld().dropItemNaturally(dropLoc, legendary);
                        notifyHarvest(player, legendary, true);
                    }
                }
            }
            case MELON -> {
                if (roll < 0.50) {
                    ItemStack item = new ItemStack(Material.MELON_SLICE, 16);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = new ItemStack(Material.MELON_SLICE, 64);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getWatermelon();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getWatermelon();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    ItemStack item = ItemRegistry.getWorldsLargestWatermelon();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                }
            }
            case PUMPKIN -> {
                if (roll < 0.50) {
                    ItemStack item = new ItemStack(Material.PUMPKIN, 16);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.80) {
                    ItemStack item = new ItemStack(Material.PUMPKIN, 64);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, false);
                } else if (roll < 0.90) {
                    ItemStack item = ItemRegistry.getJackOLantern();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getJackOLantern();
                    item.setAmount(2);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                } else {
                    ItemStack item = ItemRegistry.getWorldsLargestPumpkin();
                    block.getWorld().dropItemNaturally(dropLoc, item);
                    notifyHarvest(player, item, true);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Handles the rare item drop for eligible crops with a 1/400 chance.
     *
     * @param block     The block that was harvested.
     * @param player    The player who harvested the block.
     * @param blockType The type of the harvested block.
     */
    private void handleRareItemDrop(Block block, Player player, Material blockType) {
        // Check if the harvested crop is eligible for rare drops
        if (RARE_DROP_CROPS.contains(blockType)) {
            // 1/400 chance to drop a rare item
            if (random.nextInt(1600) == 0) {
                // Retrieve the rare item for this specific crop from ItemRegistry
                ItemStack rareItem = ItemRegistry.getRareItem(blockType); // Ensure this method is implemented in ItemRegistry

                if (rareItem != null) {
                    // Drop the rare item naturally at the block's location
                    block.getWorld().dropItemNaturally(block.getLocation(), rareItem);
 //yup
                    // Optional: Play a unique sound to indicate a rare drop
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    notifyHarvest(player, rareItem, true);
                } else {
                    // Log a warning if the rare item is not defined
                    plugin.getLogger().warning("Rare item is not defined in ItemRegistry.getRareItem(Material). Crop: " + blockType.name());
                }
            }
        }
    }

    private void notifyHarvest(Player player, ItemStack item, boolean rareOrAbove) {
        String name;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            name = item.getItemMeta().getDisplayName();
        } else {
            // default to common (white) when no display name
            name = ChatColor.WHITE + formatMaterialName(item.getType());
        }
        notifyHarvest(player, name, item.getAmount(), rareOrAbove);
    }

    private void notifyHarvest(Player player, String itemName, int amount, boolean rareOrAbove) {
        HarvestRarity rarity = detectRarity(itemName, rareOrAbove);
        String baseName = ChatColor.stripColor(itemName);
        String styledName = rarity.getColor() + rarity.getStyles() + baseName;
        String amountText = amount > 1 ? ChatColor.YELLOW + "" + amount + "x " : "";
        player.sendMessage(ChatColor.GREEN + "Harvest Reward: " + amountText + styledName);
        playRarityJingle(player, rarity);
        String subtitle = amount > 1 ? ChatColor.YELLOW + "" + amount + "x" : "";
        player.sendTitle(styledName, subtitle, rarity.getFadeIn(), rarity.getStay(), rarity.getFadeOut());
    }

    private void playRarityJingle(Player player, HarvestRarity rarity) {
        float C = 1.00f, E = 1.26f, G = 1.50f, Bn = 1.89f, C5 = 2.00f;

        Sound commonInstr = Sound.BLOCK_NOTE_BLOCK_PLING;
        Sound harp = Sound.BLOCK_NOTE_BLOCK_HARP;
        Sound bell = Sound.BLOCK_NOTE_BLOCK_BELL;
        Sound chime = Sound.BLOCK_NOTE_BLOCK_CHIME;

        int tickGap;
        Sound instr;
        float[] notes;

        switch (rarity) {
            case COMMON -> { instr = commonInstr; notes = new float[] { C }; tickGap = 0; }
            case UNCOMMON -> { instr = harp; notes = new float[] { C, E }; tickGap = 4; }
            case RARE -> { instr = bell; notes = new float[] { C, E, G }; tickGap = 3; }
            case EPIC -> { instr = chime; notes = new float[] { C, E, G, C5 }; tickGap = 3; }
            case LEGENDARY -> { instr = chime; notes = new float[] { C, E, G, Bn, C5 }; tickGap = 2; }
            default -> { instr = chime; notes = new float[] { C }; tickGap = 0; }
        }

        final Sound playInstr = instr;
        for (int i = 0; i < notes.length; i++) {
            final float pitch = notes[i];
            final int delay = i * Math.max(0, tickGap);
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.playSound(player.getLocation(), playInstr, 1.0f, pitch);
            }, delay);
        }
    }

    private HarvestRarity detectRarity(String name, boolean rareOrAbove) {
        if (name.startsWith(ChatColor.GOLD.toString())) {
            return HarvestRarity.LEGENDARY;
        }
        if (name.startsWith(ChatColor.DARK_PURPLE.toString())) {
            return HarvestRarity.EPIC;
        }
        if (name.startsWith(ChatColor.BLUE.toString())) {
            return HarvestRarity.RARE;
        }
        if (name.startsWith(ChatColor.GREEN.toString())) {
            return HarvestRarity.UNCOMMON;
        }
        return rareOrAbove ? HarvestRarity.RARE : HarvestRarity.COMMON;
    }

    private enum HarvestRarity {
        COMMON(ChatColor.WHITE, "", 0.8f, 1.0f, 5, 40, 10),
        UNCOMMON(ChatColor.GREEN, ChatColor.ITALIC.toString(), 1.0f, 1.1f, 10, 60, 10),
        RARE(ChatColor.BLUE, ChatColor.BOLD.toString(), 1.2f, 1.2f, 15, 80, 15),
        EPIC(ChatColor.DARK_PURPLE, ChatColor.BOLD.toString() + ChatColor.ITALIC, 1.4f, 1.3f, 20, 100, 20),
        LEGENDARY(ChatColor.GOLD, ChatColor.BOLD.toString() + ChatColor.ITALIC + ChatColor.UNDERLINE, 1.6f, 1.4f, 25, 120, 25);

        private final ChatColor color;
        private final String styles;
        private final float volume;
        private final float pitch;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        HarvestRarity(ChatColor color, String styles, float volume, float pitch, int fadeIn, int stay, int fadeOut) {
            this.color = color;
            this.styles = styles;
            this.volume = volume;
            this.pitch = pitch;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        public String getColor() {
            return color.toString();
        }

        public String getStyles() {
            return styles;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }

        public int getFadeIn() {
            return fadeIn;
        }

        public int getStay() {
            return stay;
        }

        public int getFadeOut() {
            return fadeOut;
        }
    }

    private String formatMaterialName(Material material) {
        String[] words = material.name().toLowerCase(Locale.ROOT).split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return builder.toString().trim();
    }

}
