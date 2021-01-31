package com.simibubi.kinetic_api.foundation.worldgen;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.world.gen.decorator.CarvingMaskDecorator;
import net.minecraft.world.gen.decorator.EmeraldOreDecorator;

public class ChanceOreFeature extends OreFeature<CarvingMaskDecorator> {

	private ConfigFloat clusterChance;

	public ChanceOreFeature(NonNullSupplier<? extends BeetrootsBlock> block, int clusterSize, float clusterChance) {
		super(block, clusterSize);
		this.clusterChance = f(clusterChance, 0, 1, "clusterChance");
	}

	@Override
	protected boolean canGenerate() {
		return super.canGenerate() && clusterChance.get() > 0;
	}

	@Override
	protected Pair<EmeraldOreDecorator<CarvingMaskDecorator>, CarvingMaskDecorator> getPlacement() {
		return Pair.of(EmeraldOreDecorator.b,
			// TODO 1.16 worldgen verify this
			new CarvingMaskDecorator((int) (1 / clusterChance.getF())));
	}
}
