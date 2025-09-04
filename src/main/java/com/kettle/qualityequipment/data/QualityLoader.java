package com.kettle.qualityequipment.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kettle.qualityequipment.QualityEquipmentForked;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.Reader;

public class QualityLoader extends SimpleJsonResourceReloadListener {
    private static final Map<Integer, Quality> QUALITIES = new HashMap<>();
    public static final Map<Item, List<Quality>> ITEM_TO_QUALITIES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FOLDER = "qualities";
    private static final String location = "net.minecraft.world.item.";

    public QualityLoader() {
        super(GSON, FOLDER); // path: data/<modid>/qualities
    }
    
    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> out = new HashMap<>();
        FileToIdConverter conv = FileToIdConverter.json(FOLDER); // same folder you pass to super

        conv.listMatchingResourceStacks(manager).forEach((fileLoc, stack) -> {
            ResourceLocation id = conv.fileToId(fileLoc);
            // Highest priority resource is last in the stack
            Resource top = stack.get(stack.size() - 1);
            try (Reader r = top.openAsReader()) {
                JsonElement je = GsonHelper.fromJson(GSON, r, JsonElement.class);

                // (Optional) debug where it came from
                QualityEquipmentForked.LOGGER.info("Added {} from pack {}", id, top.sourcePackId());
                out.put(id, je);
            } catch (Exception ex) {
                QualityEquipmentForked.LOGGER.error("Couldn't parse data file {} from {}", id, fileLoc, ex);
            }
        });
        return out;
    }


    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        QUALITIES.clear();
        int loop = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : objectMap.entrySet()) {
            try {
            	QualityEquipmentForked.LOGGER.info("Loaded key: " + entry.getKey());
                JsonObject root = entry.getValue().getAsJsonObject();
                // Parse common items/tags
                Set<ResourceLocation> items = new HashSet<>();
                if (root.has("items")) {
                    for (JsonElement e : root.getAsJsonArray("items")) {
                        items.add(new ResourceLocation(e.getAsString()));
                        QualityEquipmentForked.LOGGER.info("This key had an item: " + entry.getKey());
                    }
                }

                Set<TagKey<Item>> itemTags = new HashSet<>();
                if (root.has("item_tags")) {
                    for (JsonElement e : root.getAsJsonArray("item_tags")) {
                    	itemTags.add(TagKey.create(ForgeRegistries.Keys.ITEMS, new ResourceLocation(e.getAsString().trim())));
                    }
                }
                
                Set<Class<? extends Item>> itemClasses = new HashSet<>();
                if (root.has("class")) {
                    for (JsonElement e : root.getAsJsonArray("class")) {
                        try {
                            Class<?> clazz = Class.forName(location + e.getAsString().trim());
                            if (Item.class.isAssignableFrom(clazz)) {
                                itemClasses.add((Class<? extends Item>) clazz);
                            } else {
                                QualityEquipmentForked.LOGGER.warn("Class {} is not an Item!", e.getAsString());
                            }
                        } catch (ClassNotFoundException ex) {
                            QualityEquipmentForked.LOGGER.warn("Unknown item class: {}", e.getAsString());
                        }
                    }
                } 
                EquipmentSlot filter = EquipmentSlot.HEAD;
                if (root.has("slot")) {
                	try {
                        filter = EquipmentSlot.valueOf(root.get("slot").getAsString().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        QualityEquipmentForked.LOGGER.warn("Ignoring bad slot: " + root.get("slot").getAsString());
                    }
                }
             // Parse qualities
                if (root.has("qualities")) {
                    for (JsonElement qElem : root.getAsJsonArray("qualities")) {
                        JsonObject qObj = qElem.getAsJsonObject();
                        loop = loop + 1;
                        loadQuality(entry.getKey(), qObj, items, itemTags, itemClasses, filter, loop);
                    }
                }
                
            } catch (Exception ex) {
                QualityEquipmentForked.LOGGER.warn(
                    "Failed to load qualities from " + entry.getKey() + ": " + ex.getMessage()
                );
            }
        }
    }
    
	private void loadQuality(ResourceLocation fileId, JsonObject json, Set<ResourceLocation> items, Set<TagKey<Item>> itemTags, Set<Class<? extends Item>> Classes, EquipmentSlot filter, int loop) {
        String name = json.get("name").getAsString();
        int weight = json.get("weight").getAsInt();
        List<Quality.AttributeModifierData> attributes = new ArrayList<>();
        if (json.has("attributes")) {
            for (JsonElement e : json.getAsJsonArray("attributes")) {
                JsonObject attr = e.getAsJsonObject();
                ResourceLocation attrId = new ResourceLocation(attr.get("attribute").getAsString());
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getHolder(attrId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown attribute: " + attrId))
                    .get();

                AttributeModifier.Operation op = AttributeModifier.Operation
                    .valueOf(attr.get("operation").getAsString().toUpperCase(Locale.ROOT));
                double value = attr.get("value").getAsDouble();

                EquipmentSlot slot = EquipmentSlot.OFFHAND; // default if none
                if (attr.has("slot")) {
                    try {
                        slot = EquipmentSlot.valueOf(attr.get("slot").getAsString().toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException ex) {
                        QualityEquipmentForked.LOGGER.warn("Ignoring bad slot: " + attr.get("slot").getAsString());
                    }
                }

                attributes.add(new Quality.AttributeModifierData(attrId, op, value, slot));
            }
        }

        Quality q = new Quality(name, weight, items, itemTags, Classes, filter, attributes, loop);
        QUALITIES.put(loop, q); 
    }
    
    @SuppressWarnings("deprecation")
	public static void buildItemQualityMap() {
    	ITEM_TO_QUALITIES.clear();
        for (Quality q : QUALITIES.values()) {
            // direct item matches
            for (ResourceLocation rl : q.getItems()) {
                Item item = ForgeRegistries.ITEMS.getValue(rl);
                if (item != null) {
                    ITEM_TO_QUALITIES
                        .computeIfAbsent(item, i -> new ArrayList<>())
                        .add(q);
                }
            }

            // tag matches
            for (TagKey<Item> tag : q.getItemTags()) {
                for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
                    ITEM_TO_QUALITIES
                        .computeIfAbsent(holder.value(), i -> new ArrayList<>())
                        .add(q);
                }
            }
            
            //Item classess, this one is very easy to crash so be careful
            for (Class<? extends Item> itemclass : q.getItemClasses()) {
                for (Item item1 : ForgeRegistries.ITEMS.getValues()) {
                	if (itemclass.isAssignableFrom(item1.getClass())) {
                		if (item1 instanceof ArmorItem armor && q.getSlot() == armor.getEquipmentSlot()) {
                			ITEM_TO_QUALITIES
                            .computeIfAbsent(item1, i -> new ArrayList<>())
                            .add(q);
                		} else if (!(item1 instanceof ArmorItem)) {
                			ITEM_TO_QUALITIES
                            .computeIfAbsent(item1, i -> new ArrayList<>())
                            .add(q);
                		}
                	}
                }
            }
            
            
        }
    }
    
	public static void getRandomQuality(ItemStack stack, RandomSource random) {
		List<Quality> candidates = ITEM_TO_QUALITIES.get(stack.getItem());
	    if (candidates == null || candidates.isEmpty()) return;
        int totalWeight = candidates.stream().mapToInt(Quality::getWeight).sum();
        int r = random.nextInt(totalWeight);
        for (Quality q : candidates) {
            r -= q.getWeight();
            if (r < 0) {
                stack.getOrCreateTag().putInt("tool_quality", q.getId());
                return;
            }
        }
    }
    
    public static Map<Integer, Quality> getQualities() {
        return QUALITIES;
    }
}


