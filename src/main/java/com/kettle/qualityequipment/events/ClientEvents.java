package com.kettle.qualityequipment.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kettle.qualityequipment.QualityEquipmentConfig;
import com.kettle.qualityequipment.QualityEquipmentForked;
import com.kettle.qualityequipment.data.Quality;
import com.kettle.qualityequipment.data.QualityLoader;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.type.capability.ICurioItem;


@Mod.EventBusSubscriber(modid = QualityEquipmentForked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.hasTag() || !stack.getTag().contains("quality")) return;

        int qualityid = stack.getTag().getInt("quality");
        Quality quality = QualityLoader.getQualities().get(qualityid);
        if (quality == null) return;
        if (quality.getAttributes().isEmpty()) {
        	return;
        }
        List<Component> tooltip = event.getToolTip();
		// Quality line
        tooltip.add(Component.literal(""));
        String qualitytext = Component.translatable("quality_equipment.quality").getString();
        String qualityname = Component.translatable(quality.getName()).getString();
        tooltip.add(Component.literal(qualitytext + qualityname));
		boolean isbauble = false;
		if (QualityEquipmentConfig.isFirstAidLoaded && stack.getItem() instanceof ArmorItem) {
			if (FirstAidConfig.CLIENT.armorTooltipMode.get() == FirstAidConfig.Client.TooltipMode.REPLACE) {
				tooltip.removeIf(component -> {
					return component.getContents().toString().contains("attribute.name.generic.armor") || component.getContents().toString().contains("attribute.name.generic.armor");
				});
			}
		}
		if (QualityEquipmentConfig.isCurioLoaded) {
			if (QualityEquipmentConfig.isToolbeltLoaded) {
            	if (stack.getItem() instanceof ToolBeltItem) {
            		isbauble = true;
            	}
            }
			if (stack.getItem() instanceof ICurioItem || isbauble) {
				isbauble = true;
				List<Component> toremove = new ArrayList<>();
				for (Component component: tooltip) {
					if (component.getContents() instanceof TranslatableContents tc &&
			                (tc.getKey().startsWith("curios.modifiers.") || tc.getKey().startsWith("attribute.modifier"))) {
						toremove.add(component);
					}
				}
				
				for (Component component: toremove) {
					tooltip.remove(component);
				}
				
				tooltip.removeIf(component -> 
                component.getString().isBlank()
            );
			}
		}
        // after this everything should be only shown when shift
        if (Screen.hasShiftDown()) {
        	if (!quality.getAttributes().isEmpty()) {
    			for (EquipmentSlot slot : EquipmentSlot.values()) {
    				List<Quality.AttributeModifierData> slotAttrs = quality.getAttributes().stream()
                            .filter(attr -> Arrays.asList(attr.slot()).contains(slot))
                            .toList();

                    if (slotAttrs.isEmpty()) continue;
                    if (isbauble) {
                    	tooltip.add(Component.translatable("quality_equipment.bauble.modifiers")
                                .withStyle(ChatFormatting.GRAY));
                    } else {
                    	tooltip.add(Component.translatable("item.modifiers." + slot.getName())
                                .withStyle(ChatFormatting.GRAY));
                    	
                    }
                    for (Quality.AttributeModifierData data : slotAttrs) {
                        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(data.attribute());
                        if (attribute == null) continue;

                        double amount = data.value();
                        AttributeModifier.Operation op = data.operation();

                        // mimic vanilla formatting
                        double displayValue = amount;
                        if (op != AttributeModifier.Operation.ADDITION) {
                        	displayValue = amount * 100;
                        }
                        MutableComponent current = null;
                        MutableComponent default_attribute = Component.translatable(attribute.getDescriptionId());
                        double multiplier = 1;
                        if (QualityEquipmentConfig.isFirstAidLoaded && attribute == Attributes.ARMOR && stack.getItem() instanceof ArmorItem) {
                        	multiplier = getArmorMultiplier(slot);
                        	displayValue *= multiplier;
                        	default_attribute = Component.translatable("quality_equipment.armor_override");
                        	if (amount > 0) {
                        		current = Component.translatable(
                                        "attribute.modifier.plus." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                                        default_attribute
                                ).withStyle(ChatFormatting.BLUE);
                            } else if (amount < 0) {
                            	current = Component.translatable(
                                        "attribute.modifier.take." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-(displayValue)),
                                        default_attribute
                                ).withStyle(ChatFormatting.RED);
                            }
                        } else if (QualityEquipmentConfig.isFirstAidLoaded && attribute == Attributes.ARMOR_TOUGHNESS && stack.getItem() instanceof ArmorItem) {
                        	multiplier = getToughnessMultiplier(slot);
                        	displayValue *= multiplier;
                        	default_attribute = Component.translatable("quality_equipment.armor_toughness_override");
                        	if (amount > 0) {
                        		current = Component.translatable(
                                        "attribute.modifier.plus." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                                        default_attribute
                                ).withStyle(ChatFormatting.BLUE);
                            } else if (amount < 0) {
                            	current = Component.translatable(
                                        "attribute.modifier.take." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-(displayValue)),
                                        default_attribute
                                ).withStyle(ChatFormatting.RED);
                            }
                        } else {
                        	if (amount > 0) {
                            	current = Component.translatable(
                                        "attribute.modifier.plus." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue),
                                        default_attribute
                                ).withStyle(ChatFormatting.BLUE);
                            } else if (amount < 0) {
                                current = Component.translatable(
                                        "attribute.modifier.take." + op.toValue(),
                                        ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(-(displayValue)),
                                        default_attribute
                                ).withStyle(ChatFormatting.RED);
                            }
                        }
                        displayValue *= multiplier;
                        
                        if (current != null) {
                        	tooltip.add(current);
                        }
                    }
                }
            }
        } else {
        	tooltip.add(Component.translatable("quality_equipment.tooltip.quality_desc"));
        }
    }
	
	private static double getArmorMultiplier(EquipmentSlot slot) {
        if (QualityEquipmentConfig.isFirstAidLoaded) {
        	FirstAidConfig.Server config = FirstAidConfig.SERVER;
            switch (slot) {
                case HEAD:
                    return config.headArmorMultiplier.get();
                case CHEST:
                    return config.chestArmorMultiplier.get();
                case LEGS:
                    return config.legsArmorMultiplier.get();
                case FEET:
                    return config.feetArmorMultiplier.get();
                default:
                    throw new IllegalArgumentException("Invalid slot " + slot);
            }
        } else {
        	return 1d;
        }
    }
	
	private static double getToughnessMultiplier(EquipmentSlot slot) {
        if (QualityEquipmentConfig.isFirstAidLoaded) {
        	FirstAidConfig.Server config = FirstAidConfig.SERVER;
        	switch (slot) {
            case HEAD:
                return config.headThoughnessMultiplier.get();
            case CHEST:
                return config.chestThoughnessMultiplier.get();
            case LEGS:
                return config.legsThoughnessMultiplier.get();
            case FEET:
                return config.feetThoughnessMultiplier.get();
            default:
                throw new IllegalArgumentException("Invalid slot " + slot);
        }
        } else {
        	return 1d;
        }
    }
}
