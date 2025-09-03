package com.kettle.qualityequipment.blocks;

import com.kettle.qualityequipment.QualityEquipmentForked;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class QualityEquipmentBlocks {

	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, QualityEquipmentForked.MODID);
	public static final RegistryObject<Block> REFORGING_STATION =
	        BLOCKS.register("reforging_station",
	                () -> new ReforgingStationBlock(BlockBehaviour.Properties.of().strength(5.0F).requiresCorrectToolForDrops().sound(SoundType.ANVIL).noOcclusion()));
	
}
