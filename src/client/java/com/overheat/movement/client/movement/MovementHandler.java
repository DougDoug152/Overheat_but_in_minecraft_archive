package com.overheat.movement.client.movement;

import com.overheat.movement.ModSounds;
import com.overheat.movement.ability.Ability;
import com.overheat.movement.ability.PlayerAbilities;
import com.overheat.movement.client.sound.SlideSound;
import com.overheat.movement.client.sound.WallSlideSound;
import com.overheat.movement.content.NonSteelArtsItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class MovementHandler {

	private static final int DASH_DURATION = 3;
	private static final double DASH_LAUNCH = 0.8;
	private static final double DASH_MIN_LAUNCH = 0.45;
	private static final double DASH_DECAY = 0.94;
	private static final int DASH_MAX_CHARGES = 2;
	private static final int DASH_RECHARGE_SINGLE = 20;
	private static final int DASH_RECHARGE_DOUBLE = 60;
	private static final int DASH_FX_IN_TICKS = 1;
	private static final int DASH_FX_OUT_TICKS = 6;
	private static final int DASH_FX_TOTAL = DASH_FX_IN_TICKS + DASH_FX_OUT_TICKS;
	private static final int DASH_FX_MAX = 8;
	private static final double DASH_FOV_KICK = 10.0;
	private static final float DASH_FX_SCALE_MAX = 1.5f;
	private static final int DASH_HUD_FADE_TICKS = 20;

	private static final double WALL_REACH = 0.1;
	private static final double WALL_ANIM_REACH = 0.4;
	private static final double WALL_CORNER_REACH = 0.55;
	private static final double WALL_PROBE_MIN_Y = 0.6;
	private static final int WALL_CLIMB_TICKS = 5;
	private static final double WALL_RUN_CLIMB = 0.06;
	private static final double WALL_RUN_SLIDE = -0.05;
	private static final double WALL_RUN_MIN_SPEED = 0.34;
	private static final double WALL_RUN_NERF = 0.10;
	private static final double WALL_DIR_MOMENTUM_MIN = 0.1;
	private static final double WALL_STICK = 0.05;
	private static final int WALL_REATTACH_COOLDOWN = 6;
	private static final double LOOK_AWAY_DOT = -Math.sin(Math.toRadians(55.0));
	private static final double LOOK_INTO_DOT = Math.cos(Math.toRadians(25.0));
	private static final double ATTACH_DOT = -0.25;

	private static final double WALL_JUMP_SPEED = 0.465;
	private static final double WALL_AWAY_WEIGHT = 1.0;
	private static final double WALL_CAM_WEIGHT = 0.8;
	private static final double WALL_UP_BIAS = 0.7;
	private static final double WALL_JUMP_MIN_UP = 0.42;

	private static final int MAX_AIR_JUMPS = 1;
	private static final double AIR_JUMP_POWER = 0.5;
	private static final int AIR_JUMP_RECHARGE_TICKS = 110;
	private static final double AIR_JUMP_GRAVITY_MULT = 0.95;

	private static final int SLIDE_DURATION = 61;
	private static final double SLIDE_MIN_LAUNCH = 0.55;
	private static final double SLIDE_LAUNCH_BOOST = 0.12;
	private static final double SLIDE_FRICTION = 0.99366;
	private static final double SLIDE_TURN_LERP = 0.4;
	private static final double SLIDE_TURN_MAX_LAT = 0.32;
	private static final double SLIDE_STACK_BOOST = 0.15;
	private static final int SLIDE_STACK_EASE_TICKS = 10;
	private static final int SLIDE_STACK_WINDOW = 5;
	private static final int SLIDE_QUEUE_TICKS = 20;
	private static final int SLIDE_MIN_TICKS = 4;
	private static final int SLIDE_COOLDOWN_TICKS = 5;
	private static final int SLIDE_JUMP_GRACE = 4;
	private static final double SLIDE_GROUND_BUFFER = 0.4;
	private static final double SLIDE_FOV_KICK = 12.0;
	private static final float SLIDE_FOV_LERP = 0.25f;
	private static final float SLIDE_TILT_DEG = 4.6f;
	private static final double SLIDE_TILT_GAIN = 3.0;
	private static final double SLIDE_TILT_TURN_GAIN = 0.15;

	private static final int WALL_MIN_HEIGHT = 2;

	private static final int PUNCH_DURATION = 21;
	private static final float PUNCH_PITCH_VARIANCE = 0.06f;

	private static final float WALL_TILT_DEG = 7.0f;
	private static final float TILT_LERP = 0.35f;
	private static final float SLIDE_TILT_LERP = 0.15f;
	private static final float WALL_ANCHOR_LERP = 0.2f;
	private static final float WALL_ANCHOR_PITCH = 0f;
	private static final float ARM_STICK_DEADZONE = 12.0f;
	private static final float ARM_STICK_RELEASE = 0.7f;
	private static final float CAM_PUNCH_STIFFNESS = 0.4f;
	private static final float CAM_PUNCH_DAMPING = 0.5f;
	private static final float AIR_JUMP_PITCH_PUNCH = 6.0f;
	private static final float WALL_ATTACH_PITCH_PUNCH = 5.0f;
	private static final float WALL_ATTACH_ROLL_PUNCH = 7.0f;

	private static final float HUD_SWAY_ACCEL_GAIN = 4.913f;
	private static final float HUD_SWAY_FORWARD_GAIN = 2.4565f;
	private static final float HUD_SWAY_VERT_DEADZONE = 0.15f;
	private static final float HUD_SWAY_STIFFNESS = 0.16f;
	private static final float HUD_SWAY_DAMPING = 0.30f;
	private static final float HUD_SWAY_MAX = 12.0f;
	private static final float HUD_SWAY_KNEE = 9.0f;
	private static final float HUD_SWAY_LIMIT = HUD_SWAY_MAX * 4f;
	private static final float HUD_SWAY_IMPULSE_MAX = 3.685f;

	private static final double AIR_WISH_SPEED = 0.05;
	private static final double AIR_ACCEL = 0.045;
	private static final double AIR_DRAG_COMP = 1.0989;
	private static final double BHOP_IDLE_RETENTION = 0.95;
	private static final double BHOP_MIN_SPEED = 0.36;
	private static final double MAX_HORIZ_SPEED = 1.8;

	private final KeyMapping dashKey;
	private final KeyMapping slideKey;
	private final KeyMapping slideAltKey;

	private int dashCharges = DASH_MAX_CHARGES;
	private int dashRecharge = 0;
	private int dashRechargeTotal = 0;
	private int dashTicks = 0;
	private double dashSpeed = 0;
	private Vec3 dashDir = Vec3.ZERO;

	private int airTicks = 0;
	private int airJumpsUsed = 0;
	private int airJumpRecharge = 0;
	private boolean airJumpGravity = false;
	private Vec3 lastAirVel = Vec3.ZERO;

	private boolean wallRunning = false;
	private WallSlideSound wallSlideSound = null;
	private int wallRunTicks = 0;
	private int wallCooldown = 0;
	private double wallRunSpeed = 0;
	private Vec3 wallRunDir = Vec3.ZERO;
	private Direction wallDir = null;

	private int slideTicks = 0;
	private int slideAirTicks = 0;
	private int slideQueued = 0;
	private int slideCooldown = 0;
	private int groundTicks = 0;
	private double slideSpeed = 0;
	private double slideBoostLeft = 0;
	private Vec3 slideDir = Vec3.ZERO;
	private float slideHeading = 0f;
	private float slideTilt = 0f;
	private boolean slideRollEase = false;
	private SlideSound slideSound = null;

	private int punchTicks = 0;
	private boolean nextPunchMirrored = false;
	private boolean prevAttackHeld = false;
	private boolean prevJumpHeld = false;
	private boolean prevMoveInput = false;
	private boolean prevGrounded = true;
	private boolean wallJumpedThisTick = false;
	private int prevAbilityMask = 0;

	private static volatile float viewRoll = 0f;
	private static volatile float prevViewRoll = 0f;
	private static volatile float rollPunch = 0f;
	private static volatile float prevRollPunch = 0f;
	private static float rollPunchVel = 0f;
	private static volatile float pitchPunch = 0f;
	private static volatile float prevPitchPunch = 0f;
	private static float pitchPunchVel = 0f;
	private static volatile float hudSwayX = 0f;
	private static volatile float hudSwayY = 0f;
	private static volatile float prevHudSwayX = 0f;
	private static volatile float prevHudSwayY = 0f;
	private static float hudSwayVelX = 0f;
	private static float hudSwayVelY = 0f;
	private static Vec3 swayLastVel = Vec3.ZERO;
	private static boolean swayPrimed = false;
	private static final int[] dashFxAge = new int[DASH_FX_MAX];
	private static volatile int dashFxCount = 0;
	private static volatile int hudDashCharges = DASH_MAX_CHARGES;
	private static volatile int hudDashRecharge = 0;
	private static volatile int hudDashRechargeTotal = 0;
	private static volatile float dashHudFade = 0f;
	private static volatile float prevDashHudFade = 0f;
	private static volatile boolean wallRunActive = false;
	private static volatile boolean wallAnimActive = false;
	private static volatile boolean wallSidePositive = false;
	private static volatile boolean punchActive = false;
	private static volatile boolean punchMirrored = false;
	private static volatile float wallAnchorYaw = 0f;
	private static volatile float wallAnchorBlend = 0f;
	private static volatile float prevWallAnchorBlend = 0f;
	private static volatile boolean airFrameActive = false;
	private static volatile float airFrameYaw = 0f;
	private static volatile boolean slideActive = false;
	private static volatile float slideFov = 0f;
	private static volatile float prevSlideFov = 0f;
	private static volatile float armStickYawOff = 0f;
	private static volatile float armStickPitchOff = 0f;
	private static volatile float prevArmStickYawOff = 0f;
	private static volatile float prevArmStickPitchOff = 0f;
	private static float armStickLastYaw = 0f;
	private static float armStickLastPitch = 0f;
	private static boolean armStickPrimed = false;

	public static float getWallAnchorYaw() {
		return wallAnchorYaw;
	}

	public static float getWallAnchorPitch() {
		return WALL_ANCHOR_PITCH;
	}

	public static float getArmStickYawOffset() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return prevArmStickYawOff + (armStickYawOff - prevArmStickYawOff) * partial;
	}

	public static float getArmStickPitchOffset() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return prevArmStickPitchOff + (armStickPitchOff - prevArmStickPitchOff) * partial;
	}

	public static float getWallAnchorBlend() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return prevWallAnchorBlend + (wallAnchorBlend - prevWallAnchorBlend) * partial;
	}

	public static boolean isWallRunning() {
		return wallRunActive;
	}

	public static boolean isPunching() {
		return punchActive;
	}

	public static boolean isPunchMirrored() {
		return punchMirrored;
	}

	public static boolean isWallAnimActive() {
		return wallAnimActive;
	}

	public static boolean isWallSidePositive() {
		return wallSidePositive;
	}

	public static int getDashCharges() {
		return hudDashCharges;
	}

	public static float getDashHudAlpha() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return prevDashHudFade + (dashHudFade - prevDashHudFade) * partial;
	}

	public static float getDashRechargeProgress() {
		if (hudDashRecharge <= 0 || hudDashRechargeTotal <= 0) {
			return 1f;
		}
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		float elapsed = hudDashRechargeTotal - hudDashRecharge + partial;
		return Math.max(0f, Math.min(1f, elapsed / hudDashRechargeTotal));
	}

	public static float getViewRoll() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		float tilt = prevViewRoll + (viewRoll - prevViewRoll) * partial;
		float punch = prevRollPunch + (rollPunch - prevRollPunch) * partial;
		return tilt + punch;
	}

	public static float getViewPitchPunch() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return prevPitchPunch + (pitchPunch - prevPitchPunch) * partial;
	}

	public static float getHudSwayX() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return softLimit(prevHudSwayX + (hudSwayX - prevHudSwayX) * partial);
	}

	public static float getHudSwayY() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return softLimit(prevHudSwayY + (hudSwayY - prevHudSwayY) * partial);
	}

	private static float softLimit(float v) {
		float a = Math.abs(v);
		if (a <= HUD_SWAY_KNEE) {
			return v;
		}
		float span = HUD_SWAY_MAX - HUD_SWAY_KNEE;
		float eased = HUD_SWAY_KNEE + span * (float) Math.tanh((a - HUD_SWAY_KNEE) / span);
		return v < 0f ? -eased : eased;
	}

	private static float deadzone(float v, float dead) {
		if (Math.abs(v) <= dead) {
			return 0f;
		}
		return v - Math.signum(v) * dead;
	}

	private static void tickHudSway(LocalPlayer p) {
		float yaw = p.getYRot();
		Vec3 vel = p.getDeltaMovement();
		if (!swayPrimed) {
			swayPrimed = true;
			swayLastVel = vel;
		}

		Vec3 accel = vel.subtract(swayLastVel);
		swayLastVel = vel;
		float yawRad = yaw * Mth.DEG_TO_RAD;
		float sin = Mth.sin(yawRad);
		float cos = Mth.cos(yawRad);
		float accelRight = (float) (accel.x * -cos + accel.z * -sin);
		float accelFwd = (float) (accel.x * -sin + accel.z * cos);
		float accelUp = deadzone((float) accel.y, HUD_SWAY_VERT_DEADZONE);

		float pushX = -accelRight * HUD_SWAY_ACCEL_GAIN;
		float pushY = accelUp * HUD_SWAY_ACCEL_GAIN + accelFwd * HUD_SWAY_FORWARD_GAIN;
		hudSwayVelX += Mth.clamp(pushX, -HUD_SWAY_IMPULSE_MAX, HUD_SWAY_IMPULSE_MAX);
		hudSwayVelY += Mth.clamp(pushY, -HUD_SWAY_IMPULSE_MAX, HUD_SWAY_IMPULSE_MAX);

		prevHudSwayX = hudSwayX;
		hudSwayVelX += -HUD_SWAY_STIFFNESS * hudSwayX - HUD_SWAY_DAMPING * hudSwayVelX;
		hudSwayX = Mth.clamp(hudSwayX + hudSwayVelX, -HUD_SWAY_LIMIT, HUD_SWAY_LIMIT);

		prevHudSwayY = hudSwayY;
		hudSwayVelY += -HUD_SWAY_STIFFNESS * hudSwayY - HUD_SWAY_DAMPING * hudSwayVelY;
		hudSwayY = Mth.clamp(hudSwayY + hudSwayVelY, -HUD_SWAY_LIMIT, HUD_SWAY_LIMIT);
	}

	private static void tickCameraPunch() {
		prevRollPunch = rollPunch;
		rollPunchVel += -CAM_PUNCH_STIFFNESS * rollPunch - CAM_PUNCH_DAMPING * rollPunchVel;
		rollPunch += rollPunchVel;

		prevPitchPunch = pitchPunch;
		pitchPunchVel += -CAM_PUNCH_STIFFNESS * pitchPunch - CAM_PUNCH_DAMPING * pitchPunchVel;
		pitchPunch += pitchPunchVel;
	}

	private static void addPitchPunch(float impulse) {
		pitchPunchVel += impulse;
	}

	private static void addRollPunch(float impulse) {
		rollPunchVel += impulse;
	}

	public static boolean hasAirFrame() {
		return airFrameActive;
	}

	public static float getAirFrameYaw() {
		return airFrameYaw;
	}

	private static void snapAirFrame(LocalPlayer p) {
		airFrameYaw = p.getYRot();
		airFrameActive = true;
	}

	public static boolean isSliding() {
		return slideActive;
	}

	public static float getSlideFovOffset() {
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		return (prevSlideFov + (slideFov - prevSlideFov) * partial) * (float) SLIDE_FOV_KICK;
	}

	private static void tickSlideFov() {
		prevSlideFov = slideFov;
		slideFov += ((slideActive ? 1f : 0f) - slideFov) * SLIDE_FOV_LERP;
	}

	public static float getDashFovOffset() {
		return Math.min(dashFxSum(), 1f) * (float) DASH_FOV_KICK;
	}

	public static float getDashOverlayAlpha() {
		return Math.min(dashFxSum(), 1f);
	}

	public static float getDashOverlayScale() {
		return 1f + (DASH_FX_SCALE_MAX - 1f) * dashFxOutProgress();
	}

	private static synchronized float dashFxOutProgress() {
		if (dashFxCount == 0) {
			return 0f;
		}
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		float youngest = Float.MAX_VALUE;
		for (int i = 0; i < dashFxCount; i++) {
			float elapsed = dashFxAge[i] + partial;
			if (elapsed < youngest) {
				youngest = elapsed;
			}
		}
		if (youngest <= DASH_FX_IN_TICKS) {
			return 0f;
		}
		float out = (youngest - DASH_FX_IN_TICKS) / DASH_FX_OUT_TICKS;
		return smoothstep01(Math.min(out, 1f));
	}

	private static synchronized float dashFxSum() {
		if (dashFxCount == 0) {
			return 0f;
		}
		float partial = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		float sum = 0f;
		for (int i = 0; i < dashFxCount; i++) {
			sum += dashEnvelope(dashFxAge[i] + partial);
		}
		return sum;
	}

	private static synchronized void addDashFx() {
		if (dashFxCount < DASH_FX_MAX) {
			dashFxAge[dashFxCount++] = 0;
		} else {
			System.arraycopy(dashFxAge, 1, dashFxAge, 0, DASH_FX_MAX - 1);
			dashFxAge[DASH_FX_MAX - 1] = 0;
		}
	}

	private static synchronized void tickDashFx() {
		int w = 0;
		for (int i = 0; i < dashFxCount; i++) {
			int age = dashFxAge[i] + 1;
			if (age <= DASH_FX_TOTAL) {
				dashFxAge[w++] = age;
			}
		}
		dashFxCount = w;
	}

	private static float dashEnvelope(float elapsed) {
		if (elapsed < 0f) {
			return 0f;
		}
		if (elapsed <= DASH_FX_IN_TICKS) {
			return smoothstep01(elapsed / DASH_FX_IN_TICKS);
		}
		float out = (elapsed - DASH_FX_IN_TICKS) / DASH_FX_OUT_TICKS;
		return out >= 1f ? 0f : 1f - smoothstep01(out);
	}

	private static float smoothstep01(float t) {
		t = Math.max(0f, Math.min(1f, t));
		return t * t * (3f - 2f * t);
	}

	public MovementHandler(KeyMapping dashKey, KeyMapping slideKey, KeyMapping slideAltKey) {
		this.dashKey = dashKey;
		this.slideKey = slideKey;
		this.slideAltKey = slideAltKey;
	}

	public void register() {
		ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
	}

	private void onEndTick(Minecraft mc) {
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null) {
			if (wallSlideSound != null) {
				mc.getSoundManager().stop(wallSlideSound);
				wallSlideSound = null;
			}
			if (slideSound != null) {
				mc.getSoundManager().stop(slideSound);
				slideSound = null;
			}
			slideTicks = 0;
			slideActive = false;
			return;
		}

		int abilityMask = PlayerAbilities.mask(p);
		int gainedAbilities = abilityMask & ~prevAbilityMask;
		prevAbilityMask = abilityMask;

		prevDashHudFade = dashHudFade;
		float fadeStep = 1f / DASH_HUD_FADE_TICKS;
		if ((abilityMask & Ability.DASH.bit()) != 0) {
			dashHudFade = Math.min(1f, dashHudFade + fadeStep);
		} else {
			dashHudFade = Math.max(0f, dashHudFade - fadeStep);
		}

		if ((gainedAbilities & Ability.DASH.bit()) != 0) {
			dashCharges = DASH_MAX_CHARGES;
			dashRecharge = 0;
			dashRechargeTotal = 0;
		}
		if ((gainedAbilities & Ability.AIR_JUMP.bit()) != 0) {
			airJumpsUsed = 0;
			airJumpRecharge = 0;
		}
		if ((gainedAbilities & Ability.WALL_RUN.bit()) != 0) {
			wallCooldown = 0;
		}
		if ((abilityMask & Ability.COMBAT.bit()) == 0) {
			punchTicks = 0;
			punchActive = false;
		}

		if (dashRecharge > 0) {
			dashRecharge--;
			if (dashRecharge == 0) {
				dashCharges = DASH_MAX_CHARGES;
				playSound(p, ModSounds.RECHARGE);
			}
		}
		if (wallCooldown > 0) {
			wallCooldown--;
		}
		if (slideCooldown > 0) {
			slideCooldown--;
		}
		if (slideQueued > 0) {
			slideQueued--;
		}
		tickDashFx();
		tickCameraPunch();
		tickSlideFov();
		tickHudSway(p);
		updateWallSlideSound(p);
		if (airJumpRecharge > 0) {
			airJumpRecharge--;
			if (airJumpRecharge == 0 && airJumpsUsed > 0) {
				airJumpsUsed = 0;
				playSound(p, ModSounds.AIR_JUMP_RECHARGE);
			}
		}

		hudDashCharges = dashCharges;
		hudDashRecharge = dashRecharge;
		hudDashRechargeTotal = dashRechargeTotal;

		boolean dashPressed = false;
		while (dashKey.consumeClick()) {
			dashPressed = true;
		}
		boolean slidePressed = false;
		while (slideKey.consumeClick()) {
			slidePressed = true;
		}
		while (slideAltKey.consumeClick()) {
			slidePressed = true;
		}
		boolean slideHeld = slideKey.isDown() || slideAltKey.isDown();

		boolean active = mc.screen == null
				&& p.isAlive()
				&& !p.isSpectator()
				&& !p.getAbilities().flying
				&& !p.isPassenger()
				&& !p.onClimbable()
				&& !p.isInWater()
				&& !p.isInLava();

		if (!active) {
			if (wallRunning) {
				endWall(p, false, 0);
			}
			if (slideActive) {
				endSlide(p);
			}
			dashTicks = 0;
			airFrameActive = false;
			airJumpGravity = false;
			prevJumpHeld = false;
			slideRollEase = false;
			prevViewRoll = viewRoll;
			viewRoll += (0f - viewRoll) * TILT_LERP;
			wallAnimActive = false;
			updateWallAnchor(p);
			return;
		}

		Options o = mc.options;
		ClientInput in = p.input;
		boolean grounded = p.onGround();
		boolean slideGround = grounded || nearGround(p, SLIDE_GROUND_BUFFER);
		boolean wasGrounded = prevGrounded;
		prevGrounded = grounded;
		boolean jumpHeld = o.keyJump.isDown();
		boolean jumpPressed = jumpHeld && !prevJumpHeld;
		prevJumpHeld = jumpHeld;
		boolean sneaking = o.keyShift.isDown();
		boolean attackHeld = o.keyAttack.isDown();
		boolean attackPressed = attackHeld && !prevAttackHeld;
		prevAttackHeld = attackHeld;

		boolean moveInput = in.leftImpulse != 0.0f || in.forwardImpulse != 0.0f;
		boolean movePressed = moveInput && !prevMoveInput;
		prevMoveInput = moveInput;

		if (grounded) {
			airFrameActive = false;
		} else if (!airFrameActive || movePressed) {
			snapAirFrame(p);
		}

		if (punchTicks > 0) {
			punchTicks--;
		}
		if (attackPressed && punchTicks == 0 && holdingArts(p) && has(p, Ability.COMBAT)) {
			startPunch(p);
		}
		punchActive = punchTicks > 0;

		Vec3 vel = p.getDeltaMovement();
		double horizSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		Vec3 wish = wishDir(p.getYRot(), in.leftImpulse, in.forwardImpulse);

		if (grounded) {
			if (airJumpsUsed > 0) {
				playSound(p, ModSounds.AIR_JUMP_RECHARGE);
			}
			airJumpsUsed = 0;
			airJumpRecharge = 0;
			airJumpGravity = false;
			groundTicks = Math.min(groundTicks + 1, SLIDE_STACK_WINDOW + 1);
		} else {
			groundTicks = 0;
		}

		handleSlideInput(p, slidePressed, slideGround);

		if (dashPressed && has(p, Ability.DASH)) {
			startDash(p);
		}

		if (dashTicks > 0) {
			p.setDeltaMovement(dashDir.x * dashSpeed, dashDir.y * dashSpeed, dashDir.z * dashSpeed);
			p.resetFallDistance();
			dashSpeed *= DASH_DECAY;
			dashTicks--;
			if (wallRunning) {
				endWall(p, false, 0);
			}
			Vec3 cur = p.getDeltaMovement();
			lastAirVel = new Vec3(cur.x, 0, cur.z);
			wallAnimActive = false;
			updateWallAnchor(p);
			clampHorizontal(p);
			return;
		}

		if (slideTicks > 0 && tickSlide(p, jumpPressed, slideHeld, slideGround)) {
			wallAnimActive = false;
			updateWallAnchor(p);
			updateTilt(p, false);
			clampHorizontal(p);
			return;
		}

		wallJumpedThisTick = false;
		boolean wallRunningNow = handleWall(p, wish, jumpPressed, sneaking, grounded, horizSpeed);

		if (!wallRunningNow) {
			if (grounded) {
				if (airTicks > 0 && jumpHeld) {
					double keepSpeed = Math.sqrt(lastAirVel.x * lastAirVel.x + lastAirVel.z * lastAirVel.z);
					if (keepSpeed > BHOP_MIN_SPEED && has(p, Ability.BHOP)) {
						bhop(p, wish);
					}
				}
				airTicks = 0;
			} else {
				airTicks++;
				if (airJumpGravity) {
					Vec3 gv = p.getDeltaMovement();
					p.setDeltaMovement(gv.x,
							gv.y + (1.0 - AIR_JUMP_GRAVITY_MULT) * effectiveGravity(p),
							gv.z);
				}
				Vec3 av = p.getDeltaMovement();
				double airSpeed = Math.sqrt(av.x * av.x + av.z * av.z);
				if (airSpeed > BHOP_MIN_SPEED && has(p, Ability.BHOP)) {
					airAccelerate(p, airFrameActive
							? wishDir(airFrameYaw, in.leftImpulse, in.forwardImpulse)
							: wish);
					cancelAirDrag(p);
				}
				if (jumpPressed && !wasGrounded && !wallJumpedThisTick && airJumpsUsed < MAX_AIR_JUMPS && has(p, Ability.AIR_JUMP)) {
					Vec3 cur = p.getDeltaMovement();
					p.setDeltaMovement(cur.x, AIR_JUMP_POWER, cur.z);
					p.resetFallDistance();
					airJumpsUsed++;
					airJumpRecharge = AIR_JUMP_RECHARGE_TICKS;
					airJumpGravity = true;
					snapAirFrame(p);
					addPitchPunch(AIR_JUMP_PITCH_PUNCH);
					playSound(p, ModSounds.AIR_JUMP);
				}
				Vec3 cur = p.getDeltaMovement();
				lastAirVel = new Vec3(cur.x, 0, cur.z);
			}
		} else {
			airTicks = 0;
		}

		wallAnimActive = punchTicks == 0
				&& (wallRunningNow || wallAnimNearby(p, wish, grounded, sneaking, horizSpeed));
		updateWallAnchor(p);
		updateTilt(p, wallRunningNow);
		clampHorizontal(p);
	}

	private boolean wallAnimNearby(LocalPlayer p, Vec3 wish, boolean grounded, boolean sneaking,
			double horizSpeed) {
		if (grounded || sneaking || wallCooldown > 0 || !has(p, Ability.WALL_RUN)) {
			return false;
		}
		if (wish.lengthSqr() <= 1.0e-6 && horizSpeed <= 0.12) {
			return false;
		}
		Vec3 aim = wish.lengthSqr() > 1.0e-6 ? wish : horizLook(p);
		Direction d = findWall(p, aim, WALL_ANIM_REACH);
		if (d == null) {
			return false;
		}
		Vec3 look = horizLook(p);
		if (look.x * d.getStepX() + look.z * d.getStepZ() > LOOK_INTO_DOT) {
			return false;
		}
		wallSidePositive = look.x * d.getStepZ() - look.z * d.getStepX() >= 0;
		return true;
	}

	private void updateWallAnchor(LocalPlayer p) {
		float target = wallRunning ? 1f : 0f;
		prevWallAnchorBlend = wallAnchorBlend;
		wallAnchorBlend += (target - wallAnchorBlend) * WALL_ANCHOR_LERP;
		updateArmStick(p);
	}

	private void updateArmStick(LocalPlayer p) {
		float yaw = p.getYRot();
		float pitch = p.getXRot();
		if (!armStickPrimed) {
			armStickPrimed = true;
			armStickLastYaw = yaw;
			armStickLastPitch = pitch;
		}
		float dYaw = Mth.wrapDegrees(yaw - armStickLastYaw);
		float dPitch = pitch - armStickLastPitch;
		armStickLastYaw = yaw;
		armStickLastPitch = pitch;

		prevArmStickYawOff = armStickYawOff;
		prevArmStickPitchOff = armStickPitchOff;
		if (wallAnimActive) {
			armStickYawOff = Mth.clamp(armStickYawOff - dYaw,
					-ARM_STICK_DEADZONE, ARM_STICK_DEADZONE);
			armStickPitchOff = Mth.clamp(armStickPitchOff - dPitch,
					-ARM_STICK_DEADZONE, ARM_STICK_DEADZONE);
		} else {
			armStickYawOff *= ARM_STICK_RELEASE;
			armStickPitchOff *= ARM_STICK_RELEASE;
		}
	}

	private void updateTilt(LocalPlayer p, boolean wallRunningNow) {
		float target = 0f;
		float lerp = TILT_LERP;
		if (wallRunningNow && wallDir != null) {
			Vec3 look = horizLook(p);
			double side = look.x * wallDir.getStepZ() - look.z * wallDir.getStepX();
			wallSidePositive = side >= 0;
			target = side >= 0 ? WALL_TILT_DEG : -WALL_TILT_DEG;
			slideRollEase = false;
		} else if (slideActive) {
			target = slideTilt;
			lerp = SLIDE_TILT_LERP;
			slideRollEase = true;
		} else if (slideRollEase) {
			lerp = SLIDE_TILT_LERP;
			if (Math.abs(viewRoll) < 0.01f) {
				slideRollEase = false;
			}
		}
		prevViewRoll = viewRoll;
		viewRoll += (target - viewRoll) * lerp;
	}

	private void startDash(LocalPlayer p) {
		if (dashTicks > 0 || dashCharges <= 0) {
			return;
		}
		if (slideActive) {
			endSlide(p);
		}
		ClientInput in = p.input;
		Vec3 move = wishDir(p.getYRot(), in.leftImpulse, in.forwardImpulse);
		Vec3 dir;
		if (move.lengthSqr() > 1.0e-6) {
			boolean holdingBack = in.forwardImpulse < 0f;
			boolean pureForward = in.forwardImpulse > 0f && in.leftImpulse == 0f;
			if (holdingBack) {
				double pitchRad = p.getXRot() * (Math.PI / 180.0);
				dir = new Vec3(move.x * Math.cos(pitchRad),
						Math.sin(pitchRad),
						move.z * Math.cos(pitchRad));
			} else if (pureForward && p.getXRot() <= 0f) {
				dir = p.getLookAngle();
			} else {
				dir = move;
			}
		} else {
			dir = p.getXRot() > 0f ? horizLook(p) : p.getLookAngle();
		}
		double len = dir.length();
		dashDir = len > 1.0e-6 ? dir.scale(1.0 / len) : new Vec3(0, 0, 1);
		Vec3 vel = p.getDeltaMovement();
		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		double carried = speed + DASH_LAUNCH;
		dashSpeed = Math.max(carried, DASH_MIN_LAUNCH);
		dashTicks = DASH_DURATION;
		snapAirFrame(p);
		addDashFx();

		dashCharges--;
		if (dashCharges == 0) {
			dashRecharge = DASH_RECHARGE_DOUBLE;
			dashRechargeTotal = DASH_RECHARGE_DOUBLE;
		} else if (dashRecharge == 0) {
			dashRecharge = DASH_RECHARGE_SINGLE;
			dashRechargeTotal = DASH_RECHARGE_SINGLE;
		}
		playSound(p, ModSounds.DASH);
	}

	private static boolean has(LocalPlayer p, Ability ability) {
		return PlayerAbilities.has(p, ability);
	}

	private static boolean nearGround(LocalPlayer p, double buffer) {
		if (p.getDeltaMovement().y > 0.0) {
			return false;
		}
		AABB box = p.getBoundingBox();
		AABB probe = new AABB(box.minX, box.minY - buffer, box.minZ, box.maxX, box.minY, box.maxZ)
				.deflate(1.0e-3, 0.0, 1.0e-3);
		return !p.level().noCollision(p, probe);
	}

	private void handleSlideInput(LocalPlayer p, boolean slidePressed, boolean grounded) {
		if (!has(p, Ability.SLIDE)) {
			if (slideActive) {
				endSlide(p);
			}
			slideQueued = 0;
			return;
		}
		if (slidePressed && slideTicks == 0) {
			if (grounded) {
				if (slideCooldown == 0) {
					startSlide(p, groundTicks <= SLIDE_STACK_WINDOW);
				}
			} else {
				slideQueued = SLIDE_QUEUE_TICKS;
			}
		}
		if (slideTicks == 0 && slideQueued > 0 && grounded && slideCooldown == 0) {
			slideQueued = 0;
			startSlide(p, true);
		}
	}

	private void startSlide(LocalPlayer p, boolean landingStack) {
		Vec3 vel = p.getDeltaMovement();
		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		slideDir = slideTarget(p, slideWish(p));
		slideHeading = headingOf(slideDir);
		slideTilt = 0f;
		slideSpeed = Math.min(Math.max(speed + SLIDE_LAUNCH_BOOST, SLIDE_MIN_LAUNCH), MAX_HORIZ_SPEED);
		slideBoostLeft = landingStack ? SLIDE_STACK_BOOST : 0.0;
		slideTicks = SLIDE_DURATION;
		slideAirTicks = 0;
		slideActive = true;
		if (wallRunning) {
			endWall(p, false, 0);
		}
		playSound(p, ModSounds.SLIDE_START);
		startSlideSound(p);
	}

	private static Vec3 slideWish(LocalPlayer p) {
		ClientInput in = p.input;
		return wishDir(p.getYRot(), in.leftImpulse, in.forwardImpulse);
	}

	private static Vec3 slideTarget(LocalPlayer p, Vec3 wish) {
		return wish.lengthSqr() > 1.0e-6 ? wish : horizLook(p);
	}

	private static float headingOf(Vec3 dir) {
		return (float) (Math.atan2(-dir.x, dir.z) * (180.0 / Math.PI));
	}

	private static Vec3 limitTurn(Vec3 from, Vec3 to, double speed) {
		double cos = from.x * to.x + from.z * to.z;
		double sin = from.x * to.z - from.z * to.x;
		double angle = Math.atan2(sin, cos);
		double max = SLIDE_TURN_MAX_LAT / Math.max(speed, 1.0e-4);
		if (Math.abs(angle) <= max) {
			return to;
		}
		double a = Math.copySign(max, angle);
		double c = Math.cos(a);
		double s = Math.sin(a);
		return new Vec3(from.x * c - from.z * s, 0, from.x * s + from.z * c);
	}

	private void updateSlideTilt(Vec3 wish, Vec3 look) {
		float heading = headingOf(slideDir);
		double turn = -Mth.wrapDegrees(heading - slideHeading);
		slideHeading = heading;
		double strafe = wish.lengthSqr() > 1.0e-6 ? look.z * wish.x - look.x * wish.z : 0.0;
		slideTilt = (float) (Math.tanh(strafe * SLIDE_TILT_GAIN + turn * SLIDE_TILT_TURN_GAIN)
				* SLIDE_TILT_DEG);
	}

	private boolean tickSlide(LocalPlayer p, boolean jumpPressed, boolean slideHeld, boolean grounded) {
		if (jumpPressed) {
			if (slideAirTicks <= SLIDE_JUMP_GRACE) {
				slideJump(p);
				return true;
			}
			endSlide(p);
			return false;
		}
		if (!slideHeld && SLIDE_DURATION - slideTicks >= SLIDE_MIN_TICKS) {
			endSlide(p);
			return true;
		}
		if (grounded) {
			slideAirTicks = 0;
		} else if (slideAirTicks <= SLIDE_JUMP_GRACE) {
			slideAirTicks++;
		}
		slideTicks--;
		Vec3 wish = slideWish(p);
		Vec3 look = horizLook(p);
		Vec3 target = slideTarget(p, wish);
		double tx = slideDir.x + (target.x - slideDir.x) * SLIDE_TURN_LERP;
		double tz = slideDir.z + (target.z - slideDir.z) * SLIDE_TURN_LERP;
		double tlen = Math.sqrt(tx * tx + tz * tz);
		Vec3 next = tlen > 1.0e-6 ? new Vec3(tx / tlen, 0, tz / tlen) : target;
		slideDir = limitTurn(slideDir, next, slideSpeed);
		updateSlideTilt(wish, look);
		slideSpeed *= SLIDE_FRICTION;
		if (slideBoostLeft > 0) {
			double step = Math.min(slideBoostLeft, SLIDE_STACK_BOOST / SLIDE_STACK_EASE_TICKS);
			slideSpeed += step;
			slideBoostLeft -= step;
		}
		slideSpeed = Math.min(slideSpeed, MAX_HORIZ_SPEED);
		Vec3 vel = p.getDeltaMovement();
		p.setDeltaMovement(slideDir.x * slideSpeed, vel.y, slideDir.z * slideSpeed);
		if (slideTicks == 0) {
			if (wish.lengthSqr() > 1.0e-6) {
				slideTicks = 1;
				if (slideSound == null || !slideSound.isLooping()) {
					slideSound = new SlideSound(p, true);
					Minecraft.getInstance().getSoundManager().play(slideSound);
				}
			} else {
				endSlide(p);
			}
		}
		return true;
	}

	private void slideJump(LocalPlayer p) {
		double speed = slideSpeed;
		Vec3 look = horizLook(p);
		endSlide(p);
		p.jumpFromGround();
		double x = look.x * speed;
		double z = look.z * speed;
		p.setDeltaMovement(x, p.getDeltaMovement().y, z);
		p.resetFallDistance();
		lastAirVel = new Vec3(x, 0, z);
		snapAirFrame(p);
	}

	private void endSlide(LocalPlayer p) {
		if (!slideActive) {
			return;
		}
		slideTicks = 0;
		slideAirTicks = 0;
		slideBoostLeft = 0;
		slideActive = false;
		slideCooldown = SLIDE_COOLDOWN_TICKS;
		playSound(p, ModSounds.SLIDE_END);
		stopSlideSound();
	}

	private void startSlideSound(LocalPlayer p) {
		stopSlideSound();
		slideSound = new SlideSound(p, false);
		Minecraft.getInstance().getSoundManager().play(slideSound);
	}

	private void stopSlideSound() {
		if (slideSound != null) {
			slideSound.fadeOut();
			slideSound = null;
		}
	}

	private boolean handleWall(LocalPlayer p, Vec3 wish, boolean jumpPressed, boolean sneaking,
			boolean grounded, double horizSpeed) {
		if (!has(p, Ability.WALL_RUN)) {
			if (wallRunning) {
				endWall(p, false, 0);
			}
			return false;
		}
		if (wallRunning) {
			tryCornerTransition(p);
			boolean valid = wallPresent(p, wallDir);
			Vec3 look = horizLook(p);
			double facing = look.x * wallDir.getStepX() + look.z * wallDir.getStepZ();
			boolean lookingAway = facing < LOOK_AWAY_DOT;

			if (jumpPressed) {
				endWall(p, true, 1.0);
				wallCooldown = WALL_REATTACH_COOLDOWN;
				wallJumpedThisTick = true;
				snapAirFrame(p);
				return false;
			}
			if (grounded || sneaking || !valid || lookingAway) {
				endWall(p, false, 0);
				return false;
			}
			wallRunTicks++;
			applyWallMotion(p);
			return true;
		}

		if (grounded || sneaking || wallCooldown > 0 || punchTicks > 0) {
			return false;
		}
		boolean wantsWall = wish.lengthSqr() > 1.0e-6 || horizSpeed > 0.12;
		if (!wantsWall) {
			return false;
		}
		Vec3 aim = wish.lengthSqr() > 1.0e-6 ? wish : horizLook(p);
		Direction d = findWall(p, aim);
		if (d == null) {
			return false;
		}
		Vec3 attachLook = horizLook(p);
		double attachFacing = attachLook.x * d.getStepX() + attachLook.z * d.getStepZ();
		if (attachFacing > LOOK_INTO_DOT) {
			return false;
		}

		wallRunning = true;
		wallRunActive = true;
		wallRunTicks = 1;
		wallDir = d;
		wallRunSpeed = Math.max(horizSpeed * (1.0 - WALL_RUN_NERF), WALL_RUN_MIN_SPEED);
		wallRunDir = chooseWallDir(p, wish);
		updateAnchorYaw();
		playSound(p, ModSounds.WALL_ATTACH);
		Vec3 grabLook = horizLook(p);
		double grabSide = grabLook.x * wallDir.getStepZ() - grabLook.z * wallDir.getStepX();
		wallSidePositive = grabSide >= 0;
		addRollPunch(grabSide >= 0 ? WALL_ATTACH_ROLL_PUNCH : -WALL_ATTACH_ROLL_PUNCH);
		addPitchPunch(WALL_ATTACH_PITCH_PUNCH);
		applyWallMotion(p);
		return true;
	}

	private Vec3 chooseWallDir(LocalPlayer p, Vec3 wish) {
		double nx = wallDir.getStepX();
		double nz = wallDir.getStepZ();
		Vec3 v = p.getDeltaMovement();
		double comp = v.x * nx + v.z * nz;
		double ax = v.x - comp * nx;
		double az = v.z - comp * nz;
		double mag = Math.sqrt(ax * ax + az * az);
		if (mag > WALL_DIR_MOMENTUM_MIN) {
			return new Vec3(ax / mag, 0, az / mag);
		}
		Vec3 look = projectOntoWall(horizLook(p), nx, nz);
		if (look.lengthSqr() > 1.0e-6) {
			return look;
		}
		Vec3 w = projectOntoWall(wish, nx, nz);
		if (w.lengthSqr() > 1.0e-6) {
			return w;
		}
		return new Vec3(-nz, 0, nx);
	}

	private void applyWallMotion(LocalPlayer p) {
		double nx = wallDir.getStepX();
		double nz = wallDir.getStepZ();

		double ax = wallRunDir.x * wallRunSpeed + nx * WALL_STICK;
		double az = wallRunDir.z * wallRunSpeed + nz * WALL_STICK;

		double y = wallRunTicks <= WALL_CLIMB_TICKS ? WALL_RUN_CLIMB : WALL_RUN_SLIDE;
		p.setDeltaMovement(ax, y, az);
		p.resetFallDistance();
	}

	private void tryCornerTransition(LocalPlayer p) {
		Direction travel = travelDirection();
		if (travel == null || travel == wallDir || travel == wallDir.getOpposite()) {
			return;
		}
		if (wallPresent(p, travel, WALL_CORNER_REACH)) {
			Direction old = wallDir;
			wallDir = travel;
			wallRunDir = new Vec3(-old.getStepX(), 0, -old.getStepZ());
			updateAnchorYaw();
		}
	}

	private void updateAnchorYaw() {
		Direction travel = travelDirection();
		if (travel != null) {
			wallAnchorYaw = travel.toYRot();
		}
	}

	private Direction travelDirection() {
		if (Math.abs(wallRunDir.x) > Math.abs(wallRunDir.z)) {
			return wallRunDir.x > 0 ? Direction.EAST : Direction.WEST;
		}
		if (wallRunDir.z != 0) {
			return wallRunDir.z > 0 ? Direction.SOUTH : Direction.NORTH;
		}
		return null;
	}

	private void endWall(LocalPlayer p, boolean jumpOff, double forceScale) {
		if (jumpOff && wallDir != null) {
			double nx = wallDir.getStepX();
			double nz = wallDir.getStepZ();
			Vec3 look = p.getLookAngle();
			double dx = -nx * WALL_AWAY_WEIGHT + look.x * WALL_CAM_WEIGHT;
			double dz = -nz * WALL_AWAY_WEIGHT + look.z * WALL_CAM_WEIGHT;
			double dy = Math.max(look.y, 0.0) * WALL_CAM_WEIGHT + WALL_UP_BIAS;
			double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
			if (len > 1.0e-6) {
				dx /= len;
				dy /= len;
				dz /= len;
			}
			double power = WALL_JUMP_SPEED * forceScale;
			Vec3 cur = p.getDeltaMovement();
			double newY = Math.max(Math.max(cur.y, 0.0) + dy * power, WALL_JUMP_MIN_UP);
			double newX = cur.x + dx * power;
			double newZ = cur.z + dz * power;
			p.setDeltaMovement(newX, newY, newZ);
			p.resetFallDistance();
			lastAirVel = new Vec3(newX, 0, newZ);
		}
		if (wallRunning) {
			playSound(p, ModSounds.WALL_LET_GO);
		}
		wallRunning = false;
		wallRunActive = false;
		wallRunTicks = 0;
		wallDir = null;
	}

	private boolean wallPresent(LocalPlayer p, Direction d) {
		return wallPresent(p, d, WALL_REACH);
	}

	private boolean wallPresent(LocalPlayer p, Direction d, double reach) {
		return d != null && !p.level().noCollision(p, wallProbe(p, d, reach)) && wallTallEnough(p, d);
	}

	private Direction findWall(LocalPlayer p, Vec3 aim) {
		return findWall(p, aim, WALL_REACH);
	}

	private Direction findWall(LocalPlayer p, Vec3 aim, double reach) {
		Direction best = null;
		double bestDot = ATTACH_DOT;
		for (Direction d : Direction.Plane.HORIZONTAL) {
			if (p.level().noCollision(p, wallProbe(p, d, reach)) || !wallTallEnough(p, d)) {
				continue;
			}
			if (!WallRunFilter.isRunnable(wallBlockId(p, d))) {
				continue;
			}
			double dot = d.getStepX() * aim.x + d.getStepZ() * aim.z;
			if (dot > bestDot) {
				bestDot = dot;
				best = d;
			}
		}
		return best;
	}

	private AABB wallProbe(LocalPlayer p, Direction d) {
		return wallProbe(p, d, WALL_REACH);
	}

	private AABB wallProbe(LocalPlayer p, Direction d, double reach) {
		AABB box = p.getBoundingBox();
		AABB upper = new AABB(box.minX, box.minY + WALL_PROBE_MIN_Y, box.minZ, box.maxX, box.maxY, box.maxZ);
		return upper.move(d.getStepX() * reach, 0, d.getStepZ() * reach);
	}

	private ResourceLocation wallBlockId(LocalPlayer p, Direction d) {
		double off = p.getBbWidth() / 2.0 + 0.1;
		double bx = p.getX() + d.getStepX() * off;
		double bz = p.getZ() + d.getStepZ() * off;
		double by = p.getBoundingBox().minY + WALL_PROBE_MIN_Y;
		BlockPos at = BlockPos.containing(bx, by, bz);
		return BuiltInRegistries.BLOCK.getKey(p.level().getBlockState(at).getBlock());
	}

	private boolean wallTallEnough(LocalPlayer p, Direction d) {
		Level level = p.level();
		double off = p.getBbWidth() / 2.0 + 0.1;
		double bx = p.getX() + d.getStepX() * off;
		double bz = p.getZ() + d.getStepZ() * off;
		BlockPos feet = BlockPos.containing(bx, p.getBoundingBox().minY + 0.1, bz);
		for (int i = 0; i < WALL_MIN_HEIGHT; i++) {
			BlockPos at = feet.above(i);
			if (level.getBlockState(at).getCollisionShape(level, at).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void bhop(LocalPlayer p, Vec3 wish) {
		Vec3 keep = lastAirVel;
		if (wish.lengthSqr() < 1.0e-6) {
			keep = keep.scale(BHOP_IDLE_RETENTION);
		}
		p.jumpFromGround();
		Vec3 jv = p.getDeltaMovement();
		double keepSpeed = Math.sqrt(keep.x * keep.x + keep.z * keep.z);
		double jvSpeed = Math.sqrt(jv.x * jv.x + jv.z * jv.z);
		if (keepSpeed > jvSpeed && keepSpeed > 1.0e-4) {
			p.setDeltaMovement(keep.x, jv.y, keep.z);
		}
		p.resetFallDistance();
	}

	private static double effectiveGravity(LocalPlayer p) {
		double g = p.getGravity();
		if (p.getDeltaMovement().y <= 0.0 && p.hasEffect(MobEffects.SLOW_FALLING)) {
			return Math.min(g, 0.01);
		}
		return g;
	}

	private void airAccelerate(LocalPlayer p, Vec3 wish) {
		if (wish.lengthSqr() < 1.0e-6) {
			return;
		}
		Vec3 vel = p.getDeltaMovement();
		double curSpeed = vel.x * wish.x + vel.z * wish.z;
		double addSpeed = AIR_WISH_SPEED - curSpeed;
		if (addSpeed <= 0) {
			return;
		}
		double accel = Math.min(AIR_ACCEL, addSpeed);
		p.setDeltaMovement(vel.x + wish.x * accel, vel.y, vel.z + wish.z * accel);
	}

	private void cancelAirDrag(LocalPlayer p) {
		Vec3 vel = p.getDeltaMovement();
		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		if (speed < 1.0e-4 || speed >= MAX_HORIZ_SPEED) {
			return;
		}
		double scaled = Math.min(speed * AIR_DRAG_COMP, MAX_HORIZ_SPEED);
		double f = scaled / speed;
		p.setDeltaMovement(vel.x * f, vel.y, vel.z * f);
	}

	private void clampHorizontal(LocalPlayer p) {
		Vec3 vel = p.getDeltaMovement();
		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		if (speed > MAX_HORIZ_SPEED) {
			double f = MAX_HORIZ_SPEED / speed;
			p.setDeltaMovement(vel.x * f, vel.y, vel.z * f);
		}
	}

	private static boolean holdingArts(LocalPlayer p) {
		return NonSteelArtsItem.isHolding(p);
	}

	private void startPunch(LocalPlayer p) {
		punchTicks = PUNCH_DURATION;
		punchActive = true;
		punchMirrored = nextPunchMirrored;
		nextPunchMirrored = !nextPunchMirrored;
		playSound(p, ModSounds.PUNCH,
				1f + (p.getRandom().nextFloat() * 2f - 1f) * PUNCH_PITCH_VARIANCE);
		if (wallRunning) {
			endWall(p, false, 0);
		}
	}

	private void updateWallSlideSound(LocalPlayer p) {
		if (wallSlideSound == null || wallSlideSound.isStopped()) {
			if (!wallRunning) {
				wallSlideSound = null;
				return;
			}
			wallSlideSound = new WallSlideSound(p);
			Minecraft.getInstance().getSoundManager().play(wallSlideSound);
			return;
		}
		wallSlideSound.setActive(wallRunning);
	}

	private void playSound(LocalPlayer p, SoundEvent sound) {
		playSound(p, sound, 1.0f);
	}

	private void playSound(LocalPlayer p, SoundEvent sound, float pitch) {
		p.level().playLocalSound(p, sound, SoundSource.PLAYERS, 0.8f, pitch);
	}

	private static Vec3 wishDir(float yawDeg, float leftImpulse, float forwardImpulse) {
		if (leftImpulse == 0.0f && forwardImpulse == 0.0f) {
			return Vec3.ZERO;
		}
		float rad = yawDeg * ((float) Math.PI / 180f);
		float sin = Mth.sin(rad);
		float cos = Mth.cos(rad);
		double x = leftImpulse * cos - forwardImpulse * sin;
		double z = forwardImpulse * cos + leftImpulse * sin;
		double len = Math.sqrt(x * x + z * z);
		if (len < 1.0e-6) {
			return Vec3.ZERO;
		}
		return new Vec3(x / len, 0, z / len);
	}

	private static Vec3 projectOntoWall(Vec3 v, double nx, double nz) {
		double comp = v.x * nx + v.z * nz;
		double rx = v.x - comp * nx;
		double rz = v.z - comp * nz;
		double len = Math.sqrt(rx * rx + rz * rz);
		if (len < 1.0e-6) {
			return new Vec3(0, 0, 0);
		}
		return new Vec3(rx / len, 0, rz / len);
	}

	private static Vec3 horizLook(LocalPlayer p) {
		Vec3 look = p.getLookAngle();
		double len = Math.sqrt(look.x * look.x + look.z * look.z);
		if (len < 1.0e-6) {
			return new Vec3(0, 0, 1);
		}
		return new Vec3(look.x / len, 0, look.z / len);
	}
}
