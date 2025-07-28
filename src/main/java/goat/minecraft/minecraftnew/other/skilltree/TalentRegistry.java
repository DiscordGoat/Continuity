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
                        Talent.REPAIR_AMOUNT_I,
                        Talent.QUALITY_MATERIALS_I,
                        Talent.ALLOY_I,
                        Talent.NOVICE_SMITH,
                        Talent.SCRAPS_I,
                        Talent.NOVICE_FOUNDATIONS,

                        Talent.REPAIR_AMOUNT_II,
                        Talent.QUALITY_MATERIALS_II,
                        Talent.ALLOY_II,
                        Talent.APPRENTICE_SMITH,
                        Talent.SCRAPS_II,
                        Talent.APPRENTICE_FOUNDATIONS,

                        Talent.REPAIR_AMOUNT_III,
                        Talent.QUALITY_MATERIALS_III,
                        Talent.ALLOY_III,
                        Talent.JOURNEYMAN_SMITH,
                        Talent.SCRAPS_III,
                        Talent.JOURNEYMAN_FOUNDATIONS,

                        Talent.REPAIR_AMOUNT_IV,
                        Talent.QUALITY_MATERIALS_IV,
                        Talent.ALLOY_IV,
                        Talent.EXPERT_SMITH,
                        Talent.SCRAPS_IV,
                        Talent.EXPERT_FOUNDATIONS,

                        Talent.REPAIR_AMOUNT_V,
                        Talent.QUALITY_MATERIALS_V,
                        Talent.ALLOY_V,
                        Talent.MASTER_SMITH,
                        Talent.SCRAPS_V,
                        Talent.MASTER_FOUNDATIONS
                )
        );
        SKILL_TALENTS.put(
                Skill.CULINARY,
                Arrays.asList(
                        Talent.SATIATION_MASTERY_I,
                        Talent.CUTTING_BOARD_I,
                        Talent.LUNCH_RUSH_I,
                        Talent.SWEET_TOOTH,
                        Talent.GOLDEN_APPLE,
                        Talent.SATIATION_MASTERY_II,
                        Talent.CUTTING_BOARD_II,
                        Talent.LUNCH_RUSH_II,
                        Talent.GRAINS_GAINS,
                        Talent.PORTAL_PANTRY,
                        Talent.SATIATION_MASTERY_III,
                        Talent.CUTTING_BOARD_III,
                        Talent.LUNCH_RUSH_III,
                        Talent.AXE_BODY_SPRAY,
                        Talent.I_DO_NOT_NEED_A_SNACK,
                        Talent.SATIATION_MASTERY_IV,
                        Talent.CUTTING_BOARD_IV,
                        Talent.LUNCH_RUSH_IV,
                        Talent.RABBIT,
                        Talent.PANTRY_OF_PLENTY,
                        Talent.SATIATION_MASTERY_V,
                        Talent.CUTTING_BOARD_V,
                        Talent.LUNCH_RUSH_V,
                        Talent.CAVITY,
                        Talent.CHEFS_KISS
                )
        );
        SKILL_TALENTS.put(
                Skill.BARTERING,
                Arrays.asList(
                        Talent.HAGGLER_I,
                        Talent.STONKS_I,
                        Talent.SHUT_UP_AND_TAKE_MY_MONEY,
                        Talent.SWEATSHOP_SUPERVISOR,
                        Talent.CORPORATE_BENEFITS,

                        Talent.HAGGLER_II,
                        Talent.STONKS_II,
                        Talent.BULK,
                        Talent.DEADLINE_DICTATOR,
                        Talent.UNIFORM,

                        Talent.HAGGLER_III,
                        Talent.STONKS_III,
                        Talent.INTEREST,
                        Talent.TASKMASTER_TYRANT,
                        Talent.OVERSTOCKED,

                        Talent.HAGGLER_IV,
                        Talent.STONKS_IV,
                        Talent.OVERTIME_OVERLORD,
                        Talent.ITS_ALIVE,

                        Talent.HAGGLER_V,
                        Talent.STONKS_V,
                        Talent.SLAVE_DRIVER,
                        Talent.BILLIONAIRE_DISCOUNT
                )
        );
        SKILL_TALENTS.put(
                Skill.FORESTRY,
                Arrays.asList(
                        Talent.SPIRIT_CHANCE_I,
                        Talent.TIMBER_I,
                        Talent.LEVERAGE_I,
                        Talent.FOREST_FRENZY,
                        Talent.REGROWTH_I,

                        Talent.SPIRIT_CHANCE_II,
                        Talent.LEVERAGE_II,
                        Talent.PHOTOSYNTHESIS,
                        Talent.REGROWTH_II,

                        Talent.SPIRIT_CHANCE_III,
                        Talent.TIMBER_II,
                        Talent.LEVERAGE_III,
                        Talent.ONE_HUNDRED_ACRE_WOODS,
                        Talent.SPECTRAL_ARMOR,

                        Talent.SPIRIT_CHANCE_IV,
                        Talent.LEVERAGE_IV,
                        Talent.DEFORESTATION,
                        Talent.HEADHUNTER,
                        Talent.REGROWTH_III,

                        Talent.SPIRIT_CHANCE_V,
                        Talent.TIMBER_III,
                        Talent.LEVERAGE_V,
                        Talent.ANCIENT_CONFUSION,
                        Talent.REDEMPTION
                )
        );
        SKILL_TALENTS.put(
                Skill.TAMING,
                Arrays.asList(
                        Talent.PET_TRAINER,
                        Talent.BONUS_PET_XP_I,
                        Talent.LULLABY,
                        Talent.FLIGHT,
                        Talent.DIGGING_CLAWS,
                        Talent.DEVOUR,
                        Talent.ANGLER,
                        Talent.LEAP,
                        Talent.LUMBERJACK,

                        Talent.BONUS_PET_XP_II,
                        Talent.ANTIDOTE,
                        Talent.GREEN_THUMB,
                        Talent.COLLECTOR,
                        Talent.WALKING_FORTRESS,
                        Talent.SHOTCALLING,
                        Talent.SPEED_BOOST,

                        Talent.BONUS_PET_XP_III,
                        Talent.WATERLOGGED,
                        Talent.ENDLESS_WARP,
                        Talent.DECAY,
                        Talent.ASPECT_OF_FROST,
                        Talent.PRACTICE,

                        Talent.BONUS_PET_XP_IV,
                        Talent.SECRET_LEGION,
                        Talent.BLACKLUNG,
                        Talent.COMFORTABLE,
                        Talent.SPLASH_POTION,
                        Talent.EXPERIMENTATION,
                        Talent.REVENANT,

                        Talent.BONUS_PET_XP_V,
                        Talent.COMPACT_STONE,
                        Talent.GROOT,
                        Talent.COMPOSTER,
                        Talent.ELITE,
                        Talent.HAGGLE,
                        Talent.QUIRKY,
                        Talent.NATURAL_SELECTION
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
