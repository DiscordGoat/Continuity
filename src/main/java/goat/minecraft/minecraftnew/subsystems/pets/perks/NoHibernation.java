package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NoHibernation implements Listener {

    private final PetManager petManager;

    public NoHibernation(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
        startRegenerationTask();
    }

    private void startRegenerationTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PetManager.Pet activePet = petManager.getActivePet(player);
                    if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.NO_HIBERNATION)) continue;

                    int petLevel = activePet.getLevel();
                    double regenerationMultiplier = Math.min(petLevel / 100.0, 1.0); // Cap at 1.0 for max regeneration rate

                    if (regenerationMultiplier < 1.0) {
                        applyFasterNaturalRegeneration(player, regenerationMultiplier);
                    } else {
                        applyInstantRegeneration(player);
                    }
                }
            }
        }.runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(this.getClass()), 0L, 20L); // Check every second
    }

    private void applyFasterNaturalRegeneration(Player player, double multiplier) {
        // Base regeneration rate is 2 seconds, adjusted by multiplier
        if (player.getHealth() < player.getMaxHealth() && player.getFoodLevel() > 0) {
            double newRegenRate = 200.0 / (1 + multiplier); // Faster regeneration based on multiplier

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.getHealth() < player.getMaxHealth() && player.getFoodLevel() > 0) {
                        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + 2)); // Heal 2 health (1 full heart)

                        if (player.getSaturation() > 0) {
                            player.setSaturation(player.getSaturation() - 1);
                        } else {
                            player.setFoodLevel(player.getFoodLevel() - 1);
                        }

                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f); // Quiet heartbeat sound
                    } else {
                        cancel();
                    }
                }
            }.runTaskTimerAsynchronously(JavaPlugin.getProvidingPlugin(this.getClass()), 0L, (long) (newRegenRate * 20));
        }
    }

    private void applyInstantRegeneration(Player player) {
        if (player.getHealth() < player.getMaxHealth() && player.getFoodLevel() > 0) {
            double healthToRestore = player.getMaxHealth() - player.getHealth();
            double heartsToRestore = Math.min(healthToRestore, 2); // Heal up to 2 health (1 full heart)

            player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + heartsToRestore));

            if (player.getSaturation() > 0) {
                player.setSaturation(player.getSaturation() - 1);
            } else {
                player.setFoodLevel(player.getFoodLevel() - 1);
            }

            // Notify the player with a heartbeat sound
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.5f);
        }
    }
}
