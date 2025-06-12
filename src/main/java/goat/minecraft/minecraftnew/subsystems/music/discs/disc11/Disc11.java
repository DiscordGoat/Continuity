package goat.minecraft.minecraftnew.subsystems.music.discs.disc11;

import goat.minecraft.minecraftnew.subsystems.combat.HostilityManager;
import goat.minecraft.minecraftnew.subsystems.music.discs.MusicDisc;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Disc11 implements MusicDisc {
    private final JavaPlugin plugin;

    public Disc11(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getDiscMaterial() {
        return Material.MUSIC_DISC_11;
    }

    @Override
    public void onUse(Player player) {
        HostilityManager hostilityManager = HostilityManager.getInstance(plugin);
        hostilityManager.setPlayerTier(player, 20);
        Bukkit.broadcastMessage(ChatColor.DARK_RED + "Somehow, you've made monsters even angrier... Hostility set to Tier 20 for 20 minutes");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hostilityManager.setPlayerTier(player, 0);
            player.sendMessage(ChatColor.GREEN + "The increased hostility has subsided.");
        }, 20 * 60 * 20L);
    }
}
