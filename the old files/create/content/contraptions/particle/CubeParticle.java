package com.simibubi.kinetic_api.content.contraptions.particle;

import org.lwjgl.opengl.GL11;
import afj;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.options.AoMode;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.FixedColorVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class CubeParticle extends ExplosionLargeParticle {

	public static final EntityHitResult[] CUBE = {
		// TOP
		new EntityHitResult(1, 1, -1), new EntityHitResult(1, 1, 1), new EntityHitResult(-1, 1, 1), new EntityHitResult(-1, 1, -1),

		// BOTTOM
		new EntityHitResult(-1, -1, -1), new EntityHitResult(-1, -1, 1), new EntityHitResult(1, -1, 1), new EntityHitResult(1, -1, -1),

		// FRONT
		new EntityHitResult(-1, -1, 1), new EntityHitResult(-1, 1, 1), new EntityHitResult(1, 1, 1), new EntityHitResult(1, -1, 1),

		// BACK
		new EntityHitResult(1, -1, -1), new EntityHitResult(1, 1, -1), new EntityHitResult(-1, 1, -1), new EntityHitResult(-1, -1, -1),

		// LEFT
		new EntityHitResult(-1, -1, -1), new EntityHitResult(-1, 1, -1), new EntityHitResult(-1, 1, 1), new EntityHitResult(-1, -1, 1),

		// RIGHT
		new EntityHitResult(1, -1, 1), new EntityHitResult(1, 1, 1), new EntityHitResult(1, 1, -1), new EntityHitResult(1, -1, -1) };

	public static final EntityHitResult[] CUBE_NORMALS = {
		// modified normals for the sides
		new EntityHitResult(0, 1, 0), new EntityHitResult(0, -1, 0), new EntityHitResult(0, 0, 1), new EntityHitResult(0, 0, 1), new EntityHitResult(0, 0, 1),
		new EntityHitResult(0, 0, 1),

		/*
		 * new Vector3d(0, 1, 0), new Vector3d(0, -1, 0), new Vector3d(0, 0, 1), new Vector3d(0, 0,
		 * -1), new Vector3d(-1, 0, 0), new Vector3d(1, 0, 0)
		 */
	};

	private static final LavaEmberParticle renderType = new LavaEmberParticle() {
		@Override
		public void a(GlShader builder, MissingSprite textureManager) {
			RenderSystem.disableTexture();

			// transparent, additive blending
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(WindowSettings.q.e, WindowSettings.j.e);
			RenderSystem.enableLighting();
			RenderSystem.enableColorMaterial();

			// opaque
//			RenderSystem.depthMask(true);
//			RenderSystem.disableBlend();
//			RenderSystem.enableLighting();

			builder.a(GL11.GL_QUADS, BufferBuilder.buffer);
		}

		@Override
		public void a(FixedColorVertexConsumer tessellator) {
			tessellator.b();
			RenderSystem.blendFunc(WindowSettings.q.l,
				WindowSettings.j.j);
			RenderSystem.disableLighting();
			RenderSystem.enableTexture();
		}
	};

	protected float scale;
	protected boolean hot;

	public CubeParticle(DragonHeadEntityModel world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z);
		this.j = motionX;
		this.k = motionY;
		this.l = motionZ;

		setScale(0.2F);
	}

	public void setScale(float scale) {
		this.scale = scale;
		this.a(scale * 0.5f, scale * 0.5f);
	}

	public void averageAge(int age) {
		this.t = (int) (age + (r.nextDouble() * 2D - 1D) * 8);
	}
	
	public void setHot(boolean hot) {
		this.hot = hot;
	}
	
	private boolean billowing = false;
	
	@Override
	public void a() {
		if (this.hot && this.s > 0) {
			if (this.e == this.h) {
				billowing = true;
				B = false; // Prevent motion being ignored due to vertical collision
				if (this.j == 0 && this.l == 0) {
					EntityHitResult diff = EntityHitResult.b(new BlockPos(g, h, i)).b(0.5, 0.5, 0.5).a(g, h, i);
					this.j = -diff.entity * 0.1;
					this.l = -diff.d * 0.1;
				}
				this.j *= 1.1;
				this.k *= 0.9;
				this.l *= 1.1;
			} else if (billowing) {
				this.k *= 1.2;
			}
		}
		super.a();
	}

	@Override
	public void a(OverlayVertexConsumer builder, AoMode renderInfo, float p_225606_3_) {
		EntityHitResult projectedView = renderInfo.b();
		float lerpedX = (float) (afj.d(p_225606_3_, this.d, this.g) - projectedView.getX());
		float lerpedY = (float) (afj.d(p_225606_3_, this.e, this.h) - projectedView.getY());
		float lerpedZ = (float) (afj.d(p_225606_3_, this.f, this.i) - projectedView.getZ());

		// int light = getBrightnessForRender(p_225606_3_);
		int light = 15728880;// 15<<20 && 15<<4
		double ageMultiplier = 1 - Math.pow(s, 3) / Math.pow(t, 3);

		for (int i = 0; i < 6; i++) {
			// 6 faces to a cube
			for (int j = 0; j < 4; j++) {
				EntityHitResult vec = CUBE[i * 4 + j];
				vec = vec
					/* .rotate(?) */
					.a(scale * ageMultiplier)
					.b(lerpedX, lerpedY, lerpedZ);

				EntityHitResult normal = CUBE_NORMALS[i];
				builder.a(vec.entity, vec.c, vec.d)
					.a(v, w, x, y)
					.a(0, 0)
					.a(light)
					.b((float) normal.entity, (float) normal.c, (float) normal.d)
					.d();
			}
		}
	}

	@Override
	public LavaEmberParticle b() {
		return renderType;
	}

	public static class Factory implements LargeFireSmokeParticle<CubeParticleData> {

		public Factory() {}

		@Override
		public ExplosionLargeParticle makeParticle(CubeParticleData data, DragonHeadEntityModel world, double x, double y, double z, double motionX,
			double motionY, double motionZ) {
			CubeParticle particle = new CubeParticle(world, x, y, z, motionX, motionY, motionZ);
			particle.a(data.r, data.g, data.b);
			particle.setScale(data.scale);
			particle.averageAge(data.avgAge);
			particle.setHot(data.hot);
			return particle;
		}
	}
}
