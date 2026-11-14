package com.overheat.movement.client.mixin;

import com.overheat.movement.client.movement.MovementHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class FovKickMixin {

	@Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
	private void overheat$dashFov(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Float> cir) {
		float offset = MovementHandler.getDashFovOffset() + MovementHandler.getSlideFovOffset();
		if (offset != 0f) {
			cir.setReturnValue(cir.getReturnValueF() + offset);
		}
	}
}