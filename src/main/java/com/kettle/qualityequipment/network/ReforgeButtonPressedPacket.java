package com.kettle.qualityequipment.network;

import java.util.function.Supplier;

import com.kettle.qualityequipment.menu.ReforgingStationMenu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ReforgeButtonPressedPacket {
    private final int containerId;

    public ReforgeButtonPressedPacket(int containerId) {
        this.containerId = containerId;
    }

    public static void encode(ReforgeButtonPressedPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.containerId);
    }

    public static ReforgeButtonPressedPacket decode(FriendlyByteBuf buf) {
        return new ReforgeButtonPressedPacket(buf.readInt());
    }

    public static void handle(ReforgeButtonPressedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.containerMenu.containerId == msg.containerId) {
                if (player.containerMenu instanceof ReforgingStationMenu menu) {
                    menu.doReforge(player);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
