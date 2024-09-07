package net.demomaker.seasonalsurvival;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;

public class ClientTextChecker {

  public static void checkAndNotifyServer(String... keys) {
    boolean hasAllTranslations = true;
    for (String key : keys) {
      Text clientText = Text.translatable(key);
      if (clientText.getString().equals(key)) {
        hasAllTranslations = false;
        break;
      }
    }

    if (hasAllTranslations) {
      ClientPlayNetworking.send(new MarkPlayerTranslationPayload(""));
    }
  }
}
