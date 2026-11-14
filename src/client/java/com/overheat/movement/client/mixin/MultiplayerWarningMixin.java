package com.overheat.movement.client.mixin;

import com.overheat.movement.client.gui.MultiplayerWarningScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MultiplayerWarningMixin {

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	private void overheat$multiplayerWarning(Screen screen, CallbackInfo ci) {
		if (MultiplayerWarningScreen.bypass) {
			MultiplayerWarningScreen.bypass = false;
			return;
		}
		if (screen instanceof JoinMultiplayerScreen) {
			Minecraft mc = (Minecraft) (Object) this;
			ci.cancel();
			mc.setScreen(new MultiplayerWarningScreen(screen));
		}
	}
}
