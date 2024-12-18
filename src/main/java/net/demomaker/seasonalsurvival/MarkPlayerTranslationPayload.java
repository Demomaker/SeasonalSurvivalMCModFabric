package net.demomaker.seasonalsurvival;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record MarkPlayerTranslationPayload(String s) implements CustomPayload {

  public static final Identifier MARK_PLAYER_HAS_TRANSLATIONS_ID = Identifier.of("mark_player_has_translations");
  public static final Id<MarkPlayerTranslationPayload> ID = new Id<>(MARK_PLAYER_HAS_TRANSLATIONS_ID);
  public static final PacketCodec<RegistryByteBuf, MarkPlayerTranslationPayload> CODEC = PacketCodec.tuple(
          PacketCodecs.STRING,
          MarkPlayerTranslationPayload::s,
          MarkPlayerTranslationPayload::new
  );
  @Override
  public Id<? extends CustomPayload> getId() {
    return ID;
  }
}
