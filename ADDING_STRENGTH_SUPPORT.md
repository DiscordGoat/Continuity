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
