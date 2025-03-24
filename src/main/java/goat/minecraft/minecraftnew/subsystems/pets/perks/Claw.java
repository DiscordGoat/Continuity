package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Claw implements Listener {

    private final PetManager petManager;

    public Claw(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerDealMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getDamager();
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && activePet.hasPerk(PetManager.PetPerk.CLAW)) {
            int petLevel = activePet.getLevel();
            // Calculate bonus damage: 0.5% per pet level, capped at 10%
            double bonusDamagePercent = Math.min(petLevel * 0.5, 10.0);
            double multiplier = 1 + (bonusDamagePercent / 100.0);
            double originalDamage = event.getDamage();
            double newDamage = originalDamage * multiplier;
            event.setDamage(newDamage);

            // Feedback to the player
            player.sendMessage(ChatColor.YELLOW + "Claw Perk activated: Damage increased by " + bonusDamagePercent + "%!");
            player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 1.0f);
        }
    }
}
