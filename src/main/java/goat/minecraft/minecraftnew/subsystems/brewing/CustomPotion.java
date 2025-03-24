package goat.minecraft.minecraftnew.subsystems.brewing;

import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CustomPotion {
    private final String name;
    private final List<String> lore;
    private final Color color;
    private final int duration;   // Effect duration (seconds)
    private final int brewTime;   // Brewing time (seconds)
    private final ItemStack[] ingredients; // Up to 4 ingredients

    public CustomPotion(String name, List<String> lore, Color color, int duration, int brewTime,
                        ItemStack ingredient1, ItemStack ingredient2, ItemStack ingredient3, ItemStack ingredient4) {
        this.name = name;
        this.lore = lore;
        this.color = color;
        this.duration = duration;
        this.brewTime = brewTime;
        this.ingredients = new ItemStack[]{ ingredient1, ingredient2, ingredient3, ingredient4 };
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Color getColor() {
        return color;
    }

    public int getDuration() {
        return duration;
    }

    public int getBrewTime() {
        return brewTime;
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }
}
