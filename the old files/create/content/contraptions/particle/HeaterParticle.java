package com.simibubi.kinetic_api.content.contraptions.particle;

import afj;
import javax.annotation.ParametersAreNonnullByDefault;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeaterParticle extends ParticleTextureData {

	private final AbstractSlowingParticle animatedSprite;

	public HeaterParticle(DragonHeadEntityModel worldIn, float r, float g, float b, double x, double y, double z, double vx, double vy,
						  double vz, AbstractSlowingParticle spriteSet) {
		super(worldIn, x, y, z, spriteSet, worldIn.t.nextFloat() * .5f);

		this.animatedSprite = spriteSet;

		this.j = this.j * (double) 0.01F + vx;
		this.k = this.k * (double) 0.01F + vy;
		this.l = this.l * (double) 0.01F + vz;

		this.v = r;
		this.w = g;
		this.x = b;

		this.g += (this.r.nextFloat() - this.r.nextFloat()) * 0.05F;
		this.h += (this.r.nextFloat() - this.r.nextFloat()) * 0.05F;
		this.i += (this.r.nextFloat() - this.r.nextFloat()) * 0.05F;

		this.t = (int) (8.0D / (Math.random() * 0.8D + 0.2D)) + 4;
		this.B *= 1.875F;
		this.b(animatedSprite);

	}

	@Override
	public LavaEmberParticle b() {
		return LavaEmberParticle.d;
	}

	@Override
	public float b(float p_217561_1_) {
		float f = ((float) this.s + p_217561_1_) / (float) this.t;
		return this.B * (1.0F - f * f * 0.5F);
	}

	@Override
	public void a(double x, double y, double z) {
		this.a(this.m()
			.d(x, y, z));
		this.k();
	}

	@Override
	public int a(float p_189214_1_) {
		float f = ((float) this.s + p_189214_1_) / (float) this.t;
		f = afj.a(f, 0.0F, 1.0F);
		int i = super.a(p_189214_1_);
		int j = i & 255;
		int k = i >> 16 & 255;
		j = j + (int) (f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Override
	public void clearAtlas() {
		this.d = this.g;
		this.e = this.h;
		this.f = this.i;
		if (this.s++ >= this.t) {
			this.j();
		} else {
			this.b(animatedSprite);
			this.a(this.j, this.k, this.l);
			this.j *= (double) 0.96F;
			this.k *= (double) 0.96F;
			this.l *= (double) 0.96F;
			if (this.m) {
				this.j *= (double) 0.7F;
				this.l *= (double) 0.7F;
			}
		}
	}

	public static class Factory implements LargeFireSmokeParticle<HeaterParticleData> {
		private final AbstractSlowingParticle spriteSet;

		public Factory(AbstractSlowingParticle animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		@Override
		public ExplosionLargeParticle makeParticle(HeaterParticleData data, DragonHeadEntityModel worldIn, double x, double y, double z, double vx,
			double vy, double vz) {
			return new HeaterParticle(worldIn, data.r, data.g, data.b, x, y, z, vx, vy, vz, this.spriteSet);
		}
	}
}
