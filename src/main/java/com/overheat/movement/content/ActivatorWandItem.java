package com.overheat.movement.content;

import java.util.List;
import com.overheat.movement.ability.ActivatorData;
import com.overheat.movement.ability.ActivatorEntry;
import com.overheat.movement.ability.StepListener;
import com.overheat.movement.network.OpenActivatorConfigPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ActivatorWandItem extends Item {

	public ActivatorWandItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			handle(serverLevel, serverPlayer, context.getClickedPos());
		}
		return InteractionResult.SUCCESS;
	}

	private void handle(ServerLevel level, ServerPlayer player, BlockPos pos) {
		ActivatorData data = ActivatorData.get(level);
		boolean giver = StepListener.isIntrinsic(level.getBlockState(pos));
		boolean stored = data.isActivator(pos);

		if (player.isShiftKeyDown()) {
			if ((giver || stored) && player.isCreative()) {
				List<ActivatorEntry> config = data.getConfig(pos);
				if (config == null) {
					config = StepListener.defaultConfig();
				}
				ServerPlayNetworking.send(player, new OpenActivatorConfigPayload(pos, config));
			}
			return;
		}

		if (giver) {
			player.displayClientMessage(Component.literal("Activator — sneak + right-click to configure"), true);
			return;
		}
		if (stored) {
			List<ActivatorEntry> config = data.getConfig(pos);
			data.remove(pos);
			if (config != null) {
				StepListener.revokeGranted(player, level, pos, config);
			}
			player.displayClientMessage(Component.literal("Activator removed"), true);
		} else {
			data.setConfig(pos, StepListener.defaultConfig());
			player.displayClientMessage(Component.literal("Activator added — sneak + right-click to configure"), true);
		}
	}
}
