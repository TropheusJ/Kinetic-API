package com.simibubi.kinetic_api.foundation.worldgen;

import java.util.Optional;
import net.minecraft.world.gen.chunk.DebugChunkGenerator.b;
import net.minecraft.world.gen.feature.BonusChestFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public interface IFeature {
	
	public void setId(String id);

	public void addToConfig(ForgeConfigSpec.Builder builder);
	
	public Optional<BonusChestFeature<?, ?>> createFeature(BiomeLoadingEvent biome);
	
	public b getGenerationStage();

}