package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import cdx;
import com.simibubi.create.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock.PistonState;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public class MechanicalPistonGenerator extends SpecialBlockStateGen {

	private final BlockHalf type;

	public MechanicalPistonGenerator(BlockHalf type) {
		this.type = type;
	}

	@Override
	protected int getXRotation(PistonHandler state) {
		Direction facing = state.c(MechanicalPistonBlock.FACING);
		return facing.getAxis()
			.isVertical() ? facing == Direction.DOWN ? 180 : 0 : 90;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Direction facing = state.c(MechanicalPistonBlock.FACING);
		return facing.getAxis()
			.isVertical() ? 0 : horizontalAngle(facing) + 180;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		Direction facing = state.c(cdx.SHAPE);
		boolean axisAlongFirst = state.c(MechanicalPistonBlock.AXIS_ALONG_FIRST_COORDINATE);
		PistonState pistonState = state.c(MechanicalPistonBlock.STATE);

		String path = "block/mechanical_piston";
		String folder = pistonState == PistonState.RETRACTED ? type.a() : pistonState.a();
		String partial = facing.getAxis() == Axis.X ^ axisAlongFirst ? "block_rotated" : "block";

		return prov.models()
			.getExistingFile(prov.modLoc(path + "/" + folder + "/" + partial));
	}

}
