package com.overheat.movement.client.mixin;

import com.overheat.movement.client.movement.MovementHandler;
import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public class CameraTiltMixin {
	@Shadow @Final private static Vector3f FORWARDS;
	@Shadow @Final private static Vector3f UP;
	@Shadow @Final private static Vector3f LEFT;
	@Shadow @Final private Vector3f forwards;
	@Shadow @Final private Vector3f up;
	@Shadow @Final private Vector3f left;
	@Shadow @Final private Quaternionf rotation;

	@Inject(method = "setRotation(FF)V", at = @At("TAIL"))
	private void movement$tilt(float yaw, float pitch, CallbackInfo ci) {
		float roll = MovementHandler.getViewRoll();
		float pitchPunch = MovementHandler.getViewPitchPunch();
		if (Math.abs(roll) < 0.01f && Math.abs(pitchPunch) < 0.01f) {
			return;
		}
		this.rotation.rotateZ(roll * ((float) Math.PI / 180f));
		this.rotation.rotateX(pitchPunch * ((float) Math.PI / 180f));
		FORWARDS.rotate(this.rotation, this.forwards);
		UP.rotate(this.rotation, this.up);
		LEFT.rotate(this.rotation, this.left);
	}
}
