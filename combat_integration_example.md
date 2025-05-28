# Combat Subsystem Integration Example

This document shows how to integrate the new refactored combat subsystem into the main MinecraftNew plugin.

## Step 1: Add Field Declaration

Add this field to the MinecraftNew class:

```java
private CombatSubsystemManager combatSubsystemManager;
```

## Step 2: Initialize in onEnable()

Replace the existing combat-related registrations with this single initialization:

```java
// Initialize the new combat subsystem (replaces old combat event registrations)
combatSubsystemManager = new CombatSubsystemManager(this, xpManager);
combatSubsystemManager.initialize();
```

## Step 3: Remove Old Combat Registrations

Remove these old combat-related lines from onEnable():

```java
// REMOVE THESE LINES:
getServer().getPluginManager().registerEvents(new MobDamageHandler(), this);
DamageNotifier damageNotifier = new DamageNotifier(this);
getServer().getPluginManager().registerEvents(damageNotifier, this);
getServer().getPluginManager().registerEvents(new CombatBuffs(), this);
HostilityManager hostilityManagermanager = HostilityManager.getInstance(this);
getCommand("hostility").setExecutor(hostilityManagermanager.new HostilityCommand());
```

## Step 4: Add to onDisable()

Add cleanup to the onDisable() method:

```java
if (combatSubsystemManager != null) {
    combatSubsystemManager.shutdown();
}
```

## Complete Integration Example

Here's what the integration would look like in context:

```java
@Override
public void onEnable() {
    // ... existing initialization code ...
    
    xpManager = new XPManager(this);
    
    // Initialize the new combat subsystem
    combatSubsystemManager = new CombatSubsystemManager(this, xpManager);
    combatSubsystemManager.initialize();
    
    // ... rest of existing initialization code ...
}

@Override
public void onDisable() {
    // ... existing cleanup code ...
    
    if (combatSubsystemManager != null) {
        combatSubsystemManager.shutdown();
    }
    
    // ... rest of existing cleanup code ...
}
```

## Benefits of the New System

1. **Single Point of Control**: All combat functionality is managed through one manager
2. **Proper Configuration**: All combat settings are externalized to combat.yml
3. **Better Error Handling**: Comprehensive logging and graceful error recovery
4. **Extensible Architecture**: Easy to add new damage calculation strategies
5. **Comprehensive Testing**: Full test coverage for all combat components
6. **Resource Management**: Proper cleanup of damage indicators and other resources
7. **Performance Improvements**: Caching and optimized damage calculations

The new combat subsystem is fully backward compatible and provides significant improvements in code quality, maintainability, and performance.