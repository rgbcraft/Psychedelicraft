package ivorius.psychedelicraft.recipe;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonObject;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class BottleRecipe extends ShapedRecipe {
    public static final Map<Item, DyeColor> COLORS = Util.make(new HashMap<>(), map -> {
        map.put(Items.WHITE_STAINED_GLASS, DyeColor.WHITE);
        map.put(Items.ORANGE_STAINED_GLASS, DyeColor.ORANGE);
        map.put(Items.MAGENTA_STAINED_GLASS, DyeColor.MAGENTA);
        map.put(Items.LIGHT_BLUE_STAINED_GLASS, DyeColor.LIGHT_BLUE);
        map.put(Items.YELLOW_STAINED_GLASS, DyeColor.YELLOW);
        map.put(Items.LIME_STAINED_GLASS, DyeColor.LIME);
        map.put(Items.PINK_STAINED_GLASS, DyeColor.PINK);
        map.put(Items.GRAY_STAINED_GLASS, DyeColor.GRAY);
        map.put(Items.LIGHT_GRAY_STAINED_GLASS, DyeColor.LIGHT_GRAY);
        map.put(Items.CYAN_STAINED_GLASS, DyeColor.CYAN);
        map.put(Items.PURPLE_STAINED_GLASS, DyeColor.PURPLE);
        map.put(Items.BLUE_STAINED_GLASS, DyeColor.BLUE);
        map.put(Items.BROWN_STAINED_GLASS, DyeColor.BROWN);
        map.put(Items.GREEN_STAINED_GLASS, DyeColor.GREEN);
        map.put(Items.RED_STAINED_GLASS, DyeColor.RED);
        map.put(Items.BLACK_STAINED_GLASS, DyeColor.BLACK);
    });

    public BottleRecipe(Identifier id, ShapedRecipe recipe) {
        super(id, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getOutput());
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        ItemStack output = getOutput().copy();
        if (output.getItem() instanceof DyeableItem dyeable) {
            RecipeUtils.stacks(inventory)
                .map(stack -> stack.getItem())
                .distinct()
                .map(COLORS::get)
                .filter(Objects::nonNull)
                .findFirst().ifPresent(color -> {
                    dyeable.setColor(output, color.getSignColor());
                });
        }
        return output;
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public ShapedRecipe read(Identifier id, JsonObject json) {
            return new BottleRecipe(id, super.read(id, json));
        }

        @Override
        public ShapedRecipe read(Identifier id, PacketByteBuf buffer) {
            return new BottleRecipe(id, super.read(id, buffer));
        }
    }
}
