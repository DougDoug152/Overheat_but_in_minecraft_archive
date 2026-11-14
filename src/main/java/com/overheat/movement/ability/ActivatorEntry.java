package com.overheat.movement.ability;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;

public record ActivatorEntry(Action action, Ability ability) {

	public static void writeList(FriendlyByteBuf buf, List<ActivatorEntry> entries) {
		buf.writeVarInt(entries.size());
		for (ActivatorEntry entry : entries) {
			buf.writeByte(entry.action().ordinal());
			buf.writeByte(entry.ability().ordinal());
		}
	}

	public static List<ActivatorEntry> readList(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		Action[] actions = Action.values();
		Ability[] abilities = Ability.values();
		List<ActivatorEntry> entries = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int a = buf.readByte() & 0xFF;
			int b = buf.readByte() & 0xFF;
			if (a < actions.length && b < abilities.length) {
				entries.add(new ActivatorEntry(actions[a], abilities[b]));
			}
		}
		return entries;
	}
}
