package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Spectral implements Listener {

    private final PetManager petManager;

    public Spectral(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        if (!event.getAction().toString().contains("LEFT_CLICK")) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SPECTATOR) {
            return;
        }

        PetManager.Pet active = petManager.getActivePet(player);
        if (active != null && active.hasPerk(PetManager.PetPerk.SPECTRAL)) {
            petManager.despawnPet(player);
            event.setCancelled(true);
        }
    }
}
