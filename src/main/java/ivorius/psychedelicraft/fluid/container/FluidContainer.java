package ivorius.psychedelicraft.fluid.container;

import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface FluidContainer extends ItemConvertible {
    FluidContainer UNLIMITED = new FluidContainer() {
        @Override
        public Item asItem() {
            return Items.STONE;
        }
        @Override
        public int getMaxCapacity() {
            return Integer.MAX_VALUE;
        }
    };

    static FluidContainer withCapacity(Item item, int capacity) {
        return new FluidContainer() {
            @Override
            public Item asItem() {
                return item;
            }

            @Override
            public int getMaxCapacity() {
                return capacity;
            }
        };
    }

    static FluidContainer of(ItemStack stack) {
        return of(stack.getItem());
    }

    static FluidContainer of(ItemStack stack, FluidContainer fallback) {
        return of(stack.getItem(), fallback);
    }

    static FluidContainer of(Item item) {
        return of(item, UNLIMITED);
    }

    static FluidContainer of(Item item, FluidContainer fallback) {
        return item instanceof FluidContainer c ? c : FluidContainerRegistry.getContainer(item).orElse(fallback);
    }

    int getMaxCapacity();

    default Item asEmpty() {
        return asItem();
    }

    default Item asFilled(SimpleFluid fluid) {
        return asItem();
    }

    default MutableFluidContainer toMutable(ItemStack stack) {
        return new MutableFluidContainer(this, getFluid(stack), getLevel(stack), getFluidAttributesTag(stack, true), stack.getNbt());
    }

    default int getMaxCapacity(ItemStack stack) {
        return getMaxCapacity();
    }

    default float getFillPercentage(ItemStack stack) {
        return MathHelper.clamp(MathHelper.getLerpProgress(getLevel(stack), 0, getMaxCapacity(stack)), 0, 1);
    }

    default ItemStack getDefaultStack(SimpleFluid fluid) {
        Item bucketItem = this != UNLIMITED || !fluid.isCustomFluid() ? asItem() : of(fluid.getPhysical().getStandingFluid().getBucketItem()).asItem();
        return of(bucketItem).toMutable(bucketItem.getDefaultStack())
                .withFluid(fluid)
                .withLevel(getMaxCapacity())
                .asStack();
    }

    default SimpleFluid getFluid(ItemStack stack) {
        if (!(stack.getNbt() != null && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) || getLevel(stack) == 0) {
            return PSFluids.EMPTY;
        }
        return SimpleFluid.byId(Identifier.tryParse(stack.getSubNbt("fluid").getString("id")));
    }

    default int getLevel(ItemStack stack) {
        return stack.getNbt() != null
                && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)
                && stack.getSubNbt("fluid").contains("id")
                && !SimpleFluid.byId(Identifier.tryParse(stack.getSubNbt("fluid").getString("id"))).isEmpty() ? stack.getSubNbt("fluid").getInt("level") : 0;
    }

    NbtCompound EMPTY_NBT = new NbtCompound();

    static NbtCompound getFluidAttributesTag(ItemStack stack, boolean readOnly) {
        NbtCompound fluidTag = getFluidTag(stack, readOnly);
        if (!readOnly) {
            if (!fluidTag.contains("attributes", NbtElement.COMPOUND_TYPE)) {
                fluidTag.put("attributes", new NbtCompound());
            }
        }
        if (fluidTag.contains("attributes", NbtElement.COMPOUND_TYPE)) {
            return fluidTag.getCompound("attributes");
        }
        return EMPTY_NBT;
    }

    static NbtCompound getFluidTag(ItemStack stack, boolean readOnly) {
        if (!readOnly) {
            return stack.getOrCreateSubNbt("fluid");
        }

        if (stack.hasNbt() && stack.getNbt().contains("fluid", NbtElement.COMPOUND_TYPE)) {
            return stack.getSubNbt("fluid");
        }
        return EMPTY_NBT;
    }
}
