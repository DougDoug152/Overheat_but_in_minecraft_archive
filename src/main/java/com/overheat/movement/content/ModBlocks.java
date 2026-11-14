package com.overheat.movement.content;

import com.overheat.movement.OverheatMovement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {

	public static final Block ABILITY_GIVER = register("ability_giver",
			new AbilityGiverBlock(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, OverheatMovement.id("ability_giver")))
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(1.5f)
					.sound(SoundType.STONE)));

	public static final Block UNACTIVE_ACTIVATOR = register("unactive_activator",
			new Block(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, OverheatMovement.id("unactive_activator")))
					.mapColor(MapColor.COLOR_GREEN)
					.strength(1.5f)
					.sound(SoundType.STONE)));

	public static final Block ABILITY_GIVER_INACTIVE = register("ability_giver_inactive",
			new Block(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, OverheatMovement.id("ability_giver_inactive")))
					.mapColor(MapColor.COLOR_ORANGE)
					.strength(1.5f)
					.sound(SoundType.STONE)));

	public static final Block UNACTIVE_ACTIVATOR_INACTIVE = register("unactive_activator_inactive",
			new Block(BlockBehaviour.Properties.of()
					.setId(ResourceKey.create(Registries.BLOCK, OverheatMovement.id("unactive_activator_inactive")))
					.mapColor(MapColor.COLOR_GREEN)
					.strength(1.5f)
					.sound(SoundType.STONE)));

	private ModBlocks() {
	}

	private static Block register(String path, Block block) {
		ResourceLocation id = OverheatMovement.id(path);
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		BlockItem item = new BlockItem(block,
				new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, id, item);
		return block;
	}

	public static void init() {
	}
}
