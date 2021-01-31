package com.simibubi.kinetic_api.foundation.block.connected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import bqx;
import com.simibubi.kinetic_api.foundation.block.connected.ConnectedTextureBehaviour.CTContext;
import com.simibubi.kinetic_api.foundation.utility.Iterate;
import elg;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;
import net.minecraftforge.client.model.data.ModelProperty;

public class CTModel extends BakedModelWrapperWithData {

	protected static ModelProperty<CTData> CT_PROPERTY = new ModelProperty<>();
	private ConnectedTextureBehaviour behaviour;

	private class CTData {
		int[] indices;

		public CTData() {
			indices = new int[6];
			Arrays.fill(indices, -1);
		}

		void put(Direction face, int texture) {
			indices[face.getId()] = texture;
		}

		int get(Direction face) {
			return indices[face.getId()];
		}
	}
	
	public CTModel(elg originalModel, ConnectedTextureBehaviour behaviour) {
		super(originalModel);
		this.behaviour = behaviour;
	}

	@Override
	protected Builder gatherModelData(Builder builder, bqx world, BlockPos pos, PistonHandler state) {
		return builder.withInitial(CT_PROPERTY, createCTData(world, pos, state));
	}

	protected CTData createCTData(bqx world, BlockPos pos, PistonHandler state) {
		CTData data = new CTData();
		for (Direction face : Iterate.directions) {
			if (!BeetrootsBlock.c(state, world, pos, face) && !behaviour.buildContextForOccludedDirections())
				continue;
			CTSpriteShiftEntry spriteShift = behaviour.get(state, face);
			if (spriteShift == null)
				continue;
			CTContext ctContext = behaviour.buildContext(world, pos, state, face);
			data.put(face, spriteShift.getTextureIndex(ctContext));
		}
		return data;
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData extraData) {
		List<SpriteTexturedVertexConsumer> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));
		if (!extraData.hasProperty(CT_PROPERTY))
			return quads;
		CTData data = extraData.getData(CT_PROPERTY);

		for (int i = 0; i < quads.size(); i++) {
			SpriteTexturedVertexConsumer quad = quads.get(i);

			CTSpriteShiftEntry spriteShift = behaviour.get(state, quad.e());
			if (spriteShift == null)
				continue;
			if (quad.a() != spriteShift.getOriginal())
				continue;
			int index = data.get(quad.e());
			if (index == -1)
				continue;

			SpriteTexturedVertexConsumer newQuad = new SpriteTexturedVertexConsumer(Arrays.copyOf(quad.b(), quad.b().length),
				quad.d(), quad.e(), quad.a(), quad.f());
			Tessellator format = BufferBuilder.buffer;
			int[] vertexData = newQuad.b();

			for (int vertex = 0; vertex < vertexData.length; vertex += format.a()) {
				int uvOffset = 16 / 4;
				int uIndex = vertex + uvOffset;
				int vIndex = vertex + uvOffset + 1;
				float u = Float.intBitsToFloat(vertexData[uIndex]);
				float v = Float.intBitsToFloat(vertexData[vIndex]);
				vertexData[uIndex] = Float.floatToRawIntBits(spriteShift.getTargetU(u, index));
				vertexData[vIndex] = Float.floatToRawIntBits(spriteShift.getTargetV(v, index));
			}
			quads.set(i, newQuad);
		}
		return quads;
	}

}
