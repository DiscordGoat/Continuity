package goat.minecraft.minecraftnew.subsystems.music.discs.far;

import goat.minecraft.minecraftnew.subsystems.music.discs.MusicDisc;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class FarDisc implements MusicDisc {
    private final JavaPlugin plugin;

    public FarDisc(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getDiscMaterial() {
        return Material.MUSIC_DISC_FAR;
    }

    @Override
    public void onUse(Player player) {
        player.playSound(player.getLocation(), Sound.MUSIC_DISC_FAR, 3.0f, 1.0f);
        Bukkit.broadcastMessage(ChatColor.GOLD + "Random Loot crates event Activated!");

        int durationTicks = (120 + 54) * 20;
        int intervalTicks = durationTicks / 16;

        List<NamespacedKey> lootTables = Arrays.asList(
                LootTables.BASTION_TREASURE.getKey(),
                LootTables.BASTION_OTHER.getKey(),
                LootTables.BASTION_BRIDGE.getKey(),
                LootTables.BASTION_HOGLIN_STABLE.getKey(),
                LootTables.DESERT_PYRAMID.getKey(),
                LootTables.END_CITY_TREASURE.getKey(),
                LootTables.IGLOO_CHEST.getKey(),
                LootTables.JUNGLE_TEMPLE.getKey(),
                LootTables.JUNGLE_TEMPLE_DISPENSER.getKey(),
                LootTables.ABANDONED_MINESHAFT.getKey(),
                LootTables.NETHER_BRIDGE.getKey(),
                LootTables.PILLAGER_OUTPOST.getKey(),
                LootTables.RUINED_PORTAL.getKey(),
                LootTables.SHIPWRECK_MAP.getKey(),
                LootTables.SHIPWRECK_SUPPLY.getKey(),
                LootTables.SHIPWRECK_TREASURE.getKey(),
                LootTables.STRONGHOLD_CORRIDOR.getKey(),
                LootTables.STRONGHOLD_CROSSING.getKey(),
                LootTables.STRONGHOLD_LIBRARY.getKey(),
                LootTables.UNDERWATER_RUIN_BIG.getKey(),
                LootTables.UNDERWATER_RUIN_SMALL.getKey(),
                LootTables.VILLAGE_ARMORER.getKey(),
                LootTables.VILLAGE_BUTCHER.getKey(),
                LootTables.VILLAGE_CARTOGRAPHER.getKey(),
                LootTables.VILLAGE_DESERT_HOUSE.getKey(),
                LootTables.VILLAGE_FISHER.getKey(),
                LootTables.VILLAGE_FLETCHER.getKey(),
                LootTables.VILLAGE_MASON.getKey(),
                LootTables.VILLAGE_PLAINS_HOUSE.getKey(),
                LootTables.VILLAGE_SAVANNA_HOUSE.getKey(),
                LootTables.VILLAGE_SHEPHERD.getKey(),
                LootTables.VILLAGE_SNOWY_HOUSE.getKey(),
                LootTables.VILLAGE_TAIGA_HOUSE.getKey(),
                LootTables.VILLAGE_TANNERY.getKey(),
                LootTables.VILLAGE_TEMPLE.getKey(),
                LootTables.VILLAGE_TOOLSMITH.getKey(),
                LootTables.VILLAGE_WEAPONSMITH.getKey(),
                LootTables.WOODLAND_MANSION.getKey()
        );

        new BukkitRunnable() {
            int chestsSpawned = 0;

            @Override
            public void run() {
                if (chestsSpawned >= 16 || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                NamespacedKey randomLootTable = lootTables.get(new Random().nextInt(lootTables.size()));
                ItemStack chestItem = new ItemStack(Material.CHEST);
                ItemMeta meta = chestItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "Loot Chest: " + randomLootTable.getKey());
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "loot_table"), PersistentDataType.STRING, randomLootTable.toString());
                    chestItem.setItemMeta(meta);
                }

                Location dropLocation = player.getLocation();
                player.getWorld().dropItemNaturally(dropLocation, chestItem);

                dropLocation.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, dropLocation, 50, 0.5, 1, 0.5, 0.1);
                player.getWorld().playSound(dropLocation, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);

                chestsSpawned++;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerInteract(PlayerInteractEvent event) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Block block = event.getClickedBlock();
                    ItemStack item = event.getItem();
                    if (item != null && item.getType() == Material.CHEST) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "loot_table"), PersistentDataType.STRING)) {
                            String lootTableKey = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "loot_table"), PersistentDataType.STRING);
                            if (block != null && lootTableKey != null) {
                                NamespacedKey lootTable = NamespacedKey.fromString(lootTableKey);
                                LootTable table = Bukkit.getLootTable(lootTable);
                                if (table != null) {
                                    Location location = block.getLocation();
                                    Collection<ItemStack> loot = table.populateLoot(new Random(), new LootContext.Builder(location).build());
                                    loot.forEach(itemStack -> location.getWorld().dropItemNaturally(location, itemStack));
                                    location.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location.add(0, 1, 0), 100, 0.5, 1, 0.5, 0.1);
                                    location.getWorld().playSound(location, Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                                    item.setAmount(item.getAmount() - 1);
                                }
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }, plugin);
    }
}
