package com.overheat.movement.ability;

import com.mojang.serialization.Codec;
import com.overheat.movement.OverheatMovement;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;

public final class PlayerAbilities {

	public static final AttachmentType<Integer> ABILITIES = AttachmentRegistry.<Integer>builder()
			.initializer(() -> 0)
			.persistent(Codec.INT)
			.syncWith(ByteBufCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly())
			.buildAndRegister(OverheatMovement.id("abilities"));

	private PlayerAbilities() {
	}

	public static int mask(Entity player) {
		return player.getAttachedOrElse(ABILITIES, 0);
	}

	public static boolean has(Entity player, Ability ability) {
		return (mask(player) & ability.bit()) != 0;
	}

	public static void set(Entity player, Ability ability, boolean enabled) {
		int current = mask(player);
		int updated = enabled ? (current | ability.bit()) : (current & ~ability.bit());
		if (updated != current) {
			player.setAttached(ABILITIES, updated);
		}
	}

	public static void init() {
	}
}
