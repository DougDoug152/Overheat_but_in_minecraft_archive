package com.overheat.movement.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class MultiplayerWarningScreen extends Screen {

	public static boolean bypass = false;

	private static final int RED = 0xFFFF4040;
	private final Screen target;

	public MultiplayerWarningScreen(Screen target) {
		super(Component.literal("Multiplayer Warning"));
		this.target = target;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int by = this.height - 44;
		this.addRenderableWidget(Button.builder(
						Component.literal("I understand the risk, continue").withStyle(ChatFormatting.RED),
						b -> {
							bypass = true;
							this.minecraft.setScreen(this.target);
						})
				.bounds(cx - 205, by, 200, 20).build());
		this.addRenderableWidget(Button.builder(
						Component.literal("Back to safety").withStyle(ChatFormatting.GREEN),
						b -> this.minecraft.setScreen(new TitleScreen()))
				.bounds(cx + 5, by, 200, 20).build());
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		super.render(g, mouseX, mouseY, partialTick);
		int cx = this.width / 2;
		int top = this.height / 4;

		g.pose().pushPose();
		g.pose().translate(cx, top, 0);
		g.pose().scale(4.0f, 4.0f, 1.0f);
		g.drawCenteredString(this.font,
				Component.literal("WARNING").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), 0, 0, RED);
		g.pose().popPose();

		int y = top + 46;
		g.drawCenteredString(this.font,
				Component.literal("READ THIS BEFORE CONTINUING").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
				cx, y, RED);

		y += 24;
		g.drawCenteredString(this.font,
				Component.literal("This mod changes your movement on the client."), cx, y, 0xFFFFFFFF);
		y += 14;
		g.drawCenteredString(this.font,
				Component.literal("Using it on a ")
						.append(Component.literal("public server not made for this mod").withStyle(ChatFormatting.BOLD)),
				cx, y, 0xFFFFFFFF);
		y += 14;
		g.drawCenteredString(this.font,
				Component.literal("can or ")
						.append(Component.literal("WILL GET YOU BANNED").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
						.append(Component.literal(" by any kinda anti-cheat(s).")),
				cx, y, 0xFFFFFFFF);
		y += 18;
		g.drawCenteredString(this.font,
				Component.literal("ONLY use this on servers built for this mod.").withStyle(ChatFormatting.GRAY),
				cx, y, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(new TitleScreen());
	}
}
