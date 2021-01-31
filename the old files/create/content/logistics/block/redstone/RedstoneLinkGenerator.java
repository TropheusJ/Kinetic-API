package com.simibubi.kinetic_api.content.logistics.block.redstone;

import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class RedstoneLinkGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		Direction facing = state.c(RedstoneLinkBlock.SHAPE);
		return facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 270;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Direction facing = state.c(RedstoneLinkBlock.SHAPE);
		return facing.getAxis()
			.isVertical() ? 180 : horizontalAngle(facing);
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		String variant = state.c(RedstoneLinkBlock.RECEIVER) ? "receiver" : "transmitter";
		if (state.c(RedstoneLinkBlock.SHAPE).getAxis().isHorizontal())
			variant += "_vertical";
		if (state.c(RedstoneLinkBlock.POWERED))
			variant += "_powered";
		
		return prov.models().getExistingFile(prov.modLoc("block/redstone_link/" + variant));
	}

}
