package net.demomaker.seasonalsurvival;

import java.util.HashMap;
import java.util.Map;

public class ServerWorldSettingResolver {
  private static String mostRecentWorldName = "";
  private static final Map<String, ServerWorldSettings> serverWorldSettingsMap = new HashMap<>();

  private static ServerWorldSettings getServerWorldSettings() {
    if(!serverWorldSettingsMap.containsKey(mostRecentWorldName)) {
      return ServerWorldSettings.NOT_SEASONAL();
    }

    return serverWorldSettingsMap.get(mostRecentWorldName);
  }

  private static ServerWorldSettingsOperation putServerWorldSettings(ServerWorldSettings serverWorldSettings) {
    boolean wasAbsent = !serverWorldSettingsMap.containsKey(mostRecentWorldName);

    serverWorldSettingsMap.put(mostRecentWorldName, serverWorldSettings);

    return wasAbsent ? ServerWorldSettingsOperation.SUCCESSFUL_WAS_ABSENT : ServerWorldSettingsOperation.COMPLETE_SUCCESS;
  }

  public static ServerWorldSettingsOperation createFrom(ModStateWorldSettingsDTO dto) {
    SeasonalSurvival.LOGGER.info("Loaded SeasonalSurvival mod save with values. isWinter: " + dto.isWinter + ", isSeasonal: " + dto.isSeasonal + ", lastSeasonToggleTime: " + dto.lastSeasonToggleTime);
    return putServerWorldSettings(ServerWorldSettings.createFrom(dto.isWinter, dto.isSeasonal, dto.lastSeasonToggleTime));
  }

  public static boolean isWinter() {
    return getServerWorldSettings().isWinter();
  }

  public static boolean isNormalWorld() {
    return !getServerWorldSettings().isPlayingSeasonal();
  }

  public static ServerWorldSettingsOperation startWinter() {
    return putServerWorldSettings(ServerWorldSettings.ACTIVE_WINTER());
  }

  public static ServerWorldSettingsOperation endWinter() {
    return putServerWorldSettings(ServerWorldSettings.INACTIVE_WINTER());
  }

  public static ServerWorldSettingsOperation setWorldName(String worldName) {
    mostRecentWorldName = worldName;
    return ServerWorldSettingsOperation.COMPLETE_SUCCESS;
  }

  public static ServerWorldSettingsOperation setLastSeasonToggleTime(long lastSeasonToggleTime) {
    ServerWorldSettings previousServerWorldSettings = getServerWorldSettings();
    previousServerWorldSettings.setLastSeasonToggleTime(lastSeasonToggleTime);
    return putServerWorldSettings(previousServerWorldSettings);
  }

  public static long getLastSeasonToggleTime() {
    return getServerWorldSettings().getLastSeasonToggleTime();
  }
}
