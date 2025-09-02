package goat.minecraft.minecraftnew.subsystems.pets.perks;

import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Collector implements Listener {

    private final PetManager petManager;
    private final Map<UUID, Long> lastActivation = new HashMap<>();

    public Collector(JavaPlugin plugin) {
        this.petManager = PetManager.getInstance(plugin);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo() == null) return;
        if (event.getFrom().getX() == event.getTo().getX()
                && event.getFrom().getY() == event.getTo().getY()
                && event.getFrom().getZ() == event.getTo().getZ()) {
            return; // ignore pure rotation; require positional movement
        }
        long now = System.currentTimeMillis();
        Long last = lastActivation.get(player.getUniqueId());
        if (last != null && (now - last) < 1000) {
            return; // throttle to once per second while moving
        }
        PetManager.Pet activePet = petManager.getActivePet(player);
        if (activePet != null && (activePet.hasPerk(PetManager.PetPerk.COLLECTOR)
                || activePet.hasUniqueTraitPerk(PetManager.PetPerk.COLLECTOR))) {
            int talent = 0;
            if (SkillTreeManager.getInstance() != null) {
                talent = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.TAMING, Talent.COLLECTOR);
            }

            // Define the collection radius based on pet level:
            // start at 15 and increase by 1 per level, capped at 50
            int petLevel = activePet.getLevel();
            double radius = Math.min(50.0, 15.0 + petLevel);
            radius *= (1 + talent * 0.5);

            Location playerLocation = player.getLocation();
            World world = player.getWorld();

            // Find nearby items within the radius
            List<Item> nearbyItems = world.getNearbyEntities(playerLocation, radius, radius, radius,
                            entity -> entity instanceof Item)
                    .stream()
                    .map(entity -> (Item) entity)
                    .collect(Collectors.toList());

            if (nearbyItems.isEmpty()) {
                return; // No items to collect
            }

            Inventory playerInventory = player.getInventory();
            int itemsCollected = 0;

            for (Item itemEntity : nearbyItems) {
                ItemStack itemStack = itemEntity.getItemStack();

                // Attempt to add the item to the player's inventory
                HashMap<Integer, ItemStack> remaining = playerInventory.addItem(itemStack.clone());

                if (remaining.isEmpty()) {
                    // Successfully added to inventory; remove the item entity
                    itemEntity.remove();
                    itemsCollected++;
                }
            }

            // Notify the player about items collected, if any
            if (itemsCollected > 0) {
                player.playSound(player.getLocation(), Sound.BLOCK_AZALEA_PLACE, 5, 100);
                lastActivation.put(player.getUniqueId(), now);
            }
        }
    }
}
