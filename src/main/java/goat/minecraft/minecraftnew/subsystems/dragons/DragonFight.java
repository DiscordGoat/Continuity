package goat.minecraft.minecraftnew.subsystems.dragons;

import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.EnderDragon;

/**
 * Represents an active dragon fight.  It links the spawned EnderDragon entity
 * with its logical dragon type and associated {@link DragonHealthInstance}.
 */
public class DragonFight {

    private final NPC npc;
    private final EnderDragon dragonEntity;
    private final Dragon dragonType;
    private final DragonHealthInstance health;

    public DragonFight(NPC npc, Dragon dragonType) {
        this.npc = npc;
        this.dragonEntity = (EnderDragon) npc.getEntity();
        this.dragonType = dragonType;
        this.health = new DragonHealthInstance(dragonEntity.getUniqueId(), dragonType.getMaxHealth());
    }

    public EnderDragon getDragonEntity() {
        return dragonEntity;
    }

    public Dragon getDragonType() {
        return dragonType;
    }

    public NPC getNpc() {
        return npc;
    }

    public DragonHealthInstance getHealth() {
        return health;
    }
}
