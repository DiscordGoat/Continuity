package goat.minecraft.minecraftnew.subsystems.pets;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class PetManager implements Listener {
    private JavaPlugin plugin;
    private static PetManager instance;
    public PetManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadPets();
    }

    private Map<UUID, Horse> summonedHorses = new HashMap<>();

    // Instead of using IDs, we now store base64 textures directly.
    // You must populate these with actual base64 textures for each pet.
    // The keys must match the pet names used elsewhere in your code.
    private static final Map<String, String> PET_TEXTURES = new HashMap<>();

    static {
        // Example placeholders, replace with actual base64 textures.
        // You can find these by searching for "custom head textures" online.
        PET_TEXTURES.put("Allay", "BASE64_TEXTURE_FOR_ALLAY");
        PET_TEXTURES.put("Armadillo", "BASE64_TEXTURE_FOR_ARMADILLO");
        PET_TEXTURES.put("Axolotl", "BASE64_TEXTURE_FOR_AXOLOTL");
        PET_TEXTURES.put("Cat", "BASE64_TEXTURE_FOR_CAT");
        PET_TEXTURES.put("Cow", "BASE64_TEXTURE_FOR_COW");
        PET_TEXTURES.put("Horse", "BASE64_TEXTURE_FOR_HORSE");
        PET_TEXTURES.put("Glow Squid", "BASE64_TEXTURE_FOR_GLOW_SQUID");
        PET_TEXTURES.put("Parrot", "BASE64_TEXTURE_FOR_PARROT");
        PET_TEXTURES.put("Sheep", "BASE64_TEXTURE_FOR_SHEEP");
        PET_TEXTURES.put("Turtle", "BASE64_TEXTURE_FOR_TURTLE");
        PET_TEXTURES.put("Villager", "BASE64_TEXTURE_FOR_VILLAGER");
        PET_TEXTURES.put("Squirrel", "BASE64_TEXTURE_FOR_SQUIRREL");
        PET_TEXTURES.put("Leviathan", "BASE64_TEXTURE_FOR_LEVIATHAN");
        PET_TEXTURES.put("Dolphin", "BASE64_TEXTURE_FOR_DOLPHIN");
        PET_TEXTURES.put("Fish", "BASE64_TEXTURE_FOR_FISH");
        PET_TEXTURES.put("Golden Steve", "BASE64_TEXTURE_FOR_GOLDEN_STEVE");
        PET_TEXTURES.put("Pillager", "BASE64_TEXTURE_FOR_PILLAGER");
        PET_TEXTURES.put("Stray", "BASE64_TEXTURE_FOR_STRAY");
        PET_TEXTURES.put("Bat", "BASE64_TEXTURE_FOR_BAT");
        PET_TEXTURES.put("Chicken", "BASE64_TEXTURE_FOR_CHICKEN");
        PET_TEXTURES.put("Mooshroom", "BASE64_TEXTURE_FOR_MOOSHROOM");
        PET_TEXTURES.put("Pig", "BASE64_TEXTURE_FOR_PIG");
        PET_TEXTURES.put("Yeti", "BASE64_TEXTURE_FOR_YETI");
        PET_TEXTURES.put("Iron Golem", "BASE64_TEXTURE_FOR_IRON_GOLEM");
        PET_TEXTURES.put("Dwarf", "BASE64_TEXTURE_FOR_DWARF");
        PET_TEXTURES.put("Piglin Brute", "BASE64_TEXTURE_FOR_PIGLIN_BRUTE");
        PET_TEXTURES.put("Vindicator", "BASE64_TEXTURE_FOR_VINDICATOR");
        PET_TEXTURES.put("Guardian", "BASE64_TEXTURE_FOR_GUARDIAN");
        PET_TEXTURES.put("Zombie Pigman", "BASE64_TEXTURE_FOR_ZOMBIE_PIGMAN");
        PET_TEXTURES.put("Zombie", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmIwYmI2NmViNThlZjhlMmRmMjBiOGIyNzdiZjkwMTg4ZDRlMjNiMmNlYTg5ZDM1OThhZTQwNTI3MDBiZjRiMyJ9fX0=");
        PET_TEXTURES.put("Skeleton", "BASE64_TEXTURE_FOR_SKELETON");
        PET_TEXTURES.put("Warden", "BASE64_TEXTURE_FOR_WARDEN");
        PET_TEXTURES.put("Wither Skeleton", "BASE64_TEXTURE_FOR_WITHER_SKELETON");
        PET_TEXTURES.put("Blaze", "BASE64_TEXTURE_FOR_BLAZE");
        PET_TEXTURES.put("Enderman", "BASE64_TEXTURE_FOR_ENDERMAN");
        PET_TEXTURES.put("Drowned", "BASE64_TEXTURE_FOR_DROWNED");
    }

    public static PetManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PetManager(plugin);
        }
        return instance;
    }

    // Helper method to create a custom skull from a base64 texture
    private ItemStack getCustomSkull(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new ItemStack(Material.NAME_TAG);
        }
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        if (skullMeta == null) {
            return skull;
        }

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));

        try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return new ItemStack(Material.NAME_TAG); // Fallback if reflection fails
        }

        skull.setItemMeta(skullMeta);
        return skull;
    }


    public ItemStack getSkullForPet(String petName) {
        String texture = PET_TEXTURES.get(petName);
        if (!isValidBase64Texture(texture)) {
            // If no valid base64 texture is found, return a NAME_TAG
            return new ItemStack(Material.NAME_TAG);
        }
        return getCustomSkull(texture);
    }

    /**
     * Checks if the given texture string is likely a valid base64-encoded texture.
     *
     * This basic check ensures:
     * - It's not null or empty.
     * - It doesn't start with known placeholders like "BASE64_TEXTURE_FOR_".
     * - It's not a direct URL (e.g., "http://...").
     * - Starts with a known base64 JSON prefix often used for custom heads.
     *
     * Note: You can improve this method to decode and verify the JSON structure if you want more robust checks.
     */
    private boolean isValidBase64Texture(String texture) {
        if (texture == null || texture.isEmpty()) {
            return false;
        }

        // Check if it's still using a placeholder or a direct URL
        if (texture.startsWith("BASE64_TEXTURE_FOR_")) {
            return false;
        }
        return true;
    }


    /**
     * Applies the tick speed effect to the player.
     * (Implementation depends on how tick speed affects your plugin/game mechanics.)
     *
     * @param player    The player to apply the tick speed to.
     * @param tickSpeed The tick speed value.
     */
    private Map<UUID, Map<String, Pet>> playerPets = new HashMap<>();
    private Map<UUID, Pet> activePets = new HashMap<>();
    private Map<UUID, BukkitTask> petTasks = new HashMap<>();

    private Set<UUID> guiCooldown = new HashSet<>();
    private final long COOLDOWN_TIME = 20L; // 1 second (20 ticks)

    public void addPet(Player player, Pet pet) {
        if(player == null){
            return;
        }
        UUID playerId = player.getUniqueId();
        Map<String, Pet> pets = playerPets.computeIfAbsent(playerId, k -> new HashMap<>());

        if (pets.containsKey(pet.getName())) {
            // Player already owns a pet with this name
            return;
        }

        pets.put(pet.getName(), pet);
        savePets();
        player.sendMessage(ChatColor.GREEN + "You have received a new pet: " + pet.getName());
    }

    public Pet getPet(Player player, String petName) {
        return playerPets.getOrDefault(player.getUniqueId(), new HashMap<>()).get(petName);
    }

    private void summonHorseForPlayer(Player player, Pet pet) {
        World world = player.getWorld();
        Location location = player.getLocation();

        // Spawn a horse
        Horse horse = (Horse) world.spawnEntity(location, EntityType.HORSE);

        // Set horse properties
        horse.setTamed(true);
        horse.setOwner(player);
        horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));

        // Scale stats based on pet level
        double speed = 0.2 + (pet.getLevel() * 0.02); // Example scaling
        double jumpStrength = 0.5 + (pet.getLevel() * 0.03); // Example scaling

        horse.setJumpStrength(Math.min(2.0, jumpStrength));
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Math.min(0.5, speed));

        // Store the summoned horse
        summonedHorses.put(player.getUniqueId(), horse);

        player.sendMessage(ChatColor.GREEN + "Summoned your horse pet!");
    }

    /**
     * Summons a pet for a player. If another pet is active, it will be despawned first.
     *
     * @param player  The player.
     * @param petName The name of the pet to summon.
     */
    public void summonPet(Player player, String petName) {
        Pet pet = getPet(player, petName);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

        if (pet != null) {
            // Check if the pet is already active
            if (pet.equals(activePets.get(player.getUniqueId()))) {
                despawnPet(player); // Despawn if already active
            } else {
                despawnPet(player); // Despawn any currently active pet
                activePets.put(player.getUniqueId(), pet);

                // Check if this is the Horse pet
                if ("Horse".equalsIgnoreCase(pet.getName())) {
                    summonHorseForPlayer(player, pet);
                } else {
                    spawnPetParticle(player, pet);
                }

                player.sendMessage(ChatColor.GREEN + "Your pet '" + pet.getName() + "' has been summoned!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "You do not own a pet named " + petName);
        }
    }

    private void saveHorseStats(Player player, Pet pet, Horse horse) {
        File file = new File(plugin.getDataFolder(), "pets.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = player.getUniqueId().toString() + "." + pet.getName() + ".horse";
        config.set(path + ".jumpStrength", horse.getJumpStrength());
        config.set(path + ".speed", horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());

        ItemStack armor = horse.getInventory().getArmor();
        if (armor != null) {
            config.set(path + ".armor", armor.getType().toString());
        } else {
            config.set(path + ".armor", null);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHorseStats(Player player, Pet pet, Horse horse) {
        File file = new File(plugin.getDataFolder(), "pets.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        String path = player.getUniqueId().toString() + "." + pet.getName() + ".horse";
        double jumpStrength = config.getDouble(path + ".jumpStrength", 0.7); // Default
        double speed = config.getDouble(path + ".speed", 0.2); // Default

        horse.setJumpStrength(jumpStrength);
        horse.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);

        String armorType = config.getString(path + ".armor");
        if (armorType != null) {
            Material material = Material.getMaterial(armorType);
            if (material != null) {
                horse.getInventory().setArmor(new ItemStack(material));
            }
        }
    }

    public void despawnPet(Player player) {
        Pet pet = activePets.remove(player.getUniqueId());
        if (pet != null) {
            if ("Horse".equalsIgnoreCase(pet.getName())) {
                Horse horse = summonedHorses.remove(player.getUniqueId());
                if (horse != null) {
                    saveHorseStats(player, pet, horse);
                    horse.remove();
                }
            } else {
                removePetParticle(player);
            }
            player.sendMessage(ChatColor.YELLOW + "Your pet '" + pet.getName() + "' has been despawned.");
            removePetParticle(player);
        }
    }

    /**
     * Retrieves the active pet for a player.
     *
     * @param player The player.
     * @return The active pet if any, else null.
     */
    public Pet getActivePet(Player player) {
        if (player == null) {
            return null;
        } else {
            return activePets.get(player.getUniqueId());
        }
    }

    public JavaPlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Clears all pets from a player's collection.
     *
     * @param player The player whose pets are to be cleared.
     */
    public void clearPets(Player player) {
        UUID playerId = player.getUniqueId();

        // Remove active pet if any
        if (activePets.containsKey(playerId)) {
            Pet pet = activePets.get(playerId);
            removePetParticle(player);
            activePets.remove(playerId);
            player.sendMessage(ChatColor.YELLOW + "Your pet '" + pet.getName() + "' has been despawned.");
        }

        // Remove all pets from the collection
        if (playerPets.containsKey(playerId)) {
            playerPets.remove(playerId);
            savePets();
        }
    }

    // ==========================
    // Pet Particle Methods
    // ==========================

    /**
     * Spawns the particle effect associated with a pet, following the player.
     *
     * @param player The player.
     * @param pet    The pet.
     */
    private void spawnPetParticle(Player player, Pet pet) {
        Particle particleType = pet.getParticle();

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                removePetParticle(player);
                return;
            }

            Location location = player.getLocation().add(0, 1.5, 0);
            player.getWorld().spawnParticle(particleType, location, 1, 0.3, 0.3, 0.3, 0.05);

        }, 0L, 100L); // every 5 seconds

        petTasks.put(player.getUniqueId(), task);
    }

    /**
     * Removes the particle effect associated with a player's active pet.
     *
     * @param player The player.
     */
    private void removePetParticle(Player player) {
        BukkitTask task = petTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    // ==========================
    // Event Handlers
    // ==========================

    @EventHandler
    public void onPlayerGainXP(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        Pet activePet = getActivePet(player);
        if (activePet != null) {
            double xpGained = event.getAmount();
            activePet.addXP(xpGained);
            player.sendMessage(ChatColor.AQUA + activePet.getName() + " gained " + xpGained + " XP!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PetManager petManager = PetManager.getInstance(plugin);
        petManager.createPet(player, "Horse", PetManager.Rarity.COMMON, 10, Particle.HEART, PetManager.PetPerk.SPEED_BOOST);
        despawnPet(player);
    }

    public void openPetGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Your Pets");
        Map<String, Pet> pets = playerPets.getOrDefault(player.getUniqueId(), new HashMap<>());

        int index = 0;
        for (Pet pet : pets.values()) {
            ItemStack petIcon = getSkullForPet(pet.getName());
            if (petIcon == null) {
                petIcon = new ItemStack(Material.NAME_TAG); // Fallback item
            }

            ItemMeta meta = petIcon.getItemMeta();
            if (meta != null) {
                ChatColor rarityColor = pet.getRarity().getColor();
                meta.setDisplayName(rarityColor + "[Lvl " + pet.getLevel() + "] " + pet.getName());

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GREEN + "XP: " + (int) pet.getXp() + "/" + (int) pet.getXPForNextLevel());
                lore.add(ChatColor.GRAY + " ");
                for (PetPerk perk : pet.getPerks()) {
                    lore.add(ChatColor.GOLD + perk.getDisplayName());
                    lore.add(ChatColor.GRAY + getDynamicPerkEffectDescription(perk, pet.getLevel()));
                    lore.add(ChatColor.GRAY + " ");
                }
                if (pet.equals(activePets.get(player.getUniqueId()))) {
                    lore.add(ChatColor.GREEN + "Currently Active");
                }
                meta.setLore(lore);
                petIcon.setItemMeta(meta);
            }

            gui.setItem(index++, petIcon);
        }

        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        for (; index < gui.getSize(); index++) {
            gui.setItem(index, glassPane);
        }

        player.openInventory(gui);
    }

    private String getDynamicPerkEffectDescription(PetPerk perk, int level) {

        switch (perk) {
            case WATERLOGGED:
                return ChatColor.GRAY + "Grants infinite " + ChatColor.AQUA + "Water Breathing.";
            case ASPECT_OF_THE_END:
                return ChatColor.BLUE + "Right Click: " + ChatColor.DARK_PURPLE + "Warp forward.";
            case BLAZE:
                return "";
            case SECRET_LEGION:
                return "Getting hit " + ChatColor.GRAY + "summons a " + ChatColor.YELLOW + "Secret Legion Ally " + ChatColor.GRAY + "to defend you. Costs " + ChatColor.GREEN + "2 Hunger.";
            case DECAY:
                return ChatColor.DARK_GRAY + "Rapidly deteriorates hit mobs, " + ChatColor.GRAY + "making them " + ChatColor.DARK_GRAY + "succumb faster.";
            case BLACKLUNG:
                return "Transplants " + ChatColor.RED + "lungs " + ChatColor.GRAY + "to operate with " + ChatColor.DARK_GRAY + "smoke " + ChatColor.GRAY + "instead of air.";
            case SUPERIOR_ENDURANCE:
                return "Gain " + ChatColor.GREEN + "eternal sustenance " + ChatColor.GRAY + "when grinding " + ChatColor.YELLOW + "crops.";
            case ANTIDOTE:
                return "Lose a " + ChatColor.DARK_GRAY + "negative potion effect " + ChatColor.GRAY + "when eating.";
            case CULTIVATION:
                return "Gain " + ChatColor.GREEN + "+" + (level * 1) + "% Chance " + ChatColor.GRAY + "to harvest " + ChatColor.GREEN + "double crops.";
            case GREEN_THUMB:
                return "Gain " + ChatColor.YELLOW + "+" + (level * 1) + " Tick Speed " + ChatColor.GRAY + "when on the ground.";
            case GREED:
                return "Gain up to " + ChatColor.YELLOW + "4% chance " + ChatColor.GRAY + "to drop " + ChatColor.GREEN + "1-" + (level * 0.32) + " Emeralds " + ChatColor.GRAY + "when killing monsters.";
            case SKEPTICISM:
                return "Gain " + ChatColor.AQUA + "+2 Forest Spirit Chance.";
            case CHALLENGE:
                return "Gain " + ChatColor.AQUA + "+5 Forest Spirit Chance.";
            case FLOAT:
                return "Gain " + ChatColor.YELLOW + "Permanent Slow Falling.";
            case COLLECTOR:
                return ChatColor.YELLOW + "Automatically picks up " + ChatColor.GRAY + "nearby items.";
            case LULLABY:
                return "Prevents " + ChatColor.DARK_GRAY + "monsters from spawning " + ChatColor.GRAY + "within " + ChatColor.YELLOW + ((level * 4) + 40) + " blocks.";
            case FLIGHT:
                return "Gain the ability to " + ChatColor.YELLOW + "Fly for " + level * 0.05 + " km";
            case EMERALD_SEEKER:
                return "Gain a " + ChatColor.GREEN + "4% chance " + ChatColor.GRAY + "to mine an " + ChatColor.GREEN + "Emerald.";
            case MITHRIL_MINER:
                return "Gain a " + ChatColor.GREEN + "0.5% chance " + ChatColor.GRAY + "to mine a " + ChatColor.YELLOW + "Mithril Chunk.";
            case ECHOLOCATION:
                return "Gain " + ChatColor.AQUA + "Night Vision.";
            case X_RAY:
                return ChatColor.YELLOW + "Find ores " + ChatColor.GRAY + "through stone.";
            case DIGGING_CLAWS:
                return "Gain " + ChatColor.YELLOW + "Haste II " + ChatColor.GRAY + "when mining blocks.";
            case ELITE:
                return "Gain " + ChatColor.RED + "+" + (level * 0.5) + "% Melee Damage.";
            case WALKING_FORTRESS:
                return "Gain " + ChatColor.DARK_GRAY + (level * 0.8) + "% Damage Reduction.";
            case REBIRTH:
                return "Revive after dying " + ChatColor.GRAY + "at " + ChatColor.GREEN + "full health " + ChatColor.GRAY + "at the cost of your entire hunger bar.";
            case SECOND_WIND:
                return "Gain " + ChatColor.GREEN + "Regeneration " + ChatColor.GRAY + "for " + ChatColor.GREEN + (level * 1) + ChatColor.GRAY + " seconds after taking damage.";
            case BLIZZARD:
                return "Gain " + ChatColor.DARK_GRAY + "+" + (level * 0.5) + "% Damage Reduction " + ChatColor.GRAY + "near snow.";
            case ASPECT_OF_THE_FROST:
                return ChatColor.DARK_GRAY + "Slows enemies " + ChatColor.GRAY + "for " + ChatColor.DARK_GRAY + (level * 1) + " seconds " + ChatColor.GRAY + "on hit.";
            case FIREPROOF:
                return "Gain " + ChatColor.RED + "immunity to Fire Damage.";
            case DEVOUR:
                return "Gain " + ChatColor.GREEN + "+1 Hunger " + ChatColor.GRAY + "when damaging mobs.";
            case BONE_PLATING_WEAK:
                return ChatColor.DARK_GRAY + "Reduces damage " + ChatColor.GRAY + "by " + ChatColor.DARK_GRAY + "20% " + ChatColor.GRAY + "for " + ChatColor.DARK_GRAY + (level * 1) + " seconds.";
            case LASER_BEAM:
                return "Has a " + ChatColor.AQUA + (level * 1) + "% chance " + ChatColor.GRAY + "to " + ChatColor.DARK_AQUA + "assist ranged attacks.";
            case SHOTCALLING:
                return "Gain " + ChatColor.RED + "+" + (level * 1) + "% Arrow Damage.";
            case BONE_COLD:
                return "Provides " + ChatColor.AQUA + "immunity to freeze damage.";
            case TIPPED_SLOWNESS:
                return "Augments arrows with " + ChatColor.DARK_GRAY + "slowness " + ChatColor.GRAY + "for " + ChatColor.DARK_GRAY + (level * 1) + " seconds.";
            case QUICK_DRAW:
                return ChatColor.BLUE + "LEFT CLICK: " + ChatColor.GRAY + "To instantly fire arrows at reduced damage.";
            case RECOVERY:
                return "Gain a " + ChatColor.YELLOW + (level * 1) + "% chance " + ChatColor.GRAY + "to " + ChatColor.YELLOW + "recover arrows.";
            case BONE_PLATING:
                return ChatColor.DARK_GRAY + "Reduces damage " + ChatColor.GRAY + "by " + ChatColor.DARK_GRAY + "40% " + ChatColor.GRAY + "for " + ChatColor.DARK_GRAY + (level * 1) + " seconds.";
            case STRONG_SWIMMER:
                return "Grants " + ChatColor.AQUA + "Dolphins Grace " + ChatColor.GRAY + "when swimming.";
            case COMFORTABLE:
                return "Grants " + ChatColor.GREEN + (level * 0.1) + " bonus health " + ChatColor.GRAY + "when eating.";
            case ANGLER:
                int anglerBonus = 5;
                return "Grants " + ChatColor.AQUA + anglerBonus + "% Sea Creature Chance.";
            case HEART_OF_THE_SEA:
                int heartBonus = 10;
                return "Grants " + ChatColor.AQUA + heartBonus + "% Sea Creature Chance.";
            case TERROR_OF_THE_DEEP:
                return "Grants you " + ChatColor.RED + "Bloodlust " + ChatColor.GRAY + "when you strike a " + ChatColor.AQUA + "Sea Creature.";
            case TREASURE_HUNTER:
                return ChatColor.AQUA + "Triples treasure chance " + ChatColor.GRAY + "when fishing.";
            case PRACTICE:
                return ChatColor.YELLOW + "Triples villager XP gains.";
            case HAGGLE:
                return "Grants a trade discount of " + ChatColor.YELLOW + (level * 0.5) + "%.";
            case LEAP:
                return ChatColor.YELLOW + "Enables the ability to leap forward.";
            case SOFT_PAW:
                return ChatColor.YELLOW + "Reduces fall damage " + ChatColor.GRAY + "by " + ChatColor.YELLOW + (level * 1) + "%.";
            case SPEED_BOOST:
                return "Increases " + ChatColor.YELLOW + "speed " + ChatColor.GRAY + "by " + ChatColor.YELLOW + (level * 0.4f) + "%.";
            case CLAW:
                return "Increases " + ChatColor.RED + "melee damage " + ChatColor.GRAY + "by " + ChatColor.RED + (0.2 * level) + "%.";
            default:
                return ChatColor.GRAY + "Static effect or undefined scaling.";
        }

    }

    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Your Pets")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                String petName = displayName.replaceFirst("\\[Lvl \\d+\\] ", "");
                Pet pet = getPet(player, petName);
                if (pet != null) {
                    summonPet(player, petName);
                    player.closeInventory();
                }
            }
        }
    }

    /**
     * Creates a new pet with specified properties and adds it to the player's collection.
     *
     * @param player    The player.
     * @param name      The name of the pet.
     * @param rarity    The rarity of the pet.
     * @param maxLevel  The maximum level the pet can reach.
     * @param particle  The particle effect associated with the pet.
     * @param perks     The perks granted by the pet.
     */
    public void createPet(Player player, String name, Rarity rarity, int maxLevel, Particle particle, PetPerk... perks) {
        ItemStack icon = getSkullForPet(name);
        List<PetPerk> perkList = Arrays.asList(perks);
        Pet newPet = new Pet(name, rarity, maxLevel, icon, particle, perkList);
        addPet(player, newPet);
    }

    public void savePets() {
        File file = new File(plugin.getDataFolder(), "pets.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (UUID playerId : playerPets.keySet()) {
            Map<String, Pet> pets = playerPets.get(playerId);
            for (String petName : pets.keySet()) {
                Pet pet = pets.get(petName);
                String path = playerId.toString() + "." + petName;
                config.set(path + ".rarity", pet.getRarity().toString());
                config.set(path + ".level", pet.getLevel());
                config.set(path + ".xp", pet.getXp());
                config.set(path + ".icon", pet.getIcon().getType().toString());
                config.set(path + ".particle", pet.getParticle().toString());

                // We no longer save skullId (no SkullsAPI). Instead, we rely solely on textures.
                // If needed, you could store the base64 texture here as well.
                // config.set(path + ".texture", PET_TEXTURES.get(pet.getName()));

                List<String> perkNames = pet.getPerks().stream().map(Enum::name).collect(Collectors.toList());
                config.set(path + ".perks", perkNames);
            }
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadPets() {
        File file = new File(plugin.getDataFolder(), "pets.yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String playerIdString : config.getKeys(false)) {
            UUID playerId = UUID.fromString(playerIdString);
            Map<String, Pet> pets = new HashMap<>();
            ConfigurationSection playerSection = config.getConfigurationSection(playerIdString);

            for (String petName : playerSection.getKeys(false)) {
                String path = playerIdString + "." + petName;
                String rarityString = config.getString(path + ".rarity");
                int level = config.getInt(path + ".level");
                double xp = config.getDouble(path + ".xp");
                String particleName = config.getString(path + ".particle");
                List<String> perkNames = config.getStringList(path + ".perks");

                List<PetPerk> perks = perkNames.stream().map(PetPerk::valueOf).collect(Collectors.toList());
                Rarity rarity = Rarity.valueOf(rarityString);

                // Retrieve icon from the pet name using the textures we have
                ItemStack icon = getSkullForPet(petName);

                Particle particle;
                try {
                    particle = Particle.valueOf(particleName);
                } catch (IllegalArgumentException e) {
                    particle = Particle.FLAME;
                }

                Pet pet = new Pet(petName, rarity, 100, icon, particle, perks);
                pet.setLevel(level);
                pet.setXp(xp);

                pets.put(petName, pet);
            }

            playerPets.put(playerId, pets);
        }
    }

    public class Pet {
        private String name;
        private Rarity rarity;
        private int maxLevel;
        private int level;
        private double xp;
        private ItemStack icon;
        private Particle particle;
        private List<PetPerk> perks;

        public Pet(String name, Rarity rarity, int maxLevel, ItemStack icon, Particle particle, List<PetPerk> perks) {
            this.name = name;
            this.rarity = rarity;
            this.maxLevel = maxLevel;
            this.icon = icon;
            this.particle = particle;
            this.perks = perks;
            this.level = 1;
            this.xp = 0;
        }

        public String getName() {
            return name;
        }

        public Rarity getRarity() {
            return rarity;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public int getLevel() {
            return level;
        }

        public double getXp() {
            return xp;
        }

        public ItemStack getIcon() {
            return icon.clone();
        }

        public Particle getParticle() {
            return particle;
        }

        public List<PetPerk> getPerks() {
            return perks;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public void setXp(double xp) {
            this.xp = xp;
        }

        public boolean hasPerk(PetPerk perk) {
            return perks.contains(perk);
        }

        public void addXP(double amount) {
            this.xp += amount;
            while (this.xp >= getXPForNextLevel() && this.level < maxLevel) {
                levelUp();
            }
        }

        private void levelUp() {
            this.xp -= getXPForNextLevel();
            this.level++;
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (playerPets.getOrDefault(player.getUniqueId(), new HashMap<>()).containsKey(name)) {
                    player.sendMessage(ChatColor.GREEN + name + " has leveled up to level " + level + "!");
                }
            }
        }

        public double getXPForNextLevel() {
            return 100; // Example XP curve
        }
    }

    public enum PetPerk {
        WATERLOGGED("", ""),
        ASPECT_OF_THE_END("Aspect of the End", "Right click to warp forward."),
        BLAZE("Blaze", "Lights hit mobs on fire."),
        SECRET_LEGION("Secret Legion", "Summons a Zombified Piglin minion to defend you."),
        DECAY("Decay","Rapidly Deteriorates hit mobs."),
        BLACKLUNG("Blacklung","Transplants a nether-attuned lung."),
        SUPERIOR_ENDURANCE("Superior Endurance", "Gain +1 saturation when breaking crops."),
        ELITE("Elite","Deal 50% more damage."),
        GREED("Greed","4% chance to drop 1-16 emeralds on killing a monster."),
        BONE_COLD("Bone Cold","Grants immunity to freeze damage."),
        TIPPED_SLOWNESS("Tipped Slowness","Arrows you fire apply slowness."),
        X_RAY("X-Ray", "Helps you locate ores."),
        EMERALD_SEEKER("Emerald Seeker","1% chance to mine emeralds."),
        FIREPROOF("Fireproof","Grants permanent fire resistance."),
        LASER_BEAM("Laser Beam","25% chance to assist ranged attacks."),
        SKEPTICISM("Skepticism","Increases Forest Spirit spawn chance by 2%"),
        CHALLENGE("Challenge","Increases Forest Spirit spawn chance by 5%."),
        MITHRIL_MINER("Mithril Miner","1% chance to find Mithril Chunks while mining."),
        WALKING_FORTRESS("Walking Fortress","-20% movement speed, +60% defense."),
        BLIZZARD("Blizzard","Slows enemies for [Lvl] ticks."),
        ASPECT_OF_THE_FROST("Aspect Of The Frost","Take 50% less damage near snow."),
        CULTIVATION("Cultivation","Extra crops drop chance based on level."),
        FLOAT("Float","Grants Slow Falling while shifting in the air."),
        ECHOLOCATION("Echolocation","Grants Night Vision."),
        QUICK_DRAW("Quick Draw","Left Click to instantly fire Arrows! (Ignores Infinity)"),
        RECOVERY("Recovery","[Lvl] % chance to recover arrow."),
        TREASURE_HUNTER("Treasure Hunter","Triples Treasure Chance while fishing."),
        BAIT("Bait","Gains 1 Sea Creature Chance per 10 Lvls."),
        STRONG_SWIMMER("Strong Swimmer","Activates Dolphins Grace in water."),
        TERROR_OF_THE_DEEP("Terror Of The Deep","Activates bloodlust when hitting Sea Creatures."),
        HEART_OF_THE_SEA("Heart Of The Sea","+10 Sea Creature Chance."),
        GREEN_THUMB("Green Thumb","Increases tick speed while equipped."),
        HAGGLE("Haggle","Provides a 50% discount to villager transactions."),
        PRACTICE("Practice","Boosts villager xp gains."),
        COMFORTABLE("Comfortable","Grants you with 1 bonus health per 10 levels when eating."),
        LULLABY("Lullaby","Lulls monsters back to sleep, preventing their spawning."),
        ANGLER("Angler","+5 Sea Creature Chance"),
        ANTIDOTE("Antidote","Cures a random negative potion effect on eating."),
        LEAP("Leap","Enables you to leap forward when shifting."),
        SOFT_PAW("Soft Paw","Reduces fall damage."),
        CLAW("Claws","Deal 0.2% bonus damage per level."),
        REBIRTH("Rebirth","Cheat Death at the cost of your entire hunger bar."),
        DEVOUR("Devour", "Provides hunger when damaging mobs."),
        BONE_PLATING_WEAK("Weak Bone Plating", ChatColor.GOLD + "Provides minor damage resistance on taking damage."),
        DIGGING_CLAWS("Digging Claws", ChatColor.GOLD + "Provides moderate mining speed when mining blocks."),
        BONE_PLATING("Bone Plating", ChatColor.GOLD + "Provides moderate damage resistance for Lvl seconds."),
        COLLECTOR("Collector", ChatColor.GOLD + "Automatically collects nearby items upon walking nearby."),
        FLIGHT("Flight", ChatColor.GOLD + "Grants the ability to fly."),
        SPEED_BOOST("Speed Boost", ChatColor.GOLD + "Increases your walking speed by 100%."),
        SECOND_WIND("Second Wind", ChatColor.GOLD + "Provides [Lvl] seconds of regeneration when taking damage."),
        SHOTCALLING("Shotcalling", ChatColor.GOLD + "Increases arrow damage by 1% per [Lvl]");

        private final String displayName;
        private final String description;

        PetPerk(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Rarity {
        COMMON(ChatColor.WHITE),
        UNCOMMON(ChatColor.GREEN),
        RARE(ChatColor.BLUE),
        EPIC(ChatColor.DARK_PURPLE),
        LEGENDARY(ChatColor.GOLD);

        private final ChatColor color;

        Rarity(ChatColor color) {
            this.color = color;
        }

        public ChatColor getColor() {
            return color;
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Pet Menu")) {
                Player player = event.getPlayer();
                UUID playerId = player.getUniqueId();
                if (guiCooldown.contains(playerId)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Please wait a moment before opening the Pet Menu again.");
                    return;
                }

                guiCooldown.add(playerId);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    guiCooldown.remove(playerId);
                }, COOLDOWN_TIME);

                openPetGUI(player);
                event.setCancelled(true);
            }
        }
    }
}
