package com.overheat.movement.ability;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WallRunFilterData extends SavedData {

	private static final String NAME = "overheat_wallrun_filter";

	private final Set<ResourceLocation> allowed = new LinkedHashSet<>();
	private final Set<ResourceLocation> denied = new LinkedHashSet<>();

	public static WallRunFilterData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(WallRunFilterData::new, WallRunFilterData::load, null), NAME);
	}

	public Set<ResourceLocation> allowed() {
		return allowed;
	}

	public Set<ResourceLocation> denied() {
		return denied;
	}

	public boolean toggleAllowed(ResourceLocation id) {
		boolean nowContains = allowed.remove(id) ? false : allowed.add(id);
		setDirty();
		return nowContains;
	}

	public boolean toggleDenied(ResourceLocation id) {
		boolean nowContains = denied.remove(id) ? false : denied.add(id);
		setDirty();
		return nowContains;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("allowed", writeSet(allowed));
		tag.put("denied", writeSet(denied));
		return tag;
	}

	public static WallRunFilterData load(CompoundTag tag, HolderLookup.Provider registries) {
		WallRunFilterData data = new WallRunFilterData();
		readSet(tag.getList("allowed", Tag.TAG_STRING), data.allowed);
		readSet(tag.getList("denied", Tag.TAG_STRING), data.denied);
		return data;
	}

	private static ListTag writeSet(Set<ResourceLocation> set) {
		ListTag list = new ListTag();
		for (ResourceLocation id : set) {
			list.add(StringTag.valueOf(id.toString()));
		}
		return list;
	}

	private static void readSet(ListTag list, Set<ResourceLocation> target) {
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) {
				target.add(id);
			}
		}
	}
}
