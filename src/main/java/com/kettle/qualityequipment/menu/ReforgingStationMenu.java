package com.kettle.qualityequipment.menu;

import java.util.Map;

import com.kettle.qualityequipment.QualityEquipmentConfig;
import com.kettle.qualityequipment.blocks.QualityEquipmentBlocks;
import com.kettle.qualityequipment.data.QualityLoader;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class ReforgingStationMenu extends AbstractContainerMenu {
	private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ReforgingStationMenu(int id, Inventory playerInv, FriendlyByteBuf extraData) {
        this(id, playerInv, new SimpleContainer(2), new SimpleContainerData(1), ContainerLevelAccess.NULL);
    }

    public ReforgingStationMenu(int id, Inventory playerInv, Container container, ContainerData data, ContainerLevelAccess access) {
        super(QualityEquipmentMenuTypes.REFORGING_STATION.get(), id);
        this.container = container;
        this.data = data;
        this.access = access;

        // Slot 0: Only items with durability
        this.addSlot(new Slot(container, 0, 80, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
            	boolean curioflag = false;
                if (ModList.get().isLoaded("curios")) {
                	curioflag = stack.getItem() instanceof ICurioItem;
                }
                if (QualityEquipmentConfig.isToolbeltLoaded) {
                	curioflag = stack.getItem() instanceof ToolBeltItem || curioflag;
                }
                return (stack.isDamageableItem() || curioflag) && !QualityEquipmentConfig.black_list.contains(stack.getItem()); // only damageable items and baubles
            }
        });

        // Slot 1: Any item
        this.addSlot(new Slot(container, 1, 80, 60));

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        this.addDataSlots(data);
    }

    

	@Override
    public boolean stillValid(Player player) {
        // now you can validate that the block is still present
        return stillValid(this.access, player, QualityEquipmentBlocks.REFORGING_STATION.get());
    }
	
	private static boolean canMaterialReforge(ItemStack first, ItemStack second) {
		ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(first.getItem());
		// direct item override
		if (QualityEquipmentConfig.ITEM_OVERRIDES.containsKey(itemKey)) {
		    ResourceLocation required = QualityEquipmentConfig.ITEM_OVERRIDES.get(itemKey);
		    return second.is(ForgeRegistries.ITEMS.getValue(required)) || second.is(Items.NETHER_STAR);
		}

		// tag overrides
		for (Map.Entry<TagKey<Item>, ResourceLocation> entry : QualityEquipmentConfig.TAG_OVERRIDES.entrySet()) {
		    if (first.is(entry.getKey())) {
		        return second.is(ForgeRegistries.ITEMS.getValue(entry.getValue())) || second.is(Items.NETHER_STAR);
		    }
		}
		return first.getItem().isValidRepairItem(first, second) || second.is(Items.NETHER_STAR);
	}

 // --- helper for screen ---
    public boolean isReadyToReforge() {
        ItemStack first = container.getItem(0);
        ItemStack second = container.getItem(1);
        boolean curioflag = false;
        if (ModList.get().isLoaded("curios")) {
        	curioflag = first.getItem() instanceof ICurioItem;
        }
        if (QualityEquipmentConfig.isToolbeltLoaded) {
        	curioflag = first.getItem() instanceof ToolBeltItem || curioflag;
        }
        return !first.isEmpty() && !second.isEmpty() && (first.isDamageableItem() || curioflag) && canMaterialReforge(first, second);
    }

    // called when button pressed (we’ll hook this with a packet later)
    public void doReforge(Player player) {
        ItemStack first = container.getItem(0);
        ItemStack second = container.getItem(1);
        if (!isReadyToReforge() || player.level().isClientSide()) return;
        QualityLoader.getRandomQuality(first, player.getRandom());

        // Consume second item
        second.shrink(1);
        container.setItem(0, first);
        container.setItem(1, second);
        container.setChanged();
    }

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int index) {
		ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            int containerSlots = 2;

            if (index < containerSlots) {
                if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // try input slot first, tweak rules as needed
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
	}
}