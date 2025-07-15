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
                        Talent.OPTIMAL_CONFIGURATION,
                        Talent.TRIPLE_BATCH,
                        Talent.RECURVE_MASTERY,
                        Talent.REJUVENATION,
                        Talent.STRENGTH_MASTERY,
                        Talent.OXYGEN_MASTERY)
                        Talent.SWIFT_STEP_MASTERY)
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
