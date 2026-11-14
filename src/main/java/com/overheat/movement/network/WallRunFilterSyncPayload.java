package com.overheat.movement.network;

import java.util.ArrayList;
import java.util.List;
import com.overheat.movement.OverheatMovement;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record WallRunFilterSyncPayload(List<ResourceLocation> allowed, List<ResourceLocation> denied)
		implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<WallRunFilterSyncPayload> TYPE =
			new CustomPacketPayload.Type<>(OverheatMovement.id("wallrun_filter_sync"));

	public static final StreamCodec<RegistryFriendlyByteBuf, WallRunFilterSyncPayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				writeList(buf, payload.allowed);
				writeList(buf, payload.denied);
			},
			buf -> new WallRunFilterSyncPayload(readList(buf), readList(buf)));

	private static void writeList(RegistryFriendlyByteBuf buf, List<ResourceLocation> ids) {
		buf.writeVarInt(ids.size());
		for (ResourceLocation id : ids) {
			buf.writeResourceLocation(id);
		}
	}

	private static List<ResourceLocation> readList(RegistryFriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<ResourceLocation> ids = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			ids.add(buf.readResourceLocation());
		}
		return ids;
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
