package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

public class WaterLogged implements Listener {
    private final JavaPlugin plugin;

    public WaterLogged(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerSwim(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the WATER_LOGGED perk
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.WATERLOGGED)) {
            if (player.isSwimming()) {
                int petLevel = activePet.getLevel();

                // Apply Water Breathing effect as long as the player is swimming
                if (!player.hasPotionEffect(PotionEffectType.WATER_BREATHING)) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, petLevel * 20, 0));
                }
            }
        }
    }
}
