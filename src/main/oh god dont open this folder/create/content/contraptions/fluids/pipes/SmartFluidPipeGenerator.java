package com.simibubi.create.content.contraptions.fluids.pipes;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraftforge.client.model.generators.ModelFile;

public class SmartFluidPipeGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		BlockPredicate attachFace = state.c(SmartFluidPipeBlock.u);
		return attachFace == BlockPredicate.c ? 180 : attachFace == BlockPredicate.block ? 0 : 270;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		BlockPredicate attachFace = state.c(SmartFluidPipeBlock.u);
		int angle = horizontalAngle(state.c(SmartFluidPipeBlock.aq));
		return angle + (attachFace == BlockPredicate.c ? 180 : 0);
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return AssetLookup.partialBaseModel(ctx, prov);
	}

}
