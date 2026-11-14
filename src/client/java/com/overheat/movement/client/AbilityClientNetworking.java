package com.overheat.movement.client;

import com.overheat.movement.client.movement.WallRunFilter;
import com.overheat.movement.client.screen.ActivatorConfigScreen;
import com.overheat.movement.network.OpenActivatorConfigPayload;
import com.overheat.movement.network.WallRunFilterSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class AbilityClientNetworking {

	private AbilityClientNetworking() {
	}

	public static void register() {
		ClientPlayNetworking.registerGlobalReceiver(OpenActivatorConfigPayload.TYPE, (payload, context) ->
				context.client().execute(() ->
						context.client().setScreen(new ActivatorConfigScreen(payload.pos(), payload.entries()))));

		ClientPlayNetworking.registerGlobalReceiver(WallRunFilterSyncPayload.TYPE, (payload, context) ->
				context.client().execute(() -> WallRunFilter.update(payload.allowed(), payload.denied())));
	}
}
