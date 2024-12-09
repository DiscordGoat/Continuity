package goat.minecraft.minecraftnew.subsystems.forestry;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.utils.SpawnMonsters;
import goat.minecraft.minecraftnew.subsystems.utils.XPManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ForestSpiritManager implements Listener {

    private final MinecraftNew plugin;
    private final XPManager xpManager;
    private final Random random = new Random();

    // Define a constant for the metadata key
    private static final String PLAYER_PLACED_METADATA_KEY = "playerPlaced";

    // List of logs and stems to monitor
    public static final List<Material> logsAndStems = Arrays.asList(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            Material.MANGROVE_LOG,
            Material.CHERRY_LOG,
            Material.STRIPPED_MANGROVE_LOG // Ensure all relevant types are included
    );

    // Constructor
    public ForestSpiritManager(MinecraftNew plugin) {
        this.plugin = plugin;
        this.xpManager = new XPManager(plugin);
    }

    /**
     * Event handler for when a player places a block.
     * Marks log and stem blocks as player-placed using metadata.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        if (logsAndStems.contains(block.getType())) {
            // Mark the block as player-placed
            block.setMetadata(PLAYER_PLACED_METADATA_KEY, new FixedMetadataValue(plugin, true));
        }
    }

    /**
     * Event handler for when a player breaks a block.
     * Differentiates between player-placed and natural logs, awards XP, and spawns spirits.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Check if the block was player-placed
        if (block.hasMetadata(PLAYER_PLACED_METADATA_KEY)) {
            // Remove the metadata to clean up
            block.removeMetadata(PLAYER_PLACED_METADATA_KEY, plugin);
            return; // Do not award XP or spawn spirits
        }

        // Check if the block is a log or stem
        if (!logsAndStems.contains(block.getType())) {
            return; // Not a log or stem, ignore
        }

        // Award XP based on the log type
        int xpAwarded = getXPAwarded(block.getType());
        xpManager.addXP(player, "Forestry", xpAwarded);

        // Apply Haste effect based on the player's Forestry level
        grantHaste(player, "Forestry");

        // Chance to receive double logs based on the player's level
        if (random.nextInt(100) + 1 <= xpManager.getPlayerLevel(player, "Forestry")) { // Probability based on level
            player.getInventory().addItem(new ItemStack(block.getType()));
            player.getInventory().addItem(new ItemStack(block.getType()));
            player.playSound(player.getLocation(), Sound.BLOCK_NETHERRACK_BREAK, 100, 100);
        }

        // 1% chance to receive a Brewing Apple
        if (random.nextInt(100) + 1 <= 1) { // 1% chance
            ItemStack brewingApple = CustomItemManager.createCustomItem(
                    Material.APPLE,
                    ChatColor.GOLD + "Perfect Apple",
                    Arrays.asList(
                            ChatColor.GRAY + "An apple a day...",
                            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "A rare consumable that heals and feeds.",
                            ChatColor.DARK_PURPLE + "Artifact"
                    ),
                    1,
                    true, // Unbreakable
                    true  // Add enchantment shimmer
            );
            player.getInventory().addItem(brewingApple);
            player.sendMessage(ChatColor.YELLOW + "You received a healing Apple!");
        }

        // 0.5% chance to receive a "Secrets of Infinity" Arrow
        if (random.nextInt(1200) + 1 == 1) { // 0.5% chance
            ItemStack secretsOfInfinity = CustomItemManager.createCustomItem(
                    Material.ARROW,
                    ChatColor.DARK_PURPLE + "Secrets of Infinity",
                    Arrays.asList(
                            ChatColor.GRAY + "A piece of wood imbued with knowledge.",
                            ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Adds 1 level of Infinity.",
                            ChatColor.DARK_PURPLE + "Smithing Ingredient"
                    ),
                    1,
                    true, // Unbreakable
                    true  // Add enchantment shimmer
            );
            player.getInventory().addItem(secretsOfInfinity);
            player.sendMessage(ChatColor.YELLOW + "You received Secrets of Infinity!");
        }

        // 1% chance to spawn a Forest Spirit
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);
        if(activePet !=null && activePet.hasPerk(PetManager.PetPerk.SKEPTICISM)){
            if (random.nextInt(100) < 2) { // 1% chance
                spawnSpirit(block.getType(), player.getLocation(), player);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "A Forest Spirit has been summoned by your pet!");
            }
        }
        if(activePet !=null && activePet.hasPerk(PetManager.PetPerk.CHALLENGE)){
            if (random.nextInt(100) < 5) { // 1% chance
                spawnSpirit(block.getType(), player.getLocation(), player);
                player.sendMessage(ChatColor.LIGHT_PURPLE + "A Forest Spirit has been summoned by your pet!");
            }
        }
        if (random.nextInt(100) < 1) { // 1% chance
            spawnSpirit(block.getType(), player.getLocation(), player);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "A Forest Spirit has been summoned!");
        }
    }

    /**
     * Grants Haste effect to the player based on their Forestry level.
     *
     * @param player The player to grant the effect to.
     * @param skill  The skill name, e.g., "Forestry".
     */
    private void grantHaste(Player player, String skill) {
        int level = xpManager.getPlayerLevel(player, skill);
        if (random.nextInt(100) + 1 >= 90) { // 10% chance to grant Haste
            int hasteLevel = 1; // Haste level increases every 33 levels, max level 2
            int duration = 200 + (level * 5); // Duration scales with level (in ticks)

            // Apply Haste effect
            player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, duration, hasteLevel), true);
            player.sendMessage(ChatColor.YELLOW + "Haste activated! +" + (hasteLevel + 1) + " Haste.");
            player.playSound(player.getLocation(), Sound.BLOCK_DEEPSLATE_STEP, 1.0f, 1.0f);
        }
    }

    /**
     * Spawns a Forest Spirit based on the type of log broken.
     *
     * @param blockType The type of the block broken.
     * @param location  The location to spawn the spirit.
     * @param player    The player who broke the block.
     */
    private void spawnSpirit(Material blockType, Location location, Player player) {
        SpiritType spiritType = getSpiritType(blockType);
        if (spiritType == null) {
            return;
        }

        SpawnMonsters spawnMonsters = new SpawnMonsters(plugin, xpManager);
        World world = location.getWorld();
        if (world == null) return;

        // Spawn the Skeleton entity as the spirit
        Skeleton spirit = (Skeleton) world.spawnEntity(location, EntityType.SKELETON);
        spawnMonsters.applyMobAttributes(spirit, spiritType.level); // Apply custom attributes
        spirit.setCustomName(spiritType.getDisplayName());
        spirit.setCustomNameVisible(true);
        spirit.setPersistent(true); // Prevent despawning

        // Equip armor and weapons
        EntityEquipment equipment = spirit.getEquipment();
        if (equipment != null) {
            equipment.setHelmet(createColoredArmor(Material.LEATHER_HELMET, spiritType.getArmorColor()));
            equipment.setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, spiritType.getArmorColor()));
            equipment.setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, spiritType.getArmorColor()));
            equipment.setBoots(createColoredArmor(Material.LEATHER_BOOTS, spiritType.getArmorColor()));
            equipment.setItemInMainHand(CustomItemManager.createCustomItem(
                    Material.BOW,
                    ChatColor.WHITE + "Spirit Bow",
                    Arrays.asList(ChatColor.GRAY + "A bow wielded by the spirit."),
                    1,
                    false, // Unbreakable
                    true   // Add enchantment shimmer
            ));

            // Prevent equipment from dropping upon death
            equipment.setHelmetDropChance(0.0f);
            equipment.setChestplateDropChance(0.0f);
            equipment.setLeggingsDropChance(0.0f);
            equipment.setBootsDropChance(0.0f);
            equipment.setItemInMainHandDropChance(0.0f);
        }

        // Play sound and spawn particles to indicate spirit spawning
        world.playSound(location, Sound.BLOCK_BAMBOO_WOOD_BREAK, 1.0f, 1.0f);
        world.spawnParticle(Particle.EXPLOSION_LARGE, location, 1);
    }

    /**
     * Creates colored leather armor using the CustomItemManager.
     *
     * @param material The type of armor piece.
     * @param color    The color to apply.
     * @return The customized armor ItemStack.
     */
    private ItemStack createColoredArmor(Material material, Color color) {
        ItemStack armor = CustomItemManager.createCustomItem(
                material,
                null, // No custom name
                new ArrayList<>(), // No lore
                1,
                false, // Not unbreakable
                false  // No enchantment shimmer
        );

        // Apply color if the armor is leather
        if (material.toString().startsWith("LEATHER_")) {
            LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
            if (meta != null) {
                meta.setColor(color);
                armor.setItemMeta(meta);
            }
        }

        return armor;
    }

    /**
     * Determines the type of spirit to spawn based on the block type.
     *
     * @param blockType The type of the block broken.
     * @return The corresponding SpiritType or null if none matches.
     */
    private SpiritType getSpiritType(Material blockType) {
        switch (blockType) {
            case OAK_LOG:
            case STRIPPED_OAK_LOG:
                return SpiritType.OAK_SPIRIT;
            case SPRUCE_LOG:
            case STRIPPED_SPRUCE_LOG:
                return SpiritType.SPRUCE_SPIRIT;
            case BIRCH_LOG:
            case STRIPPED_BIRCH_LOG:
                return SpiritType.BIRCH_SPIRIT;
            case JUNGLE_LOG:
            case STRIPPED_JUNGLE_LOG:
                return SpiritType.JUNGLE_SPIRIT;
            case ACACIA_LOG:
            case STRIPPED_ACACIA_LOG:
                return SpiritType.ACACIA_SPIRIT;
            case DARK_OAK_LOG:
            case STRIPPED_DARK_OAK_LOG:
                return SpiritType.DARK_OAK_SPIRIT;
            case CHERRY_LOG:
            case STRIPPED_CHERRY_LOG:
                return SpiritType.CHERRY_SPIRIT;
            case CRIMSON_STEM:
            case STRIPPED_CRIMSON_STEM:
                return SpiritType.CRIMSON_SPIRIT;
            case WARPED_STEM:
            case STRIPPED_WARPED_STEM:
                return SpiritType.WARPED_SPIRIT;
            default:
                return null;
        }
    }

    /**
     * Handles the death of a Forest Spirit.
     * Manages custom drops and awards additional Forestry XP.
     */
    @EventHandler
    public void onSpiritDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Skeleton)) {
            return; // Not a Skeleton, ignore
        }

        Skeleton spirit = (Skeleton) event.getEntity();
        String customName = spirit.getCustomName();
        if (customName == null) {
            return; // No custom name, not a Forest Spirit
        }

        // Determine the spirit type based on its custom name
        SpiritType spiritType = null;
        for (SpiritType type : SpiritType.values()) {
            if (customName.equals(type.getDisplayName())) {
                spiritType = type;
                break;
            }
        }

        if (spiritType == null) {
            return; // Not a recognized Forest Spirit
        }

        // Clear default drops
        event.getDrops().clear();

        // Always drop a Forbidden Book
        ItemStack forbiddenBook = CustomItemManager.createCustomItem(
                Material.WRITTEN_BOOK,
                ChatColor.YELLOW + "Forbidden Book",
                Arrays.asList(
                        ChatColor.GRAY + "A dangerous book full of experimental magic.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply to equipment to push the limits of enchantments.",
                        ChatColor.DARK_PURPLE + "Enchanting Item"
                ),
                1,
                false, // Not unbreakable
                true   // Add enchantment shimmer
        );
        if(random.nextBoolean()) {
            if(random.nextBoolean()) {
                if(random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        event.getDrops().add(forbiddenBook);
                    }
                }
            }
        }
        // 50% chance to drop the rare item

            ItemStack rareDrop = CustomItemManager.createCustomItem(
                    spiritType.getRareDropMaterial(),
                    ChatColor.GOLD + spiritType.getRareDropName(),
                    spiritType.getRareDropLore(),
                    1,
                    false, // Not unbreakable
                    true   // Add enchantment shimmer
            );
            event.getDrops().add(rareDrop);
        

        // Award additional Forestry XP to the player who killed the spirit
        if (event.getEntity().getKiller() != null) {
            Player player = event.getEntity().getKiller();
            int forestryLevel = xpManager.getPlayerLevel(player, "Forestry");
            int xpAwarded = 250; // Example calculation
            xpManager.addXP(player, "Forestry", xpAwarded);
            player.sendMessage(ChatColor.AQUA + "You gained " + xpAwarded + " Forestry XP from defeating the spirit!");
        }
    }
    public int getPlayerForestryLevel(Player player){
        int forestryLevel = xpManager.getPlayerLevel(player, "Forestry");
        return forestryLevel;
    }
    /**
     * Defines different types of Forest Spirits with unique attributes.
     */
    private enum SpiritType {

        OAK_SPIRIT("Oak Spirit", ChatColor.GREEN + "Oak Spirit of the Forest", 20, Color.fromRGB(102, 51, 0), "Petrified Log",
                Arrays.asList(
                        ChatColor.GRAY + "A hardened log infused with forest magic.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.OAK_WOOD),
        SPRUCE_SPIRIT("Spruce Spirit", ChatColor.DARK_GREEN + "Spruce Spirit of the Forest", 30, Color.fromRGB(51, 25, 0), "Pinecone",
                Arrays.asList(
                        ChatColor.GRAY + "A pinecone that never withers.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Thorns.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.SPRUCE_SAPLING),
        BIRCH_SPIRIT("Birch Spirit", ChatColor.YELLOW + "Birch Spirit of the Forest", 20, Color.fromRGB(255, 255, 204), "Birch Strip",
                Arrays.asList(
                        ChatColor.GRAY + "A smooth strip from birch wood.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Projectile Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.PAPER),
        JUNGLE_SPIRIT("Jungle Spirit", ChatColor.GOLD + "Jungle Spirit of the Forest", 40, Color.LIME, "Humid Bark",
                Arrays.asList(
                        ChatColor.GRAY + "Bark soaked in jungle moisture.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Feather Falling.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.JUNGLE_LOG),
        ACACIA_SPIRIT("Acacia Spirit", ChatColor.GOLD + "Acacia Spirit of the Forest", 40, Color.ORANGE, "Acacia Gum",
                Arrays.asList(
                        ChatColor.GRAY + "Sticky gum from acacia trees.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Resistance.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.HONEY_BOTTLE),
        DARK_OAK_SPIRIT("Dark Oak Spirit", ChatColor.DARK_GRAY + "Dark Oak Spirit of the Forest", 40, Color.BLACK, "Acorn",
                Arrays.asList(
                        ChatColor.GRAY + "An acorn with dark energy.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Blast Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.BEETROOT_SEEDS), // Using beetroot seeds as acorns
        CHERRY_SPIRIT("Cherry Spirit", ChatColor.LIGHT_PURPLE + "Cherry Spirit of the Forest", 40, Color.FUCHSIA, "Cherry Blossom",
                Arrays.asList(
                        ChatColor.GRAY + "A delicate cherry blossom.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Unbreaking +II.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.PINK_TULIP), // Using pink tulip as cherry blossom
        CRIMSON_SPIRIT("Crimson Spirit", ChatColor.RED + "Crimson Spirit of the Forest", 40, Color.RED, "Maple Bark",
                Arrays.asList(
                        ChatColor.GRAY + "Bark imbued with crimson essence.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Fire Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.CRIMSON_HYPHAE),
        WARPED_SPIRIT("Warped Spirit", ChatColor.DARK_AQUA + "Warped Spirit of the Forest", 40, Color.TEAL, "Blue Nether Wart",
                Arrays.asList(
                        ChatColor.GRAY + "A warped growth from the Nether.",
                        ChatColor.BLUE + "Use: " + ChatColor.GRAY + "Apply it to equipment to unlock the secrets of Blast Protection.",
                        ChatColor.DARK_PURPLE + "Smithing Item"
                ),
                Material.NETHER_WART);

        private final String name;
        private final String displayName;
        private final int level;
        private final Color armorColor;
        private final String rareDropName;
        private final List<String> rareDropLore;
        private final Material rareDropMaterial;

        SpiritType(String name, String displayName, int level, Color armorColor, String rareDropName, List<String> rareDropLore, Material rareDropMaterial) {
            this.name = name;
            this.displayName = displayName;
            this.level = level;
            this.armorColor = armorColor;
            this.rareDropName = rareDropName;
            this.rareDropLore = rareDropLore;
            this.rareDropMaterial = rareDropMaterial;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getLevel() {
            return level;
        }

        public Color getArmorColor() {
            return armorColor;
        }

        public String getRareDropName() {
            return rareDropName;
        }

        public List<String> getRareDropLore() {
            return rareDropLore;
        }

        public Material getRareDropMaterial() {
            return rareDropMaterial;
        }
    }

    /**
     * Determines XP awarded based on the type of log broken.
     *
     * @param logType The type of log broken.
     * @return The amount of XP to award.
     */
    private int getXPAwarded(Material logType) {
        switch (logType) {
            case OAK_LOG:
                return 5;
            case SPRUCE_LOG:
                return 6;
            case BIRCH_LOG:
                return 5;
            case JUNGLE_LOG:
                return 7;
            case ACACIA_LOG:
                return 8;
            case DARK_OAK_LOG:
                return 8;
            case CHERRY_LOG:
                return 8;
            case CRIMSON_STEM:
                return 10;
            case WARPED_STEM:
                return 12;
            case MANGROVE_LOG:
            case STRIPPED_MANGROVE_LOG:
                return 10;
            default:
                return 17;
        }
    }
}
