package com.simibubi.kinetic_api.content.contraptions.components.tracks;

import com.simibubi.kinetic_api.foundation.data.AssetLookup;
import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.state.property.IntProperty;
import net.minecraftforge.client.model.generators.ModelFile;

public class ControllerRailGenerator extends SpecialBlockStateGen {

	@Override
	protected IntProperty<?>[] getIgnoredProperties() {
		return new IntProperty<?>[] { ControllerRailBlock.POWER };
	}

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Instrument shape = state.c(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);
		int rotation = backwards ? 180 : 0;

		switch (shape) {
		case EAST_WEST:
		case ASCENDING_WEST:
			return rotation + 270;
		case ASCENDING_EAST:
			return rotation + 90;
		case ASCENDING_SOUTH:
			return rotation + 180;
		default:
			return rotation;
		}
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		Instrument shape = state.c(ControllerRailBlock.SHAPE);
		boolean backwards = ControllerRailBlock.isStateBackwards(state);

		String model = shape.c() ? backwards ? "ascending_south" : "ascending_north" : "north_south";
		return AssetLookup.partialBaseModel(ctx, prov, model);
	}

}
