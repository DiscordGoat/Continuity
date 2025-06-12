package goat.minecraft.minecraftnew.resourcepack;

import goat.minecraft.minecraftnew.MinecraftNew;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Handles prompting players to download the embedded Continuity resource pack on join.
 */
public class ResourcePackHandler implements Listener {
    private final MinecraftNew plugin;
    private byte[] sha1;
    private String dataUri;

    public ResourcePackHandler(MinecraftNew plugin) {
        this.plugin = plugin;
        loadPack();
    }

    private void loadPack() {
        try {
            byte[] bytes = zipEmbeddedDirectory("Continuity/");
            if (bytes == null) {
                plugin.getLogger().warning("Embedded resource pack not found.");
                return;
            }
            this.sha1 = MessageDigest.getInstance("SHA-1").digest(bytes);
            this.dataUri = "data:application/zip;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load resource pack: " + e.getMessage());
        }
    }

    private byte[] zipEmbeddedDirectory(String dir) throws Exception {
        var codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
        if (codeSource == null) return null;
        File jarFile = new File(codeSource.getLocation().toURI());
        try (JarFile jar = new JarFile(jarFile); ByteArrayOutputStream baos = new ByteArrayOutputStream(); ZipOutputStream zos = new ZipOutputStream(baos)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(dir)) continue;
                String name = entry.getName().substring(dir.length());
                if (name.isEmpty()) continue;
                ZipEntry zipEntry = new ZipEntry(name);
                zos.putNextEntry(zipEntry);
                if (!entry.isDirectory()) {
                    try (InputStream in = jar.getInputStream(entry)) {
                        in.transferTo(zos);
                    }
                }
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (dataUri != null && sha1 != null) {
            player.setResourcePack(dataUri, sha1, "Would you like to use the Continuity resource pack?", false);
        } else {
            player.sendMessage("Continuity resource pack unavailable.");
        }
    }
}
