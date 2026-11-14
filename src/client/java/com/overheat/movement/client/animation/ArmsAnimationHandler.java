package com.overheat.movement.client.animation;

import com.overheat.movement.OverheatMovement;
import com.overheat.movement.client.movement.MovementHandler;
import com.overheat.movement.content.NonSteelArtsItem;
import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public final class ArmsAnimationHandler {

	private static final ResourceLocation IDLE_ID = OverheatMovement.id("idle");
	private static final ResourceLocation WALL_GRAB_ID = OverheatMovement.id("wall_grab");
	private static final ResourceLocation WALL_GRAB_MIRRORED_ID = OverheatMovement.id("wall_grab_mirrored");
	private static final ResourceLocation WALL_GRAB_OUT_ID = OverheatMovement.id("wall_grab_out");
	private static final ResourceLocation WALL_GRAB_OUT_MIRRORED_ID = OverheatMovement.id("wall_grab_out_mirrored");
	private static final ResourceLocation WALL_RUN_ID = OverheatMovement.id("wall_run");
	private static final ResourceLocation WALL_RUN_MIRRORED_ID = OverheatMovement.id("wall_run_mirrored");
	private static final ResourceLocation PUNCH1_ID = OverheatMovement.id("punch1");
	private static final ResourceLocation PUNCH1_MIRRORED_ID = OverheatMovement.id("punch1_mirrored");
	private static final ResourceLocation LAYER_ID = OverheatMovement.id("non_steel_arts");
	private static final int LAYER_PRIORITY = 1000;
	private static final int FADE_IN_TICKS = 3;
	private static final int OUT_BLEND_TICKS = 2;
	private static final int OUT_LENGTH_TICKS = 7;
	private static final int GRAB_LENGTH_TICKS = 7;
	private static final int PUNCH_FADE_IN_TICKS = 1;
	private static final int PUNCH_OUT_BLEND_TICKS = 0;
	private static final int PUNCH_MIRROR_BLEND_TICKS = 3;
	private static final boolean AUTHORED_SIDE_POSITIVE = true;
	private static final boolean REBONE_ARMS_TO_HEAD = true;

	private static boolean localWallGrab = false;
	private static boolean localPunch = false;
	private static boolean localMirrored = false;
	private static int outTicks = 0;
	private static int grabTicks = 0;

	private ArmsAnimationHandler() {
	}

	public static void register() {
		PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, stack) -> {
			ModifierLayer<IAnimation> layer = new ModifierLayer<>();
			if (REBONE_ARMS_TO_HEAD) {
				layer.addModifierLast(new HeadLockModifier(player));
			}
			stack.addAnimLayer(LAYER_PRIORITY, layer);
			PlayerAnimationAccess.getPlayerAssociatedData(player).set(LAYER_ID, layer);
		});

		ClientTickEvents.END_CLIENT_TICK.register(ArmsAnimationHandler::tick);
	}

	private static void tick(Minecraft mc) {
		if (mc.level == null) {
			localWallGrab = false;
			localPunch = false;
			localMirrored = false;
			outTicks = 0;
			grabTicks = 0;
			return;
		}
		for (AbstractClientPlayer player : mc.level.players()) {
			ModifierLayer<?> layer = layerOf(player);
			if (layer == null) {
				continue;
			}
			boolean local = player == mc.player;
			if (!isHolding(player)) {
				if (layer.getAnimation() != null) {
					stop(layer);
				}
				if (local) {
					localWallGrab = false;
					localPunch = false;
					localMirrored = false;
					outTicks = 0;
					grabTicks = 0;
				}
				continue;
			}
			if (!local) {
				if (layer.getAnimation() == null) {
					play(layer, IDLE_ID, 0);
				}
				continue;
			}
			boolean punch = MovementHandler.isPunching();
			boolean punchMirror = MovementHandler.isPunchMirrored();
			boolean grab = !punch && MovementHandler.isWallAnimActive();
			boolean mirrored = grab
					? MovementHandler.isWallSidePositive() != AUTHORED_SIDE_POSITIVE
					: localMirrored;
			if (layer.getAnimation() == null) {
				play(layer, punch ? punchAnimation(punchMirror)
						: grab ? grabAnimation(mirrored) : IDLE_ID, 0);
				grabTicks = grab ? GRAB_LENGTH_TICKS : 0;
				outTicks = 0;
			} else if (punch && !localPunch) {
				play(layer, punchAnimation(punchMirror), punchBlend(punchMirror, PUNCH_FADE_IN_TICKS));
				grabTicks = 0;
				outTicks = 0;
			} else if (!punch && localPunch) {
				play(layer, IDLE_ID, punchBlend(punchMirror, PUNCH_OUT_BLEND_TICKS));
				grabTicks = 0;
				outTicks = 0;
			} else if (grab && (!localWallGrab || mirrored != localMirrored)) {
				play(layer, grabAnimation(mirrored), FADE_IN_TICKS);
				grabTicks = GRAB_LENGTH_TICKS;
				outTicks = 0;
			} else if (grab && grabTicks > 0 && --grabTicks == 0) {
				play(layer, runAnimation(mirrored), 0);
			} else if (!grab && localWallGrab) {
				play(layer, outAnimation(mirrored), OUT_BLEND_TICKS);
				grabTicks = 0;
				outTicks = OUT_LENGTH_TICKS;
			} else if (!grab && outTicks > 0 && --outTicks == 0) {
				play(layer, IDLE_ID, 0);
			}
			localWallGrab = grab;
			localPunch = punch;
			localMirrored = mirrored;
		}
	}

	public static boolean isPlaying(Entity entity) {
		if (!(entity instanceof AbstractClientPlayer player)) {
			return false;
		}
		ModifierLayer<?> layer = layerOf(player);
		return layer != null && layer.getAnimation() != null && layer.getAnimation().isActive();
	}

	private static boolean isHolding(AbstractClientPlayer player) {
		return NonSteelArtsItem.isHolding(player);
	}

	@SuppressWarnings("unchecked")
	private static ModifierLayer<IAnimation> layerOf(AbstractClientPlayer player) {
		IAnimation animation = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER_ID);
		return animation instanceof ModifierLayer<?> ? (ModifierLayer<IAnimation>) animation : null;
	}

	private static ResourceLocation punchAnimation(boolean mirrored) {
		return mirrored ? PUNCH1_MIRRORED_ID : PUNCH1_ID;
	}

	private static int punchBlend(boolean mirrored, int unmirrored) {
		return mirrored ? PUNCH_MIRROR_BLEND_TICKS : unmirrored;
	}

	private static ResourceLocation grabAnimation(boolean mirrored) {
		return mirrored ? WALL_GRAB_MIRRORED_ID : WALL_GRAB_ID;
	}

	private static ResourceLocation runAnimation(boolean mirrored) {
		return mirrored ? WALL_RUN_MIRRORED_ID : WALL_RUN_ID;
	}

	private static ResourceLocation outAnimation(boolean mirrored) {
		return mirrored ? WALL_GRAB_OUT_MIRRORED_ID : WALL_GRAB_OUT_ID;
	}

	private static float smoothFade(String part, TransformType type, float progress) {
		return 0.5f * (progress + 3f * progress * progress
				- 2f * progress * progress * progress);
	}

	@SuppressWarnings("unchecked")
	private static void play(ModifierLayer<?> layer, ResourceLocation id, int fadeTicks) {
		IPlayable playable = PlayerAnimationRegistry.getAnimation(id);
		if (playable == null) {
			return;
		}
		IActualAnimation<?> animation = playable.playAnimation();
		animation.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
		animation.setFirstPersonConfiguration(new FirstPersonConfiguration()
				.setShowRightArm(true)
				.setShowLeftArm(true)
				.setShowRightItem(false)
				.setShowLeftItem(false));
		ModifierLayer<IAnimation> target = (ModifierLayer<IAnimation>) layer;
		if (fadeTicks > 0) {
			target.replaceAnimationWithFade(
					AbstractFadeModifier.functionalFadeIn(fadeTicks, ArmsAnimationHandler::smoothFade),
					animation);
		} else {
			target.setAnimation(animation);
		}
	}

	@SuppressWarnings("unchecked")
	private static void stop(ModifierLayer<?> layer) {
		((ModifierLayer<IAnimation>) layer).setAnimation(null);
	}

}
