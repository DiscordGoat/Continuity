Adding Strength Support is a simple enough process.
1: Identify deprecated damage increase logic (eg, Sword Damage Talents Granting "+4% Damage". Other possible identifiers are Bonus Damage, Increase Damage, + Damage, etc)
2: Replace deprecated displays of damage increases with Strength. 1 Strength equates to 1% Bonus damage. (for example, replacing "+4% Bonus Damage" with "+4 Strength ⚔️")
3: Replace deprecated logic (all Strength is processed through the StrengthManager, so all you need to do is add strength, no need to change logic. however, old logic will need to be removed. Example: Sword Damage Talents used to have a dedicated damage listener, which was removed when adding the StrengthManager)
4: Provide the user with a detailed report of:
A: Identified Deprecated Logic: True/False, <Location (Example, MinecraftNew.java)>"
B: Replaced Old Display: "<oldDisplay (example: "+4% Damage")>" with "<newDisplay (example: "+4 Strength ⚔️")>"
C: Removed Deprecated Logic: True/False, <oldExampleOfDeprecatedDamageIncreaseListeners/strategies>
5: When adding new components to the StrengthManager#getStrength calculation, ensure each component is also represented in the
   Strength breakdown printout so users can see its contribution.
Notes: Ensure to use the StrengthManager.DISPLAY_NAME, as it includes both the emoji and the Strength Prefix. it does not include a leading space however, so make sure to put a space before using StrengthManager.DISPLAY_NAME.


Notes from high quality examples:
Centralize Strength styling. Use StrengthManager.COLOR (red) and StrengthManager.EMOJI (“⚔”) to ensure all Strength displays match plugin-wide formatting. The preformatted DISPLAY_NAME combines both for convenience

Migrate from any prior “Damage Increase” text and insert a lore line like
StrengthManager.COLOR + "+" + amount + StrengthManager.DISPLAY_NAME

Automate migration of old lore found in itemstacks. ItemLoreFormatter converts legacy “Damage Increase” entries to the new Strength line when processing swords or axes, keeping lore in the standard order

Remove old logic not present in the StrengthManager relating to the increasing of damage. Strength is meant to be the only source of melee damage modifications.

Aggregate Strength via StrengthManager. Compute final Strength using talent levels and sword reforge tiers. Each tier’s weaponDamageIncrease is added to the total Strength value

Clean up obsolete damage logic. Remove old listeners or calculations that applied raw damage bonuses, relying on StrengthManager to provide all sword reforged-based damage boosts for consistency.

Tips for Replicating Similar Updates
Identify deprecated damage paths and confirm they’re only granting percent damage; replace such bonuses with Strength.

Update lore formatting logic (e.g., ItemLoreFormatter) to auto-migrate legacy wording to “Strength:”.

Adjust calculation utilities (StatsCalculator, etc.) so reforges and talents contribute to Strength instead of direct damage, then surface Strength through a dedicated method like getStrength.

Use the existing constants (StrengthManager.COLOR, StrengthManager.EMOJI, StrengthManager.DISPLAY_NAME) to maintain uniform UI presentation.

Run a full build (mvn package) after code changes to validate imports, color codes, and lore placement; verify no lingering listeners or damage multipliers remain outside the Strength pipeline.


