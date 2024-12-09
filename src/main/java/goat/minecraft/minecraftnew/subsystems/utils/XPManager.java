package goat.minecraft.minecraftnew.subsystems.utils;

import java.io.*;
import java.util.UUID;

import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class XPManager implements CommandExecutor {
    private final JavaPlugin plugin;

    // XP thresholds for levels 0 to 100
    private final int[] xpThresholds = new int[]{
            100*2,     // Level 1
            204*2,     // Level 2
            311*2,     // Level 3
            423*2,     // Level 4
            539*2,     // Level 5
            662*2,     // Level 6
            789*2,     // Level 7
            921*2,     // Level 8
            1057*2,    // Level 9
            1199*2,    // Level 10
            1346*2,    // Level 11
            1498*2,    // Level 12
            1656*2,    // Level 13
            1819*2,    // Level 14
            1988*2,    // Level 15
            2162*2,    // Level 16
            2342*2,    // Level 17
            2528*2,    // Level 18
            2720*2,    // Level 19
            2918*2,    // Level 20
            3123*2,    // Level 21
            3334*2,    // Level 22
            3551*2,    // Level 23
            3776*2,    // Level 24
            4007*2,    // Level 25
            4245*2,    // Level 26
            4490*2,    // Level 27
            4742*2,    // Level 28
            5002*2,    // Level 29
            5269*2,    // Level 30
            5544*2,    // Level 31
            5826*2,    // Level 32
            6117*2,    // Level 33
            6416*2,    // Level 34
            6724*2,    // Level 35
            7040*2,    // Level 36
            7365*2,    // Level 37
            7699*2,    // Level 38
            8042*2,    // Level 39
            8395*2,    // Level 40
            8757*2,    // Level 41
            9129*2,    // Level 42
            9511*2,    // Level 43
            9904*2,    // Level 44
            10307*2,   // Level 45
            10721*2,   // Level 46
            11146*2,   // Level 47
            11583*2,   // Level 48
            12032*2,   // Level 49
            12493*2,   // Level 50
            12966*2,   // Level 51
            13452*2,   // Level 52
            13951*2,   // Level 53
            14463*2,   // Level 54
            14989*2,   // Level 55
            15528*2,   // Level 56
            16082*2,   // Level 57
            16651*2,   // Level 58
            17234*2,   // Level 59
            17833*2,   // Level 60
            18447*2,   // Level 61
            19077*2,   // Level 62
            19724*2,   // Level 63
            20387*2,   // Level 64
            21068*2,   // Level 65
            21766*2,   // Level 66
            22482*2,   // Level 67
            23217*2,   // Level 68
            23971*2,   // Level 69
            24744*2,   // Level 70
            25537*2,   // Level 71
            26350*2,   // Level 72
            27183*2,   // Level 73
            28038*2,   // Level 74
            28914*2,   // Level 75
            29813*2,   // Level 76
            30735*2,   // Level 77
            31680*2,   // Level 78
            32649*2,   // Level 79
            33643*2,   // Level 80
            34662*2,   // Level 81
            35707*2,   // Level 82
            36779*2,   // Level 83
            37878*2,   // Level 84
            39005*2,   // Level 85
            40161*2,   // Level 86
            41347*2,   // Level 87
            42563*2,   // Level 88
            43811*2,   // Level 89
            45091*2,   // Level 90
            46405*2,   // Level 91
            47752*2,   // Level 92
            49135*2,   // Level 93
            50554*2,   // Level 94
            52010*2,   // Level 95
            53505*2,   // Level 96
            55039*2,   // Level 97
            56614*2,   // Level 98
            58231*2,   // Level 99
            59891*2    // Level 100
    };



    public XPManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Method to create a new XP database (file) for a player skill
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

    // Method to load XP from the file
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

    // Method to save XP to the file
    public void saveXP(UUID uuid, String skill, int xp) {
        File file = new File(plugin.getDataFolder(), uuid + "_" + skill + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(xp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method to add XP to a player
    public void addXP(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);

        // Check the player's inventory for items with the "Savant" enchantment
        int savantCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && CustomEnchantmentManager.hasEnchantment(item, "Savant")) {
                savantCount++;
            }
        }

        // Apply the XP bonus based on the number of "Savant" enchanted items
        double bonusXP = xp * 0.05 * savantCount;
        int newXP = (int) (currentXP + xp + bonusXP);
        saveXP(uuid, skill, newXP);

        int level = calculateLevel(newXP);
        int nextLevelXP = (level < xpThresholds.length) ? xpThresholds[level] : -1;

        String nextLevelMessage = (nextLevelXP > -1) ? (nextLevelXP - newXP) + " XP" : "Max Level Reached";

        if (level > calculateLevel(currentXP)) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }

        if (!skill.equalsIgnoreCase("Player")) {
            addXPToSkill(player, "Player", xp * 0.10);
        }

        if (!skill.equals("Player")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    //String.format("%.2f", damageMultiplier)
                    new TextComponent(ChatColor.GREEN + skill + " (+" + xp + " XP | +" + String.format("%.2f", bonusXP) + " bonus XP / " + nextLevelMessage + ") | " + ChatColor.AQUA + "Level: " + level)
            );
        }
        //Bukkit.broadcastMessage("Xp total is " + (xp + bonusXP) + " bonus of " + bonusXP);
    }

    // Method to add XP to a player without adding XP to the "Player" skill
    private void addXPToSkill(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int newXP = (int) (currentXP + xp);
        XPManager xpManager = new XPManager(plugin);
        // Define the maximum XP allowed per skill

        // Check if the new XP exceeds the maximum allowed

        // Save the capped XP
        saveXP(uuid, skill, newXP);

        int currentLevel = calculateLevel(currentXP);
        int newLevel = calculateLevel(newXP);
        int nextLevelXP = (newLevel < xpThresholds.length) ? xpThresholds[newLevel] : -1; // Get next level XP threshold

        // Calculate how much more XP is needed for the next level
        String nextLevelMessage = (nextLevelXP > -1) ? (nextLevelXP - newXP) + " XP" : "Max Level Reached";

        // Send the player a message about their new level
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ChatColor.GREEN + skill + " (+" + (int) xp + "/" + nextLevelMessage + ") | "
                        + ChatColor.AQUA + "Level: " + newLevel
        ));

        // Check if the player leveled up
        if (newLevel > currentLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);

            // If the skill is "Player", send the stats message
            if (skill.equalsIgnoreCase("Player")) {
                sendPlayerStatsMessage(player, newLevel);
            }
        }
    }
    public void setXP(Player player, String skill, int xp) {
        UUID uuid = player.getUniqueId();

        // Load current XP and calculate current level
        int currentXP = loadXP(uuid, skill);
        int currentLevel = calculateLevel(currentXP);

        // Ensure the XP database exists
        createDatabase(uuid, skill);

        // Save the new XP value
        saveXP(uuid, skill, xp);

        // Calculate the new level based on the new XP
        int newLevel = calculateLevel(xp);

        // Determine the XP needed for the next level
        int nextLevelXP = (newLevel < xpThresholds.length) ? xpThresholds[newLevel] : -1; // -1 indicates max level

        // Determine the message to display for next level
        String nextLevelMessage = (nextLevelXP > -1) ? (nextLevelXP - xp) + " XP" : "Max Level Reached";

        // Check if the player has leveled up
        if (newLevel > currentLevel) {
            // Play level-up sound
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);

            // Optionally, send a message to the player about the level-up
            player.sendMessage(ChatColor.GREEN + "Congratulations! You have reached level " + newLevel + " in " + skill + "!");
        }
        if (skill.equalsIgnoreCase("Oxygen")) {
            return;
        }
        // If the skill is not "Player", distribute a percentage to the "Player" skill
        if (!skill.equalsIgnoreCase("Player")) {
            double additionalXP = xp * 0.10; // 10% of the set XP
            addXPToSkill(player, "Player", additionalXP);
        }

        // Send an action bar message to the player with their updated XP and level
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                ChatColor.GREEN + skill + " (" + xp + " XP / " + nextLevelMessage + ") | " +
                        ChatColor.AQUA + "Level: " + newLevel
        ));
    }

    private void sendPlayerStatsMessage(Player player, int level) {
        // Damage increase per level: 4%
        double damageMultiplier = level * 0.02; // Total damage multiplier

        // Defense increase per level: 0.4% damage reduction per level, capped at 80%
        double damageReduction = (level * 0.001); // Total damage reduction

        // Health increase: up to double health at level 50 (max 40 HP)
        double healthMultiplier = 1 + ((Math.min(level, 50) * 0.02)); // Max double health
        double maxHealth = 20.0 * healthMultiplier;

        // Display "Level Up!" title
        player.sendTitle(ChatColor.GOLD + "Level Up!", ChatColor.YELLOW + "You are now level " + level, 10, 70, 20);

        // Restore health and saturation
        player.setSaturation(20.0f); // Max saturation

        // Apply golden apple effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1)); // 10 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0)); // 1 minute

        // Make player glow gold for 30 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0)); // 30 seconds

        // Send level up stats messages
        player.sendMessage(ChatColor.GOLD + "You leveled up to level " + level + " in the Player skill!");
        player.sendMessage(ChatColor.GREEN + "Health slightly increased");

        // Handle level-based unlocks
        XPManager xpManager = new XPManager(plugin);
        int playerLevel = xpManager.getPlayerLevel(player, "Player");

    }

    // Calculate level based on total XP
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

    // Add this method to your XPManager class
    public int getXPToNextLevel(Player player, String skill) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int currentLevel = calculateLevel(currentXP);

        // Check if the player has reached the maximum level
        if (currentLevel >= xpThresholds.length) {
            return -1; // Max level reached
        }

        int nextLevelXP = xpThresholds[currentLevel]; // XP required for next level
        int xpToNextLevel = nextLevelXP - currentXP;

        return xpToNextLevel;
    }
    public int getXP(Player player, String skill) {
        UUID uuid = player.getUniqueId();
        return loadXP(uuid, skill);
    }

    public int getPlayerLevel(Player player, String skill) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill); // Load the player's current XP for the specified skill
        return calculateLevel(currentXP); // Calculate and return the player's level
    }
    // Command handling for XP management
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

        // Handle parsing the amount for actions that require it
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
                    player.sendMessage(ChatColor.RED + "You cannot subtract more XP than you currently have.");
                    return false;
                }
                saveXP(player.getUniqueId(), skill, currentXP - amount);
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
