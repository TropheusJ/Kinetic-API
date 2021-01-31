package com.simibubi.kinetic_api.foundation.worldgen;

import static net.minecraft.sound.BiomeAdditionsSound.b.m;
import static net.minecraft.sound.BiomeAdditionsSound.b.l;

import java.util.Arrays;
import java.util.Optional;

import com.simibubi.kinetic_api.AllBlocks;
import com.simibubi.kinetic_api.content.palettes.AllPaletteBlocks;
import com.simibubi.kinetic_api.foundation.utility.Lang;
import net.minecraft.sound.BiomeAdditionsSound.b;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.BonusChestFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;

public enum AllWorldFeatures {

	COPPER_ORE(new CountedOreFeature(AllBlocks.COPPER_ORE, 18, 2).between(40, 86)),
	COPPER_ORE_OCEAN(new CountedOreFeature(AllBlocks.COPPER_ORE, 15, 4).between(20, 55).inBiomes(l)),

	ZINC_ORE(new CountedOreFeature(AllBlocks.ZINC_ORE, 14, 4).between(15, 70)),
	ZINC_ORE_DESERT(new CountedOreFeature(AllBlocks.ZINC_ORE, 17, 5).between(10, 85).inBiomes(m)),

	LIMESTONE(new ChanceOreFeature(AllPaletteBlocks.LIMESTONE, 128, 1 / 32f).between(30, 70)),
	WEATHERED_LIMESTONE(new ChanceOreFeature(AllPaletteBlocks.WEATHERED_LIMESTONE, 128, 1 / 32f).between(10, 30)),
	DOLOMITE(new ChanceOreFeature(AllPaletteBlocks.DOLOMITE, 128, 1 / 64f).between(20, 70)),
	GABBRO(new ChanceOreFeature(AllPaletteBlocks.GABBRO, 128, 1 / 64f).between(20, 70)),
	SCORIA(new ChanceOreFeature(AllPaletteBlocks.NATURAL_SCORIA, 128, 1 / 32f).between(0, 10)), 

	;

	/**
	 * Increment this number if all worldgen entries should be overwritten in this
	 * update. Worlds from the previous version will overwrite potentially changed
	 * values with the new defaults.
	 */
	public static final int forcedUpdateVersion = 1;

	public IFeature feature;

	AllWorldFeatures(IFeature feature) {
		this.feature = feature;
		this.feature.setId(Lang.asId(name()));
	}

	public static void reload(BiomeLoadingEvent event) {
		for (AllWorldFeatures entry : AllWorldFeatures.values()) {
			if (event.getName() == BiomeSource.Z.getRegistryName())
				continue;
			if (event.getCategory() == b.q)
				continue;

			Optional<BonusChestFeature<?, ?>> createFeature = entry.feature.createFeature(event);
			if (!createFeature.isPresent())
				continue;

			event.getGeneration().a(entry.feature.getGenerationStage(), createFeature.get());
		}

//		// Debug contained ore features
//		for (Biome biome : ForgeRegistries.BIOMES) {
//			Debug.markTemporary();
//			System.out.println(biome.getRegistryName().getPath() + " has the following features:");
//			for (ConfiguredFeature<?> configuredFeature : biome.getFeatures(Decoration.UNDERGROUND_ORES)) {
//				IFeatureConfig config = configuredFeature.config;
//				if (!(config instanceof DecoratedFeatureConfig))
//					continue;
//				DecoratedFeatureConfig decoConf = (DecoratedFeatureConfig) config;
//				if (!(decoConf.feature.config instanceof OreFeatureConfig))
//					continue;
//				OreFeatureConfig oreConf = (OreFeatureConfig) decoConf.feature.config;
//				System.out.println(configuredFeature.feature.getRegistryName().getPath());
//				System.out.println(oreConf.state.getBlock().getRegistryName().getPath());
//				System.out.println("--");
//			}
//		}
	}

	public static void fillConfig(ForgeConfigSpec.Builder builder) {
		Arrays.stream(values()).forEach(entry -> {
			builder.push(Lang.asId(entry.name()));
			entry.feature.addToConfig(builder);
			builder.pop();
		});
	}

}
