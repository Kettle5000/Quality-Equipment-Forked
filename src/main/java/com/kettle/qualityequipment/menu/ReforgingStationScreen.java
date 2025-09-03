package com.kettle.qualityequipment.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import com.kettle.qualityequipment.network.NetworkManager;
import com.kettle.qualityequipment.network.ReforgeButtonPressedPacket;
import com.mojang.blaze3d.systems.RenderSystem;

@OnlyIn(Dist.CLIENT)
public class ReforgingStationScreen extends AbstractContainerScreen<ReforgingStationMenu> {
	private DynamicImageButton reforgeButton;
	
    private static final ResourceLocation TEXTURE = new ResourceLocation("quality_forked:textures/screens/reforging_station_gui.png");

    public ReforgingStationScreen(ReforgingStationMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.leftPos + 88;
        int centerY = this.topPos + 83;

        this.reforgeButton = new DynamicImageButton(
                centerX - 8, // offset half button width (20/2 = 10)
                centerY - 44, // offset half button height
                16, 16,       // button size
                0, 0,         // texture offset (u, v) inside the icon file
                0,           // "hovered" texture offset Y
                new ResourceLocation("quality_forked:textures/screens/successhammer.png"),
                16, 16,       // texture width/height (20x40, because 20 normal + 20 hover stacked vertically)
                btn -> {
                    if (this.menu.isReadyToReforge()) {
                        NetworkManager.sendToServer(new ReforgeButtonPressedPacket(this.menu.containerId));
                        this.minecraft.level.playSound(this.minecraft.player, this.minecraft.player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.AMBIENT);
                    }
                }
        );
        this.addRenderableWidget(this.reforgeButton);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        reforgeButton.active = this.menu.isReadyToReforge(); // update enabled state
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Slot slot0 = this.menu.getSlot(0);
        Slot slot1 = this.menu.getSlot(1);
        //gfx.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blit(new ResourceLocation("quality_forked:textures/screens/reforging_station_gui.png"), this.leftPos, this.topPos, 0.0F, 0.0F, 176, 166, 176, 166);
        guiGraphics.blit(new ResourceLocation("quality_forked:textures/screens/ingot_img.png"), this.leftPos + slot1.x, this.topPos + slot1.y, 0.0F, 0.0F, 16, 16, 16, 16);
        guiGraphics.blit(new ResourceLocation("quality_forked:textures/screens/swordgui.png"), this.leftPos + slot0.x, this.topPos + slot0.y, 0.0F, 0.0F, 16, 16, 16, 16);
        RenderSystem.disableBlend();
     }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }
}
