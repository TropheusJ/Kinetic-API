package com.simibubi.kinetic_api.content.palettes;

import java.util.function.Supplier;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.BellBlock;

public enum PaletteStoneVariants {

	GRANITE(() -> () -> BellBlock.POWERED),
	DIORITE(() -> () -> BellBlock.EAST_WEST_SHAPE),
	ANDESITE(() -> () -> BellBlock.BELL_LIP_SHAPE),
	LIMESTONE(() -> AllPaletteBlocks.LIMESTONE),
	WEATHERED_LIMESTONE(() -> AllPaletteBlocks.WEATHERED_LIMESTONE),
	DOLOMITE(() -> AllPaletteBlocks.DOLOMITE),
	GABBRO(() -> AllPaletteBlocks.GABBRO),
	SCORIA(() -> AllPaletteBlocks.SCORIA),
	DARK_SCORIA(() -> AllPaletteBlocks.DARK_SCORIA)

	;

	private Supplier<Supplier<BeetrootsBlock>> baseBlock;

	private PaletteStoneVariants(Supplier<Supplier<BeetrootsBlock>> baseBlock) {
		this.baseBlock = baseBlock;
	}

	public Supplier<BeetrootsBlock> getBaseBlock() {
		return baseBlock.get();
	}

}
