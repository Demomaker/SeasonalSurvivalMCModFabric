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
    }
    else {
      worldSettingsObject.addProperty("useSeasonalSurvival", true);
    }

    worldSettingsObject.addProperty("isWinter", seasonalSurvival.isWinter);
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
        seasonalSurvival.isWinter = worldSettingsObject.get("isWinter").getAsBoolean();
        seasonalSurvival.isPlayingSeasonalSurvival = worldSettingsObject.get("useSeasonalSurvival").getAsBoolean();
      }
      SeasonalSurvival.LOGGER.info(worldIdentifier + ", " + seasonalSurvival.isPlayingSeasonalSurvival);
      // Restore your mod state from the JSON object
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
