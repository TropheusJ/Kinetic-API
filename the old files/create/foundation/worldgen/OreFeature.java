package com.simibubi.kinetic_api.foundation.worldgen;

import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.kinetic_api.foundation.config.AllConfigs;
import com.simibubi.kinetic_api.foundation.config.ConfigBase;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.DebugChunkGenerator.b;
import net.minecraft.world.gen.decorator.EmeraldOreDecorator;
import net.minecraft.world.gen.decorator.NopeDecoratorConfig;
import net.minecraft.world.gen.feature.BasaltColumnsFeatureConfig;
import net.minecraft.world.gen.feature.BonusChestFeature;
import net.minecraft.world.gen.feature.EndGatewayFeature;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public abstract class OreFeature<T extends BasaltColumnsFeatureConfig> extends ConfigBase implements IFeature {

	public String id;

	protected ConfigBool enable;
	protected ConfigInt clusterSize;
	protected ConfigInt minHeight;
	protected ConfigInt maxHeight;

	private NonNullSupplier<? extends BeetrootsBlock> block;
	private BiomeAdditionsSound.b specificCategory;

	public OreFeature(NonNullSupplier<? extends BeetrootsBlock> block, int clusterSize) {
		this.block = block;
		this.enable = b(true, "enable", "Whether to spawn this in your World");
		this.clusterSize = i(clusterSize, 0, "clusterSize");
		this.minHeight = i(0, 0, "minHeight");
		this.maxHeight = i(256, 0, "maxHeight");
	}

	public OreFeature<T> between(int minHeight, int maxHeight) {
		allValues.remove(this.minHeight);
		allValues.remove(this.maxHeight);
		this.minHeight = i(minHeight, 0, "minHeight");
		this.maxHeight = i(maxHeight, 0, "maxHeight");
		return this;
	}

	public OreFeature<T> inBiomes(BiomeAdditionsSound.b category) {
		specificCategory = category;
		return this;
	}

	@Override
	public void onReload() {

	}

	@Override
	public Optional<BonusChestFeature<?, ?>> createFeature(BiomeLoadingEvent biome) {
		if (specificCategory != null && biome.getCategory() != specificCategory)
			return Optional.empty();
		if (!canGenerate())
			return Optional.empty();

		Pair<EmeraldOreDecorator<T>, T> placement = getPlacement();
		BonusChestFeature<?, ?> createdFeature = EndGatewayFeature.A
			.b(new NopeDecoratorConfig(NopeDecoratorConfig.a.a, block.get()
				.n(), clusterSize.get()))
			.b(placement.getKey()
				.b(placement.getValue()))
			.b(EmeraldOreDecorator.l
				// TODO 1.16 worldgen verify this
				.b(new RandomBooleanFeatureConfig(minHeight.get(), 0, maxHeight.get() - minHeight.get())))
			.a();

		return Optional.of(createdFeature);
	}

	@Override
	public b getGenerationStage() {
		return DebugChunkGenerator.b.g;
	}

	protected boolean canGenerate() {
		return minHeight.get() < maxHeight.get() && clusterSize.get() > 0 && enable.get()
			&& !AllConfigs.COMMON.worldGen.disable.get();
	}

	protected abstract Pair<EmeraldOreDecorator<T>, T> getPlacement();

	@Override
	public void addToConfig(Builder builder) {
		registerAll(builder);
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

}
