package com.overheat.movement.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.overheat.movement.client.animation.ArmsAnimationHandler;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class FirstPersonArmsHandPassMixin {

	@Inject(method = "renderItemInHand", at = @At("HEAD"))
	private void overheat$renderFirstPersonArms(Camera camera, float partialTick, Matrix4f viewMatrix,
			CallbackInfo ci) {
		Minecraft mc = Minecraft.getInstance();
		Entity entity = mc.getCameraEntity();
		if (entity == null || !mc.options.getCameraType().isFirstPerson()) {
			return;
		}
		if (!ArmsAnimationHandler.isPlaying(entity)) {
			return;
		}

		Vec3 cam = camera.getPosition();
		double x = Mth.lerp((double) partialTick, entity.xOld, entity.getX()) - cam.x;
		double y = Mth.lerp((double) partialTick, entity.yOld, entity.getY()) - cam.y;
		double z = Mth.lerp((double) partialTick, entity.zOld, entity.getZ()) - cam.z;

		EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
		MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

		Matrix4fStack modelView = RenderSystem.getModelViewStack();
		modelView.pushMatrix().mul(viewMatrix);

		FirstPersonMode.setFirstPersonPass(true);
		dispatcher.render(entity, x, y, z, partialTick, new PoseStack(), buffers,
				dispatcher.getPackedLightCoords(entity, partialTick));
		buffers.endBatch();
		FirstPersonMode.setFirstPersonPass(false);

		modelView.popMatrix();
	}
}
