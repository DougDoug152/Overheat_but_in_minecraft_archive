package com.overheat.movement.client.hud;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.overheat.movement.client.movement.MovementHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SpeedHud {

	private static final float CENTER_X_FRAC = 0.5f;
	private static final float CENTER_Y_FRAC = 0.035f;
	private static final float RADIUS_FRAC = 0.0827f;

	private static final float MAX_BPS = 32f;
	private static final int FADE_START_TICK = 13;

	private static final float RING_INNER = 0.90f;
	private static final float RING_OUTER = 1.00f;
	private static final float TICK_INNER = 0.74f;
	private static final float TICK_OUTER = 0.87f;
	private static final float TICK_HALF_WIDTH = 0.016f;
	private static final int TICK_COUNT = 17;
	private static final float BACKING_RADIUS = 1.00f;

	private static final float TEXT_HEIGHT = 0.30f;
	private static final float TEXT_Y = 0.36f;

	private static final float SEG_DEGREES = 2f;
	private static final float SMOOTH_TAU = 0.06f;

	private static final int COLOR_ARC = 0xFF427DF5;
	private static final int COLOR_ARC_OVER = 0xFF910D0F;
	private static final int COLOR_TICK = 0x99FFFFFF;
	private static final int COLOR_BACKING = 0x4D000000;

	private static final float HUD_Z = 200f;

	private float shownBps;
	private long lastNanos;

	public void register() {
		HudRenderCallback.EVENT.register((graphics, tick) -> render(graphics));
	}

	private void render(GuiGraphics g) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null) {
			return;
		}

		Vec3 v = p.getDeltaMovement();
		float target = (float) (Math.sqrt(v.x * v.x + v.z * v.z) * 20.0);

		long now = System.nanoTime();
		float dt = lastNanos == 0L ? 0.016f : Math.min(0.1f, (now - lastNanos) / 1.0e9f);
		lastNanos = now;
		shownBps += (target - shownBps) * (1f - (float) Math.exp(-dt / SMOOTH_TAU));

		float r = g.guiHeight() * RADIUS_FRAC;
		float cx = g.guiWidth() * CENTER_X_FRAC + MovementHandler.getHudSwayX();
		float cy = g.guiHeight() * CENTER_Y_FRAC + MovementHandler.getHudSwayY();

		float fill = Mth.clamp(shownBps / MAX_BPS, 0f, 1f);
		float fadeStart = MAX_BPS * FADE_START_TICK / (TICK_COUNT - 1);
		float over = Mth.clamp((shownBps - fadeStart) / (MAX_BPS - fadeStart), 0f, 1f);
		int arcColor = lerpColor(COLOR_ARC, COLOR_ARC_OVER, over);

		g.pose().pushPose();
		g.pose().translate(0f, 0f, HUD_Z);
		Matrix4f m = g.pose().last().pose();

		g.drawSpecial(source -> {
			VertexConsumer vc = source.getBuffer(RenderType.gui());

			ring(vc, m, cx, cy, 0f, r * BACKING_RADIUS, 0f, 180f, COLOR_BACKING);

			for (int i = 0; i < TICK_COUNT; i++) {
				float a = 180f - 180f * i / (TICK_COUNT - 1);
				tick(vc, m, cx, cy, r * TICK_INNER, r * TICK_OUTER, a, TICK_HALF_WIDTH, COLOR_TICK);
			}

			if (fill > 0f) {
				ring(vc, m, cx, cy, r * RING_INNER, r * RING_OUTER, 180f - 180f * fill, 180f, arcColor);
			}
		});

		Font font = mc.font;
		String s = Integer.toString(Math.round(shownBps));
		float scale = (TEXT_HEIGHT * r) / 7f;
		g.pose().pushPose();
		g.pose().translate(cx, cy + r * TEXT_Y, 0f);
		g.pose().scale(scale, scale, 1f);
		g.pose().translate(-font.width(s) / 2f, -4.5f, 0f);
		g.drawString(font, s, 0, 0, arcColor, false);
		g.pose().popPose();

		g.pose().popPose();
	}

	private static void ring(VertexConsumer vc, Matrix4f m, float cx, float cy,
			float rIn, float rOut, float aLo, float aHi, int color) {
		int segs = Math.max(1, Math.round((aHi - aLo) / SEG_DEGREES));
		for (int i = 0; i < segs; i++) {
			float t0 = aLo + (aHi - aLo) * i / segs;
			float t1 = aLo + (aHi - aLo) * (i + 1) / segs;
			vertex(vc, m, cx, cy, t1, rIn, color);
			vertex(vc, m, cx, cy, t1, rOut, color);
			vertex(vc, m, cx, cy, t0, rOut, color);
			vertex(vc, m, cx, cy, t0, rIn, color);
		}
	}

	private static void tick(VertexConsumer vc, Matrix4f m, float cx, float cy,
			float rIn, float rOut, float deg, float halfWidth, int color) {
		float a = deg * Mth.DEG_TO_RAD;
		float ux = Mth.cos(a);
		float uy = Mth.sin(a);
		float tx = -uy * halfWidth * rOut;
		float ty = ux * halfWidth * rOut;
		vc.addVertex(m, cx + ux * rIn + tx, cy + uy * rIn + ty, 0f).setColor(color);
		vc.addVertex(m, cx + ux * rOut + tx, cy + uy * rOut + ty, 0f).setColor(color);
		vc.addVertex(m, cx + ux * rOut - tx, cy + uy * rOut - ty, 0f).setColor(color);
		vc.addVertex(m, cx + ux * rIn - tx, cy + uy * rIn - ty, 0f).setColor(color);
	}

	private static void vertex(VertexConsumer vc, Matrix4f m, float cx, float cy,
			float deg, float rad, int color) {
		float a = deg * Mth.DEG_TO_RAD;
		vc.addVertex(m, cx + Mth.cos(a) * rad, cy + Mth.sin(a) * rad, 0f).setColor(color);
	}

	private static int lerpColor(int a, int b, float t) {
		int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		int oa = Math.round(aa + (ba - aa) * t);
		int or = Math.round(ar + (br - ar) * t);
		int og = Math.round(ag + (bg - ag) * t);
		int ob = Math.round(ab + (bb - ab) * t);
		return (oa << 24) | (or << 16) | (og << 8) | ob;
	}
}
