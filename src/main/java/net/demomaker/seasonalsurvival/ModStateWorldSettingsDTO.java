package net.demomaker.seasonalsurvival;

public class ModStateWorldSettingsDTO {
  public boolean isSeasonal;
  public boolean isWinter;
  public long lastSeasonToggleTime;

  public ModStateWorldSettingsDTO(boolean isSeasonal, boolean isWinter, long lastSeasonToggleTime) {
    this.isSeasonal = isSeasonal;
    this.isWinter = isWinter;
    this.lastSeasonToggleTime = lastSeasonToggleTime;
  }
}
