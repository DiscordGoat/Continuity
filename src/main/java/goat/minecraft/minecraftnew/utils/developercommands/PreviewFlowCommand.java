package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.other.armorsets.FlowType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.*;

/**
 * Command: /previewflow <FlowType> <intensity>
 * <p>
 * Spawns a preview of Flow entities rotating around the player.
 */
public class PreviewFlowCommand implements CommandExecutor, Listener {
    private final JavaPlugin plugin;
    private final Map<UUID, Integer> tasks = new HashMap<>();
    private final Map<UUID, List<ArmorStand>> stands = new HashMap<>();

    public PreviewFlowCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Integer taskId = tasks.get(player.getUniqueId());
        if (taskId == null) {
            return;
        }
        switch (event.getAction()) {
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> {
                Bukkit.getScheduler().cancelTask(taskId);
                tasks.remove(player.getUniqueId());
                List<ArmorStand> list = stands.remove(player.getUniqueId());
                if (list != null) {
                    list.forEach(Entity::remove);
                }
                player.sendMessage(ChatColor.GRAY + "Flow preview ended.");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        if (!player.hasPermission("continuity.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /" + label + " <flow> <intensity>");
            return true;
        }

        FlowType type;
        try {
            type = FlowType.valueOf(args[0].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Unknown flow type: " + args[0]);
            return true;
        }

        int intensity;
        try {
            intensity = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Intensity must be a number.");
            return true;
        }
        if (intensity < 1) intensity = 1;
        if (intensity > 24) intensity = 24;

        Integer existing = tasks.remove(player.getUniqueId());
        if (existing != null) {
            Bukkit.getScheduler().cancelTask(existing);
            List<ArmorStand> prev = stands.remove(player.getUniqueId());
            if (prev != null) prev.forEach(Entity::remove);
        }

        List<ArmorStand> spawned = new ArrayList<>();
        Location base = player.getLocation();
        double radius = 12.0;
        for (int i = 0; i < intensity; i++) {
            double angle = 2 * Math.PI * i / intensity;
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            Location loc = base.clone().add(x, 0.5, z);
            ArmorStand stand = player.getWorld().spawn(loc, ArmorStand.class, s -> {
                s.setGravity(false);
                s.setVisible(false);
                s.setMarker(true);
                ItemStack item = type.createItem();
                if (item.getType() != Material.AIR) {
                    s.setItemInHand(item);
                }
            });
            spawned.add(stand);
        }
        stands.put(player.getUniqueId(), spawned);

        final Location[] center = {player.getLocation()};
        int finalIntensity = intensity;
        int finalIntensity1 = intensity;
        BukkitRunnable runnable = new BukkitRunnable() {
            double angle = 0;
            int tick = 0;
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    spawned.forEach(Entity::remove);
                    tasks.remove(player.getUniqueId());
                    stands.remove(player.getUniqueId());
                    return;
                }
                if (++tick % 2 == 0) {
                    center[0] = player.getLocation();
                }

                // Slow it way down:
                angle += 0.05;

                for (int i = 0; i < spawned.size(); i++) {
                    ArmorStand stand = spawned.get(i);
                    if (!stand.isValid()) continue;
                    double off = angle + 2 * Math.PI * i / spawned.size();
                    double x = radius * Math.cos(off);
                    double z = radius * Math.sin(off);
                    Location loc = center[0].clone().add(x, 0.5, z);
                    stand.teleport(loc);
                    EulerAngle pose = stand.getRightArmPose();
                    stand.setRightArmPose(new EulerAngle(
                            pose.getX() + Math.toRadians(15),
                            pose.getY(),
                            pose.getZ()
                    ));
                    stand.getWorld().spawnParticle(
                            type.getParticle(),
                            loc, 1, 0, 0, 0, 0
                    );
                }
            }
        };


        int id = runnable.runTaskTimer(plugin, 0L, 1L).getTaskId();
        tasks.put(player.getUniqueId(), id);
        player.sendMessage(ChatColor.GREEN + "Previewing flow " + type.name() + " with intensity " + intensity + ". Left click to stop.");
        return true;
    }
}
