package com.overheat.movement.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SprintKeyFix {

	private static final int CONFLICT_KEY = GLFW.GLFW_KEY_LEFT_CONTROL;
	private static final int REPLACEMENT_KEY = GLFW.GLFW_KEY_LEFT_ALT;

	private SprintKeyFix() {
	}

	public static void register() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(() -> apply(client)));
	}

	private static void apply(Minecraft mc) {
		KeyMapping sprint = mc.options.keySprint;
		if (!sprint.matches(CONFLICT_KEY, 0)) {
			return;
		}
		Component was = sprint.getTranslatedKeyMessage();
		sprint.setKey(InputConstants.Type.KEYSYM.getOrCreate(REPLACEMENT_KEY));
		KeyMapping.resetMapping();
		mc.options.save();
		Component now = sprint.getTranslatedKeyMessage();
		if (mc.player == null) {
			return;
		}
		mc.player.displayClientMessage(Component.literal("OVERHEAT: Hey! Your sprint keybind was changed from ")
				.append(was)
				.append(" to ")
				.append(now)
				.append(", this is due to conflicting settings and intended play-style.")
				.withStyle(ChatFormatting.GOLD), false);
	}
}
