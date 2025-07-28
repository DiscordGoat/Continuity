package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaterLogged implements Listener {
    private final JavaPlugin plugin;

    // Tracks the last time a player received bonus air
    private final Map<UUID, Long> lastGrantTime = new HashMap<>();

    // 3 second cooldown between air grants
    private static final long GRANT_INTERVAL_MS = 3000;
    // Amount of air ticks to grant (~1 bubble)
    private static final int AIR_BUBBLE_TICKS = 30;

    public WaterLogged(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Behaves identically to the old Rebreather merit perk. While the player
     * is underwater below Y=50 their air is periodically replenished when it
     * would otherwise decrease.
     */
    @EventHandler
    public void onAirChange(EntityAirChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Get the player's active pet
        PetManager petManager = PetManager.getInstance(plugin);
        PetManager.Pet activePet = petManager.getActivePet(player);

        // Check if the player has the WATERLOGGED perk or unique trait
        if (activePet == null || !(activePet.hasPerk(PetManager.PetPerk.WATERLOGGED)
                || activePet.hasUniqueTraitPerk(PetManager.PetPerk.WATERLOGGED))) {
            return;
        }

        // Only apply when underwater and below Y=50
        if (!player.isInWater() || player.getLocation().getY() >= 50) {
            return;
        }

        // Only trigger when air is decreasing
        if (event.getAmount() >= player.getRemainingAir()) {
            return;
        }

        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        long last = lastGrantTime.getOrDefault(id, 0L);

        int talent = 0;
        if (SkillTreeManager.getInstance() != null) {
            talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.WATERLOGGED);
        }
        long interval = GRANT_INTERVAL_MS - (talent * 1000L);
        if (now - last < interval) {
            return;
        }

        int newAir = Math.min(event.getAmount() + AIR_BUBBLE_TICKS, player.getMaximumAir());
        event.setAmount(newAir);
        lastGrantTime.put(id, now);
    }
}
