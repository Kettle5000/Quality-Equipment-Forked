package com.kettle.qualityequipment.events;

import java.util.UUID;

import com.kettle.qualityequipment.QualityEquipmentConfig;
import com.kettle.qualityequipment.data.Quality;
import com.kettle.qualityequipment.data.QualityLoader;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

public class CurioEvents {
	
	@SubscribeEvent
	public static void addCuriosModifiers(CurioAttributeModifierEvent event) {
		ItemStack stack = event.getItemStack();
	    if (!stack.hasTag() || !stack.getTag().contains("quality") || QualityEquipmentConfig.black_list.contains(stack.getItem())) return;
	    int qualityid = stack.getTag().getInt("quality");
	    Quality quality = QualityLoader.getQualities().get(qualityid);

	    if (quality == null) return;

	    for (Quality.AttributeModifierData data : quality.getAttributes()) {
	        Attribute attribute = ForgeRegistries.ATTRIBUTES.getHolder(data.attribute())
	            .orElseThrow()
	            .get();

	        AttributeModifier modifier = new AttributeModifier(
	            UUID.nameUUIDFromBytes((quality.getName() + data.attribute() + data.slot()).getBytes()),
	            "qualityequipment." + quality.getName(),
	            data.value(),
	            data.operation()
	        );
	        event.addModifier(attribute, modifier);
	    }
	}

}
