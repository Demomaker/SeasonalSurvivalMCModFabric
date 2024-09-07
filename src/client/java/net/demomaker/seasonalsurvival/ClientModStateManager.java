package net.demomaker.seasonalsurvival;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ClientModStateManager {

  private static final Gson gson = new Gson();
  private static final File MOD_STATE_FILE = new File("seasonalsurvival_mod_state.json");

  public static void saveWorldSettings(String worldName, SeasonalSurvivalWorldSettings worldSettings) {
    JsonObject json = new JsonObject();
    // Add your mod state data to the JSON object
    JsonObject worldSettingsObject = new JsonObject();
    worldSettingsObject.addProperty("useSeasonalSurvival", worldSettings.useSeasonalSurvival);
    worldSettingsObject.addProperty("isWinter", "false");
    json.add("world-" + worldName, worldSettingsObject);

    try (FileWriter writer = new FileWriter(MOD_STATE_FILE)) {
      gson.toJson(json, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void init() {
    //do nothing
  }
}
