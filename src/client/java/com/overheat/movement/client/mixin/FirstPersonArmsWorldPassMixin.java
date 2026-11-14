package com.overheat.movement.client.mixin;

import com.overheat.movement.client.animation.ArmsAnimationHandler;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class FirstPersonArmsWorldPassMixin {

	@Inject(method = "renderEntity", at = @At("HEAD"), cancellable = true)
	private void overheat$skipFirstPersonArms(Entity entity, double cameraX, double cameraY, double cameraZ,
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		if (entity != mc.getCameraEntity() || !mc.options.getCameraType().isFirstPerson()) {
			return;
		}
		if (!ArmsAnimationHandler.isPlaying(entity)) {
			return;
		}
		FirstPersonMode.setFirstPersonPass(false);
		ci.cancel();
	}
}
