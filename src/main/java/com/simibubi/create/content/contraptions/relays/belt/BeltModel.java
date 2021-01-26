package com.simibubi.create.content.contraptions.relays.belt;

import static com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CASING_PROPERTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import elg;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IModelData;

public class BeltModel extends BakedModelWrapper<elg> {

	public BeltModel(elg template) {
		super(template);
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData extraData) {
		List<SpriteTexturedVertexConsumer> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));
		if (!extraData.hasProperty(CASING_PROPERTY))
			return quads;
		CasingType type = extraData.getData(CASING_PROPERTY);
		if (type == CasingType.NONE || type == CasingType.BRASS)
			return quads;

		SpriteShiftEntry spriteShift = AllSpriteShifts.ANDESIDE_BELT_CASING;

		for (int i = 0; i < quads.size(); i++) {
			SpriteTexturedVertexConsumer quad = quads.get(i);
			if (spriteShift == null)
				continue;
			if (quad.a() != spriteShift.getOriginal())
				continue;

			MipmapHelper original = quad.a();
			MipmapHelper target = spriteShift.getTarget();
			SpriteTexturedVertexConsumer newQuad = new SpriteTexturedVertexConsumer(Arrays.copyOf(quad.b(), quad.b().length),
				quad.d(), quad.e(), target, quad.f());

			Tessellator format = BufferBuilder.buffer;
			int[] vertexData = newQuad.b();

			for (int vertex = 0; vertex < vertexData.length; vertex += format.a()) {
				int uvOffset = 16 / 4;
				int uIndex = vertex + uvOffset;
				int vIndex = vertex + uvOffset + 1;
				float u = Float.intBitsToFloat(vertexData[uIndex]);
				float v = Float.intBitsToFloat(vertexData[vIndex]);
				vertexData[uIndex] =
					Float.floatToRawIntBits(target.a((SuperByteBuffer.getUnInterpolatedU(original, u))));
				vertexData[vIndex] =
					Float.floatToRawIntBits(target.b((SuperByteBuffer.getUnInterpolatedV(original, v))));
			}

			quads.set(i, newQuad);
		}
		return quads;
	}

}
