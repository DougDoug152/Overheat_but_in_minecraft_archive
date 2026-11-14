package com.overheat.movement.content;

import com.overheat.movement.OverheatMovement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItems {

	public static final Item ACTIVATOR_WAND = register("activator_wand",
			new ActivatorWandItem(new Item.Properties()
					.setId(ResourceKey.create(Registries.ITEM, OverheatMovement.id("activator_wand")))
					.stacksTo(1)));

	public static final Item WALL_RUN_SELECTOR = register("wallrun_selector",
			new WallRunSelectorItem(new Item.Properties()
					.setId(ResourceKey.create(Registries.ITEM, OverheatMovement.id("wallrun_selector")))
					.stacksTo(1)));

	public static final Item NON_STEEL_ARTS = register("non_steel_arts",
			new NonSteelArtsItem(new Item.Properties()
					.setId(ResourceKey.create(Registries.ITEM, OverheatMovement.id("non_steel_arts")))
					.stacksTo(1)));

	private ModItems() {
	}

	private static Item register(String path, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, OverheatMovement.id(path), item);
	}

	public static void init() {
	}
}
