package com.simibubi.create.content.contraptions.components.motor;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.generators.ModelFile;

public class CreativeMotorGenerator extends SpecialBlockStateGen {

	@Override
	protected int getXRotation(PistonHandler state) {
		return state.c(CreativeMotorBlock.FACING) == Direction.DOWN ? 180 : 0;
	}

	@Override
	protected int getYRotation(PistonHandler state) {
		return state.c(CreativeMotorBlock.FACING)
			.getAxis()
			.isVertical() ? 0 : horizontalAngle(state.c(CreativeMotorBlock.FACING));
	}

	@Override
	public <T extends BeetrootsBlock> ModelFile getModel(DataGenContext<BeetrootsBlock, T> ctx, RegistrateBlockstateProvider prov,
		PistonHandler state) {
		return state.c(CreativeMotorBlock.FACING)
			.getAxis()
			.isVertical() ? AssetLookup.partialBaseModel(ctx, prov, "vertical")
				: AssetLookup.partialBaseModel(ctx, prov);
	}

}
