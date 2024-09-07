package net.demomaker.seasonalsurvival;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeasonalSurvivalClient implements ClientModInitializer {
	public static final String MOD_ID = "seasonalsurvival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ClientTextChecker.checkAndNotifyServer(
				"seasonalsurvival.checkbox",
				"seasonalsurvival.winterStart",
				"seasonalsurvival.winterEnd",
				"seasonalsurvival.winterNoSleep",
				"seasonalsurvival.coldPlayer",
				"seasonalsurvival.playerJoin"
		);
		ClientModStateManager.init();
	}
}