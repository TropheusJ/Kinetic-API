package com.simibubi.create.foundation.block.connected;

import bqx;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;

public abstract class BakedModelWrapperWithData extends BakedModelWrapper<elg> {

	public BakedModelWrapperWithData(elg originalModel) {
		super(originalModel);
	}

	@Override
	public final IModelData getModelData(bqx world, BlockPos pos, PistonHandler state, IModelData tileData) {
		Builder builder = new ModelDataMap.Builder();
		if (originalModel instanceof BakedModelWrapperWithData)
			((BakedModelWrapperWithData) originalModel).gatherModelData(builder, world, pos, state);
		return gatherModelData(builder, world, pos, state).build();
	}

	protected abstract ModelDataMap.Builder gatherModelData(ModelDataMap.Builder builder, bqx world,
		BlockPos pos, PistonHandler state);

}
