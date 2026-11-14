package com.overheat.movement.client.mixin;

import com.overheat.movement.client.movement.MovementHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class SlideLedgeMixin {

	@Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
	private void overheat$slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		if (!MovementHandler.isSliding()) {
			return;
		}
		LocalPlayer local = Minecraft.getInstance().player;
		if (local != null && local.getUUID().equals(((Player) (Object) this).getUUID())) {
			cir.setReturnValue(false);
		}
	}
}
