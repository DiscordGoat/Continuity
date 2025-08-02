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
import goat.minecraft.minecraftnew.utils.devtools.ItemRegistry;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
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
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class FarmingEvent implements Listener {
    private static final Set<Material> RARE_DROP_CROPS = EnumSet.noneOf(Material.class);
    // Define rarity probabilities

    MinecraftNew plugin = MinecraftNew.getInstance();
    public XPManager xpManager = new XPManager(plugin);
    Random random = new Random();
    private static final String PLAYER_PLACED_KEY = "player_placed";

    private static final Map<Material, Integer> cropXP = new HashMap<>();

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
    public void onCropHarvest(BlockBreakEvent e) {
        Block block = e.getBlock();
        Player player = e.getPlayer();
        Material blockType = block.getType();
        //System.out.println("Breaking block: " + blockType.name());

        // Check if the block is a recognized crop
        if (cropXP.containsKey(blockType)) {
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

            // Award Farming XP
            int xp = cropXP.get(blockType);
            ExperienceOrb orb = (ExperienceOrb) block.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
            orb.setExperience(2);
            xpManager.addXP(player, "Farming", xp);
            // Play harvest sound
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 2.0f);

            if (blockType == Material.WHEAT || blockType == Material.WHEAT_SEEDS || blockType == Material.CARROTS || blockType == Material.POTATOES ||
                    blockType == Material.BEETROOTS || blockType == Material.PUMPKIN ||
                    blockType == Material.MELON || blockType == Material.COCOA) {
                CropCountManager.getInstance(plugin).increment(player, blockType);
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

            // Unrivaled - grow nearby crops
            int un = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.UNRIVALED);
            if (un > 0 && random.nextDouble() < un * 0.01) {
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        Block b = block.getRelative(dx, 0, dz);
                        if (b.getBlockData() instanceof Ageable age) {
                            if (age.getAge() < age.getMaximumAge()) {
                                age.setAge(Math.min(age.getAge() + 1, age.getMaximumAge()));
                                b.setBlockData(age);
                            }
                        }
                    }
                }
            }

            // Festival Bees spawn
            SkillTreeManager mgr = SkillTreeManager.getInstance();
            double beeChance =
                    mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_I) * 0.25 +
                    mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_II) * 0.25 +
                    mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_III) * 0.25 +
                    mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEES_IV) * 0.25;
            beeChance /= 100.0;
            if (random.nextDouble() < beeChance) {
                int dur = 30
                        + mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEE_DURATION_I) * 10
                        + mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.FESTIVAL_BEE_DURATION_II) * 10;
                double multi = 1 + mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.HIVEMIND) * 0.25;
                dur = (int) (dur * multi);
                World world = player.getWorld();
                long before = countFestivalBees(world);
                FestivalBeeManager.getInstance(plugin).spawnFestivalBee(block.getLocation(), dur);
                int swarm = mgr.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.SWARM);
                if (swarm > 0 && random.nextDouble() < swarm * 0.10) {
                    FestivalBeeManager.getInstance(plugin).spawnFestivalBee(block.getLocation(), dur);
                }
                player.sendMessage(ChatColor.GOLD + "A Festival Bee has spawned!");
                long after = countFestivalBees(world);


                if (before == 0 && after > 0) {
                    Bukkit.getOnlinePlayers().forEach(p ->
                            p.playSound(
                                    p.getLocation(),
                                    "custom.harvest_ballad",
                                    SoundCategory.AMBIENT,
                                    1.0f, 1.0f
                            )
                    );
                }

                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 0.5f);

            }

            // Roll for custom harvest rewards (seeders, smithing items, pets)
            handleHarvestRewards(block, player, blockType);

            handleRareItemDrop(block, player, blockType);
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
                if (countFestivalBees(world) == 0) {
                    // no more bees → stop the music
                    stopAllHarvestBallads();
                }
            });
        }
    }
    private void stopAllHarvestBallads() {
        Bukkit.getOnlinePlayers().forEach(Player::stopAllSounds);
    }
    private long countFestivalBees(World world) {
        return world.getEntities().stream()
                .filter(e -> {
                    String name = e.getCustomName();
                    return name != null
                            && name.contains(ChatColor.GOLD + "Festival");
                })
                .count();

    private void handleHarvestRewards(Block block, Player player, Material blockType) {
        double roll = random.nextDouble();
        Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);

        switch (blockType) {
            case WHEAT -> {
                if (roll < 0.50) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getWheatSeeder());
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getWheatSeeder();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getEnchantedHayBale());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedHayBale();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    player.sendMessage(ChatColor.GOLD + "You got the Scarecrow pet!");
                }
            }
            case CARROTS -> {
                if (roll < 0.50) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getCarrotSeeder());
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getCarrotSeeder();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getEnchantedGoldenCarrot());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getEnchantedGoldenCarrot();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    player.sendMessage(ChatColor.GOLD + "You got the Killer Rabbit pet!");
                }
            }
            case BEETROOTS -> {
                if (roll < 0.50) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getBeetrootSeeder());
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getBeetrootSeeder();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getHeartRoot());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getHeartRoot();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    player.sendMessage(ChatColor.GOLD + "You got the Baron pet!");
                }
            }
            case POTATOES -> {
                if (roll < 0.50) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getPotatoSeeder());
                } else if (roll < 0.80) {
                    ItemStack item = ItemRegistry.getPotatoSeeder();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getImmortalPotato());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getImmortalPotato();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    player.sendMessage(ChatColor.GOLD + "You got the Mole pet!");
                }
            }
            case MELON -> {
                if (roll < 0.50) {
                    ItemStack item = new ItemStack(Material.MELON_SLICE, 16);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.80) {
                    ItemStack item = new ItemStack(Material.MELON_SLICE, 64);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getWatermelon());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getWatermelon();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getWorldsLargestWatermelon());
                }
            }
            case PUMPKIN -> {
                if (roll < 0.50) {
                    ItemStack item = new ItemStack(Material.PUMPKIN, 16);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.80) {
                    ItemStack item = new ItemStack(Material.PUMPKIN, 64);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else if (roll < 0.90) {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getJackOLantern());
                } else if (roll < 0.975) {
                    ItemStack item = ItemRegistry.getJackOLantern();
                    item.setAmount(4);
                    block.getWorld().dropItemNaturally(dropLoc, item);
                } else {
                    block.getWorld().dropItemNaturally(dropLoc, ItemRegistry.getWorldsLargestPumpkin());
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

                    // Optional: Send a message to the player
                    player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ A rare item has dropped!");
                } else {
                    // Log a warning if the rare item is not defined
                    plugin.getLogger().warning("Rare item is not defined in ItemRegistry.getRareItem(Material). Crop: " + blockType.name());
                }
            }
        }
    }

}
