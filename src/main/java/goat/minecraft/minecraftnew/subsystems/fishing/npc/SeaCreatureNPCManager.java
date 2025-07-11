package goat.minecraft.minecraftnew.subsystems.fishing.npc;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.subsystems.fishing.SeaCreature;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.combat.SpawnMonsters;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeaCreatureNPCManager {
    private static SeaCreatureNPCManager instance;
    private final MinecraftNew plugin;
    private final NPCRegistry registry;
    private final XPManager xpManager;
    private final Map<UUID, NPC> active = new HashMap<>();

    private SeaCreatureNPCManager(MinecraftNew plugin) {
        this.plugin = plugin;
        this.registry = CitizensAPI.getNPCRegistry();
        this.xpManager = new XPManager(plugin);
    }

    public static SeaCreatureNPCManager getInstance(MinecraftNew plugin) {
        if (instance == null) {
            instance = new SeaCreatureNPCManager(plugin);
        }
        return instance;
    }

    public NPC spawnSeaCreatureNPC(Player target, Location loc, SeaCreature creature, int level) {
        NPC npc = registry.createNPC(creature.getEntityType(), creature.getDisplayName());
        npc.setProtected(false);
        npc.spawn(loc);
        LivingEntity entity = (LivingEntity) npc.getEntity();

        applyEquipment(entity, creature);
        SpawnMonsters.getInstance(xpManager).applyMobAttributes(entity, level);

        entity.setCustomName(ChatColor.AQUA + "[Lvl " + level + "] " + creature.getColoredDisplayName());
        entity.setCustomNameVisible(true);
        entity.setMetadata("SEA_CREATURE", new FixedMetadataValue(plugin, creature.getDisplayName()));

        npc.addTrait(new SwimmingTrait(plugin, target.getUniqueId()));
        if (creature.getEntityType() == EntityType.SKELETON) {
            npc.addTrait(new RangedAttackTrait(plugin, target.getUniqueId()));
        }

        launchTowards(entity, target.getLocation(), loc);
        entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false));

        active.put(npc.getUniqueId(), npc);
        return npc;
    }

    private void applyEquipment(LivingEntity living, SeaCreature creature) {
        PetManager petManager = PetManager.getInstance(plugin);
        ItemStack helmet = petManager.getSkullForPet(creature.getSkullName());
        ItemStack chest = SeaCreature.createDyedLeatherArmor(org.bukkit.Material.LEATHER_CHESTPLATE, creature.getArmorColor());
        ItemStack legs = SeaCreature.createDyedLeatherArmor(org.bukkit.Material.LEATHER_LEGGINGS, creature.getArmorColor());
        ItemStack boots = SeaCreature.createDyedLeatherArmor(org.bukkit.Material.LEATHER_BOOTS, creature.getArmorColor());
        Equipment eq = npcEquipment(living);
        if (eq != null) {
            eq.set(Equipment.EquipmentSlot.HELMET, helmet);
            eq.set(Equipment.EquipmentSlot.CHESTPLATE, chest);
            eq.set(Equipment.EquipmentSlot.LEGGINGS, legs);
            eq.set(Equipment.EquipmentSlot.BOOTS, boots);
        } else {
            living.getEquipment().setHelmet(helmet);
            living.getEquipment().setChestplate(chest);
            living.getEquipment().setLeggings(legs);
            living.getEquipment().setBoots(boots);
        }
        living.getEquipment().setHelmetDropChance(0);
        living.getEquipment().setChestplateDropChance(0);
        living.getEquipment().setLeggingsDropChance(0);
        living.getEquipment().setBootsDropChance(0);
    }

    private Equipment npcEquipment(LivingEntity entity) {
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
            NPC npc = CitizensAPI.getNPCRegistry().getNPC(entity);
            return npc.getOrAddTrait(Equipment.class);
        }
        return null;
    }

    private void launchTowards(LivingEntity entity, Location playerLoc, Location bobberLocation) {
        Vector direction = playerLoc.subtract(bobberLocation).toVector().normalize();
        double verticalBoost = 0.2;
        if (entity.getType() == EntityType.SQUID || entity.getType() == EntityType.GLOW_SQUID) {
            Location above = playerLoc.clone().add(0, 2, 0);
            entity.teleport(above);
            return;
        }
        double diff = playerLoc.getY() - bobberLocation.getY();
        if (diff >= 4) {
            verticalBoost += diff * 0.1;
        }
        direction.setY(verticalBoost);
        direction.multiply(2);
        entity.setVelocity(direction);
    }
}
