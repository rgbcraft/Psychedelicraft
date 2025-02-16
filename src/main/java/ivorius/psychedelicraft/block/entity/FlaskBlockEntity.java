/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */
package ivorius.psychedelicraft.block.entity;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.fluid.*;
import ivorius.psychedelicraft.fluid.container.FluidContainer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.util.NbtSerialisable;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 2 Jan 2023
 */
public class FlaskBlockEntity extends SyncedBlockEntity
        implements BlockWithFluid.DirectionalFluidResovoir,
                   Resovoir.ChangeListener, SidedInventory,
                   SidedStorageBlockEntity {
    private static final int[] NO_SLOT_ID = {};
    private static final int[] INPUT_SLOT_ID = {0};
    private static final int[] OUTPUT_SLOT_ID = {1};

    public static final int FLASK_CAPACITY = FluidVolumes.BUCKET * 8;

    private final Resovoir tank;
    private boolean pendingSync;

    public final IoSlot inputSlot = new IoSlot(0);
    public final IoSlot outputSlot = new IoSlot(1);

    public final IoInventory ioInventory = new IoInventory();

    public final PropertyDelegate propertyDelegate = new ArrayPropertyDelegate(getTotalProperties());

    public FlaskBlockEntity(BlockPos pos, BlockState state) {
        this(PSBlockEntities.FLASK, pos, state, FLASK_CAPACITY);
    }

    public FlaskBlockEntity(BlockEntityType<? extends FlaskBlockEntity> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        tank = new Resovoir(capacity, this);
    }

    public void markForUpdate() {
        pendingSync = true;
    }

    protected int getTotalProperties() {
        return 4;
    }

    @Override
    public void onDrain(Resovoir resovoir) {
        onIdle(resovoir);
    }

    @Override
    public void onFill(Resovoir resovoir, int amountFilled) {
        onIdle(resovoir);
    }

    @Override
    public void onIdle(Resovoir resovoir) {
        markForUpdate();
    }

    @Override
    public Resovoir getTank(Direction direction) {
        return tank;
    }

    @Override
    public void tick(ServerWorld world) {
        ItemStack output = outputSlot.getStack();
        boolean playSound = false;
        FluidContainer container = FluidContainer.of(output, null);
        if (container != null && container.getFillPercentage(output) < 1) {
            int oldLevel = container.getLevel(output);
            ioInventory.setStack(1, tank.drain(50, output, outputSlot::incrementLevelsTransferred));
            playSound |= oldLevel != FluidContainer.of(outputSlot.getStack()).getLevel(outputSlot.getStack());
        }

        ItemStack input = inputSlot.getStack();
        container = FluidContainer.of(input, null);
        if (container != null && container.getFillPercentage(input) > 0) {
            int oldLevel = container.getLevel(input);
            ioInventory.setStack(0, tank.deposit(50, input, inputSlot::incrementLevelsTransferred));
            playSound |= oldLevel != FluidContainer.of(inputSlot.getStack()).getLevel(inputSlot.getStack());
        }

        if (playSound && world.getTime() % 9 == 0) {
            world.playSound(null, getPos(), SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 0.025F, 0.5F);
        }

        if (pendingSync) {
            pendingSync = false;
            markDirty();
            world.getChunkManager().markForUpdate(getPos());
        }
    }

    @Override
    public List<ItemStack> getDroppedStacks(FluidContainer container) {
        List<ItemStack> stacks = new ArrayList<>();
        ItemStack flaskStack = container.getDefaultStack(PSFluids.EMPTY);
        int maxCapacity = container.getMaxCapacity(flaskStack);
        while (!tank.isEmpty()) {
            stacks.add(tank.drain(maxCapacity, flaskStack));
        }
        return stacks;
    }

    @Override
    public void writeNbt(NbtCompound compound) {
        super.writeNbt(compound);
        compound.put("tank", tank.toNbt());
        Inventories.writeNbt(compound, ioInventory.stacks);
        compound.put("inputSlot", inputSlot.toNbt());
        compound.put("outputSlot", outputSlot.toNbt());
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        tank.fromNbt(compound.getCompound("tank"));
        Inventories.readNbt(compound, ioInventory.stacks);
        inputSlot.fromNbt(compound.getCompound("inputSlot"));
        outputSlot.fromNbt(compound.getCompound("outputSlot"));
    }

    @Override
    public int size() {
        return ioInventory.size();
    }

    @Override
    public boolean isEmpty() {
        return ioInventory.isEmpty() && tank.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return ioInventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return ioInventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return ioInventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ioInventory.setStack(slot, stack);
        onContentsExternallyChanged(slot);
    }

    public void onContentsExternallyChanged(int slot) {
        if (slot == 0) {
            inputSlot.onChange();
        }
        if (slot == 1) {
            outputSlot.onChange();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity var1) {
        return true;
    }

    @Override
    public void clear() {
        ioInventory.clear();
        tank.clear();
    }

    @Override
    public int[] getAvailableSlots(Direction direction) {
        Direction facing = getCachedState().getOrEmpty(Properties.HORIZONTAL_FACING).orElse(Direction.UP);
        if (facing.getAxis() != Direction.Axis.Y && direction.getAxis() != Direction.Axis.Y) {
            direction = Direction.fromRotation(facing.asRotation() - direction.asRotation());
        }

        if (direction == Direction.EAST) {
            return INPUT_SLOT_ID;
        }
        if (direction == Direction.WEST) {
            return OUTPUT_SLOT_ID;
        }
        if (direction == Direction.UP || direction == Direction.DOWN) {
            return new int[] {0, 1};
        }
        return NO_SLOT_ID;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction direction) {
        int[] availableSlots = getAvailableSlots(direction);

        if (availableSlots.length == 0) {
            return false;
        }

        if (slot == OUTPUT_SLOT_ID[0]) {
            return ioInventory.isValid(slot, stack) && FluidContainer.of(stack).getFillPercentage(stack) < 1;
        }

        if (slot == INPUT_SLOT_ID[0]) {
            return ioInventory.isValid(slot, stack) && FluidContainer.of(stack).getFillPercentage(stack) > 0;
        }

        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction direction) {
        int[] availableSlots = getAvailableSlots(direction);

        if (availableSlots.length == 0) {
            return false;
        }

        if (slot == OUTPUT_SLOT_ID[0] && outputSlot.getFillPercentage(-1) >= 1) {
            return true;
        }

        if (slot == INPUT_SLOT_ID[0] && inputSlot.getFillPercentage(1) <= 0) {
            return true;
        }

        return false;
    }

    @Override
    public Storage<FluidVariant> getFluidStorage(Direction side) {
        return getTank(side);
    }

    class IoInventory extends SimpleInventory {
        public IoInventory() {
            super(2);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public boolean isValid(int slot, ItemStack stack) {
            var container = FluidContainer.of(stack, null);
            return container != null && (slot == 0 ? container.getFillPercentage(stack) > 0 : container.getFillPercentage(stack) < 1);
        }
    }

    public class IoSlot implements NbtSerialisable {
        public final int index;

        private final int levelsTransferredIndex;
        private final int inputtedLevelsIndex;

        public IoSlot(int index) {
            this.index = index;
            levelsTransferredIndex = 0 + (index * 2);
            inputtedLevelsIndex = 1 + (index * 2);
        }

        public float getProgress() {
            return propertyDelegate.get(inputtedLevelsIndex) == 0 ? 0 : (float)propertyDelegate.get(levelsTransferredIndex) / propertyDelegate.get(inputtedLevelsIndex);
        }

        public void incrementLevelsTransferred(int amount) {
            propertyDelegate.set(levelsTransferredIndex, propertyDelegate.get(levelsTransferredIndex) + amount);
        }

        public void onChange() {
            ItemStack stack = ioInventory.getStack(index);
            var container = FluidContainer.of(stack);
            int levels = container.getLevel(stack);
            if (index == 1) {
                levels = container.getMaxCapacity(stack) - levels;
            }
            propertyDelegate.set(inputtedLevelsIndex, levels);
            propertyDelegate.set(levelsTransferredIndex, 0);
        }

        public ItemStack getStack() {
            return ioInventory.getStack(index);
        }

        public float getFillPercentage(float def) {
            ItemStack stack = getStack();
            return stack.getItem() instanceof FluidContainer container ? container.getFillPercentage(stack) : def;
        }

        @Override
        public void toNbt(NbtCompound compound) {
            compound.putInt("inputtedLevels", propertyDelegate.get(inputtedLevelsIndex));
            compound.putInt("levelsTransferred", propertyDelegate.get(levelsTransferredIndex));
        }

        @Override
        public void fromNbt(NbtCompound compound) {
            propertyDelegate.set(inputtedLevelsIndex, compound.getInt("inputtedLevels"));
            propertyDelegate.set(levelsTransferredIndex, compound.getInt("levelsTransferred"));
        }
    }
}
