package com.overheat.movement.client.sound;

import com.overheat.movement.ModSounds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public final class SlideSound extends AbstractTickableSoundInstance {

	private static final float TARGET_VOLUME = 0.56f;
	private static final int FADE_IN_TICKS = 2;
	private static final int FADE_OUT_TICKS = 3;

	private final LocalPlayer player;
	private boolean fading = false;
	private float fade;

	public SlideSound(LocalPlayer player, boolean loop) {
		super(loop ? ModSounds.SLIDE_SLIDING_LOOP : ModSounds.SLIDE_SLIDING, SoundSource.PLAYERS,
				SoundInstance.createUnseededRandom());
		this.player = player;
		this.looping = loop;
		this.delay = 0;
		this.fade = loop ? 0f : 1f;
		this.volume = TARGET_VOLUME * fade;
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	public void fadeOut() {
		fading = true;
	}

	@Override
	public void tick() {
		if (player.isRemoved() || !player.isAlive()) {
			stop();
			return;
		}
		x = player.getX();
		y = player.getY();
		z = player.getZ();
		if (!fading) {
			fade = Math.min(fade + 1f / FADE_IN_TICKS, 1f);
			volume = TARGET_VOLUME * fade;
			return;
		}
		fade -= 1f / FADE_OUT_TICKS;
		if (fade <= 0f) {
			volume = 0f;
			stop();
			return;
		}
		volume = TARGET_VOLUME * fade;
	}
}
