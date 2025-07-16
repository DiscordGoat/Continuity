To add a new potion to the game, follow these steps:
Before adding any Potion to the game, verify these key data values from the user:

1: Potion Name
2: Potion Duration
3: Potion Color
4: Potion Logic (What the potion affects)
5: Potion Recipe Ingredients (The combination of items to begin brewing)
6: Relic Name
7: Relic Obtainment Logic (how to get the Seed)
8: Cleric Cost

When asked to add a new potion to the game, Respond with that list of clarifying information to ensure quality output.

1: Create the custom consumption logic found in src/main/java/goat/minecraft/minecraftnew/subsystems/brewing/custompotions.
You will need information regarding initial duration (if not given, go with 3 minutes).
You will need information regarding the final Potions name (example: "Potion of Fountains"). 

2: Register the new potion's effect listener (if applicable) in the main class. 
example: getServer().getPluginManager().registerEvents(new PotionOfSolarFury(), this); adds the Potion Of Solar Fury consumption logic to the server.

3: Navigate to the PotionBrewingSubsystem.java class and add the new Potion's recipe to the recipeRegistry. All potions require nether wart and glass bottles followed by 
additional ingredients. All Potions require a Verdant Relic for rarity purposes. 

4: Create the effect logic. If possible, place this logic within the Potion's custompotions consumption listener. Otherwise, apply the logic in the relevant class. For 
example, Potion of Fountains increases sea creature chance, but sea creature chance is only managed within the fishingEvent listener, so we apply the logic in that class.

5: After logic has been integrated, then move onto the Verdant Relic logic. As stated, all new Potions require an exclusive Verdant Relic. To create this Relic and its seed,
navigate to the ItemRegistry.java class and create the seed and its "final output" form. An example is given below:

This is the creation of the Sunflare Relic. For the seed's functionality to work, it must be named with the "Verdant Relic <name>" format, otherwise its logic wont be 
plantable. 


public static ItemStack getSunflare() {
return createCustomItem(
Material.BLAZE_POWDER,
ChatColor.GOLD + "Sunflare",
Arrays.asList(
ChatColor.GRAY + "A blazing relic radiating intense heat.",
ChatColor.BLUE + "Used in brewing the Potion of Solar Fury."
),
1,
false,
true
);
}

public static ItemStack getVerdantRelicSunflareSeed() {
        return createCustomItem(
                Material.WHEAT_SEEDS,
                ChatColor.GOLD + "Verdant Relic Sunflare",
                Arrays.asList(
                        ChatColor.GRAY + "A relic seed ignited with solar energy.",
                        ChatColor.BLUE + "Dropped from monsters slain by Fire Level.",
                        ChatColor.BLUE + "Right-click on dirt/grass to plant."
                ),
                1,
                false,
                true
        );
    }

6: Upon the finalization of the Verdant Relic entries, logic for obtaining the Relic Seed must be integrated as well. Relics have no themed source, and as such can be 
obtained from any logic. Fishing, Forestry, Combat, even loot tables. (loot tables will likely be integrated after a while). For example, the Treasury Relic is dropped
from a Midas Sea Creature.

7: Finally, the Cleric needs to have all Recipes to all new Potions within his purchases whitelist, allowing the player to purchase the recipes from him.

8: To recap, the player obtains the recipe from the cleric via purchasing it, then obtains the ingredients in any order, getting a Verdant Relic seed,
planting the seed and caring for it until its ripe, harvesting the Relic and using it to begin the brewing process, then gets the output potion and drinks it for the effect.





















