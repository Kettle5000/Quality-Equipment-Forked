package com.kettle.qualityequipment.blockentities;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.kettle.qualityequipment.QualityEquipmentForked;
import com.kettle.qualityequipment.blocks.QualityEquipmentBlocks;

public class QualityEquipmentBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, QualityEquipmentForked.MODID);
	public static final RegistryObject<BlockEntityType<ReforgingStationBlockEntity>> REFORGING_STATION =
	        BLOCK_ENTITIES.register("reforging_station",
	                () -> BlockEntityType.Builder.of(ReforgingStationBlockEntity::new, QualityEquipmentBlocks.REFORGING_STATION.get()).build(null));

}
