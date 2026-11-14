package com.overheat.movement.network;

import com.overheat.movement.OverheatMovement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeleteActivatorPayload(BlockPos pos) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<DeleteActivatorPayload> TYPE =
			new CustomPacketPayload.Type<>(OverheatMovement.id("delete_activator"));

	public static final StreamCodec<RegistryFriendlyByteBuf, DeleteActivatorPayload> CODEC = StreamCodec.of(
			(buf, payload) -> buf.writeBlockPos(payload.pos),
			buf -> new DeleteActivatorPayload(buf.readBlockPos()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
