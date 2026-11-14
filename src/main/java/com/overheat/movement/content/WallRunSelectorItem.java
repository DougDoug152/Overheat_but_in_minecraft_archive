package com.overheat.movement.content;

import java.util.List;
import com.overheat.movement.ability.WallRunFilterData;
import com.overheat.movement.network.AbilityNetworking;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class WallRunSelectorItem extends Item {

	public WallRunSelectorItem(Properties properties) {
		super(properties);
	}

	public static boolean isAllowMode(ItemStack stack) {
		CustomModelData data = stack.get(DataComponents.CUSTOM_MODEL_DATA);
		return data != null && Boolean.TRUE.equals(data.getBoolean(0));
	}

	private static boolean toggleMode(ItemStack stack) {
		boolean allow = !isAllowMode(stack);
		stack.set(DataComponents.CUSTOM_MODEL_DATA,
				new CustomModelData(List.of(), List.of(allow), List.of(), List.of()));
		return allow;
	}

	public static void registerEvents() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.is(ModItems.WALL_RUN_SELECTOR) || !player.isShiftKeyDown()) {
				return InteractionResult.PASS;
			}
			if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
				boolean allow = toggleMode(stack);
				serverPlayer.displayClientMessage(Component.literal("Selection mode: ")
						.append(Component.literal(allow ? "Allowlist" : "Blocklist")
								.withStyle(allow ? ChatFormatting.GREEN : ChatFormatting.RED)), true);
			}
			return InteractionResult.SUCCESS;
		});
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		if (player == null) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			handle(serverLevel, serverPlayer, context.getClickedPos(), context.getItemInHand());
		}
		return InteractionResult.SUCCESS;
	}

	private void handle(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack stack) {
		ResourceLocation id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock());
		WallRunFilterData data = WallRunFilterData.get(level);
		boolean allow = isAllowMode(stack);
		boolean nowContains = allow ? data.toggleAllowed(id) : data.toggleDenied(id);
		AbilityNetworking.broadcastFilter(level);

		Component blockName = level.getBlockState(pos).getBlock().getName();
		String listName = allow ? "wall-run allowlist" : "wall-run blocklist";
		Component action = Component.literal(nowContains ? "Added " : "Removed ")
				.append(blockName)
				.append(Component.literal((nowContains ? " to " : " from ") + listName));
		player.displayClientMessage(action, true);
	}
}
