package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Obsession implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;

    public Obsession(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
        // Register this listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PetManager.Pet activePet = petManager.getActivePet(player);

        if (activePet == null ||
                !activePet.hasPerk(PetManager.PetPerk.OBSESSION)) {
            return;
        }

        int level = activePet.getLevel();

        // Scale chance from 10% at lvl1 → 30% at lvl100
        double percent = 10.0 + (level - 1) * (20.0 / 99.0);
        double chance = percent / 100.0;

        if (Math.random() <= chance) {
            // Grant 1 hunger point (max 20)
            int newFood = Math.min(player.getFoodLevel() + 1, 20);
            player.setFoodLevel(newFood);
            // (Optional) Give a little saturation so it's usable immediately
            player.setSaturation(Math.min(player.getSaturation() + 1.0f, newFood));

            // Feedback (optional)
            player.sendMessage("§aObsession restored §6+1 hunger§a!");
        }
    }
}
