package com.simibubi.create.content.contraptions.components.flywheel;

import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraftforge.client.model.generators.ModelFile;

public class FlywheelGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return horizontalAngle(state.c(FlywheelBlock.HORIZONTAL_FACING)) + 90;
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return prov.models()
			.getExistingFile(prov.modLoc("block/" + ctx.getName() + "/casing_" + state.c(FlywheelBlock.CONNECTION)
				.a()));
	}
}
