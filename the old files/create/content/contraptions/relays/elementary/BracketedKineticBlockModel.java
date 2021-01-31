package com.simibubi.kinetic_api.content.contraptions.relays.elementary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import bqx;
import com.simibubi.kinetic_api.foundation.block.render.WrappedBakedModel;
import com.simibubi.kinetic_api.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.kinetic_api.foundation.utility.VirtualEmptyModelData;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class BracketedKineticBlockModel extends WrappedBakedModel {

	private static ModelProperty<BracketedModelData> BRACKET_PROPERTY = new ModelProperty<>();

	public BracketedKineticBlockModel(elg template) {
		super(template);
	}

	@Override
	public IModelData getModelData(bqx world, BlockPos pos, PistonHandler state, IModelData tileData) {
		if (tileData == VirtualEmptyModelData.INSTANCE)
			return tileData;
		BracketedModelData data = new BracketedModelData();
		BracketedTileEntityBehaviour attachmentBehaviour =
			TileEntityBehaviour.get(world, pos, BracketedTileEntityBehaviour.TYPE);
		if (attachmentBehaviour != null)
			data.putBracket(attachmentBehaviour.getBracket());
		return new ModelDataMap.Builder().withInitial(BRACKET_PROPERTY, data)
			.build();
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData data) {
		if (data instanceof ModelDataMap) {
			List<SpriteTexturedVertexConsumer> quads = new ArrayList<>();
			ModelDataMap modelDataMap = (ModelDataMap) data;
			if (modelDataMap.hasProperty(BRACKET_PROPERTY)) {
				quads = new ArrayList<>(quads);
				addQuads(quads, state, side, rand, modelDataMap, modelDataMap.getData(BRACKET_PROPERTY));
			}
			return quads;
		}
		return super.getQuads(state, side, rand, data);
	}

	private void addQuads(List<SpriteTexturedVertexConsumer> quads, PistonHandler state, Direction side, Random rand, IModelData data,
		BracketedModelData pipeData) {
		elg bracket = pipeData.getBracket();
		if (bracket == null)
			return;
		quads.addAll(bracket.getQuads(state, side, rand, data));
	}

	private class BracketedModelData {
		elg bracket;

		public void putBracket(PistonHandler state) {
			this.bracket = KeyBinding.B()
				.aa()
				.a(state);
		}

		public elg getBracket() {
			return bracket;
		}

	}

}
