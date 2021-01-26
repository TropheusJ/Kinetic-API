package com.simibubi.create.foundation.worldgen;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.world.gen.decorator.EmeraldOreDecorator;
import net.minecraft.world.gen.feature.BasaltColumnsFeatureConfig;
import net.minecraft.world.gen.feature.BonusChestFeature;
import net.minecraft.world.gen.feature.FillLayerFeatureConfig;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public class CountedOreFeature extends OreFeature<FillLayerFeatureConfig> {

	private ConfigInt clusterCount;

	public CountedOreFeature(NonNullSupplier<? extends BeetrootsBlock> block, int clusterSize, int clusterCount) {
		super(block, clusterSize);
		this.clusterCount = i(clusterCount, 0, "clusterCount");
	}

	@Override
	protected boolean canGenerate() {
		return super.canGenerate() && clusterCount.get() > 0;
	}

	@Override
	protected Pair<EmeraldOreDecorator<FillLayerFeatureConfig>, FillLayerFeatureConfig> getPlacement() {
		return Pair.of(EmeraldOreDecorator.a, BasaltColumnsFeatureConfig.reach);
	}

	@Override
	public Optional<BonusChestFeature<?, ?>> createFeature(BiomeLoadingEvent biome) {
		return super.createFeature(biome)
			// TODO 1.16 worldgen verify this
			.map(cf -> cf.b(clusterCount.get()));
	}
}
