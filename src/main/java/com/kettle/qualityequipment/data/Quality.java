package com.kettle.qualityequipment.data;

import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

public class Quality {
    private final int id; // unique identifier
    private final String name;
    private final int weight;
    private final Set<ResourceLocation> items;
    private final Set<TagKey<Item>> itemTags;
    private final Set<Class<? extends Item>> itemClasses;
    private final List<AttributeModifierData> attributes;
    private final EquipmentSlot slot;

    public Quality(String name, int weight, Set<ResourceLocation> items, Set<TagKey<Item>> itemTags, Set<Class<? extends Item>> Classes, EquipmentSlot slot, List<AttributeModifierData> attributes, int id) {
        this.id = id; // assign unique ID
        this.name = name;
        this.weight = weight;
        this.items = items;
        this.itemTags = itemTags;
        this.attributes = attributes;
        this.itemClasses = Classes;
        this.slot = slot;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getWeight() { return weight; }
    public Set<ResourceLocation> getItems() { return items; }
    public Set<TagKey<Item>> getItemTags() { return itemTags; }
    public Set<Class<? extends Item>> getItemClasses() { return itemClasses; }
    public List<AttributeModifierData> getAttributes() { return attributes; }
    public EquipmentSlot getSlot() { return slot; }

    public record AttributeModifierData(ResourceLocation attribute,
                                        AttributeModifier.Operation operation,
                                        double value,
                                        EquipmentSlot slot) {}
}
