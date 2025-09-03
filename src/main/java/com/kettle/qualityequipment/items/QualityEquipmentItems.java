package com.kettle.qualityequipment.items;

import com.kettle.qualityequipment.QualityEquipmentForked;
import com.kettle.qualityequipment.blocks.QualityEquipmentBlocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class QualityEquipmentItems {

	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, QualityEquipmentForked.MODID);
	public static final RegistryObject<Item> REFORGING_STATION = REGISTRY.register("reforging_station",
			() -> new BlockItem(QualityEquipmentBlocks.REFORGING_STATION.get(), new Item.Properties()));
}
