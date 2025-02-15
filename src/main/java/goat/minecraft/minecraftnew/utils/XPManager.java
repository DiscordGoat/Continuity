package goat.minecraft.minecraftnew.utils;

import java.io.*;
import java.util.UUID;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class XPManager implements CommandExecutor {

    private final JavaPlugin plugin;

    // XP thresholds for levels 0 to 100
    private final int[] xpThresholds = new int[] {
            100*2,   // Level 1
            204*2,   // Level 2
            311*2,   // Level 3
            423*2,   // Level 4
            539*2,   // Level 5
            662*2,   // Level 6
            789*2,   // Level 7
            921*2,   // Level 8
            1057*2,  // Level 9
            1199*2,  // Level 10
            1346*2,  // Level 11
            1498*2,  // Level 12
            1656*2,  // Level 13
            1819*2,  // Level 14
            1988*2,  // Level 15
            2162*2,  // Level 16
            2342*2,  // Level 17
            2528*2,  // Level 18
            2720*2,  // Level 19
            2918*2,  // Level 20
            3123*2,  // Level 21
            3334*2,  // Level 22
            3551*2,  // Level 23
            3776*2,  // Level 24
            4007*2,  // Level 25
            4245*2,  // Level 26
            4490*2,  // Level 27
            4742*2,  // Level 28
            5002*2,  // Level 29
            5269*2,  // Level 30
            5544*2,  // Level 31
            5826*2,  // Level 32
            6117*2,  // Level 33
            6416*2,  // Level 34
            6724*2,  // Level 35
            7040*2,  // Level 36
            7365*2,  // Level 37
            7699*2,  // Level 38
            8042*2,  // Level 39
            8395*2,  // Level 40
            8757*2,  // Level 41
            9129*2,  // Level 42
            9511*2,  // Level 43
            9904*2,  // Level 44
            10307*2, // Level 45
            10721*2, // Level 46
            11146*2, // Level 47
            11583*2, // Level 48
            12032*2, // Level 49
            12493*2, // Level 50
            12966*2, // Level 51
            13452*2, // Level 52
            13951*2, // Level 53
            14463*2, // Level 54
            14989*2, // Level 55
            15528*2, // Level 56
            16082*2, // Level 57
            16651*2, // Level 58
            17234*2, // Level 59
            17833*2, // Level 60
            18447*2, // Level 61
            19077*2, // Level 62
            19724*2, // Level 63
            20387*2, // Level 64
            21068*2, // Level 65
            21766*2, // Level 66
            22482*2, // Level 67
            23217*2, // Level 68
            23971*2, // Level 69
            24744*2, // Level 70
            25537*2, // Level 71
            26350*2, // Level 72
            27183*2, // Level 73
            28038*2, // Level 74
            28914*2, // Level 75
            29813*2, // Level 76
            30735*2, // Level 77
            31680*2, // Level 78
            32649*2, // Level 79
            33643*2, // Level 80
            34662*2, // Level 81
            35707*2, // Level 82
            36779*2, // Level 83
            37878*2, // Level 84
            39005*2, // Level 85
            40161*2, // Level 86
            41347*2, // Level 87
            42563*2, // Level 88
            43811*2, // Level 89
            45091*2, // Level 90
            46405*2, // Level 91
            47752*2, // Level 92
            49135*2, // Level 93
            50554*2, // Level 94
            52010*2, // Level 95
            53505*2, // Level 96
            55039*2, // Level 97
            56614*2, // Level 98
            58231*2, // Level 99
            59891*2  // Level 100
    };

    public XPManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // =================================================
    // ===============  FILE OPERATIONS  ===============
    // =================================================
    public void createDatabase(UUID uuid, String skill) {
        File file = new File(plugin.getDataFolder(), uuid + "_" + skill + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
                saveXP(uuid, skill, 0); // Initialize with 0 XP
                Bukkit.getLogger().info("Created new XP database for " + uuid + " and skill " + skill);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int loadXP(UUID uuid, String skill) {
        File file = new File(plugin.getDataFolder(), uuid + "_" + skill + ".txt");
        if (!file.exists()) {
            createDatabase(uuid, skill);
            return 0;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void saveXP(UUID uuid, String skill, int xp) {
        File file = new File(plugin.getDataFolder(), uuid + "_" + skill + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(xp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =================================================
    // ===============     CORE XP LOGIC   =============
    // =================================================

    /**
     * This is the main XP-adding method. If a player gains XP in a skill,
     * we check if they leveled up. If so, we play a sound and send them
     * one consolidated chat message with their new stats.
     */
    public void addXP(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);

        // Count how many 'Savant' enchantment items they have for a bonus
        int savantCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && CustomEnchantmentManager.hasEnchantment(item, "Savant")) {
                savantCount++;
            }
        }
        double bonusXP = xp * 0.05 * savantCount;
        int newXP = (int) (currentXP + xp + bonusXP);

        saveXP(uuid, skill, newXP);

        int oldLevel = calculateLevel(currentXP);
        int newLevel = calculateLevel(newXP);

        // Check if we leveled up
        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);
        }

        // If skill != Player, also add 10% XP to "Player" skill
        if (!skill.equalsIgnoreCase("Player")) {
            addXPToSkill(player, "Player", xp * 0.10);
        }
    }

    /**
     * Similar to addXP, but this method DOES NOT also push XP into "Player" skill.
     * It's used internally so you don't recursively keep adding XP into Player.
     */
    private void addXPToSkill(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int newXP = (int) (currentXP + xp);
        saveXP(uuid, skill, newXP);

        int oldLevel = calculateLevel(currentXP);
        int newLevel = calculateLevel(newXP);

        // If skill leveled up, show the consolidated message
        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);
        }
    }

    /**
     * Directly sets a player's XP for a given skill. If it causes a level-up,
     * we do the same single-message approach.
     */
    public void setXP(Player player, String skill, int xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int oldLevel = calculateLevel(currentXP);

        // Ensure the file for that skill exists
        createDatabase(uuid, skill);

        // Save the new XP
        saveXP(uuid, skill, xp);

        int newLevel = calculateLevel(xp);

        // If skill leveled up, show the consolidated message
        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);
        }

        // If the skill is not "Player", distribute 10% to "Player"
        if (!skill.equalsIgnoreCase("Player")) {
            double additionalXP = xp * 0.10;
            addXPToSkill(player, "Player", additionalXP);
        }
    }

    // =================================================
    // ===============   LEVEL CALC / GETTERS  =========
    // =================================================

    public int calculateLevel(int xp) {
        int level = 0;
        for (int i = 0; i < xpThresholds.length; i++) {
            if (xp < xpThresholds[i]) {
                break;
            }
            level++;
        }
        return level;
    }

    public int getXPToNextLevel(Player player, String skill) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int currentLevel = calculateLevel(currentXP);

        if (currentLevel >= xpThresholds.length) {
            return -1; // Max level
        }

        int nextLevelXP = xpThresholds[currentLevel];
        return nextLevelXP - currentXP;
    }

    public int getXP(Player player, String skill) {
        return loadXP(player.getUniqueId(), skill);
    }

    public int getPlayerLevel(Player player, String skill) {
        int currentXP = loadXP(player.getUniqueId(), skill);
        return calculateLevel(currentXP);
    }

    // =================================================
    // ===============  SINGLE LEVEL-UP MESSAGE  =======
    // =================================================
    /**
     * Sends a single chat message that says "You leveled up to X in skill Y!"
     * and includes all relevant stat changes for that skill. Replaces all old
     * messages, titles, etc.
     *
     * If it's the "Player" skill, we also apply the regeneration/glowing effects
     * that used to be in sendPlayerStatsMessage(), but with a single chat message.
     */
    public void sendSkillLevelUpMessage(Player player, String skill, int newLevel) {

        //==========================
        // 1) Special "Player" logic
        //==========================
        if (skill.equalsIgnoreCase("Player")) {
            player.setSaturation(20.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));

            // Adjust max health
            double healthMultiplier = 1 + (Math.min(newLevel, 50) * 0.02);
            double newMaxHealth = 20.0 * healthMultiplier;
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
        }

        //==========================
        // 2) Build the skill message
        //==========================
        String borderTop    = ChatColor.DARK_AQUA + "╔═════════════════════╗";
        String borderBottom = ChatColor.DARK_AQUA + "╚═════════════════════╝";
        // We’ll fill this in below:
        StringBuilder body = new StringBuilder();

        // Put a big label so it’s clear
        body.append(ChatColor.DARK_AQUA).append("   ❖ ").append(ChatColor.WHITE)
                .append("Level Up: ").append(ChatColor.AQUA).append("[").append(skill).append("] ")
                .append(ChatColor.WHITE).append("→ Level ").append(ChatColor.YELLOW).append(newLevel)
                .append("\n\n");

        // Then skill-specific details:
        switch (skill.toLowerCase()) {
            case "player":
                double healthMultiplier = 1 + ((Math.min(newLevel, 50) * 0.02));
                double newMaxHealth = 20.0 * healthMultiplier;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.GREEN).append("Max Health ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.GREEN).append((int)newMaxHealth).append(" HP.\n");
                break;

            case "combat":
                double damageMult = 1.0 + (0.03 * newLevel);
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.RED).append("Damage Multiplier ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.RED).append(String.format("%.2f", damageMult)).append("x.\n");
                break;

            case "fishing":
                int seaChance = newLevel / 2;
                body.append(ChatColor.WHITE).append("Your base ")
                        .append(ChatColor.DARK_AQUA).append("Sea Creature Chance ")
                        .append(ChatColor.YELLOW).append("chance ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(seaChance).append("%.\n");
                break;

            case "farming":
                int doubleCropChance = newLevel / 2;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.YELLOW).append("Double Crop Chance ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(doubleCropChance).append("%.\n");
                break;

            case "mining":
                int doubleDropsChance = newLevel / 2;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.DARK_GRAY).append("Double Drops Chance ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(doubleDropsChance).append("%.\n");
                break;

            case "forestry":
                int doubleLogsChance = newLevel; // 1% per level
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.GOLD).append("Double Logs Chance ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(doubleLogsChance).append("%.\n");
                break;

            case "bartering":
                double discount = newLevel * 0.25;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.DARK_GREEN).append("Trade Discount ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(String.format("%.2f", discount)).append("%.\n");
                break;

            case "culinary":
                double additionalSaturation = Math.min(newLevel * 0.05, 20.0);
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.GOLD).append("Extra Saturation ")
                        .append(ChatColor.WHITE).append("is now +")
                        .append(ChatColor.GREEN).append(String.format("%.2f", additionalSaturation))
                        .append(".\n");
                break;

            case "smithing":
                double successChance = 50.0 + (0.5 * newLevel);
                if (successChance > 100.0) successChance = 100.0;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.DARK_PURPLE).append("Sharpen/Polish/Reinforce success ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.YELLOW).append(String.format("%.0f", successChance)).append("%.\n");
                break;

            default:
                // Unknown skill fallback
                body.append(ChatColor.WHITE).append("Enjoy your new level in ")
                        .append(skill).append("!\n");
                break;
        }

        //==========================
        // 3) Combine the message, send
        //==========================
        player.sendMessage(borderTop);
        player.sendMessage(body.toString().trim()); // trim in case of trailing newlines
        player.sendMessage(borderBottom);
    }


    // =================================================
    // ===============      COMMAND EXECUTOR  ==========
    // =================================================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return false;
        }

        Player player = (Player) sender;

        // Check for minimum arguments
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /xp <add|subtract|get|set> <skill> <amount>");
            return false;
        }

        String action = args[0].toLowerCase();
        String skill = args[1];
        int amount = 0;

        // "get" doesn't require an amount
        if (!action.equals("get")) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Amount must be a number.");
                return false;
            }
        }

        switch (action) {
            case "add":
                addXP(player, skill, amount);
                break;

            case "subtract":
                int currentXP = loadXP(player.getUniqueId(), skill);
                if (currentXP < amount) {
                    player.sendMessage(ChatColor.RED + "You cannot subtract more XP than you have.");
                    return false;
                }
                saveXP(player.getUniqueId(), skill, currentXP - amount);

                // Optionally, you can also check if this lost XP means they
                // dropped a level, but typically you don’t do “down-level” messages.
                player.sendMessage(ChatColor.GREEN + "Subtracted " + amount + " XP from your " + skill + " skill.");
                break;

            case "get":
                int xp = loadXP(player.getUniqueId(), skill);
                player.sendMessage(ChatColor.GREEN + "You have " + xp + " XP in " + skill + ".");
                break;

            case "set":
                setXP(player, skill, amount);
                player.sendMessage(ChatColor.GREEN + "Set your " + skill + " skill XP to " + amount + ".");
                break;

            default:
                player.sendMessage(ChatColor.RED + "Invalid action. Use add, subtract, get, or set.");
                return false;
        }
        return true;
    }
}
