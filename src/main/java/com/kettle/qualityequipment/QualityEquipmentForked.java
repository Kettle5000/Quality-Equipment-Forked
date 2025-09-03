package com.kettle.qualityequipment;

import com.kettle.qualityequipment.attributes.QualityEquipmentForkedAttributes;
import com.kettle.qualityequipment.blockentities.QualityEquipmentBlockEntities;
import com.kettle.qualityequipment.blocks.QualityEquipmentBlocks;
import com.kettle.qualityequipment.data.QualityLoader;
import com.kettle.qualityequipment.events.CurioEvents;
import com.kettle.qualityequipment.items.QualityEquipmentItems;
import com.kettle.qualityequipment.menu.QualityEquipmentMenuTypes;
import com.kettle.qualityequipment.menu.ReforgingStationScreen;
import com.kettle.qualityequipment.network.NetworkManager;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.config.ModConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(QualityEquipmentForked.MODID)
public class QualityEquipmentForked {
	public static final Logger LOGGER = LogManager.getLogger(QualityEquipmentForked.class);
	public static final String MODID = "quality_forked";

	public QualityEquipmentForked() {
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		QualityEquipmentBlocks.BLOCKS.register(bus);
		QualityEquipmentBlockEntities.BLOCK_ENTITIES.register(bus);
		QualityEquipmentItems.REGISTRY.register(bus);
		QualityEquipmentMenuTypes.MENUS.register(bus);
		QualityEquipmentForkedAttributes.ATTRIBUTES.register(bus);
		NetworkManager.register();
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, QualityEquipmentConfig.SPEC, "qualityequipmentfork.toml");
		bus.addListener(this::registerScreen);
		MinecraftForge.EVENT_BUS.addListener(this::addReloadListeners);
		MinecraftForge.EVENT_BUS.addListener(this::onTagsUpdated);
		if (FMLLoader.getLoadingModList().getModFileById("curios") != null) {
			MinecraftForge.EVENT_BUS.register(CurioEvents.class);
		}
	}
	
	public void registerScreen(FMLClientSetupEvent event){
		MenuScreens.register(QualityEquipmentMenuTypes.REFORGING_STATION.get(), ReforgingStationScreen::new);
    }
	
	public void addReloadListeners(AddReloadListenerEvent event) {
	    event.addListener(new QualityLoader());
	}
	
    public void onTagsUpdated(TagsUpdatedEvent event) {
		if (event.shouldUpdateStaticData()) {
			QualityLoader.buildItemQualityMap();
		}
    }
}
