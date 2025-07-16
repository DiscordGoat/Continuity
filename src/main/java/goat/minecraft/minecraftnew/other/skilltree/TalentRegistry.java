package goat.minecraft.minecraftnew.other.skilltree;

import java.util.*;

/**
 * Central registry that maps {@link Skill} objects to the talents that belong
 * to that skill.  Additional talents can be added here in the future.
 */
public final class TalentRegistry {

    private static final Map<Skill, List<Talent>> SKILL_TALENTS = new HashMap<>();

    static {
        // Currently only the Brewing skill has talents defined.
        SKILL_TALENTS.put(
                Skill.BREWING,
                Arrays.asList(
                        Talent.REDSTONE_ONE,
                        Talent.REDSTONE_TWO,
                        Talent.REDSTONE_THREE,
                        Talent.REDSTONE_FOUR,
                        Talent.OPTIMAL_CONFIGURATION,
                        Talent.TRIPLE_BATCH,
                        Talent.RECURVE_MASTERY,
                        Talent.REJUVENATION,
                        Talent.STRENGTH_MASTERY,
                        Talent.LIQUID_LUCK_MASTERY,
                        Talent.SOVEREIGNTY_MASTERY,
                        Talent.STRENGTH_MASTERY,
                        Talent.OXYGEN_MASTERY,
                        Talent.SWIFT_STEP_MASTERY,
                        Talent.METAL_DETECTION_MASTERY,
                        Talent.NIGHT_VISION_MASTERY,
                        Talent.SOLAR_FURY_MASTERY,
                        Talent.FOUNTAIN_MASTERY,
                        Talent.CHARISMA_MASTERY)
        );

        SKILL_TALENTS.put(
                Skill.COMBAT,
                Arrays.asList(
                        Talent.WOODEN_SWORD,
                        Talent.STONE_SWORD,
                        Talent.IRON_SWORD,
                        Talent.GOLD_SWORD,
                        Talent.DIAMOND_SWORD,
                        Talent.NETHERITE_SWORD,
                        Talent.BOW_MASTERY,
                        Talent.DONT_MINE_AT_NIGHT,
                        Talent.ULTIMATUM,
                        Talent.ARMAGEDDON,
                        Talent.VAMPIRIC_STRIKE
                )
        );
    //SKILL_TALENTS.put(Skill.BREWING, Collections.singletonList(Talent.REDSTONE_TWO));
    }

    private TalentRegistry() {
    }

    /**
     * Returns all talents associated with the given skill.
     */
    public static List<Talent> getTalents(Skill skill) {
        return SKILL_TALENTS.getOrDefault(skill, Collections.emptyList());
    }

    /**
     * Returns the skill associated with the given talent or {@code null} if the
     * talent is not registered.
     */
    public static Skill getSkillForTalent(Talent talent) {
        for (Map.Entry<Skill, List<Talent>> entry : SKILL_TALENTS.entrySet()) {
            if (entry.getValue().contains(talent)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
