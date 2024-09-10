package net.demomaker.seasonalsurvival;

public class ServerWorldSettings {
  private final MOD_STATE worldModState;
  private long lastSeasonToggleTime = -1;

  private ServerWorldSettings(MOD_STATE worldModState) {
    this.worldModState = worldModState;
  }

  public static ServerWorldSettings NOT_SEASONAL() {
    return new ServerWorldSettings(MOD_STATE.NOT_SEASONAL);
  }

  public static ServerWorldSettings ACTIVE_WINTER() {
    return new ServerWorldSettings(MOD_STATE.ACTIVE_WINTER);
  }

  public static ServerWorldSettings INACTIVE_WINTER() {
    return new ServerWorldSettings(MOD_STATE.INACTIVE_WINTER);
  }

  public static ServerWorldSettings createFrom(boolean isWinter, boolean isSeasonal, long lastSeasonToggleTime) {
    MOD_STATE worldModState = MOD_STATE.NOT_SEASONAL;
    if(isSeasonal) {
      worldModState = MOD_STATE.INACTIVE_WINTER;
      if(isWinter) {
        worldModState = MOD_STATE.ACTIVE_WINTER;
      }
    }

    ServerWorldSettings serverWorldSettings = new ServerWorldSettings(worldModState);
    serverWorldSettings.setLastSeasonToggleTime(lastSeasonToggleTime);
    return serverWorldSettings;
  }

  public boolean isWinter() {
    return worldModState == MOD_STATE.ACTIVE_WINTER;
  }

  public boolean isPlayingSeasonal() {
    return worldModState != MOD_STATE.NOT_SEASONAL;
  }

  public long getLastSeasonToggleTime() {
    return this.lastSeasonToggleTime;
  }

  public void setLastSeasonToggleTime(long lastSeasonToggleTime) {
    this.lastSeasonToggleTime = lastSeasonToggleTime;
  }
}

enum MOD_STATE {
  NOT_SEASONAL,
  INACTIVE_WINTER,
  ACTIVE_WINTER,
}
