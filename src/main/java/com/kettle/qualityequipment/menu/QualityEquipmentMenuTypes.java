package com.kettle.qualityequipment.menu;

import com.kettle.qualityequipment.QualityEquipmentForked;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class QualityEquipmentMenuTypes {

	public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, QualityEquipmentForked.MODID);

    public static final RegistryObject<MenuType<ReforgingStationMenu>> REFORGING_STATION =
            MENUS.register("reforging_station",
                    () -> IForgeMenuType.create(ReforgingStationMenu::new));
}
