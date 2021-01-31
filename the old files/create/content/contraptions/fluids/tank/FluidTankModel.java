package com.simibubi.kinetic_api.content.contraptions.fluids.tank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import bqx;
import com.simibubi.kinetic_api.AllSpriteShifts;
import com.simibubi.kinetic_api.foundation.block.connected.CTModel;
import com.simibubi.kinetic_api.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class FluidTankModel extends CTModel {

	protected static ModelProperty<CullData> CULL_PROPERTY = new ModelProperty<>();

	public static FluidTankModel standard(elg originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.FLUID_TANK, AllSpriteShifts.COPPER_CASING);
	}
	
	public static FluidTankModel creative(elg originalModel) {
		return new FluidTankModel(originalModel, AllSpriteShifts.CREATIVE_FLUID_TANK, AllSpriteShifts.CREATIVE_CASING);
	}
	
	private FluidTankModel(elg originalModel, CTSpriteShiftEntry side, CTSpriteShiftEntry top) {
		super(originalModel, new FluidTankCTBehaviour(side, top));
	}
	
	@Override
	protected Builder gatherModelData(Builder builder, bqx world, BlockPos pos, PistonHandler state) {
		CullData cullData = new CullData();
		for (Direction d : Iterate.horizontalDirections)
			cullData.setCulled(d, FluidTankConnectivityHandler.isConnected(world, pos, pos.offset(d)));
		return super.gatherModelData(builder, world, pos, state).withInitial(CULL_PROPERTY, cullData);
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData extraData) {
		if (side != null)
			return Collections.emptyList();

		List<SpriteTexturedVertexConsumer> quads = new ArrayList<>();
		for (Direction d : Iterate.directions) {
			if (extraData.hasProperty(CULL_PROPERTY) && extraData.getData(CULL_PROPERTY)
				.isCulled(d))
				continue;
			quads.addAll(super.getQuads(state, d, rand, extraData));
		}
		quads.addAll(super.getQuads(state, null, rand, extraData));
		return quads;
	}

	private class CullData {
		boolean[] culledFaces;

		public CullData() {
			culledFaces = new boolean[4];
			Arrays.fill(culledFaces, false);
		}

		void setCulled(Direction face, boolean cull) {
			if (face.getAxis()
				.isVertical())
				return;
			culledFaces[face.getHorizontal()] = cull;
		}

		boolean isCulled(Direction face) {
			if (face.getAxis()
				.isVertical())
				return false;
			return culledFaces[face.getHorizontal()];
		}
	}

}
