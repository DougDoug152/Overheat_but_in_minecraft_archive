package com.overheat.movement;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/** Custom sound events for the movement mod. */
public final class ModSounds {

	public static final SoundEvent WALL_ATTACH = register("wall_attach");
	public static final SoundEvent WALL_LET_GO = register("wall_let_go");
	public static final SoundEvent WALL_SLIDE = register("wall_slide");
	public static final SoundEvent UNUSED_SPECIAL_WALL_JUMP = register("unused_special_wall_jump");
	public static final SoundEvent UNUSED_SPECIAL_WALL_JUMP_RECHARGE = register("unused_special_wall_jump_recharge");
	public static final SoundEvent DASH = register("dash");
	public static final SoundEvent RECHARGE = register("recharge");
	public static final SoundEvent AIR_JUMP_RECHARGE = register("air_jump_recharge");
	public static final SoundEvent AIR_JUMP = register("air_jump");
	public static final SoundEvent PUNCH = register("punch");
	public static final SoundEvent SLIDE_START = register("slide_start");
	public static final SoundEvent SLIDE_SLIDING = register("slide_sliding");
	public static final SoundEvent SLIDE_SLIDING_LOOP = register("slide_sliding_loop");
	public static final SoundEvent SLIDE_END = register("slide_end");
	public static final SoundEvent ABILITY_GAIN = register("ability_gain");
	public static final SoundEvent ABILITY_REMOVE = register("ability_remove");

	private ModSounds() {
	}

	private static SoundEvent register(String path) {
		ResourceLocation id = OverheatMovement.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	/** Forces class loading so the static fields above are registered. */
	public static void init() {
	}
}
