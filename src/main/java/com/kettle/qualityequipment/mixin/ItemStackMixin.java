package com.kettle.qualityequipment.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.*;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Redirect(
        method = "getTooltipLines",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;getAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"
        ), remap = false
    )
    private Multimap<Attribute, AttributeModifier> filterQualityModifiers(ItemStack stack, EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> original = stack.getAttributeModifiers(slot);
        // Create a copy to avoid modifying vanilla data
        Multimap<Attribute, AttributeModifier> filtered = HashMultimap.create();
        for (Map.Entry<Attribute, AttributeModifier> entry : original.entries()) {
            AttributeModifier mod = entry.getValue();
            // Only add if it’s NOT a quality modifier
            if (!mod.getName().startsWith("qualityequipment.")) {
                filtered.put(entry.getKey(), mod);
            }
        }
        return filtered;
    }
}
