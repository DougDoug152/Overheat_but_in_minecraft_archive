package com.overheat.movement.content;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public final class ModContent {

	private ModContent() {
	}

	public static void init() {
		ModBlocks.init();
		ModItems.init();

		ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
				ResourceLocation.withDefaultNamespace("tools_and_utilities"));
		ItemGroupEvents.modifyEntriesEvent(tab).register(entries -> {
			entries.accept(ModBlocks.ABILITY_GIVER);
			entries.accept(ModBlocks.UNACTIVE_ACTIVATOR);
			entries.accept(ModBlocks.ABILITY_GIVER_INACTIVE);
			entries.accept(ModBlocks.UNACTIVE_ACTIVATOR_INACTIVE);
			entries.accept(ModItems.ACTIVATOR_WAND);
			entries.accept(ModItems.WALL_RUN_SELECTOR);
			entries.accept(ModItems.NON_STEEL_ARTS);
		});
	}
}
