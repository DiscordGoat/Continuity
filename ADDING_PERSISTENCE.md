# Adding Persistence to New Features

This guide explains how to persist custom data for new subsystems or features in the
plugin. Several existing systems use YAML files to store session or inventory data
between server restarts. Studying them provides a repeatable pattern for adding
persistence elsewhere.

## 1. Create a Data File

Use `YamlConfiguration` along with a `File` in the plugin's data folder. Initialize
the file if it does not exist and keep a `YamlConfiguration` instance in memory.

```java
private final File dataFile;
private YamlConfiguration dataConfig;

// inside the constructor
dataFile = new File(plugin.getDataFolder(), "my_feature.yml");
if (!dataFile.exists()) {
    try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
}
dataConfig = YamlConfiguration.loadConfiguration(dataFile);
```

Examples can be found in `DoubleEnderchest` where `double_enderchests.yml` is
created and loaded【F:src/main/java/goat/minecraft/minecraftnew/other/meritperks/DoubleEnderchest.java†L31-L49】,
and in the `SatchelManager` initialization of `satchels.yml`【F:src/main/java/goat/minecraft/minecraftnew/other/trinkets/SatchelManager.java†L40-L52】.

## 2. Loading Data on Enable

When the subsystem starts, read existing entries from the YAML file and rebuild
any in-memory sessions or inventories. Each entry is keyed by an identifier
(e.g., player UUID or world location).

```java
public void onEnable() {
    dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    for (String key : dataConfig.getKeys(false)) {
        // reconstruct objects based on stored values
        MySession session = loadSession(key, dataConfig);
        activeSessions.put(key, session);
    }
}
```

The `VerdantRelicsSubsystem` loads relic sessions in `loadAllRelics()` and
restores timers and metadata from disk【F:src/main/java/goat/minecraft/minecraftnew/subsystems/farming/VerdantRelicsSubsystem.java†L80-L118】.
Similarly, `PotionBrewingSubsystem` rebuilds brewing sessions in
`loadAllBrews()`【F:src/main/java/goat/minecraft/minecraftnew/subsystems/brewing/PotionBrewingSubsystem.java†L87-L125】.

## 3. Saving Data

Provide methods that serialize in‑memory state back to the YAML file. Clear old
entries first, then write all active sessions.

```java
private void saveAll() {
    for (String key : dataConfig.getKeys(false)) {
        dataConfig.set(key, null); // remove previous data
    }
    for (String key : activeSessions.keySet()) {
        MySession session = activeSessions.get(key);
        dataConfig.set(key + ".value", session.value);
        // ...other fields
    }
    try { dataConfig.save(dataFile); } catch (IOException e) { e.printStackTrace(); }
}
```

This pattern mirrors the save logic in `VerdantRelicsSubsystem.saveAllRelics()`
【F:src/main/java/goat/minecraft/minecraftnew/subsystems/farming/VerdantRelicsSubsystem.java†L124-L143】
and `PotionBrewingSubsystem.saveAllBrews()`【F:src/main/java/goat/minecraft/minecraftnew/subsystems/brewing/PotionBrewingSubsystem.java†L131-L152】.
Inventory based features like the custom anvil or brewing GUI also use the same
approach to persist contents【F:src/main/java/goat/minecraft/minecraftnew/cut_content/CancelBrewing.java†L422-L447】.

## 4. Saving on Shutdown

Ensure that your subsystem saves its state when the plugin disables.
Register a call in `onDisable()` of the main class or the subsystem itself.
`MinecraftNew` calls several managers to persist inventories when shutting down
【F:src/main/java/goat/minecraft/minecraftnew/MinecraftNew.java†L623-L649】.

```java
@Override
public void onDisable() {
    saveAll();
}
```

For systems without persistence (e.g., `CulinarySubsystem`) cleanup methods drop
items and clear sessions instead of saving to disk【F:src/main/java/goat/minecraft/minecraftnew/subsystems/culinary/CulinarySubsystem.java†L252-L314】.

## 5. Accessing Saved Data

Provide public methods like `loadInventory(UUID id)` or `getSession(String key)`
so other parts of the plugin can retrieve persisted information on demand. This
mirrors the `DoubleEnderchest` `loadInventory()` method which reads slots from
the YAML file【F:src/main/java/goat/minecraft/minecraftnew/other/meritperks/DoubleEnderchest.java†L90-L102】.

## Summary

1. **Create** a YAML file under the plugin data folder and keep a
   `YamlConfiguration` instance.
2. **Load** existing entries on enable and rebuild in-memory objects.
3. **Save** all active sessions or inventories back to the file.
4. **Hook** the save method into plugin shutdown to avoid data loss.

Following the patterns above allows any new subsystem to preserve its state
across server restarts using simple YAML storage.
