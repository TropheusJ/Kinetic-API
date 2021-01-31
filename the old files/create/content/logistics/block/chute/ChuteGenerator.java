package com.simibubi.kinetic_api.content.logistics.block.chute;

import com.simibubi.kinetic_api.content.logistics.block.chute.ChuteBlock.Shape;
import com.simibubi.kinetic_api.foundation.data.AssetLookup;
import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChuteGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(ChuteBlock.FACING));
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		boolean horizontal = state.c(ChuteBlock.FACING) != Direction.DOWN;
		ChuteBlock.Shape shape = state.c(ChuteBlock.SHAPE);

		if (!horizontal)
			return shape == Shape.NORMAL ? AssetLookup.partialBaseModel(ctx, prov)
				: shape == Shape.INTERSECTION ? AssetLookup.partialBaseModel(ctx, prov, "intersection")
					: AssetLookup.partialBaseModel(ctx, prov, "windowed");
		return shape == Shape.INTERSECTION ? AssetLookup.partialBaseModel(ctx, prov, "diagonal", "intersection")
			: AssetLookup.partialBaseModel(ctx, prov, "diagonal");
	}

}
