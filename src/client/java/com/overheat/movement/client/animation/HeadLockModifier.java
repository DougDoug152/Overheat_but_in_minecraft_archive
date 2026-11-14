package com.overheat.movement.client.animation;

import com.overheat.movement.client.movement.MovementHandler;
import dev.kosmx.playerAnim.api.PartKey;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractModifier;
import dev.kosmx.playerAnim.core.util.Vec3f;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class HeadLockModifier extends AbstractModifier {

	private static final float EYE_PIVOT_Y = -1.9f;

	private final AbstractClientPlayer player;

	public HeadLockModifier(AbstractClientPlayer player) {
		this.player = player;
	}

	@Override
	public Vec3f get3DTransform(PartKey part, TransformType type, float tickDelta, Vec3f value) {
		Vec3f base = super.get3DTransform(part, type, tickDelta, value);
		if (!PartKey.RIGHT_ARM.equals(part) && !PartKey.LEFT_ARM.equals(part)) {
			return base;
		}
		float x = base.getX().floatValue();
		float y = base.getY().floatValue();
		float z = base.getZ().floatValue();
		if (type == TransformType.ROTATION) {
			Vector3f euler = headRotation(tickDelta)
					.mul(new Quaternionf().rotationZYX(z, y, x))
					.getEulerAnglesZYX(new Vector3f());
			return new Vec3f(euler.x, euler.y, euler.z);
		}
		if (type == TransformType.POSITION) {
			Vector3f orbit = headRotation(tickDelta)
					.transform(new Vector3f(x, y - EYE_PIVOT_Y, z));
			return new Vec3f(orbit.x, orbit.y + EYE_PIVOT_Y, orbit.z);
		}
		return base;
	}

	private Quaternionf headRotation(float tickDelta) {
		float blend = MovementHandler.getWallAnchorBlend();
		float anchored = Mth.rotLerp(blend,
				player.getViewYRot(tickDelta) + MovementHandler.getArmStickYawOffset(),
				MovementHandler.getWallAnchorYaw());
		float yaw = Mth.wrapDegrees(anchored
				- Mth.rotLerp(tickDelta, player.yBodyRotO, player.yBodyRot));
		float pitch = Mth.lerp(blend,
				player.getViewXRot(tickDelta) + MovementHandler.getArmStickPitchOffset(),
				MovementHandler.getWallAnchorPitch());
		float roll = MovementHandler.getViewRoll();
		return new Quaternionf()
				.rotationZYX(0f, yaw * Mth.DEG_TO_RAD, pitch * Mth.DEG_TO_RAD)
				.rotateZ(roll * Mth.DEG_TO_RAD);
	}
}
