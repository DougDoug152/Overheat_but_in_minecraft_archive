package com.overheat.movement.content;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NonSteelArtsItem extends Item {

	public NonSteelArtsItem(Properties properties) {
		super(properties);
	}

	public static boolean isHolding(Player player) {
		return player.getMainHandItem().is(ModItems.NON_STEEL_ARTS)
				|| player.getOffhandItem().is(ModItems.NON_STEEL_ARTS);
	}

	public static void registerEvents() {
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			return isHolding(player) ? InteractionResult.FAIL : InteractionResult.PASS;
		});
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
		return false;
	}
}
