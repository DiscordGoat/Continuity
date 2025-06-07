package goat.minecraft.minecraftnew.utils.developercommands;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.UUID;

/**
 * Developer command that spawns a zombie which follows its owner
 * and attacks anything that hurts the owner. Uses NMS APIs.
 */
public class ZombiePetCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player bPlayer = (Player) sender;
        ServerLevel nmsWorld = ((CraftWorld) bPlayer.getWorld()).getHandle();
        ZombiePet pet = new ZombiePet(nmsWorld, bPlayer);
        pet.setPos(bPlayer.getLocation().getX(), bPlayer.getLocation().getY(), bPlayer.getLocation().getZ());
        nmsWorld.addFreshEntity(pet);
        sender.sendMessage(ChatColor.GREEN + "Zombie pet summoned!");
        return true;
    }

    private static class ZombiePet extends Zombie {
        private final UUID ownerId;
        private final Player owner;

        ZombiePet(ServerLevel world, Player owner) {
            super(EntityType.ZOMBIE, world);
            this.ownerId = owner.getUniqueId();
            this.owner = owner;
            this.setPersistenceRequired(true);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FollowOwnerGoal(this, owner, 1.1D));
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
            this.targetSelector.addGoal(0, new OwnerAttackedGoal(this, owner));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
    }

    /** Goal that makes the zombie follow its owner */
    private static class FollowOwnerGoal extends Goal {
        private final Zombie zombie;
        private final Player owner;
        private final double speed;

        FollowOwnerGoal(Zombie zombie, Player owner, double speed) {
            this.zombie = zombie;
            this.owner = owner;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return owner.isOnline() && owner.getLocation().distanceSquared(zombie.getBukkitEntity().getLocation()) > 4;
        }

        @Override
        public void tick() {
            Vec3 vec = new Vec3(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ());
            zombie.getNavigation().moveTo(vec.x, vec.y, vec.z, speed);
        }
    }

    /** Goal that targets entities which attack the owner */
    private static class OwnerAttackedGoal extends Goal {
        private final Zombie zombie;
        private final Player owner;

        OwnerAttackedGoal(Zombie zombie, Player owner) {
            this.zombie = zombie;
            this.owner = owner;
        }

        @Override
        public boolean canUse() {
            return false; // activated externally
        }

        void setTarget(LivingEntity attacker) {
            if (attacker != null) {
                zombie.setTarget(((CraftZombie) zombie.getBukkitEntity()).getHandle().getLevel().getEntity(attacker.getUniqueId()));
            }
        }
    }

    static {
        // Hook into Bukkit events to redirect attackers to the pet
        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerDamaged(org.bukkit.event.entity.EntityDamageByEntityEvent event) {
                if (!(event.getEntity() instanceof Player player)) return;
                for (org.bukkit.entity.Entity ent : player.getWorld().getEntities()) {
                    if (ent instanceof org.bukkit.entity.Zombie bukkitZombie) {
                        net.minecraft.world.entity.Entity nms = ((CraftZombie) bukkitZombie).getHandle();
                        if (nms instanceof ZombiePet pet && pet.ownerId.equals(player.getUniqueId())) {
                            if (event.getDamager() instanceof LivingEntity attacker) {
                                pet.setTarget(((CraftZombie) bukkitZombie).getHandle().getLevel().getEntity(attacker.getUniqueId()));
                            }
                        }
                    }
                }
            }
        }, Bukkit.getPluginManager().getPlugin("MinecraftNew"));
    }
}
