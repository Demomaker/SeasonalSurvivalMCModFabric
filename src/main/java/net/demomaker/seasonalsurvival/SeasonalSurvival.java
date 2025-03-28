package net.demomaker.seasonalsurvival;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeasonalSurvival implements ModInitializer, ServerStarted, ServerStopped, ServerTickEvents.EndWorldTick, ServerWorldEvents.Load, ServerWorldEvents.Unload, ServerPlayConnectionEvents.Join {
	public static final String MOD_ID = "seasonalsurvival";
	private static final long TEN_DAYS_IN_TICKS = 240000L;
	private static final int SNOW_TIME = 240000;
	private static final long ONE_SECOND_IN_TICKS = 20;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static DamageSources damageSources;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerLifecycleEvents.SERVER_STARTED.register(this);
		ServerLifecycleEvents.SERVER_STOPPED.register(this);
		ServerTickEvents.END_WORLD_TICK.register(this);
		ServerWorldEvents.LOAD.register(this);
		ServerWorldEvents.UNLOAD.register(this);
		ServerPlayConnectionEvents.JOIN.register(this);
		PayloadTypeRegistry.playS2C().register(AlertWinterStartServerPayload.ID, AlertWinterStartServerPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(AlertWinterEndServerPayload.ID, AlertWinterEndServerPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MarkPlayerTranslationPayload.ID, MarkPlayerTranslationPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MarkPlayerTranslationPayload.ID, (payload, context) -> {
			ServerTextTranslator.markPlayerHasTranslation(context.player());
		});
	}

	@Override
	public void onServerStarted(MinecraftServer server) {
		ModServerObjects.server = server;
	}

	@Override
	public void onServerStopped(MinecraftServer server) {
		ModServerObjects.server = server;
	}

	@Override
	public void onWorldLoad(MinecraftServer server, ServerWorld world) {
		ModServerObjects.server = server;
		damageSources = world.getDamageSources();

		try {
			ServerWorldSettingResolver.setWorldName(ServerUtil.getWorldName(server));
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		ModStateManager.loadModState();
	}

	@Override
	public void onWorldUnload(MinecraftServer server, ServerWorld world) {
		ModServerObjects.server = server;
		try {
			ModStateManager.saveModState();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onEndTick(ServerWorld world) {
		if(ServerWorldSettingResolver.isNormalWorld()) {
			return;
		}
		long worldTime = world.getTimeOfDay();

		if(ServerWorldSettingResolver.getLastSeasonToggleTime() == -1) {
			ServerWorldSettingResolver.setLastSeasonToggleTime(worldTime);
		}

		if (worldTime >= ServerWorldSettingResolver.getLastSeasonToggleTime() + TEN_DAYS_IN_TICKS) {
			toggleWinterSeason(world);
			ServerWorldSettingResolver.setLastSeasonToggleTime(worldTime);
		}

		if (ServerWorldSettingResolver.isWinter() && worldTime % ONE_SECOND_IN_TICKS == 0) {
			damagePlayersThatAreNotWarm(world);
		}
	}

	@Override
	public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		if(ServerWorldSettingResolver.isNormalWorld()) {
			return;
		}

		if(!ServerWorldSettingResolver.isWinter()) {
			handler.player.sendMessage(ServerTextTranslator.getTextFromTranslationKey("seasonalsurvival.playerJoin", handler.player));
		}


		if(ServerWorldSettingResolver.isWinter()) {
			AlertWinterStartServerPayload alertWinterStartServerPayload = new AlertWinterStartServerPayload("");
			ServerPlayNetworking.send(handler.player, alertWinterStartServerPayload);
		}
		else {
			AlertWinterEndServerPayload alertWinterEndServerPayload = new AlertWinterEndServerPayload("");
			ServerPlayNetworking.send(handler.player, alertWinterEndServerPayload);
		}
	}

	private void damagePlayersThatAreNotWarm(ServerWorld world) {
		world.getPlayers().forEach(player -> {
			if(player.isAlive() && isInOveworld(player) && !player.isCreative() && !player.isSpectator() && !isPlayerSheltered(world, player)) {
				// Calculate the number of leather armor pieces
				int leatherArmorCount = getLeatherArmourCount(player);

				float damage = 1.0F - (leatherArmorCount * 0.75F);
				if (damage < 0) damage = 0;
				if (damage > 0) player.sendMessage(ServerTextTranslator.getTextFromTranslationKey("seasonalsurvival.coldPlayer", player));
				player.damage(world, damageSources.freeze(), damage);
			}
		});
	}

	private int getLeatherArmourCount(ServerPlayerEntity player) {
		int leatherArmorCount = 0;
		for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
			if(equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				ItemStack armorItem = player.getEquippedStack(equipmentSlot);

				if (armorItem.getItem() == Items.LEATHER_CHESTPLATE) leatherArmorCount++;
				if (armorItem.getItem() == Items.LEATHER_LEGGINGS) leatherArmorCount++;
				if (armorItem.getItem() == Items.LEATHER_BOOTS) leatherArmorCount++;
				if (armorItem.getItem() == Items.LEATHER_HELMET) leatherArmorCount++;
			}
		}

		return leatherArmorCount;
	}

	private boolean isInOveworld(ServerPlayerEntity player) {
		return player.getWorld().getRegistryKey() == World.OVERWORLD;
	}

	private void messagePlayers(ServerWorld world, String key) {
		world.getPlayers().forEach(player -> {
			player.sendMessage(ServerTextTranslator.getTextFromTranslationKey(key, player));
		});
	}

	private boolean isPlayerSheltered(ServerWorld world, ServerPlayerEntity player) {
		return SeasonalSurvivalHelper.isPlayerSheltered(world, player);
	}

	private void toggleWinterSeason(ServerWorld world) {
		if(!ServerWorldSettingResolver.isWinter()) {
			ServerWorldSettingResolver.startWinter();
			startSnowing(world);
			messagePlayers(world, "seasonalsurvival.winterStart");
		}
		else {
			ServerWorldSettingResolver.endWinter();
			stopSnowing(world);
			messagePlayers(world, "seasonalsurvival.winterEnd");
		}
	}

	private void startSnowing(ServerWorld world) {
		AlertWinterStartServerPayload alertWinterStartServerPayload = new AlertWinterStartServerPayload("");
		ModServerObjects.server.getPlayerManager().getPlayerList().forEach(player -> ServerPlayNetworking.send(player, alertWinterStartServerPayload));
		world.setWeather(0, SNOW_TIME, true, true);
	}

	private void stopSnowing(ServerWorld world) {
		AlertWinterEndServerPayload alertWinterEndServerPayload = new AlertWinterEndServerPayload("");
		ModServerObjects.server.getPlayerManager().getPlayerList().forEach(player -> ServerPlayNetworking.send(player, alertWinterEndServerPayload));
		world.setWeather(0, 0, false, false);
	}
}