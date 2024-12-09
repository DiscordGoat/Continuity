package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class Antidote implements Listener {

    private final PetManager petManager;
    private final Random random = new Random();

    private static final List<PotionEffectType> NEGATIVE_EFFECTS = List.of(
            PotionEffectType.BLINDNESS,
            PotionEffectType.CONFUSION, // Nausea
            PotionEffectType.HUNGER,
            PotionEffectType.POISON,
            PotionEffectType.SLOW,
            PotionEffectType.SLOW_DIGGING, // Mining Fatigue
            PotionEffectType.UNLUCK,
            PotionEffectType.WEAKNESS,
            PotionEffectType.WITHER
    );

    public Antidote(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    private void removeRandomNegativePotionEffect(Player player, int petLevel) {
        // Collect active negative potion effects
        List<PotionEffect> activeNegativeEffects = player.getActivePotionEffects().stream()
                .filter(effect -> NEGATIVE_EFFECTS.contains(effect.getType()))
                .toList();

        // If there are no negative effects, exit
        if (activeNegativeEffects.isEmpty()) {
            return;
        }


        // Randomly pick one negative effect to remove
        PotionEffect effectToRemove = activeNegativeEffects.get(random.nextInt(activeNegativeEffects.size()));
        player.removePotionEffect(effectToRemove.getType());

        // Notify the player
        player.sendMessage(ChatColor.AQUA + "Your pet's Antidote perk removed the " +
                ChatColor.RED + effectToRemove.getType().getName() + ChatColor.AQUA + " effect!");
        player.playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_MILK, 100, 100);
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Check if the player has an active pet with the Antidote perk
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet == null || !activePet.hasPerk(PetManager.PetPerk.ANTIDOTE)) {
            return;
        }
        // Remove a negative effect with a chance based on pet level
        int petLevel = activePet.getLevel();
        removeRandomNegativePotionEffect(player, petLevel);

    }
}
