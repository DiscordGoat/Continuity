package goat.minecraft.minecraftnew.subsystems.music.discs.mall;

import goat.minecraft.minecraftnew.subsystems.music.discs.MusicDisc;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MallDisc implements MusicDisc {
    private final JavaPlugin plugin;

    public MallDisc(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getDiscMaterial() {
        return Material.MUSIC_DISC_MALL;
    }

    @Override
    public void onUse(Player player) {
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_MALL, 3.0f, 1.0f);
        Bukkit.getWorlds().forEach(world -> {
            world.setStorm(true);
            world.setWeatherDuration(10 * 60 * 20);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        });

        Bukkit.broadcastMessage(ChatColor.AQUA + "A soothing rainstorm has begun, and monster spawns are disabled for 10 minutes!");
        player.sendMessage(ChatColor.GREEN + "You feel empowered by the rain!");

        int durationTicks = 40 * 60 * 20;
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONDUIT_POWER, durationTicks, 0, true, false, false));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_MOB_SPAWNING, true));
            Bukkit.broadcastMessage(ChatColor.RED + "The rainstorm has ended, and monsters are free to spawn again.");
        }, durationTicks);
    }
}
