package com.simibubi.create.foundation.data;

import com.simibubi.create.content.contraptions.relays.gauge.GaugeBlock;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraftforge.client.model.generators.ModelFile;

public abstract class DirectionalAxisBlockStateGen extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		Direction direction = state.c(GaugeBlock.FACING);
		boolean alongFirst = state.c(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

		if (direction == Direction.DOWN)
			return 180;
		if (direction == Direction.UP)
			return 0;
		if ((direction.getAxis() == Axis.X) == alongFirst)
			return 90;

		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Direction direction = state.c(GaugeBlock.FACING);
		boolean alongFirst = state.c(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

		if (direction.getAxis()
			.isVertical())
			return alongFirst ? 90 : 0;

		return horizontalAngle(direction) + 90;
	}

	public abstract <T extends BeetrootsBlock> String getModelPrefix(DataGenContext<BeetrootsBlock, T> ctx,
		RegistrateBlockstateProvider prov, PistonHandler state);

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		boolean vertical = state.c(GaugeBlock.FACING)
			.getAxis()
			.isVertical();
		String partial = vertical ? "" : "_wall";
		return prov.models()
			.getExistingFile(prov.modLoc(getModelPrefix(ctx, prov, state) + partial));
	}

}
