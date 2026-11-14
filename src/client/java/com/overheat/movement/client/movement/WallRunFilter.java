package com.overheat.movement.client.movement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class WallRunFilter {

	private static volatile Set<ResourceLocation> allowed = Set.of();
	private static volatile Set<ResourceLocation> denied = Set.of();

	private WallRunFilter() {
	}

	public static void update(List<ResourceLocation> newAllowed, List<ResourceLocation> newDenied) {
		allowed = new HashSet<>(newAllowed);
		denied = new HashSet<>(newDenied);
	}

	public static boolean isRunnable(ResourceLocation blockId) {
		if (denied.contains(blockId)) {
			return false;
		}
		if (!allowed.isEmpty()) {
			return allowed.contains(blockId);
		}
		return true;
	}
}
