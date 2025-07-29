package goat.minecraft.minecraftnew.other.qol;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class Lavabug implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final Set<String> cleansedChunks = new HashSet<>();
    private static final int CHUNK_RADIUS = 8;
    private static final int LAVA_CLEAR_Y_THRESHOLD = -50;

    public Lavabug(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return;
        }

        if (player.getLocation().getY() > LAVA_CLEAR_Y_THRESHOLD) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                processChunksAroundPlayer(player);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void processChunksAroundPlayer(Player player) {
        World world = player.getWorld();
        Chunk playerChunk = player.getLocation().getChunk();
        int centerX = playerChunk.getX();
        int centerZ = playerChunk.getZ();

        for (int x = centerX - CHUNK_RADIUS; x <= centerX + CHUNK_RADIUS; x++) {
            for (int z = centerZ - CHUNK_RADIUS; z <= centerZ + CHUNK_RADIUS; z++) {
                String chunkKey = world.getName() + ":" + x + ":" + z;
                
                if (!cleansedChunks.contains(chunkKey)) {
                    if (world.isChunkLoaded(x, z)) {
                        Chunk chunk = world.getChunkAt(x, z);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                clearLavaInChunkGradually(chunk, chunkKey);
                            }
                        }.runTask(plugin);
                    }
                }
            }
        }
    }

    private void clearLavaInChunk(Chunk chunk, String chunkKey) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        int blocksCleared = 0;

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int z = chunkZ; z < chunkZ + 16; z++) {
                for (int y = world.getMinHeight(); y <= LAVA_CLEAR_Y_THRESHOLD; y++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (block.getType() == Material.LAVA) {
                        block.setType(Material.AIR);
                        blocksCleared++;
                    }
                }
            }
        }

        cleansedChunks.add(chunkKey);

        if (blocksCleared > 0) {
            plugin.getLogger().info("Cleared " + blocksCleared + " lava blocks from chunk " + chunkKey);
        }
    }

    private void clearLavaInChunkGradually(Chunk chunk, String chunkKey) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;

        new BukkitRunnable() {
            int y = world.getMinHeight();
            int cleared = 0;

            @Override
            public void run() {
                if (y > LAVA_CLEAR_Y_THRESHOLD) {
                    cleansedChunks.add(chunkKey);
                    if (cleared > 0) {
                        plugin.getLogger().info("Cleared " + cleared + " lava blocks from chunk " + chunkKey);
                    }
                    cancel();
                    return;
                }

                for (int x = chunkX; x < chunkX + 16; x++) {
                    for (int z = chunkZ; z < chunkZ + 16; z++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.LAVA) {
                            block.setType(Material.AIR);
                            cleared++;
                        }
                    }
                }
                y++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("clearlava")) {
            return false;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("§eClearing lava in 64x64 area around you...");

        new BukkitRunnable() {
            @Override
            public void run() {
                clearLavaInArea(player);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    private void clearLavaInArea(Player player) {
        Location center = player.getLocation();
        World world = center.getWorld();
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();
        
        int totalCleared = 0;
        
        new BukkitRunnable() {
            int x = centerX - 32;
            int blocksCleared = 0;
            
            @Override
            public void run() {
                if (x >= centerX + 32) {
                    player.sendMessage("§aLava clearing complete! Cleared " + blocksCleared + " lava blocks in 64x64 area.");
                    this.cancel();
                    return;
                }
                
                for (int z = centerZ - 32; z < centerZ + 32; z++) {
                    for (int y = world.getMinHeight(); y <= LAVA_CLEAR_Y_THRESHOLD; y++) {
                        Block block = world.getBlockAt(x, y, z);
                        if (block.getType() == Material.LAVA) {
                            block.setType(Material.AIR);
                            blocksCleared++;
                        }
                    }
                }
                x++;
                
                if (x % 8 == 0) {
                    player.sendMessage("§7Progress: " + ((x - (centerX - 32)) * 100 / 64) + "%");
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}