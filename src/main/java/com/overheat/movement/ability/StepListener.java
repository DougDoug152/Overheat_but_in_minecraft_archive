package com.overheat.movement.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.overheat.movement.ModSounds;
import com.overheat.movement.content.ModBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

public final class StepListener {

	private static final List<ActivatorEntry> GIVER_DEFAULT = List.of(
			new ActivatorEntry(Action.ACTIVATE, Ability.DASH),
			new ActivatorEntry(Action.ACTIVATE, Ability.AIR_JUMP),
			new ActivatorEntry(Action.ACTIVATE, Ability.WALL_RUN),
			new ActivatorEntry(Action.ACTIVATE, Ability.BHOP),
			new ActivatorEntry(Action.ACTIVATE, Ability.COMBAT),
			new ActivatorEntry(Action.ACTIVATE, Ability.SLIDE));

	private static final Map<UUID, Long> lastPos = new HashMap<>();

	private StepListener() {
	}

	public static List<ActivatorEntry> defaultConfig() {
		return GIVER_DEFAULT;
	}

	public static boolean isIntrinsic(BlockState state) {
		return state.is(ModBlocks.ABILITY_GIVER) || state.is(ModBlocks.UNACTIVE_ACTIVATOR);
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player);
			}
		});
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world instanceof ServerLevel level) {
				ActivatorData.get(level).remove(pos);
			}
		});
	}

	private static void tickPlayer(ServerPlayer player) {
		UUID id = player.getUUID();
		if (!player.onGround()) {
			return;
		}
		ServerLevel level = player.serverLevel();
		BlockPos pos = player.blockPosition().below();
		List<ActivatorEntry> config = configFor(level, pos);
		if (config == null) {
			lastPos.remove(id);
			return;
		}
		Long last = lastPos.get(id);
		if (last != null && last == pos.asLong()) {
			return;
		}
		lastPos.put(id, pos.asLong());
		applyConfig(player, level, pos, config);
	}

	private static List<ActivatorEntry> configFor(ServerLevel level, BlockPos pos) {
		List<ActivatorEntry> stored = ActivatorData.get(level).getConfig(pos);
		if (stored != null) {
			return stored;
		}
		if (isIntrinsic(level.getBlockState(pos))) {
			return GIVER_DEFAULT;
		}
		return null;
	}

	private static void applyConfig(ServerPlayer player, ServerLevel level, BlockPos pos, List<ActivatorEntry> config) {
		boolean gained = false;
		boolean lost = false;
		for (ActivatorEntry entry : config) {
			boolean enable = entry.action() == Action.ACTIVATE;
			if (PlayerAbilities.has(player, entry.ability()) != enable) {
				PlayerAbilities.set(player, entry.ability(), enable);
				if (enable) {
					player.displayClientMessage(gainedMessage(entry.ability()), false);
					gained = true;
				} else {
					player.displayClientMessage(lostMessage(entry.ability()), false);
					lost = true;
				}
			}
		}
		if (gained) {
			level.playSound(null, pos, ModSounds.ABILITY_GAIN, SoundSource.PLAYERS, 0.8f, 1.0f);
		}
		if (lost) {
			level.playSound(null, pos, ModSounds.ABILITY_REMOVE, SoundSource.PLAYERS, 0.8f, 1.0f);
		}
	}

	public static void revokeGranted(ServerPlayer player, ServerLevel level, BlockPos pos, List<ActivatorEntry> config) {
		boolean lost = false;
		for (ActivatorEntry entry : config) {
			if (entry.action() == Action.ACTIVATE && PlayerAbilities.has(player, entry.ability())) {
				PlayerAbilities.set(player, entry.ability(), false);
				player.displayClientMessage(lostMessage(entry.ability()), false);
				lost = true;
			}
		}
		if (lost) {
			level.playSound(null, pos, ModSounds.ABILITY_REMOVE, SoundSource.PLAYERS, 0.8f, 1.0f);
		}
	}

	private static Component gainedMessage(Ability ability) {
		return Component.empty()
				.append(Component.literal("You've gained the ability to ").withStyle(ChatFormatting.WHITE))
				.append(Component.literal(ability.displayName())
						.withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD))
				.append(Component.literal("!").withStyle(ChatFormatting.WHITE));
	}

	private static Component lostMessage(Ability ability) {
		return Component.empty()
				.append(Component.literal("You've lost the ability to ").withStyle(ChatFormatting.WHITE))
				.append(Component.literal(ability.displayName())
						.withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
				.append(Component.literal("!").withStyle(ChatFormatting.WHITE));
	}
}
