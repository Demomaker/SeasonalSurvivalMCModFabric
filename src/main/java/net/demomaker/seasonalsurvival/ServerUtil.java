package net.demomaker.seasonalsurvival;

import net.minecraft.server.MinecraftServer;

public class ServerUtil {
  public static String getWorldName(MinecraftServer server) throws Exception {
    if(server == null) {
      throw new Exception("No server");
    }

    return server.getSaveProperties().getLevelName();
  }

  public static String getWorldIdentifier(MinecraftServer server) throws Exception {
    return "world-" + getWorldName(server);
  }

}
