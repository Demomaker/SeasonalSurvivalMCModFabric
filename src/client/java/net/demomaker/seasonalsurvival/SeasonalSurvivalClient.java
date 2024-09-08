package net.demomaker.seasonalsurvival;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeasonalSurvivalClient implements ClientModInitializer, ClientPlayConnectionEvents.Join {
	public static final String MOD_ID = "seasonalsurvival";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static boolean isWinter = false;

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
			isWinter = true;
			context.player().sendMessage(Text.of("isWinter: " + isWinter));
		});
		ClientPlayNetworking.registerGlobalReceiver(AlertWinterEndServerPayload.ID, (payload, context) -> {
			isWinter = false;
			context.player().sendMessage(Text.of("isWinter: " + isWinter));
		});
		ClientPlayConnectionEvents.JOIN.register(this);
	}

	@Override
	public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
	}
}