Guide: Adding New Skill Talents
This guide walks you through all the steps to create, configure, and register a new Talent for any Skill in the plugin.

1. Define the Talent in Talent.java
   Open goat.minecraft.minecraftnew.other.skilltree.Talent.java.

Add a new enum entry at the bottom, e.g.:

MY_NEW_TALENT(
"My New Talent",
ChatColor.GRAY + "Short description of what it does.",
ChatColor.GREEN + "+10% Resource gain "
+ ChatColor.GRAY + "when you mine rare ore.",
  /* levelRequirement= / 30,
  / maxLevel = / 5,
  / iconMaterial = */ Material.DIAMOND_ORE
  ),

Display name (first argument): Shown in the GUI.

Flavor text (second argument): Always prefixed with ChatColor.GRAY.

Effect line (third argument): Wrap all dynamic descriptions following the DYNAMIC_DESCRIPTIONS.md files instructions to ensure proper formatting.

Level requirement (fourth): Minimum skill level before this talent becomes available.

Max levels (fifth): How many points the player can invest.

Icon (sixth): Material used in the skill tree GUI.

2. Set Rarity via TalentRarity
   Talents inherit their rarity color from their levelRequirement. The mapping lives in TalentRarity.fromRequirement(int):

< 20 → COMMON (white)

< 40 → UNCOMMON (green)

< 60 → RARE (blue)

< 80 → EPIC (dark purple)

>= 80 → LEGENDARY (gold)

No changes needed here—just pick an appropriate levelRequirement.

3. Register the Talent to a Skill in TalentRegistry.java
   Open goat.minecraft.minecraftnew.other.skilltree.TalentRegistry.java.

In the static initializer, find the SKILL_TALENTS.put(...) for your target skill (e.g. Skill.BREWING).

Add your new talent to its list:

SKILL_TALENTS.put(
Skill.BREWING,
Arrays.asList(
Talent.REDSTONE_ONE,
Talent.REDSTONE_TWO,
Talent.OPTIMAL_CONFIGURATION,
Talent.TRIPLE_BATCH,
Talent.MY_NEW_TALENT // ← your new entry
)
);

If the skill isn’t yet registered, follow the same pattern but for your own Skill.YOUR_SKILL.


Verify:

The new talent appears at the correct page and position.

Tooltip colors, descriptions, and level requirements match your definitions.

Investing points applies the intended effect.

Quick Checklist
Enum constant added to Talent.java

Semantic colors & dynamic values in effect line

Registered in TalentRegistry under the correct Skill

(Optional) Custom logic in SkillTreeManager if needed

Compilation, reload, and in‑game verification

Pro Tip: Keep your flavor text under one line (≤ 60 characters) and stick to the established color conventions for consistency across all talents.

if a user requests to add a new Talent to the game, you will need the following EXPLICITLY verified before continuing:

Talent Name: (eg, Triple Batch)
Talent Icon: (eg, cauldron)
Talent Description: (eg, adds glass bottle to catch excess potion)
Talent Technical Description: ("+(5*TalentLevel)% chance to brew 3 Potions". make sure to follow the color coding conventions)
Talent Max level: (eg, 10)
Talent Required Skill Level: (eg, 50)
OPTIONAL: Functionality: (eg, a finished brewing potion has the (5*TalentLevel)% chance to drop 3 potions instead of 1). Many users do this part manually, so be prepared for them to request no functionality.

 










