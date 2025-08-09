Adding Health Support is a straightforward process.
1: Identify deprecated max health increase logic (eg, Talents granting "+4 HP" or other direct heart increases like Bonus Health, +Hearts, etc)
2: Replace deprecated displays of health increases with Health. 2 Health equates to one heart (for example, replacing "+4 HP" with "+4 Health ❤")
3: Replace deprecated logic (all Health is processed through the HealthManager, so all you need to do is add health, no need to change logic. however, old logic will need to be removed. Example: Health Talents used to have a dedicated health listener, which was removed when adding the HealthManager)
4: Provide the user with a detailed report of:
A: Identified Deprecated Logic: True/False, <Location (Example, MinecraftNew.java)>"
B: Replaced Old Display: "<oldDisplay (example: "+4 HP")>" with "<newDisplay (example: "+4 Health ❤")>"
C: Removed Deprecated Logic: True/False, <oldExampleOfDeprecatedHealthIncreaseListeners/strategies>
5: When adding new components to the HealthManager#getMaxHealth calculation, ensure each component is also represented in the Health breakdown printout so users can see its contribution.

Notes: Ensure to use the HealthManager.DISPLAY_NAME, as it includes both the emoji and the Health Prefix. it does not include a leading space however, so make sure to put a space before using HealthManager.DISPLAY_NAME.


Notes from high quality examples:
Centralize Health styling. Use HealthManager.COLOR (red) and HealthManager.EMOJI (“❤”) to ensure all Health displays match plugin-wide formatting. The preformatted DISPLAY_NAME combines both for convenience

Migrate from any prior “Bonus Hearts” or “HP” text and insert a lore line like
HealthManager.COLOR + "+" + amount + HealthManager.DISPLAY_NAME

Automate migration of old lore found in itemstacks. ItemLoreFormatter converts legacy “Bonus Health” entries to the new Health line when processing items, keeping lore in the standard order

Remove old logic not present in the HealthManager relating to the increasing of max health. Health is meant to be the only source of health modifications.

Aggregate Health via HealthManager. Compute final Health using talent levels, beacon passives, set bonuses, pet traits, etc., and surface Health through a dedicated method like getMaxHealth

Clean up obsolete health logic. Remove old listeners or calculations that applied raw health bonuses, relying on HealthManager to provide all max health boosts for consistency.

Tips for Replicating Similar Updates
Identify deprecated health paths and confirm they’re only granting flat health; replace such bonuses with Health.

Update lore formatting logic (e.g., ItemLoreFormatter) to auto-migrate legacy wording to “Health:”.

Adjust calculation utilities (StatsCalculator, etc.) so reforges, talents, beacon passives, and set bonuses contribute to Health, then surface Health through HealthManager#getMaxHealth.

Use the existing constants (HealthManager.COLOR, HealthManager.EMOJI, HealthManager.DISPLAY_NAME) to maintain uniform UI presentation.

Run a full build (mvn package) after code changes to validate imports, color codes, and lore placement; verify no lingering listeners or health multipliers remain outside the Health pipeline.

