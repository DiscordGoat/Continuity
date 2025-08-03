package goat.minecraft.minecraftnew.subsystems.dragons;

import org.bukkit.entity.EnderDragon;

/**
 * Represents an active dragon fight.  It links the spawned EnderDragon entity
 * with its logical dragon type and associated {@link DragonHealthInstance}.
 */
public class DragonFight {

    private final EnderDragon dragonEntity;
    private final Dragon dragonType;
    private final DragonHealthInstance health;

    public DragonFight(EnderDragon dragonEntity, Dragon dragonType) {
        this.dragonEntity = dragonEntity;
        this.dragonType = dragonType;
        this.health = new DragonHealthInstance(dragonEntity.getUniqueId(), dragonType.getMaxHealth());
    }

    public EnderDragon getDragonEntity() {
        return dragonEntity;
    }

    public Dragon getDragonType() {
        return dragonType;
    }

    public DragonHealthInstance getHealth() {
        return health;
    }
}
