package com.simibubi.create.content.contraptions.components.saw;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class SawGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return state.c(SawBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Direction facing = state.c(SawBlock.FACING);
		boolean axisAlongFirst = state.c(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
		if (facing.getAxis()
			.isVertical())
			return axisAlongFirst ? 90 : 0;
		return horizontalAngle(facing);
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		String path = "block/" + ctx.getName() + "/";
		String orientation = state.c(SawBlock.FACING)
			.getAxis()
			.isVertical() ? "vertical" : "horizontal";

		return prov.models()
			.getExistingFile(prov.modLoc(path + orientation));
	}

}
