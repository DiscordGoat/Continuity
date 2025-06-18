package goat.minecraft.minecraftnew.other.additionalfunctionality;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class BankAccountManager {
    private static BankAccountManager instance;
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration config;

    private BankAccountManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "bank_accounts.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new BankAccountManager(plugin);
        }
    }

    public static BankAccountManager getInstance() {
        return instance;
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getBalance(UUID uuid) {
        return config.getInt(uuid.toString(), 0);
    }

    public void deposit(UUID uuid, int amount) {
        if (amount <= 0) return;
        int bal = getBalance(uuid) + amount;
        config.set(uuid.toString(), bal);
        save();
    }

    public boolean removeEmeralds(UUID uuid, int amount) {
        int bal = getBalance(uuid);
        if (bal < amount) {
            return false;
        }
        config.set(uuid.toString(), bal - amount);
        save();
        return true;
    }

    public int withdrawAll(UUID uuid) {
        int bal = getBalance(uuid);
        if (bal > 0) {
            config.set(uuid.toString(), 0);
            save();
        }
        return bal;
    }
}
