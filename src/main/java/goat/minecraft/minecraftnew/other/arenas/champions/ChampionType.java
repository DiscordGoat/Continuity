package goat.minecraft.minecraftnew.other.arenas.champions;

import java.util.List;

/**
 * Defines a single Champion type that can appear in arenas.
 * For now champions behave similarly to Corpses and use the same trait.
 */
public class ChampionType {
    private final String name;
    private final int health;
    private final String armorFile;
    private final String swordFile;
    private final String bowFile;
    private final List<String> greetingMessages;
    private final String skinValue;
    private final String skinSig;

    public ChampionType(String name, int health, String armorFile,
                        String swordFile, String bowFile,
                        List<String> greetingMessages,
                        String skinValue, String skinSig) {
        this.name = name;
        this.health = health;
        this.armorFile = armorFile;
        this.swordFile = swordFile;
        this.bowFile = bowFile;
        this.greetingMessages = greetingMessages;
        this.skinValue = skinValue;
        this.skinSig = skinSig;
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public String getArmorFile() {
        return armorFile;
    }

    public String getSwordFile() {
        return swordFile;
    }

    public String getBowFile() {
        return bowFile;
    }

    public List<String> getGreetingMessages() {
        return greetingMessages;
    }

    public String getSkinValue() {
        return skinValue;
    }

    public String getSkinSig() {
        return skinSig;
    }
}
