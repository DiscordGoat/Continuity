package goat.minecraft.minecraftnew.subsystems.pets;

import ca.tweetzy.skulls.Skulls;
import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.utils.CustomItemManager;
import goat.minecraft.minecraftnew.subsystems.villagers.VillagerWorkCycleManager;
import org.apache.commons.lang.WordUtils;
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
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.io.File;
import java.io.IOException;
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
    private static final Map<String, Integer> PET_SKULL_IDS = new HashMap<>();

    static {
        PET_SKULL_IDS.put("Allay", 71212);
        PET_SKULL_IDS.put("Armadillo", 48145);
        PET_SKULL_IDS.put("Axolotl", 40750);
        PET_SKULL_IDS.put("Cat", 4166);
        PET_SKULL_IDS.put("Cow", 335);
        PET_SKULL_IDS.put("Horse", 25385);
        PET_SKULL_IDS.put("Glow Squid", 40581);
        PET_SKULL_IDS.put("Parrot", 6534);
        PET_SKULL_IDS.put("Sheep", 334);
        PET_SKULL_IDS.put("Turtle", 353);
        PET_SKULL_IDS.put("Villager", 1530);
        PET_SKULL_IDS.put("Squirrel", 2916);
        PET_SKULL_IDS.put("Leviathan", 57185);
        PET_SKULL_IDS.put("Dolphin", 44599);
        PET_SKULL_IDS.put("Fish", 17897);
        PET_SKULL_IDS.put("Golden Steve", 18802);
        PET_SKULL_IDS.put("Pillager", 25149);
        PET_SKULL_IDS.put("Stray", 3244);
        PET_SKULL_IDS.put("Bat", 4109);
        PET_SKULL_IDS.put("Chicken", 351);
        PET_SKULL_IDS.put("Mooshroom", 339);
        PET_SKULL_IDS.put("Pig", 31373);
        PET_SKULL_IDS.put("Yeti", 16792);
        PET_SKULL_IDS.put("Iron Golem", 33179);
        PET_SKULL_IDS.put("Dwarf", 3423);
        PET_SKULL_IDS.put("Piglin Brute", 38372);
        PET_SKULL_IDS.put("Vindicator", 3079);
        PET_SKULL_IDS.put("Guardian", 25292);
        PET_SKULL_IDS.put("Zombie Pigman", 324);
        PET_SKULL_IDS.put("Zombie", 3094);
        PET_SKULL_IDS.put("Skeleton", 2746);
        PET_SKULL_IDS.put("Warden", 58402);
        PET_SKULL_IDS.put("Wither Skeleton", 5265);
        PET_SKULL_IDS.put("Blaze", 322);
        PET_SKULL_IDS.put("Enderman", 318);
        PET_SKULL_IDS.put("Drowned", 15967);
    }

    public static PetManager getInstance(JavaPlugin plugin) {
        if (instance == null) {
            instance = new PetManager(plugin);
        }
        return instance;
    }
    public static int getSkullId(String petName) {
        return PET_SKULL_IDS.getOrDefault(petName, -1); // Return -1 if not found
    }

    public static String getPetNameBySkullId(int skullId) {
        return PET_SKULL_IDS.entrySet().stream()
                .filter(entry -> entry.getValue() == skullId)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null); // Return null if not found
    }
    public ItemStack getSkullForPet(String petName) {
        int skullId = getSkullId(petName);
        if (skullId == -1) {
            return new ItemStack(Material.NAME_TAG); // Default icon
        }
        return Skulls.getAPI().getSkullItem(skullId);
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

                spawnPetParticle(player, pet);
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
            if (activePets.get(player.getUniqueId()) != null) {
                return activePets.get(player.getUniqueId());
            }
            return null;
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
        // Use the particle type defined in the pet
        Particle particleType = pet.getParticle();

        // Schedule a repeating task to display the particle effect
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline()) {
                removePetParticle(player);
                return;
            }

            Location location = player.getLocation().add(0, 1.5, 0); // Adjust the height as needed
            player.getWorld().spawnParticle(particleType, location, 1, 0.3, 0.3, 0.3, 0.05); // Adjust particle parameters as needed

        }, 0L, 100L); // 0L delay, 100L period (5 seconds)

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



    /**
     * Check if the player has the GREED perk.
     * Replace this with your actual perk-checking logic.
     */
    // ==========================
    // Event Handlers
    // ==========================

    /**
     * Handles experience gain for players, attributing XP to their active pets.
     *
     * @param event The experience change event.
     */
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

    /**
     * Handles player logout by despawning their active pet.
     *
     * @param event The player quit event.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PetManager petManager = PetManager.getInstance(plugin);
        petManager.createPet(player, "Horse", PetManager.Rarity.COMMON, 10, Particle.HEART, PetManager.PetPerk.SPEED_BOOST);
        despawnPet(player);
    }

    /**
     * Handles entity damage events for scaling perks like SECOND_WIND, BONE_PLATING, and REBIRTH.
     *
     * @param event The entity damage event.
     */

    public void openPetGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Your Pets");

        // Get the player's pets
        Map<String, Pet> pets = playerPets.getOrDefault(player.getUniqueId(), new HashMap<>());

        int index = 0;
        for (Pet pet : pets.values()) {
            // Fetch the updated skull texture
            ItemStack petIcon = getSkullForPet(pet.getName());
            if (petIcon == null) {
                petIcon = new ItemStack(Material.NAME_TAG); // Fallback item
            }

            // Set item meta for the pet icon
            ItemMeta meta = petIcon.getItemMeta();
            if (meta != null) {
                ChatColor rarityColor = pet.getRarity().getColor();
                meta.setDisplayName(rarityColor + "[Lvl " + pet.getLevel() + "] " + pet.getName());

                // Dynamic lore
                List<String> lore = new ArrayList<>();

                //lore.add(ChatColor.GRAY + "Rarity: " + pet.getRarity().toString());
                lore.add(ChatColor.GREEN + "XP: " + (int) pet.getXp() + "/" + (int) pet.getXPForNextLevel());
                lore.add(ChatColor.GRAY + " ");
                //lore.add(ChatColor.GRAY + "Perks:");
                for (PetPerk perk : pet.getPerks()) {
                    lore.add(ChatColor.GOLD + "" + perk.getDisplayName());
                    lore.add(ChatColor.GRAY + "" + getDynamicPerkEffectDescription(perk, pet.getLevel()));
                    lore.add(ChatColor.GRAY + " ");
                }
                if (pet.equals(activePets.get(player.getUniqueId()))) {
                    lore.add(ChatColor.GREEN + "Currently Active");
                }
                meta.setLore(lore);
                petIcon.setItemMeta(meta);
            }

            // Add the pet item to the GUI
            gui.setItem(index++, petIcon);
        }

        // Fill the rest of the GUI with glass panes for aesthetics
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        for (; index < gui.getSize(); index++) {
            gui.setItem(index, glassPane);
        }

        // Open the GUI for the player
        player.openInventory(gui);
    }
    private String getDynamicPerkEffectDescription(PetPerk perk, int level) {

        switch (perk) {
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
                return "Increases " + ChatColor.YELLOW + "speed " + ChatColor.GRAY + "by " + ChatColor.YELLOW + (level * 1) + "%.";
            case CLAW:
                return "Increases " + ChatColor.RED + "melee damage " + ChatColor.GRAY + "by " + ChatColor.RED + (0.2 * level) + "%.";
            default:
                return ChatColor.GRAY + "Static effect or undefined scaling.";
        }

    }



    /**
     * Handles inventory click events within the pet management GUI to toggle pets.
     *
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Your Pets")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.hasItemMeta()) {
                String displayName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                // Remove the [Lvl X] prefix using regex
                String petName = displayName.replaceFirst("\\[Lvl \\d+\\] ", "");
                Pet pet = getPet(player, petName);
                if (pet != null) {
                    summonPet(player, petName);
                    player.closeInventory();
                }
            }
        }
    }

    // ==========================
    // Pet Creation Method
    // ==========================

    /**
     * Creates a new pet with specified properties and adds it to the player's collection.
     *
     * @param player    The player.
     * @param name      The name of the pet.
     * @param rarity    The rarity of the pet.
     * @param maxLevel  The maximum level the pet can reach.
     * @param icon      The icon representing the pet in the GUI.
     * @param particle  The particle effect associated with the pet.
     * @param perks     The perks granted by the pet.
     */


    /**
     * Overloaded method to create a pet using a Material for the icon instead of an ItemStack.
     *
     * @param player       The player.
     * @param name         The name of the pet.
     * @param rarity       The rarity of the pet.
     * @param maxLevel     The maximum level the pet can reach
     * @param particle     The particle effect associated with the pet.
     * @param perks        The perks granted by the pet.
     */
    public void createPet(Player player, String name, Rarity rarity, int maxLevel, Particle particle, PetPerk... perks) {
        ItemStack icon = getSkullForPet(name);
        List<PetPerk> perkList = Arrays.asList(perks);
        Pet newPet = new Pet(name, rarity, maxLevel, icon, particle, perkList);
        addPet(player, newPet);
    }


    // ==========================
    // Saving and Loading Pets
    // ==========================

    /**
     * Saves all players' pets to a YAML configuration file.
     */
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
                config.set(path + ".skullId", getSkullId(pet.getName()));

                // Save perks
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

    /**
     * Loads all players' pets from a YAML configuration file.
     */
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
                int skullId = config.getInt(path + ".skullId", -1);
                String particleName = config.getString(path + ".particle");
                List<String> perkNames = config.getStringList(path + ".perks");

                List<PetPerk> perks = perkNames.stream().map(PetPerk::valueOf).collect(Collectors.toList());
                Rarity rarity = Rarity.valueOf(rarityString);

                // Retrieve the correct icon using the skull ID
                ItemStack icon;
                if (skullId != -1) {
                    icon = Skulls.getAPI().getSkullItem(skullId);
                } else {
                    Material iconMaterial = Material.getMaterial(config.getString(path + ".icon", "NAME_TAG"));
                    icon = new ItemStack(iconMaterial != null ? iconMaterial : Material.NAME_TAG);
                }

                // Determine the particle effect
                Particle particle;
                try {
                    particle = Particle.valueOf(particleName);
                } catch (IllegalArgumentException e) {
                    particle = Particle.FLAME; // Default particle if invalid
                }

                // Create the pet instance
                Pet pet = new Pet(petName, rarity, 100, icon, particle, perks);
                pet.setLevel(level);
                pet.setXp(xp);

                pets.put(petName, pet);
            }

            playerPets.put(playerId, pets);
        }
    }


    // ==========================
    // Inner Classes and Enums
    // ==========================

    /**
     * Represents a pet with various attributes.
     */
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

        /**
         * Checks if the pet has a specific perk.
         *
         * @param perk The perk to check.
         * @return True if the pet has the perk, false otherwise.
         */
        public boolean hasPerk(PetPerk perk) {
            return perks.contains(perk);
        }

        /**
         * Adds XP to the pet and handles leveling up.
         *
         * @param amount The amount of XP to add.
         */
        public void addXP(double amount) {
            this.xp += amount;
            while (this.xp >= getXPForNextLevel() && this.level < maxLevel) {
                levelUp();
            }
        }

        /**
         * Handles leveling up the pet.
         */
        private void levelUp() {
            this.xp -= getXPForNextLevel();
            this.level++;
            // Notify the owner
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (playerPets.getOrDefault(player.getUniqueId(), new HashMap<>()).containsKey(name)) {
                    player.sendMessage(ChatColor.GREEN + name + " has leveled up to level " + level + "!");
                }
            }
        }

        /**
         * Calculates the XP required for the next level.
         *
         * @return The XP needed for the next level.
         */
        public double getXPForNextLevel() {
            // Example XP curve
            return 100;
        }
    }

    /**
     * Enum representing the perks a pet can grant.
     */
    public enum PetPerk {
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
        // Add more perks as needed

        private final String displayName;
        private final String description;

        /**
         * Constructor for PetPerk enum.
         *
         * @param displayName The display name of the perk.
         * @param description The detailed description of the perk.
         */
        PetPerk(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        /**
         * Gets the display name of the perk.
         *
         * @return The display name.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Gets the detailed description of the perk.
         *
         * @return The description.
         */
        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum representing the rarity levels of pets.
     */
    public enum Rarity {
        COMMON(ChatColor.WHITE),
        UNCOMMON(ChatColor.GREEN),
        RARE(ChatColor.BLUE),
        EPIC(ChatColor.DARK_PURPLE),
        LEGENDARY(ChatColor.GOLD);

        private final ChatColor color;

        /**
         * Constructor for Rarity enum.
         *
         * @param color The color associated with the rarity.
         */
        Rarity(ChatColor color) {
            this.color = color;
        }

        /**
         * Gets the color associated with the rarity.
         *
         * @return The ChatColor.
         */
        public ChatColor getColor() {
            return color;
        }
    }

    // ==========================
    // Command Handling
    // ==========================

    /**
     * Handles player interactions, specifically right-clicking the "Pet Menu" item to open the GUI.
     *
     * @param event The player interact event.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // If player right-clicks with a specific item, open the pet GUI
        // For example, a bone named "Pet Menu"
        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta()) {
            if (item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Pet Menu")) {
                Player player = event.getPlayer();

                UUID playerId = player.getUniqueId();
                if (guiCooldown.contains(playerId)) {
                    // Player is on cooldown, ignore the click
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Please wait a moment before opening the Pet Menu again.");
                    return;
                }

                // Add player to cooldown
                guiCooldown.add(playerId);

                // Schedule removal of cooldown after COOLDOWN_TIME ticks
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    guiCooldown.remove(playerId);
                }, COOLDOWN_TIME);

                // Open the Pet GUI
                openPetGUI(player);
                event.setCancelled(true);
            }
        }
    }
}
