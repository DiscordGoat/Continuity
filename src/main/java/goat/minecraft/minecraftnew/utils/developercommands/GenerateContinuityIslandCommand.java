package goat.minecraft.minecraftnew.utils.developercommands;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.logging.Logger;

public class GenerateContinuityIslandCommand implements CommandExecutor {
    
    private static final Logger logger = Bukkit.getLogger();
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        player.sendMessage(ChatColor.YELLOW + "Installing Continuity Island world...");
        logger.info("[GenerateContinuityIsland] Starting world installation process");
        
        try {
            File worldContainer = Bukkit.getWorldContainer();
            File continuityWorldFolder = new File(worldContainer, "continuity");
            
            logger.info("[GenerateContinuityIsland] World container path: " + worldContainer.getAbsolutePath());
            
            // Check if continuity world already exists
            if (continuityWorldFolder.exists()) {
                player.sendMessage(ChatColor.YELLOW + "Continuity world already exists, removing...");
                logger.info("[GenerateContinuityIsland] Removing existing continuity world");
                deleteDirectory(continuityWorldFolder);
            }
            
            // Extract continuity world from zip
            player.sendMessage(ChatColor.YELLOW + "Extracting Continuity Island world...");
            logger.info("[GenerateContinuityIsland] Extracting world from continuity.zip");
            
            if (!extractWorldFromZip(worldContainer)) {
                player.sendMessage(ChatColor.RED + "Failed to extract world from continuity.zip!");
                return true;
            }
            
            player.sendMessage(ChatColor.GREEN + "Continuity Island world installed successfully!");
            logger.info("[GenerateContinuityIsland] World installation completed");
            
            // Reload server to apply the new world
            player.sendMessage(ChatColor.YELLOW + "Reloading server to apply Continuity world...");
            logger.info("[GenerateContinuityIsland] Initiating server reload to apply new world");
            
            Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("MinecraftNew"), () -> {
                try {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "reload");
                    logger.info("[GenerateContinuityIsland] Server reload completed");
                } catch (Exception e) {
                    logger.severe("[GenerateContinuityIsland] Error during reload: " + e.getMessage());
                }
            }, 20L); // 1 second delay
            
            player.sendMessage(ChatColor.GREEN + "Continuity world installation complete!");
            player.sendMessage(ChatColor.YELLOW + "Server is reloading... Use /continuitytp to teleport to the world once reload is finished.");
            
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Error installing world: " + e.getMessage());
            logger.severe("[GenerateContinuityIsland] Exception during installation: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    private boolean extractWorldFromZip(File worldContainer) {
        try {
            InputStream zipStream = getClass().getResourceAsStream("/continuity.zip");
            if (zipStream == null) {
                logger.severe("[GenerateContinuityIsland] Could not find continuity.zip in resources!");
                return false;
            }
            
            logger.info("[GenerateContinuityIsland] Found continuity.zip, starting world extraction...");
            
            try (ZipInputStream zis = new ZipInputStream(zipStream)) {
                ZipEntry entry;
                int fileCount = 0;
                int dirCount = 0;
                
                while ((entry = zis.getNextEntry()) != null) {
                    // Create file path directly in the continuity world folder
                    File continuityFolder = new File(worldContainer, "continuity");
                    File entryFile = new File(continuityFolder, entry.getName());
                    
                    logger.info("[GenerateContinuityIsland] Processing: " + entry.getName());
                    
                    if (entry.isDirectory()) {
                        boolean created = entryFile.mkdirs();
                        logger.info("[GenerateContinuityIsland] Created directory: " + entry.getName() + " (success: " + created + ")");
                        dirCount++;
                    } else {
                        // Ensure parent directories exist
                        File parentDir = entryFile.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            boolean created = parentDir.mkdirs();
                            logger.info("[GenerateContinuityIsland] Created parent directories for: " + entry.getName() + " (success: " + created + ")");
                        }
                        
                        // Extract file
                        try (FileOutputStream fos = new FileOutputStream(entryFile);
                             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                            
                            byte[] buffer = new byte[1024];
                            int length;
                            long totalBytes = 0;
                            
                            while ((length = zis.read(buffer)) > 0) {
                                bos.write(buffer, 0, length);
                                totalBytes += length;
                            }
                            
                            logger.info("[GenerateContinuityIsland] Extracted: " + entry.getName() + " (" + totalBytes + " bytes)");
                            fileCount++;
                        }
                    }
                    zis.closeEntry();
                }
                
                logger.info("[GenerateContinuityIsland] Extraction complete - " + fileCount + " files, " + dirCount + " directories");
                return fileCount > 0; // Success if we extracted at least one file
            }
            
        } catch (Exception e) {
            logger.severe("[GenerateContinuityIsland] Error extracting world: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void deleteDirectory(File directory) {
        try {
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            deleteDirectory(file);
                        } else {
                            file.delete();
                        }
                    }
                }
                directory.delete();
                logger.info("[GenerateContinuityIsland] Deleted directory: " + directory.getName());
            }
        } catch (Exception e) {
            logger.severe("[GenerateContinuityIsland] Error deleting directory: " + e.getMessage());
        }
    }
}