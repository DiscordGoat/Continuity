package goat.minecraft.minecraftnew.subsystems.combat;

import goat.minecraft.minecraftnew.subsystems.combat.config.CombatConfiguration;
import goat.minecraft.minecraftnew.subsystems.combat.damage.DamageCalculationService;
import goat.minecraft.minecraftnew.subsystems.combat.damage.strategies.MeleeDamageStrategy;
import goat.minecraft.minecraftnew.subsystems.combat.damage.strategies.MonsterLevelDamageStrategy;
import goat.minecraft.minecraftnew.subsystems.combat.damage.strategies.RangedDamageStrategy;
import goat.minecraft.minecraftnew.subsystems.combat.commands.CombatReloadCommand;
import goat.minecraft.minecraftnew.subsystems.combat.hostility.HostilityGUIController;
import goat.minecraft.minecraftnew.subsystems.combat.hostility.HostilityService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.DamageNotificationService;
import goat.minecraft.minecraftnew.subsystems.combat.notification.PlayerFeedbackService;
import goat.minecraft.minecraftnew.subsystems.combat.FireDamageHandler;
import goat.minecraft.minecraftnew.utils.devtools.XPManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central manager for the combat subsystem.
 * Handles initialization, configuration, and coordination of all combat components.
 */
public class CombatSubsystemManager implements CommandExecutor {
    
    private static final Logger logger = Logger.getLogger(CombatSubsystemManager.class.getName());
    
    private final JavaPlugin plugin;
    private final XPManager xpManager;
    
    // Core services
    private CombatConfiguration configuration;
    private DamageCalculationService damageCalculationService;
    private DamageNotificationService notificationService;
    private PlayerFeedbackService feedbackService;
    private HostilityService hostilityService;

    private FireDamageHandler fireDamageHandler;
    
    // Controllers and handlers
    private CombatEventHandler eventHandler;
    private HostilityGUIController hostilityGUIController;
    
    // Initialization state
    private boolean initialized = false;
    
    public CombatSubsystemManager(JavaPlugin plugin, XPManager xpManager) {
        this.plugin = plugin;
        this.xpManager = xpManager;
    }
    
    /**
     * Initializes the combat subsystem with all its components.
     * 
     * @throws CombatSubsystemException if initialization fails
     */
    public void initialize() {
        if (initialized) {
            logger.warning("Combat subsystem is already initialized");
            return;
        }
        
        try {
            logger.info("Initializing combat subsystem...");
            
            // Load configuration
            initializeConfiguration();
            
            // Initialize core services
            initializeServices();
            
            // Register damage calculation strategies
            registerDamageStrategies();
            
            // Initialize controllers and handlers
            initializeControllersAndHandlers();
            
            // Register event listeners
            registerEventListeners();
            
            // Register commands
            registerCommands();
            
            initialized = true;
            logger.info("Combat subsystem initialized successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize combat subsystem", e);
            cleanup(); // Clean up partial initialization
            throw new CombatSubsystemException("Combat subsystem initialization failed", e);
        }
    }
    
    /**
     * Shuts down the combat subsystem and cleans up resources.
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        try {
            logger.info("Shutting down combat subsystem...");
            
            cleanup();
            initialized = false;
            
            logger.info("Combat subsystem shut down successfully");
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during combat subsystem shutdown", e);
        }
    }
    
    /**
     * Reloads the combat subsystem configuration.
     */
    public void reload() {
        try {
            logger.info("Reloading combat subsystem configuration...");
            
            configuration.reload();
            
            if (hostilityService != null) {
                hostilityService.reloadConfig();
            }
            
            logger.info("Combat subsystem configuration reloaded successfully");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload combat subsystem configuration", e);
            throw new CombatSubsystemException("Configuration reload failed", e);
        }
    }
    
    /**
     * Checks if the combat subsystem is initialized and ready.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the damage calculation service.
     */
    public DamageCalculationService getDamageCalculationService() {
        return damageCalculationService;
    }
    
    /**
     * Gets the hostility service.
     */
    public HostilityService getHostilityService() {
        return hostilityService;
    }
    
    /**
     * Gets the notification service.
     */
    public DamageNotificationService getNotificationService() {
        return notificationService;
    }
    
    /**
     * Handles the /hostility command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!"hostility".equals(command.getName())) {
            return false;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }
        
        if (!initialized) {
            sender.sendMessage("§cCombat subsystem is not initialized.");
            return true;
        }
        
        try {
            Player player = (Player) sender;
            hostilityGUIController.openHostilityGUI(player);
            return true;
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error handling hostility command", e);
            sender.sendMessage("§cAn error occurred while opening the hostility GUI.");
            return true;
        }
    }
    
    /**
     * Initializes the configuration system.
     */
    private void initializeConfiguration() {
        configuration = new CombatConfiguration(plugin);
        logger.fine("Combat configuration initialized");
    }
    
    /**
     * Initializes core services.
     */
    private void initializeServices() {
        // Initialize services in dependency order
        damageCalculationService = new DamageCalculationService(configuration.getBuffConfig());
        notificationService = new DamageNotificationService(plugin, configuration.getNotificationConfig());
        feedbackService = new PlayerFeedbackService(configuration.getSoundConfig());
        hostilityService = new HostilityService(plugin, configuration.getHostilityConfig());
        
        logger.fine("Core combat services initialized");
    }
    
    /**
     * Registers damage calculation strategies.
     */
    private void registerDamageStrategies() {
        // Register strategies based on configuration
        if (configuration.getBuffConfig().isSkillDamageScaling()) {
            damageCalculationService.registerStrategy(
                new MeleeDamageStrategy(configuration.getDamageConfig(), xpManager));
            
            damageCalculationService.registerStrategy(
                new RangedDamageStrategy(configuration.getDamageConfig(), xpManager));
        }
        
        if (configuration.getBuffConfig().isMonsterLevelScaling()) {
            damageCalculationService.registerStrategy(
                new MonsterLevelDamageStrategy(configuration.getDamageConfig()));
        }
        
        logger.fine("Damage calculation strategies registered");
    }
    
    /**
     * Initializes controllers and event handlers.
     */
    private void initializeControllersAndHandlers() {
        eventHandler = new CombatEventHandler(
            damageCalculationService,
            notificationService,
            feedbackService,
            configuration
        );
        
        hostilityGUIController = new HostilityGUIController(
            hostilityService,
            configuration.getHostilityConfig()
        );

        fireDamageHandler = new FireDamageHandler(plugin, notificationService);
        
        logger.fine("Combat controllers and handlers initialized");
    }
    
    /**
     * Registers event listeners with the plugin manager.
     */
    private void registerEventListeners() {
        Bukkit.getPluginManager().registerEvents(eventHandler, plugin);
        Bukkit.getPluginManager().registerEvents(hostilityGUIController, plugin);
        Bukkit.getPluginManager().registerEvents(fireDamageHandler, plugin);

        logger.fine("Combat event listeners registered");
    }
    
    /**
     * Registers command executors.
     */
    private void registerCommands() {
        plugin.getCommand("hostility").setExecutor(this);
        
        // Register reload command if it exists in plugin.yml
        if (plugin.getCommand("combatreload") != null) {
            plugin.getCommand("combatreload").setExecutor(new CombatReloadCommand(this));
        }
        
        logger.fine("Combat commands registered");
    }
    
    /**
     * Cleans up resources and services.
     */
    private void cleanup() {
        try {
            if (notificationService != null) {
                notificationService.cleanup();
            }
            
            if (hostilityService != null) {
                hostilityService.saveConfig();
            }
            
            if (configuration != null) {
                configuration.save();
            }
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during combat subsystem cleanup", e);
        }
    }
    
    /**
     * Exception thrown when combat subsystem operations fail.
     */
    public static class CombatSubsystemException extends RuntimeException {
        public CombatSubsystemException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public CombatSubsystemException(String message) {
            super(message);
        }
    }
}