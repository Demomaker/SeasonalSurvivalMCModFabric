package net.demomaker.seasonalsurvival;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeasonalSurvivalClient implements ClientModInitializer, ClientPlayConnectionEvents.Join, ClientPlayConnectionEvents.Disconnect {
	public static final String MOD_ID = "seasonalsurvival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static boolean isWinter = false;

	public static boolean isWinter() {
		return isWinter;
	}

	public static void setIsWinter(boolean isWinter) {
		SeasonalSurvivalClient.isWinter = isWinter;
	}

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
		ClientPlayNetworking.registerGlobalReceiver(AlertWinterStartServerPayload.ID, (payload, context) -> {
			setIsWinter(true);
		});
		ClientPlayNetworking.registerGlobalReceiver(AlertWinterEndServerPayload.ID, (payload, context) -> {
			setIsWinter(false);
		});
		ClientPlayConnectionEvents.JOIN.register(this);
		ClientPlayConnectionEvents.DISCONNECT.register(this);
	}

	@Override
	public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
	}

	@Override
	public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
		setIsWinter(false);
	}
}