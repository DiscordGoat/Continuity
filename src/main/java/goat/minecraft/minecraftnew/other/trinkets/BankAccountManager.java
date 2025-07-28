package goat.minecraft.minecraftnew.other.trinkets;

import goat.minecraft.minecraftnew.other.additionalfunctionality.CustomBundleGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BankAccountManager {
    private static BankAccountManager instance;
    private final JavaPlugin plugin;
    private File bankFile;
    private FileConfiguration bankConfig;

    private BankAccountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        initFile();
    }

    public static BankAccountManager init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BankAccountManager(plugin);
        }
        return instance;
    }

    public static BankAccountManager getInstance() {
        return instance;
    }

    private void initFile() {
        bankFile = new File(plugin.getDataFolder(), "bank_accounts.yml");
        if (!bankFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                bankFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        bankConfig = YamlConfiguration.loadConfiguration(bankFile);
    }

    private void save() {
        try {
            bankConfig.save(bankFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(UUID id) {
        return bankConfig.getInt(id.toString(), 0);
    }

    private void setBalance(UUID id, int amount) {
        bankConfig.set(id.toString(), amount);
        save();
    }

    private void addBalance(UUID id, int amount) {
        int bal = getBalance(id);
        setBalance(id, bal + amount);
    }

    public void addEmeralds(UUID id, int amount) {
        addBalance(id, amount);
    }

    public int depositAll(Player player) {
        int total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            if (item.getType() == Material.EMERALD && item.getEnchantments().isEmpty()) {
                total += item.getAmount();
                inv.setItem(i, null);
            } else if (item.getType() == Material.EMERALD_BLOCK && item.getEnchantments().isEmpty()) {
                total += item.getAmount() * 9;
                inv.setItem(i, null);
            }
        }
        total += CustomBundleGUI.getInstance().removeAllEmeraldsAndReturnCount(player);
        if (total > 0) {
            addBalance(player.getUniqueId(), total);
        }
        return total;
    }

    public int withdrawAll(Player player) {
        UUID id = player.getUniqueId();
        int bal = getBalance(id);
        if (bal <= 0) return 0;
        setBalance(id, 0);
        return bal;
    }

    private int countEmeraldsInInventory(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null &&
                    item.getType() == Material.EMERALD &&
                    item.getEnchantments().isEmpty()) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeFromInventory(Player player, int amount) {
        int remaining = amount;
        Inventory inv = player.getInventory();
        for (ItemStack item : inv.getContents()) {
            if (item == null ||
                    item.getType() != Material.EMERALD ||
                    !item.getEnchantments().isEmpty()) {
                continue;
            }
            int amt = item.getAmount();
            if (amt <= remaining) {
                inv.removeItem(item);
                remaining -= amt;
            } else {
                item.setAmount(amt - remaining);
                remaining = 0;
                break;
            }
        }
    }

    public boolean removeEmeralds(Player player, int amount) {
        Inventory inv = player.getInventory();

        int invCount = countEmeraldsInInventory(player);
        if (invCount >= amount) {
            removeFromInventory(player, amount);
            return true;
        }

        int shortfall = amount - invCount;
        int bal = getBalance(player.getUniqueId());
        if (bal >= shortfall) {
            setBalance(player.getUniqueId(), bal - shortfall);
            TrinketManager.getInstance().refreshBankLore(player);
            if (invCount > 0) {
                removeFromInventory(player, invCount);
            }
            return true;
        }

        return false;
    }

    // Removed unused hasEnough helper method
}
