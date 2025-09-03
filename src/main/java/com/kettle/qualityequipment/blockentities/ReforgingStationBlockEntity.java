package com.kettle.qualityequipment.blockentities;

import javax.annotation.Nullable;

import com.kettle.qualityequipment.menu.ReforgingStationMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ReforgingStationBlockEntity extends BlockEntity implements MenuProvider, Container {

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY); 
    public ReforgingStationBlockEntity(BlockPos pos, BlockState state) {
        super(QualityEquipmentBlockEntities.REFORGING_STATION.get(), pos, state);
    }
    
    // Called every tick if needed
    public static void tick(Level level, BlockPos pos, BlockState state, ReforgingStationBlockEntity be) {
        // logic like progress bars, crafting checks, etc.
    }

    // MenuProvider -> opens GUI
    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.quality_forked.reforging_station_gui.label_reforging_station");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
    	return new ReforgingStationMenu(id, playerInv, this, new SimpleContainerData(1), ContainerLevelAccess.create(level, worldPosition));
    }

    // Container basics
    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return ContainerHelper.removeItem(items, slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                (double) worldPosition.getX() + 0.5D,
                (double) worldPosition.getY() + 0.5D,
                (double) worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.clear();
    }
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
    }
}