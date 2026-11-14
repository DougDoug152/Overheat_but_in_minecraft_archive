package com.overheat.movement.client.hud;

import com.overheat.movement.client.movement.MovementHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class DashOverlay {

	private static final ResourceLocation TEX =
			ResourceLocation.fromNamespaceAndPath("overheat", "textures/gui/dash_visuals.png");
	private static final int TEX_W = 1717;
	private static final int TEX_H = 916;
	private static final float HUD_Z = 200f;

	public void register() {
		HudRenderCallback.EVENT.register((graphics, tick) -> render(graphics));
	}

	private void render(GuiGraphics g) {
		float alpha = MovementHandler.getDashOverlayAlpha();
		if (alpha <= 0.001f) {
			return;
		}
		float scale = MovementHandler.getDashOverlayScale();
		int gw = g.guiWidth();
		int gh = g.guiHeight();
		int w = Math.round(gw * scale);
		int h = Math.round(gh * scale);
		int x = (gw - w) / 2;
		int y = (gh - h) / 2;
		int color = (Math.round(alpha * 255f) << 24) | 0xFFFFFF;
		g.pose().pushPose();
		g.pose().translate(0f, 0f, HUD_Z);
		g.blit(RenderType::guiTextured, TEX, x, y, 0f, 0f,
				w, h, TEX_W, TEX_H, TEX_W, TEX_H, color);
		g.pose().popPose();
	}
}
