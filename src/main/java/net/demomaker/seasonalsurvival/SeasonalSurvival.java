package net.demomaker.seasonalsurvival;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.Items;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiling.jfr.event.WorldLoadFinishedEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.OceanRuinStructure.BiomeTemperature;
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
	public static boolean isWinter = false;
	public static boolean isPlayingSeasonalSurvival = false;
	private DamageSources damageSources;

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
		PayloadTypeRegistry.playC2S().register(MarkPlayerTranslationPayload.ID, MarkPlayerTranslationPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MarkPlayerTranslationPayload.ID, (payload, context) -> {
			ServerTextTranslator.markPlayerHasTranslation(context.player());
		});

	}

	@Override
	public void onServerStarted(MinecraftServer server) {
	}

	@Override
	public void onServerStopped(MinecraftServer server) {
	}

	@Override
	public void onWorldLoad(MinecraftServer server, ServerWorld world) {
		this.damageSources = world.getDamageSources();

		ModStateManager.loadModState(world, this);
		if(server instanceof MinecraftDedicatedServer) {
			isPlayingSeasonalSurvival = true;
		}
	}

	@Override
	public void onWorldUnload(MinecraftServer server, ServerWorld world) {
		if(!isPlayingSeasonalSurvival) {
			return;
		}
		ModStateManager.saveModState(world, this);
	}

	@Override
	public void onEndTick(ServerWorld world) {
		if(!isPlayingSeasonalSurvival) {
			return;
		}
		long worldTime = world.getTimeOfDay();

		if (worldTime % TEN_DAYS_IN_TICKS == 0) {
			toggleWinterSeason(world);
		}

		if (isWinter && worldTime % ONE_SECOND_IN_TICKS == 0) {
			damagePlayersThatAreNotWarm(world);
		}
	}

	@Override
	public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		if(isPlayingSeasonalSurvival && !isWinter) {
			handler.player.sendMessage(ServerTextTranslator.getTextFromTranslationKey("seasonalsurvival.playerJoin", handler.player));
		}
	}

	private void damagePlayersThatAreNotWarm(ServerWorld world) {
		world.getPlayers().forEach(player -> {
			if(player.isAlive() && !player.isCreative() && !player.isSpectator() && !isPlayerSheltered(world, player)) {
				// Calculate the number of leather armor pieces
				int leatherArmorCount = 0;

				for (var armorItem : player.getArmorItems()) {
					if (armorItem.getItem() == Items.LEATHER_CHESTPLATE) leatherArmorCount++;
					if (armorItem.getItem() == Items.LEATHER_LEGGINGS) leatherArmorCount++;
					if (armorItem.getItem() == Items.LEATHER_BOOTS) leatherArmorCount++;
					if (armorItem.getItem() == Items.LEATHER_HELMET) leatherArmorCount++;
				}

				float damage = 1.0F - (leatherArmorCount * 0.75F);
				if (damage < 0) damage = 0;
				if (damage > 0) player.sendMessage(ServerTextTranslator.getTextFromTranslationKey("seasonalsurvival.coldPlayer", player));
				player.damage(damageSources.freeze(), damage);
			}
		});
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
		isWinter = !isWinter;
		if(isWinter) {
			startSnowing(world);
			messagePlayers(world, "seasonalsurvival.winterStart");
		}
		else {
			stopSnowing(world);
			messagePlayers(world, "seasonalsurvival.winterEnd");
		}
	}

	private void startSnowing(ServerWorld world) {
		world.setWeather(0, SNOW_TIME, true, true);
	}

	private void stopSnowing(ServerWorld world) {
		world.setWeather(0, 0, false, false);
	}
}