package com.overheat.movement.client;

import com.overheat.movement.client.animation.ArmsAnimationHandler;
import com.overheat.movement.client.hud.DashChargeHud;
import com.overheat.movement.client.hud.DashOverlay;
import com.overheat.movement.client.hud.SpeedHud;
import com.overheat.movement.client.movement.MovementHandler;
import com.overheat.movement.content.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class OverheatMovementClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		KeyMapping dashKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.overheat.dash",
				GLFW.GLFW_KEY_R,
				"key.categories.overheat"
		));

		KeyMapping slideKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.overheat.slide",
				GLFW.GLFW_KEY_LEFT_CONTROL,
				"key.categories.overheat"
		));

		KeyMapping slideAltKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.overheat.slide_alt",
				GLFW.GLFW_KEY_C,
				"key.categories.overheat"
		));

		new MovementHandler(dashKey, slideKey, slideAltKey).register();
		SprintKeyFix.register();
		new SpeedHud().register();
		new DashOverlay().register();
		new DashChargeHud().register();
		AbilityClientNetworking.register();
		ArmsAnimationHandler.register();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}
			if (client.player.getMainHandItem().is(ModItems.WALL_RUN_SELECTOR)
					|| client.player.getOffhandItem().is(ModItems.WALL_RUN_SELECTOR)) {
				client.player.displayClientMessage(
						Component.literal("Crouch and left click to change selection mode"), true);
			}
		});
	}
}
