package net.demomaker.seasonalsurvival;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AlertWinterEndServerPayload(String s) implements CustomPayload {

    public static final Identifier ALERT_WINTER_ENDED_ID = Identifier.of("alert_winter_ended");
    public static final CustomPayload.Id<AlertWinterEndServerPayload> ID = new CustomPayload.Id<>(ALERT_WINTER_ENDED_ID);
    public static final PacketCodec<RegistryByteBuf, AlertWinterEndServerPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            AlertWinterEndServerPayload::s,
            AlertWinterEndServerPayload::new
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}