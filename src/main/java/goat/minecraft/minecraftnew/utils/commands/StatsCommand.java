package goat.minecraft.minecraftnew.utils.commands;

import goat.minecraft.minecraftnew.MinecraftNew;
import goat.minecraft.minecraftnew.other.PlayerResistanceManager;
import goat.minecraft.minecraftnew.other.health.HealthManager;
import goat.minecraft.minecraftnew.other.skilltree.Skill;
import goat.minecraft.minecraftnew.other.skilltree.SkillTreeManager;
import goat.minecraft.minecraftnew.other.skilltree.Talent;
import goat.minecraft.minecraftnew.subsystems.pets.PetManager;
import goat.minecraft.minecraftnew.subsystems.pets.PetTrait;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager;
import goat.minecraft.minecraftnew.subsystems.smithing.tierreforgelisteners.ReforgeManager.ReforgeTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import goat.minecraft.minecraftnew.utils.devtools.PlayerMeritManager;
import goat.minecraft.minecraftnew.subsystems.fishing.FishingUpgradeSystem;
import goat.minecraft.minecraftnew.subsystems.brewing.PotionManager;
import goat.minecraft.minecraftnew.other.enchanting.CustomEnchantmentManager;
import goat.minecraft.minecraftnew.subsystems.forestry.EffigyUpgradeSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays a GUI summarizing a player's various stats.
 */
public class StatsCommand implements CommandExecutor, Listener {

    private final MinecraftNew plugin;

    public StatsCommand(MinecraftNew plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        String title = ChatColor.stripColor(event.getView().getTitle());
        if (!"Player Stats".equals(title)) return;
        event.setCancelled(true);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        openStatsGUI(player);
        return true;
    }

    private void openStatsGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Player Stats");

        gui.setItem(0, createStatItem(Material.RED_DYE, ChatColor.RED + "Health",
                String.format("%.0f", HealthManager.getInstance(plugin).computeMaxHealth(player))));
        gui.setItem(1, createStatItem(Material.DIAMOND_SWORD, ChatColor.RED + "Damage",
                String.format("%.1f%%", computeDamageIncrease(player))));
        gui.setItem(2, createStatItem(Material.BOW, ChatColor.RED + "Arrow Damage",
                String.format("%.1f%%", computeArrowDamage(player))));
        gui.setItem(3, createStatItem(Material.SHIELD, ChatColor.AQUA + "Resistance",
                String.format("%.1f%%", PlayerResistanceManager.computeTotalResistance(player))));
        gui.setItem(4, createStatItem(Material.FEATHER, ChatColor.YELLOW + "Flight Distance",
                String.format("%.2f km", computeFlightDistance(player))));
        gui.setItem(5, createStatItem(Material.BONE, ChatColor.LIGHT_PURPLE + "Grave Chance",
                String.format("%.3f%%", computeGraveChance(player) * 100)));
        gui.setItem(6, createStatItem(Material.COD, ChatColor.AQUA + "Sea Creature Chance",
                String.format("%.2f%%", computeSeaCreatureChance(player))));
        gui.setItem(7, createStatItem(Material.CHEST, ChatColor.GOLD + "Treasure Chance",
                String.format("%.2f%%", computeTreasureChance(player))));
        gui.setItem(8, createStatItem(Material.SOUL_TORCH, ChatColor.DARK_AQUA + "Spirit Chance",
                String.format("%.2f%%", computeSpiritChance(player) * 100)));
        gui.setItem(9, createStatItem(Material.EMERALD, ChatColor.GREEN + "Discount",
                String.format("%.1f%%", computeDiscount(player) * 100)));
        gui.setItem(10, createStatItem(Material.SUGAR, ChatColor.YELLOW + "Speed",
                String.format("%.1f%%", computeSpeed(player))));
        gui.setItem(11, createStatItem(Material.POTION, ChatColor.LIGHT_PURPLE + "Brew Time Reduction",
                String.format("%.1f%%", computeBrewReduction(player))));
        gui.setItem(12, createStatItem(Material.DIAMOND_ORE, ChatColor.GRAY + "Double Ore Chance",
                String.format("%.1f%%", computeDoubleOreChance(player))));
        gui.setItem(13, createStatItem(Material.OAK_LOG, ChatColor.GRAY + "Double Log Chance",
                String.format("%.1f%%", computeDoubleLogChance(player))));
        gui.setItem(14, createStatItem(Material.WHEAT, ChatColor.GRAY + "Double Crop Chance",
                String.format("%.1f%%", computeDoubleCropChance(player))));
        gui.setItem(15, createStatItem(Material.ANVIL, ChatColor.GREEN + "Repair Amount",
                String.valueOf(computeRepairAmount(player))));
        gui.setItem(16, createStatItem(Material.EXPERIENCE_BOTTLE, ChatColor.GREEN + "Repair Quality",
                String.valueOf(computeRepairQuality(player))));

        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) {
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
        }
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) gui.setItem(i, filler);
        }

        player.openInventory(gui);
    }

    private ItemStack createStatItem(Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + value);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private double computeDamageIncrease(Player player) {
        double percent = 0.0;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.STRONG) {
            percent += pet.getTrait().getValueForRarity(pet.getTraitRarity());
        }
        ItemStack weapon = player.getInventory().getItemInMainHand();
        ReforgeManager rm = new ReforgeManager();
        ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(weapon));
        percent += tier.getWeaponDamageIncrease();
        return percent;
    }

    private double computeArrowDamage(Player player) {
        double percent = 0.0;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.getTrait() == PetTrait.PRECISE) {
            percent += pet.getTrait().getValueForRarity(pet.getTraitRarity());
        }
        ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType().toString().endsWith("BOW")) {
            ReforgeManager rm = new ReforgeManager();
            ReforgeTier tier = rm.getReforgeTierByTier(rm.getReforgeTier(bow));
            percent += tier.getBowDamageIncrease();
        }
        return percent;
    }

    private double computeFlightDistance(Player player) {
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        int level = pet != null ? pet.getLevel() : 0;
        double distance = level / 100.0; // max 1km at level 100
        if (PlayerMeritManager.getInstance(plugin).hasPerk(player.getUniqueId(), "Icarus")) {
            distance *= 2;
        }
        return distance;
    }

    private double computeGraveChance(Player player) {
        double chance = 0.0005; // base
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null) {
            if (pet.getTrait() == PetTrait.PARANORMAL) {
                chance += pet.getTrait().getValueForRarity(pet.getTraitRarity());
            }
            if (pet.hasPerk(PetManager.PetPerk.MEMORY)) chance += 0.001;
            if (pet.hasPerk(PetManager.PetPerk.HAUNTING)) chance += 0.002;
            if (pet.hasPerk(PetManager.PetPerk.SCREAM)) chance += 0.004;
            if (pet.hasPerk(PetManager.PetPerk.COLD)) chance += 0.005;
            if (pet.hasPerk(PetManager.PetPerk.MALIGNANCE)) chance += 0.01;
        }
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            int level = stm.getTalentLevel(player.getUniqueId(), Skill.TERRAFORMING, Talent.GRAVE_INTUITION);
            chance += level * 0.001;
        }
        return chance;
    }

    private double computeSeaCreatureChance(Player player) {
        double base = 0.0;
        double total = base;
        int instinct = SkillTreeManager.getInstance().getTalentLevel(player.getUniqueId(), Skill.FISHING, Talent.ANGLERS_INSTINCT);
        total += instinct * 0.25;
        int cotv = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Call of the Void");
        total += cotv;
        if (PotionManager.isActive("Potion of Fountains", player)) total += 10.0;
        if (SkillTreeManager.getInstance().hasTalent(player, Talent.FOUNTAIN_MASTERY)) total += 5.0;
        // Depth catalyst and other bonuses ignored here for brevity
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.ANGLER)) total += 5.0;
        if (pet != null && pet.hasPerk(PetManager.PetPerk.HEART_OF_THE_SEA)) total += 10.0;
        return total;
    }

    private double computeTreasureChance(Player player) {
        double base = 5.0;
        double total = base;
        int upgrade = FishingUpgradeSystem.getUpgradeLevel(player.getInventory().getItemInMainHand(), FishingUpgradeSystem.UpgradeType.TREASURE_HUNTER);
        total += upgrade;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.TREASURE_HUNTER)) {
            total += pet.getLevel() * 0.1;
        }
        if (PotionManager.isActive("Potion of Liquid Luck", player)) total += 20.0;
        int piracy = CustomEnchantmentManager.getEnchantmentLevel(player.getInventory().getItemInMainHand(), "Piracy");
        total += piracy;
        return total;
    }

    private double computeSpiritChance(Player player) {
        double base = 0.01;
        double total = base;
        int effigy = EffigyUpgradeSystem.getUpgradeLevel(player.getInventory().getItemInMainHand(), EffigyUpgradeSystem.UpgradeType.EFFIGY_YIELD);
        total += effigy * 0.000333;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null) {
            if (pet.hasPerk(PetManager.PetPerk.SKEPTICISM)) total += 0.001;
            if (pet.hasPerk(PetManager.PetPerk.CHALLENGE)) total += 0.002;
        }
        return total;
    }

    private double computeDiscount(Player player) {
        double finalCost = 100.0;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.HAGGLE)) {
            int level = pet.getLevel();
            double discountFactor;
            if (level >= 100) discountFactor = 0.25;
            else if (level >= 75) discountFactor = 0.20;
            else if (level >= 50) discountFactor = 0.15;
            else if (level >= 25) discountFactor = 0.10;
            else if (level >= 1) discountFactor = 0.05;
            else discountFactor = 0.0;
            finalCost *= (1 - discountFactor);
        }
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            int level = stm.getTalentLevel(player.getUniqueId(), Skill.BARTERING, Talent.BARTER_DISCOUNT);
            finalCost *= (1 - level * 0.04);
        }
        if (pet != null && pet.getTrait() == PetTrait.FINANCIAL) {
            double pct = pet.getTrait().getValueForRarity(pet.getTraitRarity()) / 100.0;
            finalCost *= (1 - pct);
        }
        if (PotionManager.isActive("Potion of Charismatic Bartering", player)) {
            double discount = 0.20;
            if (stm.hasTalent(player, Talent.CHARISMA_MASTERY)) {
                int level = stm.getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.CHARISMA_MASTERY);
                discount += 0.05 * level;
            }
            finalCost *= (1 - discount);
        }
        return 1.0 - (finalCost / 100.0);
    }

    private double computeSpeed(Player player) {
        double percent = (player.getWalkSpeed() / 0.2 - 1) * 100.0;
        PotionEffect speed = player.getPotionEffect(PotionEffectType.SPEED);
        if (speed != null) {
            percent += 20.0 * (speed.getAmplifier() + 1);
        }
        return percent;
    }

    private double computeBrewReduction(Player player) {
        double percent = 0.0;
        PetManager.Pet pet = PetManager.getInstance(plugin).getActivePet(player);
        if (pet != null && pet.hasPerk(PetManager.PetPerk.SPLASH_POTION)) {
            percent += pet.getLevel() / 2.0;
        }
        SkillTreeManager stm = SkillTreeManager.getInstance();
        if (stm != null) {
            int level = stm.getTalentLevel(player.getUniqueId(), Skill.BREWING, Talent.OPTIMAL_CONFIGURATION);
            percent += level * 4; // approximate
        }
        return percent;
    }

    private double computeDoubleOreChance(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        int level = stm.getTalentLevel(player.getUniqueId(), Skill.MINING, Talent.RICH_VEINS);
        return level * 4.0;
    }

    private double computeDoubleLogChance(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        int level = stm.getTalentLevel(player.getUniqueId(), Skill.FORESTRY, Talent.DOUBLE_LOGS);
        return level * 10.0;
    }

    private double computeDoubleCropChance(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        int level = stm.getTalentLevel(player.getUniqueId(), Skill.FARMING, Talent.BOUNTIFUL_HARVEST);
        return level * 4.0;
    }

    private int computeRepairAmount(Player player) {
        SkillTreeManager stm = SkillTreeManager.getInstance();
        int amt = 0;
        amt += stm.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_ONE) * 1;
        amt += stm.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_TWO) * 2;
        amt += stm.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_THREE) * 3;
        amt += stm.getTalentLevel(player.getUniqueId(), Skill.SMITHING, Talent.REPAIR_FOUR) * 4;
        return amt;
    }

    private int computeRepairQuality(Player player) {
        return 0; // placeholder - system not implemented
    }
}
