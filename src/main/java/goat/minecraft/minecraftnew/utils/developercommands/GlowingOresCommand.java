package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Command to highlight nearby ores with a colored glowing effect.
 * Usage: /glowingores <seconds>
 */
public class GlowingOresCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final Scoreboard scoreboard;
    private final Map<ChatColor, Team> teams = new HashMap<>();

    public GlowingOresCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        plugin.getCommand("glowingores").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        int seconds = 5;
        if (args.length > 0) {
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Seconds must be a number.");
                return true;
            }
        }
        highlightOres(player, seconds);
        player.sendMessage(ChatColor.GRAY + "Highlighted nearby ores for " + seconds + "s.");
        return true;
    }

    private void highlightOres(Player player, int seconds) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        int radius = 10;

        Set<Material> ores = EnumSet.of(
                Material.COAL_ORE,
                Material.IRON_ORE,
                Material.GOLD_ORE,
                Material.DIAMOND_ORE,
                Material.EMERALD_ORE,
                Material.LAPIS_ORE,
                Material.REDSTONE_ORE,
                Material.NETHER_GOLD_ORE,
                Material.NETHER_QUARTZ_ORE,
                Material.ANCIENT_DEBRIS,
                Material.COPPER_ORE,
                Material.DEEPSLATE_COAL_ORE,
                Material.DEEPSLATE_IRON_ORE,
                Material.DEEPSLATE_GOLD_ORE,
                Material.DEEPSLATE_DIAMOND_ORE,
                Material.DEEPSLATE_EMERALD_ORE,
                Material.DEEPSLATE_LAPIS_ORE,
                Material.DEEPSLATE_REDSTONE_ORE,
                Material.DEEPSLATE_COPPER_ORE
        );

        List<FallingBlock> spawned = new ArrayList<>();
        for (int x = loc.getBlockX() - radius; x <= loc.getBlockX() + radius; x++) {
            for (int y = loc.getBlockY() - radius; y <= loc.getBlockY() + radius; y++) {
                for (int z = loc.getBlockZ() - radius; z <= loc.getBlockZ() + radius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    if (ores.contains(block.getType())) {
                        ChatColor color = getColorForOre(block.getType());
                        Team team = getTeam(color);
                        Location spawn = block.getLocation().add(0.5, 0.0, 0.5);
                        FallingBlock fb = world.spawnFallingBlock(spawn, block.getBlockData());
                        fb.setGravity(false);
                        fb.setDropItem(false);
                        fb.setHurtEntities(false);
                        fb.setInvulnerable(true);
                        fb.setGlowing(true);
                        team.addEntry(fb.getUniqueId().toString());
                        spawned.add(fb);
                    }
                }
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                for (FallingBlock fb : spawned) {
                    Team t = scoreboard.getEntryTeam(fb.getUniqueId().toString());
                    if (t != null) t.removeEntry(fb.getUniqueId().toString());
                    fb.remove();
                }
            }
        }.runTaskLater(plugin, seconds * 20L);
    }

    private Team getTeam(ChatColor color) {
        return teams.computeIfAbsent(color, c -> {
            String name = ("glow_" + c.name().toLowerCase()).replace('-', '_');
            if (name.length() > 16) name = name.substring(0, 16);
            Team team = scoreboard.getTeam(name);
            if (team == null) {
                team = scoreboard.registerNewTeam(name);
            }
            team.setColor(c);
            return team;
        });
    }

    private ChatColor getColorForOre(Material mat) {
        return switch (mat) {
            case COAL_ORE, DEEPSLATE_COAL_ORE -> ChatColor.DARK_GRAY;
            case IRON_ORE, DEEPSLATE_IRON_ORE -> ChatColor.YELLOW;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> ChatColor.GOLD;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> ChatColor.AQUA;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> ChatColor.GREEN;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> ChatColor.BLUE;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> ChatColor.RED;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> ChatColor.GOLD;
            case NETHER_QUARTZ_ORE -> ChatColor.WHITE;
            case ANCIENT_DEBRIS -> ChatColor.DARK_RED;
            default -> ChatColor.WHITE;
        };
    }
}
