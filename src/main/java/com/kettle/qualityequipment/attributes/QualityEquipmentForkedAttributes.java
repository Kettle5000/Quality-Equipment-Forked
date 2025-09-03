package com.kettle.qualityequipment.attributes;

import com.kettle.qualityequipment.QualityEquipmentForked;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD)
public class QualityEquipmentForkedAttributes {
	public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES,
			QualityEquipmentForked.MODID);

	public static final RegistryObject<Attribute> DIG_SPEED = ATTRIBUTES.register("dig_speed",
			() -> new RangedAttribute("attribute.quality_forked.dig_speed", 0.0D, 0.0D, 1024.0D).setSyncable(true));

	public static final RegistryObject<Attribute> JUMP_HEIGHT = ATTRIBUTES.register("jump_height",
			() -> new RangedAttribute("attribute.quality_forked.jump_height", 1.0D, 0.0D, 16.0D).setSyncable(true));

	public static final RegistryObject<Attribute> DAMAGE_RESISTANCE = ATTRIBUTES.register("damage_resistance",
			() -> new RangedAttribute("attribute.quality_forked.damage_resistance", 0.0D, 0.0D, 1.0D)
					.setSyncable(true));

	public static final RegistryObject<Attribute> PROJECTILE_DAMAGE = ATTRIBUTES.register("projectile_damage",
			() -> new RangedAttribute("attribute.quality_forked.projectile_damage", 2.0D, -1024.0D, 1024.0D)
					.setSyncable(true));
	
	public static final RegistryObject<Attribute> MAGIC_RESISTANCE = ATTRIBUTES.register("magic_resistance",
			() -> new RangedAttribute("attribute.quality_forked.magic_resistance", 0.0D, -1024.0D, 1024.0D)
					.setSyncable(true));
	
	public static final RegistryObject<Attribute> MAGIC_DAMAGE = ATTRIBUTES.register("magic_damage",
			() -> new RangedAttribute("attribute.quality_forked.magic_damage", 0.0D, -1024.0D, 1024.0D)
					.setSyncable(true));

	@SubscribeEvent
	public static void addAttributes(EntityAttributeModificationEvent event) {
		event.add(EntityType.PLAYER, DIG_SPEED.get());
		event.add(EntityType.PLAYER, JUMP_HEIGHT.get());
		event.add(EntityType.PLAYER, DAMAGE_RESISTANCE.get());
		event.add(EntityType.PLAYER, PROJECTILE_DAMAGE.get());
		event.add(EntityType.PLAYER, MAGIC_DAMAGE.get());
		event.add(EntityType.PLAYER, MAGIC_RESISTANCE.get());
	}
}