package goat.minecraft.minecraftnew.other.qol;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Sleep implements Listener {

    private final JavaPlugin plugin;

    // Keep track of who started a vote, who voted, and cooldowns
    private Player voteInitiator = null;
    private final Set<UUID> yesVotes = new HashSet<>();
    private final Set<UUID> noVotes = new HashSet<>();

    private final HashMap<UUID, Long> lastVoteTime = new HashMap<>();
    private static final long VOTE_COOLDOWN = 10 * 60 * 1000; // 10 minutes, in ms

    // Flag to indicate if a vote is currently active
    private boolean voteInProgress = false;

    public Sleep(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle main-hand interactions and ensure the player is sneaking (shift-clicking)
        if (event.getHand() == EquipmentSlot.HAND && event.getPlayer().isSneaking()) {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
    
            // Check if clicked block is a bed and it's day (time < 12541)
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK
                    && event.getClickedBlock() != null
                    && event.getClickedBlock().getType().name().contains("BED")
                    && player.getWorld().getTime() < 12541) {
    
                // Check cooldown for the player initiating the vote
                long currentTime = System.currentTimeMillis();
                if (lastVoteTime.containsKey(playerUUID)) {
                    long timeSinceLastVote = currentTime - lastVoteTime.get(playerUUID);
                    if (timeSinceLastVote < VOTE_COOLDOWN) {
                        long secondsLeft = (VOTE_COOLDOWN - timeSinceLastVote) / 1000;
                        player.sendMessage(ChatColor.RED + "You must wait " + secondsLeft + "s before starting another vote!");
                        return;
                    }
                }
    
                // If a vote is already in progress, don't start another
                if (voteInProgress) {
                    player.sendMessage(ChatColor.RED + "A vote to skip to night is already in progress!");
                    return;
                }
    
                // Start a new vote
                voteInProgress = true;
                voteInitiator = player;
                yesVotes.clear();
                noVotes.clear();
                yesVotes.add(playerUUID); // Let the initiator auto-vote yes if you wish
    
                lastVoteTime.put(playerUUID, currentTime);
    
                // Broadcast so people know how to vote
                Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " has started a vote to skip to night! "
                        + "Type \"yes\" or \"no\" in chat to cast your vote. (10s remains!)");
    
                // After 10 seconds, process the results
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        voteInProgress = false;
                        handleVoteResults(player.getWorld());
                    }
                }.runTaskLater(plugin, 200L); // 200 ticks = 10 seconds
            }
        }
    }

    /**
     * Listen for players typing "yes" or "no" in chat if a vote is in progress.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!voteInProgress) return;

        String message = event.getMessage().trim().toLowerCase();
        if (!message.equals("yes") && !message.equals("no")) {
            return; // ignore other messages
        }

        Player voter = event.getPlayer();
        UUID voterID = voter.getUniqueId();

        // Add them to the appropriate set
        if (message.equals("yes")) {
            yesVotes.add(voterID);
            noVotes.remove(voterID);
        } else if (message.equals("no")) {
            noVotes.add(voterID);
            yesVotes.remove(voterID);
        }
        // (No immediate broadcast is strictly necessary, but you could add one if you want.)
    }

    private void handleVoteResults(World world) {
        // If no one voted "no" => accelerate to night in a "timelapse."
        // That includes the scenario: no votes at all (besides possibly the initiator's auto-yes).
        if (noVotes.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.GREEN + "No negative votes! Time-lapse to night...");
            accelerateTimeToNight(world);
        } else {
            // If anyone votes no, the vote fails. The initiator is on cooldown for 10 min.
            Bukkit.broadcastMessage(ChatColor.RED + "The vote to skip night has failed (someone voted no).");
            // The initiator’s lastVoteTime is already set when they started the vote, so it’s enforced next time.
        }
        voteInitiator = null;
    }

    /**
     * Smoothly accelerate time from current day time to 13000 (start of night).
     * You can adjust how “fast” the timelapse is by changing duration/increments.
     */
    private void accelerateTimeToNight(World world) {
        long current = world.getTime();
        final long target = 13000L;
        if (current >= target) {
            // Already at or past night time
            return;
        }
        final long difference = target - current;
        final int steps = 100; // how many increments
        final long increment = difference / steps;
        final long remainder = difference % steps;

        new BukkitRunnable() {
            int counter = 0;
            @Override
            public void run() {
                if (counter >= steps) {
                    // Add leftover if difference wasn't perfectly divisible
                    if (remainder > 0) {
                        world.setTime(world.getTime() + remainder);
                    }
                    this.cancel();
                    return;
                }
                world.setTime(world.getTime() + increment);
                counter++;
            }
        }.runTaskTimer(plugin, 0L, 1L); // increments once per tick (every 50ms)
    }
}
