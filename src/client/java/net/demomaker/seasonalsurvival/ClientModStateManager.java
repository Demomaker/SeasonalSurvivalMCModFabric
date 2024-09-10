package net.demomaker.seasonalsurvival;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClientModStateManager {

  private static final Gson gson = new Gson();
  private static final File MOD_STATE_FILE = new File("seasonalsurvival_mod_state.json");

  public static void saveWorldSettings(String worldName, SeasonalSurvivalWorldSettings worldSettings) {
    JsonObject json = getExistingModStateFileJsonObject();
    JsonObject worldSettingsObject = new JsonObject();
    worldSettingsObject.addProperty("useSeasonalSurvival", worldSettings.useSeasonalSurvival);
    worldSettingsObject.addProperty("isWinter", false);
    json.add("world-" + worldName, worldSettingsObject);

    try (FileWriter writer = new FileWriter(MOD_STATE_FILE)) {
      gson.toJson(json, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void deleteWorldByName(String directoryName) {
    JsonObject json = getExistingModStateFileJsonObject();
    String worldIdentifier = "world-" + directoryName;

    if (json.has(worldIdentifier)) {
      json.remove(worldIdentifier);
    }

    try (FileWriter writer = new FileWriter(MOD_STATE_FILE)) {
      gson.toJson(json, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void init() {
    //do nothing
  }

  private static JsonObject getExistingModStateFileJsonObject() {
    try (FileReader reader = new FileReader(MOD_STATE_FILE)) {
      return gson.fromJson(reader, JsonObject.class);
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return new JsonObject();
  }
}
