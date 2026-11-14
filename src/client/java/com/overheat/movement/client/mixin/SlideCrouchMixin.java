package com.overheat.movement.client.mixin;

import com.overheat.movement.client.movement.MovementHandler;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class SlideCrouchMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void overheat$slideCrouch(CallbackInfo ci) {
		if (!MovementHandler.isSliding()) {
			return;
		}
		ClientInput self = (ClientInput) (Object) this;
		Input k = self.keyPresses;
		if (k.shift()) {
			return;
		}
		self.keyPresses = new Input(k.forward(), k.backward(), k.left(), k.right(), k.jump(), true, k.sprint());
	}
}
