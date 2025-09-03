package com.kettle.qualityequipment;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = QualityEquipmentForked.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QualityEquipmentConfig
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final HashMap<ResourceLocation, ResourceLocation> ITEM_OVERRIDES = new HashMap<>();
    public static final HashMap<TagKey<Item>, ResourceLocation> TAG_OVERRIDES = new HashMap<>();

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_OVERRIDE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLACK_LIST;
    
    static final ForgeConfigSpec SPEC;

    public static Set<Item> black_list;
    public static boolean isCurioLoaded;
    public static boolean isBountifulBaublesLoaded;
    public static boolean isFirstAidLoaded;
    public static boolean isDamageLibraryLoaded;
    public static boolean isToolbeltLoaded;
    
    static {
    	List<String> defaultOverrides = new ArrayList<>(List.of("minecraft:bow|minecraft:string"));
    	if (ModList.get().isLoaded("bountifulbaubles")) {
    		defaultOverrides.add(
                    "#bountifulbaubles:baubles|bountifulbaubles:spectral_silt"
            );
        }
    	if (ModList.get().isLoaded("trinketsandbaubles")) {
    		defaultOverrides.add(
                    "#trinketsandbaubles:baubles|trinketsandbaubles:glowing_ingot"
            );
        }
    	if (ModList.get().isLoaded("toolbelt")) {
    		defaultOverrides.add(
                    "toolbelt:belt|minecraft:leather"
            );
        }
    	BLACK_LIST = BUILDER
        	    .comment("List of items that cannot get any kind of qualities, even if they match tags or datapacks.")
        	    .defineListAllowEmpty("Black list",
        	    		new ArrayList<>(List.of()), // default
        	            obj -> obj instanceof String str && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(str)));
    	ITEM_OVERRIDE = BUILDER
        	    .comment("Overrides reforging requirements. Format: item_or_tag|material_item. Ex: #minecraft:swords|minecraft_diamond")
        	    .defineListAllowEmpty("Override",
        	    		defaultOverrides, // default
        	            obj -> obj instanceof String str && str.contains("|"));
        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ITEM_OVERRIDES.clear();
        TAG_OVERRIDES.clear();

        for (String entry : ITEM_OVERRIDE.get()) {
            String[] split = entry.split("\\|");
            if (split.length != 2) {
                QualityEquipmentForked.LOGGER.warn("Invalid override entry: {}", entry);
                continue;
            }

            String keyStr = split[0].trim();
            ResourceLocation material = new ResourceLocation(split[1].trim());

            if (keyStr.startsWith("#")) {
                // it's a tag
                ResourceLocation tagLoc = new ResourceLocation(keyStr.substring(1));
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagLoc);
                TAG_OVERRIDES.put(tag, material);
            } else {
                // it's a normal item
                ResourceLocation itemLoc = new ResourceLocation(keyStr);
                ITEM_OVERRIDES.put(itemLoc, material);
            }
        }
        
        black_list = BLACK_LIST.get().stream()
                .map(id -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        isFirstAidLoaded = ModList.get().isLoaded("firstaid");
        isDamageLibraryLoaded = ModList.get().isLoaded("pml");
        isToolbeltLoaded = ModList.get().isLoaded("toolbelt");
        isCurioLoaded = ModList.get().isLoaded("curios");
        isBountifulBaublesLoaded = ModList.get().isLoaded("bountifulbaubles");
    }
}
