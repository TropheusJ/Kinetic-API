package com.simibubi.kinetic_api.foundation.block.render;

import elg;
import java.util.List;
import java.util.Random;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class WrappedBakedModel implements elg {

	protected elg template;

	public WrappedBakedModel(elg template) {
		this.template = template;
	}
	
	@Override
	public elg getBakedModel() {
		return template;
	}

	@Override
	public boolean a() {
		return template.a();
	}

	@Override
	public boolean b() {
		return template.b();
	}

	@Override
	public boolean d() {
		return template.d();
	}

	@Override
	public MipmapHelper getParticleTexture(IModelData data) {
		return template.getParticleTexture(data);
	}

	@Override
	public ModelElementFace g() {
		return template.g();
	}

	@Override
	public elg handlePerspective(b cameraTransformType, BufferVertexConsumer mat) {
		template.handlePerspective(cameraTransformType, mat);
		return this;
	}

	@Override
	public List<SpriteTexturedVertexConsumer> a(PistonHandler state, Direction side, Random rand) {
		return getQuads(state, side, rand, EmptyModelData.INSTANCE);
	}

	@Override
	public List<SpriteTexturedVertexConsumer> getQuads(PistonHandler state, Direction side, Random rand, IModelData data) {
		return template.getQuads(state, side, rand, data);
	}

	@Override
	public MipmapHelper e() {
		return getParticleTexture(EmptyModelData.INSTANCE);
	}

	@Override
	public boolean c() {
		return template.c();
	}
}
