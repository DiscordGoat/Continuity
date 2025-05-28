package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Earthworm implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;

    public Earthworm(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
        // Register this listener
        plugin.getServer().getPluginManager()
                .registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material mat = event.getBlock().getType();

        // Only apply on dirt, grass block, sand or gravel
        if (!(mat == Material.DIRT
                || mat == Material.GRASS_BLOCK
                || mat == Material.SAND
                || mat == Material.GRAVEL)) {
            return;
        }

        // Check for active pet and Earthworm perk
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null
                || !activePet.hasPerk(PetManager.PetPerk.EARTHWORM)) {
            return;
        }

        // Give Haste 8 (amplifier 7) for 10 ticks (0.5s)
        PotionEffect haste = new PotionEffect(
                PotionEffectType.FAST_DIGGING,
                10,     // duration in ticks
                50,      // amplifier (8 = amplifier+1)
                false,  // ambient
                false   // particles
        );
        player.addPotionEffect(haste, true);
    }
}
