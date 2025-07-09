package goat.minecraft.minecraftnew.utils.devtools;

import java.io.*;
import java.util.UUID;

import goat.minecraft.minecraftnew.subsystems.beacon.Catalyst;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystManager;
import goat.minecraft.minecraftnew.subsystems.beacon.CatalystType;
import goat.minecraft.minecraftnew.subsystems.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.other.trims.CustomTrimEffects;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
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
import org.bukkit.scheduler.BukkitRunnable;

public class XPManager implements CommandExecutor {

    private final JavaPlugin plugin;
    private final java.util.Map<String, HotbarAnimation> activeAnimations = new java.util.HashMap<>();

    // XP thresholds for levels 0 to 100
    private final int[] xpThresholds = new int[] {
            100*4,   // Level 1
            204*4,   // Level 2
            311*4,   // Level 3
            423*4,   // Level 4
            539*4,   // Level 5
            662*4,   // Level 6
            789*4,   // Level 7
            921*4,   // Level 8
            1057*4,  // Level 9
            1199*4,  // Level 10
            1346*4,  // Level 11
            1498*4,  // Level 12
            1656*4,  // Level 13
            1819*4,  // Level 14
            1988*4,  // Level 15
            2162*4,  // Level 16
            2342*4,  // Level 17
            2528*4,  // Level 18
            2720*4,  // Level 19
            2918*4,  // Level 20
            3123*4,  // Level 21
            3334*4,  // Level 22
            3551*4,  // Level 23
            3776*4,  // Level 24
            4007*4,  // Level 25
            4245*4,  // Level 26
            4490*4,  // Level 27
            4742*4,  // Level 28
            5002*4,  // Level 29
            5269*4,  // Level 30
            5544*4,  // Level 31
            5826*4,  // Level 32
            6117*4,  // Level 33
            6416*4,  // Level 34
            6724*4,  // Level 35
            7040*4,  // Level 36
            7365*4,  // Level 37
            7699*4,  // Level 38
            8042*4,  // Level 39
            8395*4,  // Level 40
            8757*4,  // Level 41
            9129*4,  // Level 42
            9511*4,  // Level 43
            9904*4,  // Level 44
            10307*4, // Level 45
            10721*4, // Level 46
            11146*4, // Level 47
            11583*4, // Level 48
            12032*4, // Level 49
            12493*6, // Level 50
            12966*6, // Level 51
            13452*6, // Level 52
            13951*6, // Level 53
            14463*6, // Level 54
            14989*6, // Level 55
            15528*6, // Level 56
            16082*6, // Level 57
            16651*6, // Level 58
            17234*6, // Level 59
            17833*6, // Level 60
            18447*6, // Level 61
            19077*6, // Level 62
            19724*6, // Level 63
            20387*6, // Level 64
            21068*6, // Level 65
            21766*6, // Level 66
            22482*6, // Level 67
            23217*6, // Level 68
            23971*6, // Level 69
            24744*6, // Level 70
            25537*6, // Level 71
            26350*6, // Level 72
            27183*6, // Level 73
            28038*6, // Level 74
            28914*6, // Level 75
            29813*8, // Level 76
            30735*8, // Level 77
            31680*8, // Level 78
            32649*8, // Level 79
            33643*8, // Level 80
            34662*8, // Level 81
            35707*8, // Level 82
            36779*8, // Level 83
            37878*8, // Level 84
            39005*8, // Level 85
            40161*8, // Level 86
            41347*8, // Level 87
            42563*8, // Level 88
            43811*8, // Level 89
            45091*8, // Level 90
            46405*8, // Level 91
            47752*8, // Level 92
            49135*8, // Level 93
            50554*8, // Level 94
            52010*8, // Level 95
            53505*8, // Level 96
            55039*8, // Level 97
            56614*8, // Level 98
            58231*8, // Level 99
            59891*8  // Level 100
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
    // Updated sendHotbarMessage to include bonus XP info
    private void sendHotbarMessage(Player player, String skill, double xpGained, int currentXP, int xpToNextLevel, double bonusXP) {
        int finalXP = (int) xpGained;
        if(finalXP <= 0) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(
                    ChatColor.AQUA + "[+0 XP " + ChatColor.DARK_AQUA + "(0.00%)]" +
                            (bonusXP > 0 ? ChatColor.LIGHT_PURPLE + " + Bonus: " + (int) bonusXP + " XP" : "") +
                            " " + ChatColor.GREEN + skill
            ));
            return;
        }

        // We'll recalc the increment dynamically so that the animation never exceeds 7 seconds (140 ticks).
        // Calculate the player's final progress toward the next level based on the new currentXP.
        int levelFinal = calculateLevel(currentXP);
        int levelStart = getLevelStartXP(levelFinal);
        int levelEnd = getLevelEndXP(levelFinal);
        int xpNeeded = levelEnd - levelStart;
        int newXPInLevel = currentXP - levelStart;
        double finalProgressPercentage = (xpNeeded > 0) ? ((double)newXPInLevel / xpNeeded * 100.0) : 100.0;

        // Compute the previous progress (before this addition).
        int previousXP = currentXP - finalXP - (int) bonusXP;
        int previousXPInLevel = previousXP - levelStart;
        if(previousXPInLevel < 0) {
            previousXPInLevel = 0;
        }
        double previousPercentage = (xpNeeded > 0) ? ((double) previousXPInLevel / xpNeeded * 100.0) : 100.0;

        // Create a key for stacking animations.
        String key = player.getUniqueId().toString() + ":" + skill;

        // If an animation is already active for this skill, update it.
        if(activeAnimations.containsKey(key)) {
            HotbarAnimation anim = activeAnimations.get(key);
            double currentAnimatedPerc = anim.getAnimatedPercentage();
            anim.addXP(finalXP, bonusXP, finalProgressPercentage, currentAnimatedPerc);
            return;
        }

        // Otherwise, create a new animation.
        HotbarAnimation anim = new HotbarAnimation(player, skill, finalXP, bonusXP, previousPercentage, finalProgressPercentage);
        activeAnimations.put(key, anim);

        // Create the runnable.
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                anim.tickCount++;
                // Calculate remaining ticks until 7 seconds (140 ticks) are reached.
                int remainingTicks = 140 - anim.tickCount;
                if (remainingTicks <= 0) {
                    // Accelerate: if we're out of time, finish the animation.
                    anim.displayedXP = anim.finalXP;
                } else {
                    int needed = anim.finalXP - anim.displayedXP;
                    // Calculate a dynamic increment so that the remaining xp is animated over the remaining ticks.
                    int calculatedIncrement = (int) Math.ceil((double) needed / remainingTicks);
                    if (calculatedIncrement < 1) calculatedIncrement = 1;
                    anim.displayedXP += calculatedIncrement;
                    if (anim.displayedXP > anim.finalXP) {
                        anim.displayedXP = anim.finalXP;
                    }
                }
                double animatedPercentage = anim.getAnimatedPercentage();
                String bonusMessage = "";
                if(anim.bonusXP > 0) {
                    bonusMessage = ChatColor.LIGHT_PURPLE + " + Bonus: " + (int) anim.bonusXP + " XP";
                }
                String message = ChatColor.AQUA + "[+" + anim.displayedXP + " XP " + ChatColor.DARK_AQUA + "(" +
                        String.format("%.2f", animatedPercentage) + "%)]" +
                        bonusMessage + " " + ChatColor.GREEN + skill;
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                if(anim.displayedXP >= anim.finalXP) {
                    activeAnimations.remove(key);
                    cancel();
                }
            }
        };
        // Schedule the runnable and store its reference.
        runnable.runTaskTimer(plugin, 0L, 1L);
        anim.task = runnable;
    }


    public int getLevelStartXP(int level) {
        if (level <= 0) return 0;
        // If level exceeds our threshold array, return the last threshold as base.
        if (level > xpThresholds.length) return xpThresholds[xpThresholds.length - 1];
        return xpThresholds[level - 1];
    }

    /**
     * Returns the XP threshold required to reach the given level.
     * If the level is beyond our thresholds, return the highest threshold.
     */
    public int getLevelEndXP(int level) {
        if (level >= xpThresholds.length) return xpThresholds[xpThresholds.length - 1];
        return xpThresholds[level];
    }

    /**
     * Main XP-adding method. If a player gains XP:
     *  - We check if they leveled up (oldLevel < newLevel).
     *  - If so, we send them a single consolidated chat message.
     *  - If skill != "Player", we also funnel some extra XP into the "Player" skill.
     */
    public void addXP(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);

        String trimMaterial = CustomTrimEffects.getFullTrimMaterial(player);
        if (trimMaterial != null && trimMaterial.equalsIgnoreCase("Lapis")) {
            xp *= 1.25;
        }

        CatalystManager catalystManager = CatalystManager.getInstance();
        if (catalystManager != null && catalystManager.isNearCatalyst(player.getLocation(), CatalystType.PROSPERITY)) {
            Catalyst catalyst = catalystManager.findNearestCatalyst(player.getLocation(), CatalystType.PROSPERITY);
            if (catalyst != null) {
                int tier = catalystManager.getCatalystTier(catalyst);
                double bonus = 0.20 + (tier * 0.05);
                bonus = Math.min(bonus, 0.50);
                xp *= 1.0 + bonus;
            }
        }

        // Count how many 'Savant' enchantment items they have for a bonus.
        int savantCount = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && CustomEnchantmentManager.hasEnchantment(item, "Savant")) {
                savantCount++;
            }
        }
        // Calculate bonus XP (5% per Savant)
        double bonusXP = xp * 0.05 * savantCount;
        int newXP = (int) (currentXP + xp + bonusXP);

        saveXP(uuid, skill, newXP);

        int oldLevel = calculateLevel(currentXP);
        int newLevel = calculateLevel(newXP);

        // Now send the hotbar message with bonus XP information
        sendHotbarMessage(player, skill, xp, newXP, getXPToNextLevel(player, skill), bonusXP);


        // Check if we leveled up
        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);

            // ---------- ADDITIONAL CHECK FOR HOSTILITY UNLOCKS ----------
            // Only do this check if the skill is "Player"
            // (assuming that's what controls hostility tier).
            if (skill.equalsIgnoreCase("Player")) {
                int oldTier = getTierFromLevel(oldLevel);
                int newTier = getTierFromLevel(newLevel);

                if (newTier > oldTier) {
                    // They have unlocked a higher hostility tier; let them know.
                    // We’re not forcibly setting it, just telling them they can do it.
                    player.sendMessage(ChatColor.GREEN + "You have unlocked Hostility Tier "
                            + newTier + "! Use "
                            + ChatColor.YELLOW + "/hostility"
                            + ChatColor.GREEN + " to select it.");
                }
            }
            // -----------------------------------------------------------
        }

        // If skill != "Player", also add some XP to "Player"
        if (!skill.equalsIgnoreCase("Player")) {
            addXPToSkill(player, "Player", Math.max(xp * 0.1, 1));
        }
    }

    /**
     * Internal method to add XP specifically to a skill
     * (bypassing the "add some to 'Player'" recursion).
     */
    private void addXPToSkill(Player player, String skill, double xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int newXP = (int) (currentXP + xp);
        saveXP(uuid, skill, newXP);

        int oldLevel = calculateLevel(currentXP);
        int newLevel = calculateLevel(newXP);

        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);
        }
    }

    /**
     * Directly sets a player's XP for a given skill.
     */
    public void setXP(Player player, String skill, int xp) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int oldLevel = calculateLevel(currentXP);

        createDatabase(uuid, skill);
        saveXP(uuid, skill, xp);

        int newLevel = calculateLevel(xp);

        if (newLevel > oldLevel) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
            sendSkillLevelUpMessage(player, skill, newLevel);

            // Also pass some XP to "Player" if it's not the "Player" skill
            if (!skill.equalsIgnoreCase("Player")) {
                double additionalXP = xp * 0.2;
                addXPToSkill(player, "Player", additionalXP);
            }
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
        // Safety check in case XP somehow exceeds our configured thresholds
        // so levels never go beyond 100.
        if (level > 100) {
            level = 100;
        }
        return level;
    }

    public int getXPToNextLevel(Player player, String skill) {
        UUID uuid = player.getUniqueId();
        int currentXP = loadXP(uuid, skill);
        int currentLevel = calculateLevel(currentXP);

        if (currentLevel >= xpThresholds.length) {
            return 0; // Max level
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
    public void sendSkillLevelUpMessage(Player player, String skill, int newLevel) {

        //==========================
        // 1) If it's "Player", do special effects
        //==========================
        if (skill.equalsIgnoreCase("Player")) {
            player.setSaturation(20.0f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 600, 0));

            // Recalculate health using central manager
            goat.minecraft.minecraftnew.subsystems.health.HealthManager.getInstance(
                    plugin, this).recalculate(player);
        }

        //==========================
        // 2) Build the chat message
        //==========================
        String borderTop    = ChatColor.DARK_AQUA + "╔═════════════════════╗";
        String borderBottom = ChatColor.DARK_AQUA + "╚═════════════════════╝";

        StringBuilder body = new StringBuilder();

        body.append(ChatColor.DARK_AQUA).append("   ❖ ").append(ChatColor.WHITE)
                .append("Level Up: ").append(ChatColor.AQUA).append("[").append(skill).append("] ")
                .append(ChatColor.WHITE).append("→ Level ").append(ChatColor.YELLOW).append(newLevel)
                .append("\n\n");

        // Then skill-specific details
        switch (skill.toLowerCase()) {
            case "player":
                goat.minecraft.minecraftnew.subsystems.health.HealthManager.getInstance(
                        plugin, this).recalculate(player);
                double newMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                int displayHealth = (int) newMaxHealth + 1;
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.GREEN).append("Max Health ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.GREEN).append(displayHealth).append(" HP.\n");
                break;


            case "combat":
                double damageMult = 1.0 + (0.03 * newLevel);
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.RED).append("Damage Multiplier ")
                        .append(ChatColor.WHITE).append("is now ")
                        .append(ChatColor.RED).append(String.format("%.2f", damageMult)).append("x.\n");
                break;

            case "fishing":
                double seaChance = (double) newLevel / 2;
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
                int doubleLogsChance = newLevel;
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
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.DARK_GRAY).append("Repair Amount")
                        .append(ChatColor.WHITE).append(" is now ")
                        .append(ChatColor.YELLOW).append("" + (25+newLevel));
                break;
            case "brewing":
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.LIGHT_PURPLE).append("Bonus Potion Duration")
                        .append(ChatColor.WHITE).append(" is now ")
                        .append(ChatColor.YELLOW).append("" + (newLevel * 10));
                break;
            case "taming":
                body.append(ChatColor.WHITE).append("Your ")
                        .append(ChatColor.LIGHT_PURPLE).append("Bonus Pet XP")
                        .append(ChatColor.WHITE).append(" is now ")
                        .append(ChatColor.YELLOW).append("" + (newLevel) + "%");
                break;
            default:
                body.append(ChatColor.WHITE).append("Enjoy your new level in ")
                        .append(skill).append("!\n");
                break;
        }

        player.sendMessage(borderTop);
        player.sendMessage(body.toString().trim());
        player.sendMessage(borderBottom);

        // Award a merit point for leveling up the Player skill
        if (skill.equalsIgnoreCase("Player")) {
            PlayerMeritManager meritManager = PlayerMeritManager.getInstance(plugin);
            UUID id = player.getUniqueId();
            int newPoints = meritManager.getMeritPoints(id) + 1;
            meritManager.setMeritPoints(id, newPoints);
            player.sendMessage(ChatColor.GOLD + "You earned a merit point! (" + newPoints + ")");
        }
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

    // =================================================
    // ===============  HOSTILITY TIER UTILITY  ========
    // =================================================
    /**
     * Figures out which Hostility Tier a given Player-level unlocks:
     *
     *  - Level < 10 => Tier 1
     *  - Level = 10..19 => Tier 2
     *  - Level = 20..29 => Tier 3
     *  ...
     *  - Level = 90..99 => Tier 10
     *  - Level >= 100 => still Tier 10, but doesn't unlock anything new at 100
     */
    public int getTierFromLevel(int level) {
        if (level < 10) {
            return 1;
        } else if (level >= 90) {
            return 10;
        } else {
            // e.g. Level=10 => 2, 20 => 3, 30 =>4, ... up to 80 =>9
            return (level / 10) + 1;
        }

    }

    private class HotbarAnimation {
        Player player;
        String skill;
        int finalXP;          // Total xp to animate for this batch (sum of consecutive additions)
        int displayedXP;      // Current animated xp
        double previousPercentage; // Baseline percentage when this batch began.
        double finalPercentage;    // Target percentage (computed from the player's new xp total).
        double bonusXP;            // Bonus xp associated with this batch.
        int tickCount;             // Number of ticks elapsed.
        BukkitRunnable task;

        HotbarAnimation(Player player, String skill, int initialXP, double bonus, double prevPerc, double finalPerc) {
            this.player = player;
            this.skill = skill;
            this.finalXP = initialXP;
            this.bonusXP = bonus;
            this.displayedXP = 0;
            this.previousPercentage = prevPerc;
            this.finalPercentage = finalPerc;
            this.tickCount = 0;
        }

        // Interpolate the current animated percentage from previousPercentage to finalPercentage.
        double getAnimatedPercentage() {
            if (finalXP == 0) return previousPercentage;
            return previousPercentage + ((double) displayedXP / finalXP) * (finalPercentage - previousPercentage);
        }

        // When new xp is added during an active animation.
        void addXP(int xp, double bonus, double newFinalPercentage, double newPrevPercentage) {
            // Set new baseline from current animated percentage.
            this.previousPercentage = newPrevPercentage;
            this.finalXP += xp;
            this.bonusXP += bonus;
            this.finalPercentage = newFinalPercentage;
        }
    }
}
