package com.simibubi.kinetic_api.content.contraptions.relays.encased;

import java.util.function.BiFunction;

import com.simibubi.kinetic_api.content.contraptions.relays.encased.EncasedBeltBlock.Part;
import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public class EncasedBeltGenerator extends SpecialBlockStateGen {

	private BiFunction<PistonHandler, String, ModelFile> modelFunc;

	public EncasedBeltGenerator(BiFunction<PistonHandler, String, ModelFile> modelFunc) {
		this.modelFunc = modelFunc;
	}

	@Override
	protected int getXRotation(PistonHandler state) {
		EncasedBeltBlock.Part part = state.c(EncasedBeltBlock.PART);
		boolean connectedAlongFirst = state.c(EncasedBeltBlock.CONNECTED_ALONG_FIRST_COORDINATE);
		Axis axis = state.c(EncasedBeltBlock.AXIS);

		if (part == Part.NONE)
			return axis == Axis.Y ? 90 : 0;
		if (axis == Axis.X)
			return (connectedAlongFirst ? 90 : 0) + (part == Part.START ? 180 : 0);
		if (axis == Axis.Z)
			return (connectedAlongFirst ? 0 : (part == Part.START ? 270 : 90));
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		EncasedBeltBlock.Part part = state.c(EncasedBeltBlock.PART);
		boolean connectedAlongFirst = state.c(EncasedBeltBlock.CONNECTED_ALONG_FIRST_COORDINATE);
		Axis axis = state.c(EncasedBeltBlock.AXIS);

		if (part == Part.NONE)
			return axis == Axis.X ? 90 : 0;
		if (axis == Axis.Z)
			return (connectedAlongFirst && part == Part.END ? 270 : 90);
		boolean flip = part == Part.END && !connectedAlongFirst || part == Part.START && connectedAlongFirst;
		if (axis == Axis.Y)
			return (connectedAlongFirst ? 90 : 0) + (flip ? 180 : 0);
		return 0;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return modelFunc.apply(state, getModelSuffix(state));
	}

	protected String getModelSuffix(PistonHandler state) {
		EncasedBeltBlock.Part part = state.c(EncasedBeltBlock.PART);
		Axis axis = state.c(EncasedBeltBlock.AXIS);

		if (part == Part.NONE)
			return "single";

		String orientation = axis == Axis.Y ? "vertical" : "horizontal";
		String section = part == Part.MIDDLE ? "middle" : "end";
		return section + "_" + orientation;
	}

}
