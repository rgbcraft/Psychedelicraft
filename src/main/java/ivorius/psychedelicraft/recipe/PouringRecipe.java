package ivorius.psychedelicraft.recipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.MutableFluidContainer;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

/**
 * Recipe for pouring fluid from one container to another.
 */
class PouringRecipe extends SpecialCraftingRecipe {
    public PouringRecipe(Identifier id) {
        super(id);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.POUR_DRINK;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        List<MutableFluidContainer> recepticals = getRecepticals(inventory).map(e -> e.content().getKey().toMutable(e.content().getValue())).toList();
        if (RecipeUtils.stacks(inventory).count() != recepticals.size() || recepticals.size() < 2) {
            return false;
        }

        return recepticals.get(1).canReceive(recepticals.get(0).getFluid());
    }

    private Stream<RecipeUtils.Slot<Map.Entry<FluidContainer, ItemStack>>> getRecepticals(CraftingInventory inventory) {
        return RecipeUtils.recepticalSlots(inventory).limit(2);
    }

    @Override
    public ItemStack craft(CraftingInventory inventory) {
        var recepticals = getRecepticals(inventory).toList();

        MutableFluidContainer mutableTo = recepticals.get(1).map(e -> e.getKey().toMutable(e.getValue()));
        recepticals.get(0).map(e -> e.getKey().toMutable(e.getValue()))
                .transfer(mutableTo.getCapacity() - mutableTo.getLevel(), mutableTo, null);

        return mutableTo.asStack();
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingInventory inventory) {
        var recepticals = getRecepticals(inventory).toList();
        if (recepticals.size() < 2) {
            return DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        }

        var from = recepticals.get(0);

        MutableFluidContainer mutableTo = recepticals.get(1).map(e -> e.getKey().toMutable(e.getValue()));
        MutableFluidContainer mutableFrom = from.content().getKey().toMutable(from.content().getValue())
                .transfer(mutableTo.getCapacity() - mutableTo.getLevel(), mutableTo, null);

        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        remainder.set(from.slot(), mutableFrom.asStack());
        return remainder;
    }


    @Override
    public boolean fits(int width, int height) {
        return (width * height) > 2;
    }
}
