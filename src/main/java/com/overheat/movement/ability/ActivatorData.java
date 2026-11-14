package com.overheat.movement.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ActivatorData extends SavedData {

	private static final String NAME = "overheat_activators";

	private final Map<Long, List<ActivatorEntry>> configs = new HashMap<>();

	public static ActivatorData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(ActivatorData::new, ActivatorData::load, null), NAME);
	}

	public boolean isActivator(BlockPos pos) {
		return configs.containsKey(pos.asLong());
	}

	public List<ActivatorEntry> getConfig(BlockPos pos) {
		List<ActivatorEntry> entries = configs.get(pos.asLong());
		return entries == null ? null : List.copyOf(entries);
	}

	public void setConfig(BlockPos pos, List<ActivatorEntry> entries) {
		if (entries == null || entries.isEmpty()) {
			configs.remove(pos.asLong());
		} else {
			configs.put(pos.asLong(), new ArrayList<>(entries));
		}
		setDirty();
	}

	public void remove(BlockPos pos) {
		if (configs.remove(pos.asLong()) != null) {
			setDirty();
		}
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (Map.Entry<Long, List<ActivatorEntry>> e : configs.entrySet()) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putLong("pos", e.getKey());
			ListTag actions = new ListTag();
			for (ActivatorEntry entry : e.getValue()) {
				CompoundTag actionTag = new CompoundTag();
				actionTag.putByte("action", (byte) entry.action().ordinal());
				actionTag.putByte("ability", (byte) entry.ability().ordinal());
				actions.add(actionTag);
			}
			entryTag.put("actions", actions);
			list.add(entryTag);
		}
		tag.put("entries", list);
		return tag;
	}

	public static ActivatorData load(CompoundTag tag, HolderLookup.Provider registries) {
		ActivatorData data = new ActivatorData();
		Action[] actions = Action.values();
		Ability[] abilities = Ability.values();
		ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entryTag = list.getCompound(i);
			long pos = entryTag.getLong("pos");
			ListTag actionList = entryTag.getList("actions", Tag.TAG_COMPOUND);
			List<ActivatorEntry> entries = new ArrayList<>();
			for (int j = 0; j < actionList.size(); j++) {
				CompoundTag actionTag = actionList.getCompound(j);
				int ai = actionTag.getByte("action") & 0xFF;
				int abi = actionTag.getByte("ability") & 0xFF;
				if (ai < actions.length && abi < abilities.length) {
					entries.add(new ActivatorEntry(actions[ai], abilities[abi]));
				}
			}
			if (!entries.isEmpty()) {
				data.configs.put(pos, entries);
			}
		}
		return data;
	}
}
