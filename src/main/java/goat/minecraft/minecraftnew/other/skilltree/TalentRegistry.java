package goat.minecraft.minecraftnew.other.skilltree;

import java.util.*;

/**
 * Central registry that maps {@link Skill} objects to the talents that belong
 * to that skill.  Additional talents can be added here in the future.
 */
public final class TalentRegistry {

    private static final Map<Skill, List<Talent>> SKILL_TALENTS = new HashMap<>();

    static {
        // Register talents for each supported skill.
        SKILL_TALENTS.put(
                Skill.BREWING,
                Arrays.asList(
                        Talent.REDSTONE_ONE,
                        Talent.OPTIMAL_CONFIGURATION,
                        Talent.REDSTONE_TWO,
                        Talent.STRENGTH_MASTERY,
                        Talent.RECURVE_MASTERY,
                        Talent.SWIFT_STEP_MASTERY,
                        Talent.NIGHT_VISION_MASTERY,
                        Talent.SOLAR_FURY_MASTERY,
                        Talent.REDSTONE_THREE,
                        Talent.TRIPLE_BATCH,
                        Talent.SOVEREIGNTY_MASTERY,
                        Talent.REDSTONE_FOUR,
                        Talent.LIQUID_LUCK_MASTERY,
                        Talent.FOUNTAIN_MASTERY,
                        Talent.OXYGEN_MASTERY,
                        Talent.METAL_DETECTION_MASTERY,
                        Talent.CHARISMA_MASTERY,
                        Talent.REDSTONE_FIVE,
                        Talent.NUTRITION_MASTERY,
                        Talent.ETERNAL_ELIXIR)
        );

        SKILL_TALENTS.put(
                Skill.COMBAT,
                Arrays.asList(
                        Talent.ARROW_DAMAGE_INCREASE_I,
                        Talent.SWORD_DAMAGE_I,
                        Talent.VAMPIRIC_STRIKE,
                        Talent.BLOODLUST,
                        Talent.BLOODLUST_DURATION_I,
                        Talent.ARROW_DAMAGE_INCREASE_II,
                        Talent.SWORD_DAMAGE_II,
                        Talent.BLOODLUST_DURATION_II,
                        Talent.RETRIBUTION,
                        Talent.VENGEANCE,
                        Talent.ARROW_DAMAGE_INCREASE_III,
                        Talent.SWORD_DAMAGE_III,
                        Talent.DONT_MINE_AT_NIGHT,
                        Talent.HELLBENT,
                        Talent.ARROW_DAMAGE_INCREASE_IV,
                        Talent.SWORD_DAMAGE_IV,
                        Talent.BLOODLUST_DURATION_III,
                        Talent.ANTAGONIZE,
                        Talent.ARROW_DAMAGE_INCREASE_V,
                        Talent.SWORD_DAMAGE_V,
                        Talent.ULTIMATUM,
                        Talent.REVENANT,
                        Talent.BLOODLUST_DURATION_IV
                )
        );

        SKILL_TALENTS.put(
                Skill.SMITHING,
                Arrays.asList(
                        Talent.REPAIR_ONE,
                        Talent.REPAIR_TWO,
                        Talent.REPAIR_THREE,
                        Talent.REPAIR_FOUR
                  )
          );
        SKILL_TALENTS.put(
                Skill.CULINARY,
                Arrays.asList(
                        Talent.SATIATION_MASTERY,
                        Talent.FEASTING_CHANCE,
                        Talent.MASTER_CHEF
                  )
          );
        SKILL_TALENTS.put(
                Skill.BARTERING,
                Arrays.asList(
                        Talent.BARTER_DISCOUNT,
                        Talent.FREE_TRANSACTION,
                        Talent.SELL_PRICE_BOOST,
                        Talent.WORK_CYCLE_EFFICIENCY
                  )
          );
        SKILL_TALENTS.put(
                Skill.FORESTRY,
                Arrays.asList(
                        Talent.DOUBLE_LOGS,
                        Talent.FORESTRY_HASTE,
                        Talent.HASTE_POTENCY,
                        Talent.TREECAP_SPIRIT
                  )
          );
         SKILL_TALENTS.put(
                Skill.TAMING,
                Arrays.asList(
                        Talent.PET_TRAINER
                )
        );
        SKILL_TALENTS.put(
                Skill.PLAYER,
                Collections.singletonList(Talent.VITALITY
                )
          );
              SKILL_TALENTS.put(
                Skill.TERRAFORMING,
                Arrays.asList(
                        Talent.CONSERVATIONIST,
                        Talent.GRAVE_INTUITION
          )
          );
        SKILL_TALENTS.put(
                Skill.FARMING,
                Arrays.asList(
                        Talent.EXTRA_CROP_CHANCE_I,
                        Talent.FOR_THE_STREETS,
                        Talent.REAPER_I,
                        Talent.FAST_FARMER,
                        Talent.HARVEST_FESTIVAL,
                        Talent.EXTRA_CROP_CHANCE_II,
                        Talent.UNRIVALED,
                        Talent.REAPER_II,
                        Talent.HYDRO_FARMER,
                        Talent.FESTIVAL_BEES_I,
                        Talent.EXTRA_CROP_CHANCE_III,
                        Talent.REAPER_III,
                        Talent.HALLOWEEN,
                        Talent.FESTIVAL_BEE_DURATION_I,
                        Talent.FESTIVAL_BEES_II,
                        Talent.EXTRA_CROP_CHANCE_IV,
                        Talent.REAPER_IV,
                        Talent.FERTILIZER_EFFICIENCY,
                        Talent.FESTIVAL_BEE_DURATION_II,
                        Talent.FESTIVAL_BEES_III,
                        Talent.EXTRA_CROP_CHANCE_V,
                        Talent.REAPER_V,
                        Talent.FESTIVAL_BEES_IV,
                        Talent.SWARM,
                        Talent.HIVEMIND
                )
        );
              SKILL_TALENTS.put(
                Skill.FISHING,
                Arrays.asList(
                        Talent.ANGLERS_INSTINCT
               )
        );
              SKILL_TALENTS.put(
                Skill.MINING,
                Arrays.asList(
                        Talent.RICH_VEINS,
                        Talent.DEEP_LUNGS
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
