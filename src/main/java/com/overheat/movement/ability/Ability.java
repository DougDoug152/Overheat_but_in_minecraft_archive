package com.overheat.movement.ability;

import java.util.Collection;
import java.util.EnumSet;

public enum Ability {
	DASH("Dash"),
	AIR_JUMP("Air Jump"),
	WALL_RUN("Wall Run"),
	BHOP("Bhop"),
	COMBAT("Combat"),
	SLIDE("Slide");

	private final String displayName;

	Ability(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}

	public int bit() {
		return 1 << ordinal();
	}

	public static EnumSet<Ability> fromMask(int mask) {
		EnumSet<Ability> set = EnumSet.noneOf(Ability.class);
		for (Ability a : values()) {
			if ((mask & a.bit()) != 0) {
				set.add(a);
			}
		}
		return set;
	}

	public static int toMask(Collection<Ability> set) {
		int mask = 0;
		for (Ability a : set) {
			mask |= a.bit();
		}
		return mask;
	}
}
