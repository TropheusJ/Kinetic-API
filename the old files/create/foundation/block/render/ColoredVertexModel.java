package com.simibubi.kinetic_api.foundation.block.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import bqx;
import com.simibubi.kinetic_api.foundation.block.IBlockVertexColor;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

public class ColoredVertexModel extends BakedModelWrapper<elg> {

	private IBlockVertexColor color;
	private static ModelProperty<BlockPos> POSITION_PROPERTY = new ModelProperty<>();

	public ColoredVertexModel(elg originalModel, IBlockVertexColor color) {
		super(originalModel);
		this.color = color;
	}

	@Override
	public IModelData getModelData(bqx world, BlockPos pos, PistonHandler state, IModelData tileData) {
		return new ModelDataMap.Builder().withInitial(POSITION_PROPERTY, pos).build();
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData extraData) {
		List<SpriteTexturedVertexConsumer> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));
		if (!extraData.hasProperty(POSITION_PROPERTY))
			return quads;
		if (quads.isEmpty())
			return quads;
		
		// Optifine might've rejigged vertex data_unused
		Tessellator format = BufferBuilder.buffer;
		int colorIndex = 0;
		for (int j = 0; j < format.c().size(); j++) {
			VertexBuffer e = format.c().get(j);
			if (e.b() == VertexBuffer.b.c)
				colorIndex = j;
		}
		int colorOffset = format.getOffset(colorIndex) / 4; 
		BlockPos data = extraData.getData(POSITION_PROPERTY);
		
		for (int i = 0; i < quads.size(); i++) {
			SpriteTexturedVertexConsumer quad = quads.get(i);
			SpriteTexturedVertexConsumer newQuad = new SpriteTexturedVertexConsumer(Arrays.copyOf(quad.b(), quad.b().length),
					quad.d(), quad.e(), quad.a(), quad.f());
			int[] vertexData = newQuad.b();

			for (int vertex = 0; vertex < vertexData.length; vertex += format.a()) {
				float x = Float.intBitsToFloat(vertexData[vertex]);
				float y = Float.intBitsToFloat(vertexData[vertex + 1]);
				float z = Float.intBitsToFloat(vertexData[vertex + 2]);
				int color = this.color.getColor(x + data.getX(), y + data.getY(), z + data.getZ());
				vertexData[vertex + colorOffset] = color;
			}

			quads.set(i, newQuad);
		}
		return quads;
	}

}
