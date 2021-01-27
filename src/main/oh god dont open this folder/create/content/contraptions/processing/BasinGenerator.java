package com.simibubi.create.content.contraptions.processing;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraftforge.client.model.generators.ModelFile;

public class BasinGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(BasinBlock.FACING));
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		if (state.c(BasinBlock.FACING).getAxis().isVertical())
			return AssetLookup.partialBaseModel(ctx, prov);
		return AssetLookup.partialBaseModel(ctx, prov, "directional");
	}

}
