package net.demomaker.seasonalsurvival;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerTextTranslator {
  private static Set<UUID> playersWithTranslations = new HashSet<>();

  public static void markPlayerHasTranslation(ServerPlayerEntity player) {
    playersWithTranslations.add(player.getUuid());
  }
  public static Map<String, String> serverTranslations = Map.of(
      "seasonalsurvival.checkbox", "Seasonal",
      "seasonalsurvival.winterStart", "The biting chill creeps in... Winter has begun!",
      "seasonalsurvival.winterEnd","The ice melts and warmth returns. The harsh winter is over.",
      "seasonalsurvival.winterNoSleep","The cold is too harsh to rest! Sleeping is impossible during winter's icy grip—stay warm or face the frost!",
      "seasonalsurvival.coldPlayer", "The cold gnaws at you... Find warmth before it claims you!",
      "seasonalsurvival.playerJoin", "Welcome to Seasonal Survival! Every 10 days, the world flips between normal and winter modes. Keep your wits about you and stay warm when the cold sets in!"
    );

  public static Text getTextFromTranslationKey(String key, ServerPlayerEntity player) {
    Text clientText = Text.translatable(key);
    if(playersWithTranslations.contains(player.getUuid())) {
      return clientText;
    }

    if(serverTranslations.containsKey(key)) {
      return Text.of(serverTranslations.get(key));
    }

    return Text.of(key);
  }
}
