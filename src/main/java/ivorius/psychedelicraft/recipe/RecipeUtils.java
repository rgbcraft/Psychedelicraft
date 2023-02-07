/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import ivorius.psychedelicraft.fluid.FluidContainer;

public interface RecipeUtils {
    static Stream<Map.Entry<FluidContainer, ItemStack>> recepticals(Inventory inventory) {
        return stacks(inventory)
            .filter(stack -> stack.getItem() instanceof FluidContainer)
            .map(stack -> Map.entry(FluidContainer.of(stack), stack));
    }

    static Stream<ItemStack> stacks(Inventory inventory) {
        return Stream.iterate(0, i -> i < inventory.size(), i -> i + 1)
                .map(inventory::getStack)
                .filter(s -> !s.isEmpty());
    }


    static DefaultedList<Ingredient> getIngredients(JsonArray json) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();
        for (int i = 0; i < json.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(json.get(i));
            if (ingredient.isEmpty()) continue;
            defaultedList.add(ingredient);
        }
        return defaultedList;
    }

    static <T> DefaultedList<T> checkLength(DefaultedList<T> ingredients) {
        if (ingredients.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }
        if (ingredients.size() > 9) {
            throw new JsonParseException("Too many ingredients for shapeless recipe");
        }
        return ingredients;
    }
}
