package com.overheat.movement;

import com.overheat.movement.ability.PlayerAbilities;
import com.overheat.movement.ability.StepListener;
import com.overheat.movement.content.ModContent;
import com.overheat.movement.content.NonSteelArtsItem;
import com.overheat.movement.content.WallRunSelectorItem;
import com.overheat.movement.network.AbilityNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverheatMovement implements ModInitializer {
	public static final String MOD_ID = "overheat";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModSounds.init();
		ModContent.init();
		PlayerAbilities.init();
		StepListener.init();
		WallRunSelectorItem.registerEvents();
		NonSteelArtsItem.registerEvents();
		AbilityNetworking.registerCommon();
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
