package net.demomaker.seasonalsurvival;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record AlertWinterStartServerPayload(String s) implements CustomPayload {

    public static final Identifier ALERT_WINTER_START_ID = Identifier.of("alert_winter_start");
    public static final CustomPayload.Id<AlertWinterStartServerPayload> ID = new CustomPayload.Id<>(ALERT_WINTER_START_ID);
    public static final PacketCodec<RegistryByteBuf, AlertWinterStartServerPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            AlertWinterStartServerPayload::s,
            AlertWinterStartServerPayload::new
    );
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}