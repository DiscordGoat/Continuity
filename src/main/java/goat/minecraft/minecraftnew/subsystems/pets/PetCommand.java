package goat.minecraft.minecraftnew.subsystems.pets;


import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class PetCommand implements CommandExecutor {
    private static final String SEA_CREATURE_METADATA = "SEA_CREATURE";
    private PetManager petManager;



    public PetCommand(PetManager petManager) {
        this.petManager = petManager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("pet")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }
            Player player = (Player) sender;
            petManager.openPetGUI(player);
            return true;
        }

        if (label.equalsIgnoreCase("getpet")) {
            if (!sender.hasPermission("yourplugin.getpet")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
                return true;
            }

            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /getpet <petName> [playerName]");
                return true;
            }

            String petName = args[0];
            Player targetPlayer;

            if (args.length >= 2) {
                // Give pet to specified player
                targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    return true;
                }
            } else {
                // Give pet to the sender
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can receive pets.");
                    return true;
                }
                targetPlayer = (Player) sender;
            }

            //economical pets (trading/selling)
            //trading
            petManager.createPet(targetPlayer, "Villager", PetManager.Rarity.LEGENDARY, 100, Particle.VILLAGER_ANGRY, PetManager.PetPerk.HAGGLE, PetManager.PetPerk.PRACTICE);
            //selling

            //fishing pets (treasure/creatures)
            //treasure
            petManager.createPet(targetPlayer, "Golden Steve", PetManager.Rarity.LEGENDARY, 100, Particle.VILLAGER_ANGRY, PetManager.PetPerk.TREASURE_HUNTER, PetManager.PetPerk.COMFORTABLE);
            //creatures
            petManager.createPet(targetPlayer, "Leviathan", PetManager.Rarity.LEGENDARY, 100, Particle.VILLAGER_ANGRY , PetManager.PetPerk.ANGLER, PetManager.PetPerk.HEART_OF_THE_SEA, PetManager.PetPerk.TERROR_OF_THE_DEEP);
            petManager.createPet(targetPlayer, "Turtle", PetManager.Rarity.EPIC, 100, Particle.CRIMSON_SPORE , PetManager.PetPerk.HEART_OF_THE_SEA, PetManager.PetPerk.BONE_PLATING, PetManager.PetPerk.COMFORTABLE);
            petManager.createPet(targetPlayer, "Dolphin", PetManager.Rarity.RARE, 100, Particle.WATER_SPLASH , PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.ANGLER);
            petManager.createPet(targetPlayer, "Glow Squid", PetManager.Rarity.UNCOMMON, 100, Particle.GLOW_SQUID_INK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.ANGLER);
            petManager.createPet(targetPlayer, "Fish", PetManager.Rarity.COMMON, 100, Particle.GLOW_SQUID_INK, PetManager.PetPerk.ANGLER);

            //combat pets (melee/ranged)
            //ranged
            petManager.createPet(targetPlayer, "Stray", PetManager.Rarity.LEGENDARY, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.TIPPED_SLOWNESS, PetManager.PetPerk.BONE_COLD, PetManager.PetPerk.QUICK_DRAW);
            petManager.createPet(targetPlayer, "Guardian", PetManager.Rarity.EPIC, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.RECOVERY, PetManager.PetPerk.LASER_BEAM);
            petManager.createPet(targetPlayer, "Skeleton", PetManager.Rarity.UNCOMMON, 100, Particle.WHITE_ASH, PetManager.PetPerk.SHOTCALLING, PetManager.PetPerk.BONE_PLATING_WEAK);
            //melee
            petManager.createPet(targetPlayer, "Zombie Pigman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SECRET_LEGION, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF);
            petManager.createPet(targetPlayer, "Enderman", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.ELITE, PetManager.PetPerk.ASPECT_OF_THE_END, PetManager.PetPerk.COLLECTOR);
            petManager.createPet(targetPlayer, "Blaze", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.FLIGHT);
            petManager.createPet(targetPlayer, "Wither Skeleton", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.FIREPROOF, PetManager.PetPerk.DECAY);
            petManager.createPet(targetPlayer, "Cat", PetManager.Rarity.LEGENDARY, 100, Particle.ASH, PetManager.PetPerk.CLAW, PetManager.PetPerk.SOFT_PAW, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.LEAP);
            petManager.createPet(targetPlayer, "Yeti", PetManager.Rarity.EPIC, 100, Particle.CRIT_MAGIC, PetManager.PetPerk.ASPECT_OF_THE_FROST, PetManager.PetPerk.BLIZZARD, PetManager.PetPerk.SPEED_BOOST);
            petManager.createPet(targetPlayer, "Axolotl", PetManager.Rarity.EPIC, 100, Particle.SPELL_WITCH, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.REBIRTH);
            petManager.createPet(targetPlayer, "Zombie", PetManager.Rarity.RARE, 100, Particle.CRIT_MAGIC, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.DEVOUR, PetManager.PetPerk.ECHOLOCATION);
            petManager.createPet(targetPlayer, "Iron Golem", PetManager.Rarity.RARE, 100, Particle.ASH, PetManager.PetPerk.WALKING_FORTRESS, PetManager.PetPerk.ELITE);

            //mining pets (breaking/sustain)
            //sustain
            petManager.createPet(targetPlayer, "Bat", PetManager.Rarity.RARE, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.ECHOLOCATION);
            //breaking
            petManager.createPet(targetPlayer, "Warden", PetManager.Rarity.LEGENDARY, 100, Particle.WARPED_SPORE, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.ECHOLOCATION, PetManager.PetPerk.ELITE);
            petManager.createPet(targetPlayer, "Dwarf", PetManager.Rarity.EPIC, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.DIGGING_CLAWS, PetManager.PetPerk.MITHRIL_MINER, PetManager.PetPerk.EMERALD_SEEKER);
            petManager.createPet(targetPlayer, "Armadillo", PetManager.Rarity.RARE, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.BONE_PLATING, PetManager.PetPerk.DIGGING_CLAWS);
            petManager.createPet(targetPlayer, "Drowned", PetManager.Rarity.EPIC, 100, Particle.DAMAGE_INDICATOR, PetManager.PetPerk.WATERLOGGED, PetManager.PetPerk.STRONG_SWIMMER, PetManager.PetPerk.DEVOUR);

            //movement pets (flying/running)
            //flying
            petManager.createPet(targetPlayer, "Parrot", PetManager.Rarity.LEGENDARY, 100, Particle.TOTEM, PetManager.PetPerk.FLIGHT, PetManager.PetPerk.LULLABY);
            petManager.createPet(targetPlayer, "Allay", PetManager.Rarity.EPIC, 100, Particle.END_ROD, PetManager.PetPerk.COLLECTOR, PetManager.PetPerk.FLIGHT);
            petManager.createPet(targetPlayer, "Chicken", PetManager.Rarity.RARE, 100, Particle.END_ROD, PetManager.PetPerk.FLOAT);
            //running
            petManager.createPet(targetPlayer, "Horse", PetManager.Rarity.COMMON, 10, Particle.HEART, PetManager.PetPerk.SPEED_BOOST);
            //forestry pets
            petManager.createPet(targetPlayer, "Piglin Brute", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.CHALLENGE, PetManager.PetPerk.BLACKLUNG, PetManager.PetPerk.SECOND_WIND, PetManager.PetPerk.ELITE);
            petManager.createPet(targetPlayer, "Vindicator", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.SKEPTICISM, PetManager.PetPerk.GREED, PetManager.PetPerk.ELITE);
            //farming pets
            petManager.createPet(targetPlayer, "Pig", PetManager.Rarity.LEGENDARY, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.CULTIVATION, PetManager.PetPerk.SUPERIOR_ENDURANCE);
            petManager.createPet(targetPlayer, "Mooshroom", PetManager.Rarity.EPIC, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.CULTIVATION);
            petManager.createPet(targetPlayer, "Cow", PetManager.Rarity.RARE, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB, PetManager.PetPerk.ANTIDOTE);
            petManager.createPet(targetPlayer, "Sheep", PetManager.Rarity.UNCOMMON, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.SPEED_BOOST, PetManager.PetPerk.GREEN_THUMB);
            petManager.createPet(targetPlayer, "Squirrel", PetManager.Rarity.COMMON, 100, Particle.FIREWORKS_SPARK, PetManager.PetPerk.GREEN_THUMB);


            return true;
        }

        return false;
    }
}
