package goat.minecraft.minecraftnew.other.resourcepack;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ResourcePackListener implements Listener {

    private static final String PACK_URL      =
            "https://discordgoat.github.io/Continuity/continuity.zip";
    private static final String PACK_HASH_HEX =
            "f88d0d8b0afe1f800e1f7f810f3bc4566954ade2";
    private static final byte[] PACK_HASH     =
            hexStringToByteArray(PACK_HASH_HEX);


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // prompt first, then forced flag
        String prompt = ChatColor.translateAlternateColorCodes(
                '&',
                "&Latest Continuity Textures"
        );
        player.setResourcePack(
                PACK_URL,    // String url
                PACK_HASH,   // byte[] sha1
                prompt,      // String prompt

                true         // boolean forced
        );
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(s.charAt(i), 16);
            int lo = Character.digit(s.charAt(i+1), 16);
            data[i/2] = (byte)((hi << 4) + lo);
        }
        return data;
    }
}
