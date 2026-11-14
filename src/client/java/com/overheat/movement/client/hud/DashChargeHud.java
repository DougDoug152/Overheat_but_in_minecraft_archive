package com.overheat.movement.client.hud;

import com.overheat.movement.client.movement.MovementHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class DashChargeHud {

	private static final ResourceLocation TEX_FULL =
			ResourceLocation.fromNamespaceAndPath("overheat", "textures/gui/dash_ui_2.png");
	private static final ResourceLocation TEX_ONE =
			ResourceLocation.fromNamespaceAndPath("overheat", "textures/gui/dash_ui_1.png");
	private static final ResourceLocation TEX_NONE =
			ResourceLocation.fromNamespaceAndPath("overheat", "textures/gui/dash_ui_0.png");
	private static final int TEX_SIZE = 1254;

	private static final float CENTER_X_FRAC = 0.15f;
	private static final float CENTER_Y_FRAC = 0.75f;
	private static final float HEIGHT_FRAC = 0.24f;
	private static final float NONE_SIZE_MULT = 0.95914f;
	private static final int DIM_ALPHA = 128;
	private static final float HUD_Z = 200f;

	public void register() {
		HudRenderCallback.EVENT.register((graphics, tick) -> render(graphics));
	}

	private void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) {
			return;
		}
		float fade = MovementHandler.getDashHudAlpha();
		if (fade <= 0.001f) {
			return;
		}
		int fullAlpha = Math.round(fade * 255f) << 24;
		int dimAlpha = Math.round(fade * DIM_ALPHA) << 24;

		int charges = MovementHandler.getDashCharges();
		ResourceLocation tex = charges >= 2 ? TEX_FULL : charges == 1 ? TEX_ONE : TEX_NONE;

		int gw = g.guiWidth();
		int gh = g.guiHeight();
		float sizeFrac = charges == 0 ? HEIGHT_FRAC * NONE_SIZE_MULT : HEIGHT_FRAC;
		int h = Math.round(gh * sizeFrac);
		int w = h;
		float swayX = MovementHandler.getHudSwayX();
		float swayY = MovementHandler.getHudSwayY();
		int x = Math.round(gw * CENTER_X_FRAC - w / 2f);
		int y = Math.round(gh * CENTER_Y_FRAC - h / 2f);
		if (charges == 0) {
			x -= 1;
			y -= 1;
		}
		g.pose().pushPose();
		g.pose().translate(swayX, swayY, HUD_Z);
		if (charges >= 2) {
			g.blit(RenderType::guiTextured, tex, x, y, 0f, 0f, w, h,
					TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE, fullAlpha | 0xFFFFFF);
		} else {
			float progress = MovementHandler.getDashRechargeProgress();
			int sweepH = Math.round(h * progress);

			if (sweepH > 0) {
				g.enableScissor(x, y, x + w, y + sweepH);
				g.blit(RenderType::guiTextured, tex, x, y, 0f, 0f, w, h,
						TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE, fullAlpha | 0xFFFFFF);
				g.disableScissor();
			}
			if (sweepH < h) {
				g.enableScissor(x, y + sweepH, x + w, y + h);
				g.blit(RenderType::guiTextured, tex, x, y, 0f, 0f, w, h,
						TEX_SIZE, TEX_SIZE, TEX_SIZE, TEX_SIZE, dimAlpha | 0xFFFFFF);
				g.disableScissor();
			}
		}
		g.pose().popPose();
	}
}
