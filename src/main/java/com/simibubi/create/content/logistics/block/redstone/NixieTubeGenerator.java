package com.simibubi.create.content.logistics.block.redstone;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraftforge.client.model.generators.ModelFile;

public class NixieTubeGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return state.c(NixieTubeBlock.CEILING) ? 180 : 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(NixieTubeBlock.aq));
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return AssetLookup.partialBaseModel(ctx, prov);
	}

}
