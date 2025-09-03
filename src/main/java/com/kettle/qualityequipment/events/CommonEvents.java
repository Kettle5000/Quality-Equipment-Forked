package com.kettle.qualityequipment.events;

import java.util.Collection;
import java.util.UUID;

import com.jinqinxixi.bountifulbaubles.item.Baubles.AnkhShieldItem;
import com.jinqinxixi.bountifulbaubles.item.Baubles.CobaltShieldItem;
import com.jinqinxixi.bountifulbaubles.item.Baubles.ObsidianShieldItem;
import com.kettle.pml.core.DamageControl;
import com.kettle.pml.events.DamageHandler;
import com.kettle.qualityequipment.QualityEquipmentConfig;
import com.kettle.qualityequipment.attributes.QualityEquipmentForkedAttributes;
import com.kettle.qualityequipment.data.Quality;
import com.kettle.qualityequipment.data.QualityLoader;

import dev.gigaherz.toolbelt.belt.ToolBeltItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class CommonEvents {
	@SubscribeEvent
	public static void addQualityModifier(ItemAttributeModifierEvent event) {
		ItemStack stack = event.getItemStack();
	    if (!stack.hasTag() || !stack.getTag().contains("quality") || QualityEquipmentConfig.black_list.contains(stack.getItem())) return;
	    boolean flag = true;
	    if (QualityEquipmentConfig.isCurioLoaded) {
	    	if (stack.getItem() instanceof ICurioItem) flag = false;
	    	if (QualityEquipmentConfig.isToolbeltLoaded) {
            	if (stack.getItem() instanceof ToolBeltItem) {
            		flag = false;
            	}
            }
	    	if (QualityEquipmentConfig.isBountifulBaublesLoaded) {
	    		if (stack.getItem() instanceof CobaltShieldItem || stack.getItem() instanceof ObsidianShieldItem || stack.getItem() instanceof AnkhShieldItem) {
	    			flag = true;
	    		}
	    	}
	    }
	    if (flag) {
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
		        if (event.getSlotType() == data.slot()) {
		        	event.addModifier(attribute, modifier);
		        }
		    }
	    }
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		if (event.phase == Phase.END && event.player.tickCount % 20 == 0 && event.side == LogicalSide.SERVER) {
			Player player = event.player;
			for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
				ItemStack stack = player.getInventory().getItem(slot);
				boolean curioflag = false;
				if (QualityEquipmentConfig.isCurioLoaded) {
					curioflag = stack.getItem() instanceof ICurioItem;
					if (QualityEquipmentConfig.isToolbeltLoaded) {
						curioflag = stack.getItem() instanceof ToolBeltItem;
		            }
				}
				if (!stack.isEmpty() && (stack.isDamageableItem() || curioflag) && !QualityEquipmentConfig.black_list.contains(stack.getItem())) {
					if (stack.hasTag() && !stack.getTag().contains("quality")) {
						stack.getOrCreateTag().putInt("quality", -1);
						QualityLoader.getRandomQuality(stack, player.getRandom());
					}
				}
			}
		}
	}
	

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onBreakSpeed(BreakSpeed event) {
	    Player entity = event.getEntity();
	    if (entity == null) return;

	    AttributeInstance digSpeedAttr = entity.getAttribute(QualityEquipmentForkedAttributes.DIG_SPEED.get());
	    if (digSpeedAttr == null) return;

	    // Start with the mining speed already calculated by vanilla
	    double speed = event.getNewSpeed();

	    // Flat additions
	    for (AttributeModifier mod : digSpeedAttr.getModifiers(AttributeModifier.Operation.ADDITION)) {
	        speed += mod.getAmount();
	    }

	    double baseSpeed = speed;

	    // Multiply base
	    for (AttributeModifier mod : digSpeedAttr.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE)) {
	        speed += baseSpeed * mod.getAmount();
	    }

	    // Multiply total
	    for (AttributeModifier mod : digSpeedAttr.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL)) {
	        speed *= (1.0D + mod.getAmount());
	    }

	    event.setNewSpeed((float) speed);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLivingJump(LivingJumpEvent event) {
	    if (event.getEntity() instanceof Player player) {
	        double jumpBoost = player.getAttributeValue(QualityEquipmentForkedAttributes.JUMP_HEIGHT.get());
	        if (jumpBoost > 1) {
	        	double bonus = (Math.sqrt((jumpBoost - 1.45f) * 0.08));
				player.setDeltaMovement(new Vec3((player.getDeltaMovement().x()), (player.getDeltaMovement().y() + bonus), (player.getDeltaMovement().z())));
				//player.hurtMarked = true;
	        }
	    }
	}
	
	@SubscribeEvent
	public static void onEntityFall(LivingFallEvent event) {
		if (!event.isCanceled() && event.getEntity() instanceof Player player) {
			float distance = event.getDistance();
			double jumpBoost = player.getAttributeValue(QualityEquipmentForkedAttributes.JUMP_HEIGHT.get());
			if (jumpBoost > 1f) {
				jumpBoost = jumpBoost - 1;
				event.setDistance(distance - (float)jumpBoost);
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onEntityJoin(EntityJoinLevelEvent event) {
		if (!event.isCanceled() && event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner() instanceof Player player) {
			double projectileBonus = player.getAttributeValue(QualityEquipmentForkedAttributes.PROJECTILE_DAMAGE.get());
			if (projectileBonus > 2f) {
				arrow.setBaseDamage(arrow.getBaseDamage() + projectileBonus - 2f);
			}
		}
	}
	
	@SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!event.isCanceled() && event.getEntity() instanceof Player player && player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get()) != null ) {
        	if (!QualityEquipmentConfig.isDamageLibraryLoaded) {
        		float damage = event.getAmount();
        		if (event.getSource().is(DamageTypes.MAGIC) && player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get()) != null) {
        			float flatmagicdmg = (float)player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                            .getModifiers(AttributeModifier.Operation.ADDITION).stream()
                            .mapToDouble(AttributeModifier::getAmount)
                            .sum();
        			
        			damage -= flatmagicdmg;
        			Collection<AttributeModifier> magicmultModifiers =
                            player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                                  .getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
                    if (!magicmultModifiers.isEmpty()) {
                    	for (AttributeModifier mod : magicmultModifiers) {
                            float reduction = (float)mod.getAmount(); // e.g., 0.20 = 20% reduction
                            damage *= (1.0 - reduction);
                        }
                    }
                    Collection<AttributeModifier> magictotalModifiers =
                            player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                                  .getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
                    if (!magictotalModifiers.isEmpty()) {
                    	for (AttributeModifier mod : magictotalModifiers) {
                            float reduction2 = (float)mod.getAmount();
                            damage *= (1.0f - reduction2);
                        }
                    }
        			
        		}
        		
                float flatFromAdditions = (float)player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                                                .getModifiers(AttributeModifier.Operation.ADDITION).stream()
                                                .mapToDouble(AttributeModifier::getAmount)
                                                .sum();
                // apply flat reduction
                damage -= flatFromAdditions;

                // --- multiplicative modifiers ---
                // Here is the scope where you can get them:
                Collection<AttributeModifier> multModifiers =
                        player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                              .getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
                if (!multModifiers.isEmpty()) {
                	for (AttributeModifier mod : multModifiers) {
                        float reduction = (float)mod.getAmount(); // e.g., 0.20 = 20% reduction
                        damage *= (1.0 - reduction);
                    }
                }
                Collection<AttributeModifier> totalModifiers =
                        player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                              .getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
                if (!totalModifiers.isEmpty()) {
                	for (AttributeModifier mod : totalModifiers) {
                        float reduction2 = (float)mod.getAmount();
                        damage *= (1.0f - reduction2);
                    }
                }
                // Finalize damage (don’t let it go negative)
                if (damage < 0) damage = 0;

                event.setAmount( damage);
        	} else {
        		DamageTakenWhenLibraryOn(player, event.getAmount(), event.getSource());
        	}
        } else if (!event.isCanceled() && event.getSource().getEntity() instanceof Player player && player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get()) != null && event.getSource().is(DamageTypes.MAGIC) ) {
        	if (!QualityEquipmentConfig.isDamageLibraryLoaded) {
        		float damage = event.getAmount();
				float flatFromAdditions = (float) player
						.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
						.getModifiers(AttributeModifier.Operation.ADDITION).stream()
						.mapToDouble(AttributeModifier::getAmount).sum();
				damage += flatFromAdditions;

				Collection<AttributeModifier> multModifiers = player
						.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
						.getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
				if (!multModifiers.isEmpty()) {
					for (AttributeModifier mod : multModifiers) {
						float reduction = (float) mod.getAmount(); // e.g., 0.20 = 20% reduction
						damage *= (1.0 + reduction);
					}
				}
				Collection<AttributeModifier> totalModifiers = player
						.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
						.getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
				if (!totalModifiers.isEmpty()) {
					for (AttributeModifier mod : totalModifiers) {
						float reduction2 = (float) mod.getAmount();
						damage *= (1.0f + reduction2);
					}
				}
				if (damage < 0)
					damage = 0;

				event.setAmount(damage);
        	} else {
        		DamageIncreasedWhenLibraryOn(player, event.getAmount(), event.getSource());
        	}
        }
    }
	
	private static void DamageIncreasedWhenLibraryOn(Player player, float amount, DamageSource source) {
		DamageControl controller = DamageHandler.registerBonus(source);
		float flatmagicdmg = (float)player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
                .getModifiers(AttributeModifier.Operation.ADDITION).stream()
                .mapToDouble(AttributeModifier::getAmount)
                .sum();
		
		controller.addBaseFlatBonus(flatmagicdmg);
		Collection<AttributeModifier> magicmultModifiers =
                player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
                      .getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
        if (!magicmultModifiers.isEmpty()) {
       		float it = 1f;
        	for (AttributeModifier mod : magicmultModifiers) {
                float reduction = (float)mod.getAmount(); // e.g., 0.20 = 20% reduction
                it *= (1.0 + reduction);
            }
        	controller.addBasePercentBonus(it);
        }
        Collection<AttributeModifier> magictotalModifiers =
                player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_DAMAGE.get())
                      .getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (!magictotalModifiers.isEmpty()) {
        	float it2 = 1f;
        	for (AttributeModifier mod : magictotalModifiers) {
                float reduction2 = (float)mod.getAmount();
                it2 *= (1.0f + reduction2);
            }
        	controller.addPercentBonus(it2);
        }
	}
	
	private static void DamageTakenWhenLibraryOn(Player player, float amount, DamageSource source) {
		// If you want flat to only apply from modifiers with Operation.ADDITION:
		DamageControl controller = DamageHandler.registerBonus(source);
        float flatFromAdditions = (float)player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                                        .getModifiers(AttributeModifier.Operation.ADDITION).stream()
                                        .mapToDouble(AttributeModifier::getAmount)
                                        .sum();
        // apply flat reduction
        controller.addBaseFlatBonus(-flatFromAdditions);

        // --- multiplicative modifiers ---
        // Here is the scope where you can get them:
        Collection<AttributeModifier> multModifiers =
                player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                      .getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
        if (!multModifiers.isEmpty()) {
        	float iterator = 1f;
            for (AttributeModifier mod : multModifiers) {
                float reduction = (float)mod.getAmount(); // e.g., 0.20 = 20% reduction
                iterator *= (1.0f - reduction);
            }
            controller.addBasePercentBonus(-(1f - iterator));
        }
        Collection<AttributeModifier> totalModifiers =
                player.getAttribute(QualityEquipmentForkedAttributes.DAMAGE_RESISTANCE.get())
                      .getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (!totalModifiers.isEmpty()) {
        	float iterator2 = 1f;
            for (AttributeModifier mod : totalModifiers) {
                float reduction2 = (float)mod.getAmount();
                iterator2 *= (1.0 - reduction2);
            }
            controller.addPercentBonus(-(1f - iterator2));
        }
        
        if (source.is(DamageTypes.MAGIC) && player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get()) != null) {
			float flatmagicdmg = (float)player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                    .getModifiers(AttributeModifier.Operation.ADDITION).stream()
                    .mapToDouble(AttributeModifier::getAmount)
                    .sum();
			
			controller.addBaseFlatBonus(-flatmagicdmg);
			Collection<AttributeModifier> magicmultModifiers =
                    player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                          .getModifiers(AttributeModifier.Operation.MULTIPLY_BASE);
            if (!magicmultModifiers.isEmpty()) {
           		float it = 1f;
            	for (AttributeModifier mod : magicmultModifiers) {
                    float reduction = (float)mod.getAmount(); // e.g., 0.20 = 20% reduction
                    it *= (1.0 - reduction);
                }
            	controller.addBasePercentBonus(-(1f - it));
            }
            Collection<AttributeModifier> magictotalModifiers =
                    player.getAttribute(QualityEquipmentForkedAttributes.MAGIC_RESISTANCE.get())
                          .getModifiers(AttributeModifier.Operation.MULTIPLY_TOTAL);
            if (!magictotalModifiers.isEmpty()) {
            	float it2 = 1f;
            	for (AttributeModifier mod : magictotalModifiers) {
                    float reduction2 = (float)mod.getAmount();
                    it2 *= (1.0f - reduction2);
                }
            	controller.addPercentBonus(-(1f - it2));
            }
			
		}
	}
}
