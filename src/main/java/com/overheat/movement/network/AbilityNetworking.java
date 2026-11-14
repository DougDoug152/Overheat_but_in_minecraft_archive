package com.overheat.movement.network;

import java.util.ArrayList;
import java.util.List;
import com.overheat.movement.ability.ActivatorData;
import com.overheat.movement.ability.ActivatorEntry;
import com.overheat.movement.ability.StepListener;
import com.overheat.movement.ability.WallRunFilterData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class AbilityNetworking {

	private AbilityNetworking() {
	}

	public static void registerCommon() {
		PayloadTypeRegistry.playS2C().register(OpenActivatorConfigPayload.TYPE, OpenActivatorConfigPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SaveActivatorConfigPayload.TYPE, SaveActivatorConfigPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SaveActivatorConfigPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();
			if (server != null) {
				server.execute(() -> handleSave(player, payload));
			}
		});

		PayloadTypeRegistry.playC2S().register(DeleteActivatorPayload.TYPE, DeleteActivatorPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DeleteActivatorPayload.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			MinecraftServer server = player.getServer();
			if (server != null) {
				server.execute(() -> handleDelete(player, payload));
			}
		});

		PayloadTypeRegistry.playS2C().register(WallRunFilterSyncPayload.TYPE, WallRunFilterSyncPayload.CODEC);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendFilter(handler.player));
	}

	public static void sendFilter(ServerPlayer player) {
		ServerPlayNetworking.send(player, filterPayload(player.serverLevel()));
	}

	public static void broadcastFilter(ServerLevel level) {
		WallRunFilterSyncPayload payload = filterPayload(level);
		for (ServerPlayer player : level.players()) {
			ServerPlayNetworking.send(player, payload);
		}
	}

	private static WallRunFilterSyncPayload filterPayload(ServerLevel level) {
		WallRunFilterData data = WallRunFilterData.get(level);
		return new WallRunFilterSyncPayload(new ArrayList<>(data.allowed()), new ArrayList<>(data.denied()));
	}

	private static void handleDelete(ServerPlayer player, DeleteActivatorPayload payload) {
		if (!player.isCreative()) {
			return;
		}
		ServerLevel level = player.serverLevel();
		BlockPos pos = payload.pos();
		if (!level.isLoaded(pos) || player.blockPosition().distSqr(pos) > 64 * 64) {
			return;
		}
		ActivatorData data = ActivatorData.get(level);
		List<ActivatorEntry> config = data.getConfig(pos);
		if (config != null) {
			StepListener.revokeGranted(player, level, pos, config);
		}
		data.remove(pos);
	}

	private static void handleSave(ServerPlayer player, SaveActivatorConfigPayload payload) {
		if (!player.isCreative()) {
			return;
		}
		ServerLevel level = player.serverLevel();
		BlockPos pos = payload.pos();
		if (!level.isLoaded(pos) || player.blockPosition().distSqr(pos) > 64 * 64) {
			return;
		}
		ActivatorData data = ActivatorData.get(level);
		boolean giver = StepListener.isIntrinsic(level.getBlockState(pos));
		if (!giver && !data.isActivator(pos)) {
			return;
		}
		data.setConfig(pos, payload.entries());
	}
}
