package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.combat.DeteriorationDamageHandler;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class Decay implements Listener {

    private final PetManager petManager;
    private final JavaPlugin plugin;
    private final PlayerMeritManager meritManager;

    public Decay(JavaPlugin plugin) {
        this.plugin = plugin;
        this.petManager = PetManager.getInstance(plugin);
        this.meritManager = PlayerMeritManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerHitMob(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.DECAY)) return;

        int stacks = 40;
        if (meritManager.hasPerk(player.getUniqueId(), "Decay Mastery")) {
            stacks *= 2;
        }
        DeteriorationDamageHandler.getInstance().addDeterioration(target, stacks);
    }
}
