package com.simibubi.kinetic_api.content.contraptions.relays.belt;

import com.simibubi.kinetic_api.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraftforge.client.model.generators.ModelFile;

public class BeltGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.c(BeltBlock.SLOPE);
		return slope == BeltSlope.VERTICAL ? 90
			: slope == BeltSlope.SIDEWAYS && direction.getDirection() == AxisDirection.NEGATIVE ? 180 : 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		Boolean casing = state.c(BeltBlock.CASING);
		BeltSlope slope = state.c(BeltBlock.SLOPE);

		boolean flip = slope == BeltSlope.UPWARD;
		boolean rotate = casing && slope == BeltSlope.VERTICAL;
		Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
		return horizontalAngle(direction) + (flip ? 180 : 0) + (rotate ? 90 : 0);
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		Boolean casing = state.c(BeltBlock.CASING);

		if (!casing)
			return prov.models()
				.getExistingFile(prov.modLoc("block/belt/particle"));
		
		BeltPart part = state.c(BeltBlock.PART);
		Direction direction = state.c(BeltBlock.HORIZONTAL_FACING);
		BeltSlope slope = state.c(BeltBlock.SLOPE);
		boolean downward = slope == BeltSlope.DOWNWARD;
		boolean diagonal = slope == BeltSlope.UPWARD || downward;
		boolean vertical = slope == BeltSlope.VERTICAL;
		boolean pulley = part == BeltPart.PULLEY;
		boolean sideways = slope == BeltSlope.SIDEWAYS;
		boolean negative = direction.getDirection() == AxisDirection.NEGATIVE;

		if (!casing && pulley)
			part = BeltPart.MIDDLE;

		if ((vertical && negative || downward || sideways && negative) && part != BeltPart.MIDDLE && !pulley)
			part = part == BeltPart.END ? BeltPart.START : BeltPart.END;

		if (!casing && vertical)
			slope = BeltSlope.HORIZONTAL;
		if (casing && vertical)
			slope = BeltSlope.SIDEWAYS;

		String path = "block/" + (casing ? "belt_casing/" : "belt/");
		String slopeName = slope.a();
		String partName = part.a();

		if (diagonal)
			slopeName = "diagonal";

		Identifier location = prov.modLoc(path + slopeName + "_" + partName);
		return prov.models()
			.getExistingFile(location);
	}

}
