/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created from "RecipeFillDrink" by Sollace on 5 Jan 2023
 * Original by lukas on 21.10.14.
 * Recipe that takes as a config:
 * - Input Ingrediences (unshaped)
 * - Input Container
 * - Preconfigured fluid+level
 *
 * Outputs:
 * - Original Container filled with assigned fluid and level
 */
public class FillRecepticalRecipe extends ShapelessRecipe {
    private final Ingredient receptical;
    private final DefaultedList<Ingredient> input;
    private final FluidIngredient output;

    public FillRecepticalRecipe(String group, CraftingRecipeCategory category, FluidIngredient output, DefaultedList<Ingredient> input, Ingredient receptical) {
        super(group, category, ItemStack.EMPTY, receptical.isEmpty() ? input : DefaultedList.copyOf(Ingredient.EMPTY, Stream.concat(Stream.of(receptical), input.stream()).toArray(Ingredient[]::new)));
        this.input = input;
        this.output = output;
        this.receptical = receptical;
    }

    public FluidIngredient getOutputFluid() {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.FILL_RECEPTICAL;
    }

    @Override
    public boolean matches(RecipeInputInventory inventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        return RecipeUtils.recepticals(inventory).count() == 1
                && RecipeUtils.stacks(inventory).filter(stack -> {
                    recipeMatcher.addInput(stack, 1);
                    return true;
                }).count() == getIngredients().size()
                && recipeMatcher.match(this, null);
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registries) {
        return RecipeUtils.recepticals(inventory).findFirst().map(receptical -> {
            ItemStack stack = output.fluid().getDefaultStack(receptical.getKey(), output.level() <= 0
                ? receptical.getKey().getMaxCapacity(receptical.getValue())
                : (output.level() + receptical.getKey().getLevel(receptical.getValue()))
            );
            stack.getOrCreateSubNbt("fluid").copyFrom(output.attributes());
            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    static class Serializer implements RecipeSerializer<FillRecepticalRecipe> {
        private static final Codec<FillRecepticalRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.createStrictOptionalFieldCodec(Codec.STRING, "group", "").forGetter(FillRecepticalRecipe::getGroup),
                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(FillRecepticalRecipe::getCategory),
                FluidIngredient.CODEC.fieldOf("result").forGetter(FillRecepticalRecipe::getOutputFluid),
                RecipeUtils.SHAPELESS_RECIPE_INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(recipe -> recipe.input),
                Ingredient.ALLOW_EMPTY_CODEC.fieldOf("receptical").forGetter(recipe -> recipe.receptical)
        ).apply(instance, FillRecepticalRecipe::new));

        @Override
        public Codec<FillRecepticalRecipe> codec() {
            return CODEC;
        }

        @Override
        public FillRecepticalRecipe read(PacketByteBuf buffer) {
            return new FillRecepticalRecipe(
                    buffer.readString(),
                    buffer.readEnumConstant(CraftingRecipeCategory.class),
                    new FluidIngredient(buffer),
                    buffer.readCollection(DefaultedList::ofSize, Ingredient::fromPacket),
                    Ingredient.fromPacket(buffer)
            );
        }

        @Override
        public void write(PacketByteBuf buffer, FillRecepticalRecipe recipe) {
            buffer.writeString(recipe.getGroup());
            buffer.writeEnumConstant(recipe.getCategory());
            recipe.output.write(buffer);
            buffer.writeCollection(recipe.input, (b, c) -> c.write(b));
            recipe.receptical.write(buffer);
        }
    }
}
