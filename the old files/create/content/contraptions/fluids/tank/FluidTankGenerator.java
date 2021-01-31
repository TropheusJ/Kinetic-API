package com.simibubi.kinetic_api.content.contraptions.fluids.tank;

import com.simibubi.kinetic_api.content.contraptions.fluids.tank.FluidTankBlock.Shape;
import com.simibubi.kinetic_api.foundation.data.AssetLookup;
import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraftforge.client.model.generators.ModelFile;

public class FluidTankGenerator extends SpecialBlockStateGen {

	private String prefix;

	public FluidTankGenerator() {
		this("");
	}

	public FluidTankGenerator(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return 0;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		Boolean top = state.c(FluidTankBlock.TOP);
		Boolean bottom = state.c(FluidTankBlock.BOTTOM);
		Shape shape = state.c(FluidTankBlock.SHAPE);

		String shapeName = "middle";
		if (top && bottom)
			shapeName = "single";
		else if (top)
			shapeName = "top";
		else if (bottom)
			shapeName = "bottom";

		String modelName = shapeName + (shape == Shape.PLAIN ? "" : "_" + shape.a());

		if (!prefix.isEmpty())
			return prov.models()
				.withExistingParent(prefix + modelName, prov.modLoc("block/fluid_tank/block_" + modelName))
				.texture("0", prov.modLoc("block/" + prefix + "casing"))
				.texture("1", prov.modLoc("block/" + prefix + "fluid_tank"))
				.texture("3", prov.modLoc("block/" + prefix + "fluid_tank_window"))
				.texture("4", prov.modLoc("block/" + prefix + "fluid_tank_window_single"))
				.texture("particle", prov.modLoc("block/" + prefix + "fluid_tank"));

		return AssetLookup.partialBaseModel(ctx, prov, modelName);
	}

}
