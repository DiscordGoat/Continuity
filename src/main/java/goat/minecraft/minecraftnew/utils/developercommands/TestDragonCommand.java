package goat.minecraft.minecraftnew.utils.developercommands;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.cut_content.dragons.StrongDragon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class TestDragonCommand implements CommandExecutor, Listener {

    // References to our dragons for testing
    private EnderDragon initialDragon;
    private StrongDragon strongDragon;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure the command is run by a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        if (!sender.hasPermission("continuity.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return true;
        }
        Player player = (Player) sender;
        // Verify that the player is in The End
        if (!player.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            player.sendMessage(ChatColor.RED + "You must be in The End to use this command.");
            return true;
        }

        Location spawnLocation = player.getLocation();
        World endWorld = player.getWorld();
        setInitialDragon(player);
        // Summon the strong dragon a few blocks away (e.g. 30 blocks east)
        Location strongLocation = spawnLocation.clone().add(30, 0, 0);
        EnderDragon strongBukkitDragon = (EnderDragon) endWorld.spawn(strongLocation, EnderDragon.class);
        strongDragon = new StrongDragon(strongBukkitDragon);

        player.sendMessage(ChatColor.GREEN + "Strong dragon has been summoned for testing.");

        // Register this class as a listener so that we can intercept damage events.
        Bukkit.getPluginManager().registerEvents(this, MinecraftNew.getInstance());

        return true;
    }

    // Listen for damage events on the initial dragon.
    private void setInitialDragon(Player player) {
        World world = player.getWorld();
        for (EnderDragon dragon : world.getEntitiesByClass(EnderDragon.class)) {
            if (dragon.getCustomName() != null &&
                    dragon.getCustomName().equals(ChatColor.DARK_RED + "[Lv: 1000] Ender Dragon")) {
                initialDragon = dragon;
                player.sendMessage(ChatColor.AQUA + "Initial dragon found and set.");
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "No initial dragon found with the specified name.");
    }

    @EventHandler
    public void onInitialDragonDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;

        if (event.getDamager() instanceof Player) {
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();
            }
        }

        if (attacker == null) {
            return;
        }

        // If initialDragon or strongDragon are not set, exit.
        if (initialDragon == null || strongDragon == null) {
            return;
        }

        // Ensure the damaged entity is the initial dragon.
        if (!event.getEntity().equals(initialDragon)) {
            return;
        }

        // Activate the custom phase on the strong dragon.
        strongDragon.performCounterattack(attacker);

    }
}
