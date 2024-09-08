package net.demomaker.seasonalsurvival;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class ModStateManager {

  private static final Gson gson = new Gson();
  private static final File MOD_STATE_FILE = new File("seasonalsurvival_mod_state.json");

  public static void saveModState(World world, SeasonalSurvival seasonalSurvival) {
    JsonObject worldSettingsObject = new JsonObject();
    JsonObject json = new JsonObject();
    MinecraftServer server = world.getServer();
    if(server == null) {
      return;
    }

    String worldIdentifier = "world-" + server.getSaveProperties().getLevelName();

    try (FileReader reader = new FileReader(MOD_STATE_FILE)) {
      json = gson.fromJson(reader, JsonObject.class);
      if(json.has(worldIdentifier)) {
        worldSettingsObject = json.getAsJsonObject(worldIdentifier);
      }
      // Restore your mod state from the JSON object
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (json.has(worldIdentifier)) {
      json.remove(worldIdentifier);
      worldSettingsObject.remove("isWinter");
      if(worldSettingsObject.has("lastSeasonToggleTime")) {
        worldSettingsObject.remove("lastSeasonToggleTime");
      }
    }
    else {
      worldSettingsObject.addProperty("useSeasonalSurvival", true);
    }

    worldSettingsObject.addProperty("isWinter", SeasonalSurvival.isIsWinter());
    worldSettingsObject.addProperty("lastSeasonToggleTime", SeasonalSurvival.lastSeasonToggleTime);
    json.add(worldIdentifier, worldSettingsObject);


    try (FileWriter writer = new FileWriter(MOD_STATE_FILE)) {
      gson.toJson(json, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void loadModState(World world, SeasonalSurvival seasonalSurvival) {
    if (!MOD_STATE_FILE.exists()) {
      return; // No saved state to load
    }

    try (FileReader reader = new FileReader(MOD_STATE_FILE)) {
      JsonObject json = gson.fromJson(reader, JsonObject.class);
      MinecraftServer server = world.getServer();
      if(server == null) {
        return;
      }

      String worldIdentifier = "world-" + server.getSaveProperties().getLevelName();
      if(json.has(worldIdentifier)) {
        JsonObject worldSettingsObject = json.getAsJsonObject(worldIdentifier);
        SeasonalSurvival.setIsWinter(worldSettingsObject.get("isWinter").getAsBoolean());
        SeasonalSurvival.isPlayingSeasonalSurvival = worldSettingsObject.get("useSeasonalSurvival").getAsBoolean();
        if(worldSettingsObject.has("lastSeasonToggleTime")) {
          SeasonalSurvival.lastSeasonToggleTime = worldSettingsObject.get("lastSeasonToggleTime").getAsLong();
        }
        else {
          SeasonalSurvival.lastSeasonToggleTime = 0;
        }
      }
      SeasonalSurvival.LOGGER.info("Loaded seasonalSurvival settings for " + worldIdentifier + ", useSeasonalSurvival : " +
          SeasonalSurvival.isPlayingSeasonalSurvival +
          ", isWinter : " + SeasonalSurvival.isIsWinter());
      // Restore your mod state from the JSON object
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
