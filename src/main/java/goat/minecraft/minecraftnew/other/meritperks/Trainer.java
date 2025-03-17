package goat.minecraft.minecraftnew.other.meritperks;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class Trainer implements Listener {

    private final PlayerMeritManager playerData;

    public Trainer(PlayerMeritManager playerData) {
        this.playerData = playerData;
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        // Check if the player has purchased the Trainer perk.
        if (playerData.hasPerk(player.getUniqueId(), "Trainer")) {
            // Get the XP amount from the orb.
            int orbXP = event.getAmount();

            PetManager petManager = PetManager.getInstance(MinecraftNew.getInstance());
            PetManager.Pet activePet = petManager.getActivePet(player);
            if(activePet != null){
                activePet.addXP(1);
            }

            // For now, we'll simply notify the player that the Trainer perk is active.
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }
    }
}
