package com.overheat.movement.client.sound;

import com.overheat.movement.ModSounds;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public final class WallSlideSound extends AbstractTickableSoundInstance {

	private static final float TARGET_VOLUME = 0.56f;
	private static final int FADE_IN_TICKS = 4;
	private static final int FADE_OUT_TICKS = 4;

	private final LocalPlayer player;
	private boolean active = true;
	private float fade = 0f;

	public WallSlideSound(LocalPlayer player) {
		super(ModSounds.WALL_SLIDE, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
		this.player = player;
		this.looping = true;
		this.delay = 0;
		this.volume = 0f;
		this.x = player.getX();
		this.y = player.getY();
		this.z = player.getZ();
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	public void setActive(boolean active) {
		this.active = active;
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
		fade = Mth.clamp(fade + (active ? 1f / FADE_IN_TICKS : -1f / FADE_OUT_TICKS), 0f, 1f);
		volume = TARGET_VOLUME * fade;
		if (!active && fade <= 0f) {
			stop();
		}
	}
}
