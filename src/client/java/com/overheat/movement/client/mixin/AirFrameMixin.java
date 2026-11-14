package com.overheat.movement.client.mixin;

import com.overheat.movement.client.movement.MovementHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class AirFrameMixin {

	@Redirect(
			method = "moveRelative",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getYRot()F"))
	private float overheat$airFrameYaw(Entity entity) {
		if (entity instanceof LocalPlayer && MovementHandler.hasAirFrame()) {
			return MovementHandler.getAirFrameYaw();
		}
		return entity.getYRot();
	}
}
