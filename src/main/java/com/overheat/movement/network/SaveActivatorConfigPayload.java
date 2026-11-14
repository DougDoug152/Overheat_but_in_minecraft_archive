package com.overheat.movement.network;

import java.util.List;
import com.overheat.movement.OverheatMovement;
import com.overheat.movement.ability.ActivatorEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SaveActivatorConfigPayload(BlockPos pos, List<ActivatorEntry> entries) implements CustomPacketPayload {

	public static final CustomPacketPayload.Type<SaveActivatorConfigPayload> TYPE =
			new CustomPacketPayload.Type<>(OverheatMovement.id("save_activator_config"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SaveActivatorConfigPayload> CODEC = StreamCodec.of(
			(buf, payload) -> {
				buf.writeBlockPos(payload.pos);
				ActivatorEntry.writeList(buf, payload.entries);
			},
			buf -> new SaveActivatorConfigPayload(buf.readBlockPos(), ActivatorEntry.readList(buf)));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
