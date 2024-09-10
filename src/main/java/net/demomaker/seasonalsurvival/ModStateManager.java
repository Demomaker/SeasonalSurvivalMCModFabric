package net.demomaker.seasonalsurvival;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

public class ModStateManager {

  private static final Gson gson = new Gson();
  private static final File MOD_STATE_FILE = new File("seasonalsurvival_mod_state.json");

  public static void saveModState() throws Exception {
    JsonObject worldSettingsObject = new JsonObject();
    JsonObject json = getExistingModStateFileJsonObject();
    String worldIdentifier = ServerUtil.getWorldIdentifier(ModServerObjects.server);

    if (json.has(worldIdentifier)) {
      json.remove(worldIdentifier);
    }

    boolean useSeasonalSurvival = !ServerWorldSettingResolver.isNormalWorld();
    boolean isWinter = ServerWorldSettingResolver.isWinter();
    long lastSeasonToggleTime = ServerWorldSettingResolver.getLastSeasonToggleTime();
    SeasonalSurvival.LOGGER.info("Saving for worldIdentifier : " + worldIdentifier);
    SeasonalSurvival.LOGGER.info("Saved SeasonalSurvival mod world with values. isWinter: " + isWinter + ", isSeasonal: " + useSeasonalSurvival + ", lastSeasonToggleTime: " + lastSeasonToggleTime);
    worldSettingsObject.addProperty("useSeasonalSurvival", useSeasonalSurvival);
    worldSettingsObject.addProperty("isWinter", isWinter);
    worldSettingsObject.addProperty("lastSeasonToggleTime", lastSeasonToggleTime);
    json.add(worldIdentifier, worldSettingsObject);


    try (FileWriter writer = new FileWriter(MOD_STATE_FILE)) {
      SeasonalSurvival.LOGGER.info("Writing : " + json.toString());
      gson.toJson(json, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void loadModState() {
    ModStateWorldSettingsDTO modStateWorldSettingsDTO = new ModStateWorldSettingsDTO(false, false, -1);
    boolean modStateFileExists = MOD_STATE_FILE.exists();
    if (!modStateFileExists) {
      if(ModServerObjects.server instanceof MinecraftDedicatedServer) {
        modStateWorldSettingsDTO.isSeasonal = true;
      }
    }
    else {
      readModStateFile(modStateWorldSettingsDTO);
    }
    ServerWorldSettingResolver.createFrom(modStateWorldSettingsDTO);
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

  private static void readModStateFile(ModStateWorldSettingsDTO modStateWorldSettingsDTO) {
    try (FileReader reader = new FileReader(MOD_STATE_FILE)) {
      JsonObject json = gson.fromJson(reader, JsonObject.class);
      String worldIdentifier = ServerUtil.getWorldIdentifier(ModServerObjects.server);
      SeasonalSurvival.LOGGER.info("Reading : " + json.toString());
      SeasonalSurvival.LOGGER.info("Reading for worldIdentifier : " + worldIdentifier);
      if(json.has(worldIdentifier)) {
        JsonObject worldSettingsObject = json.getAsJsonObject(worldIdentifier);
        if(worldSettingsObject.has("useSeasonalSurvival")) {
          modStateWorldSettingsDTO.isSeasonal = worldSettingsObject.get("useSeasonalSurvival").getAsBoolean();
        }
        if(worldSettingsObject.has("isWinter")) {
          modStateWorldSettingsDTO.isWinter = worldSettingsObject.get("isWinter").getAsBoolean();
        }
        if(worldSettingsObject.has("lastSeasonToggleTime")) {
          modStateWorldSettingsDTO.lastSeasonToggleTime = worldSettingsObject.get("lastSeasonToggleTime").getAsLong();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      //ignored
    }
  }
}
