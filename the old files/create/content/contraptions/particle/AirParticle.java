package com.simibubi.kinetic_api.content.contraptions.particle;

import afj;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;

public class AirParticle extends ParticleTextureData {

	private float originX, originY, originZ;
	private float targetX, targetY, targetZ;
	private float drag;

	private float twirlRadius, twirlAngleOffset;
	private Axis twirlAxis;

	protected AirParticle(DragonHeadEntityModel world, AirParticleData data, double x, double y, double z, double dx, double dy,
						  double dz, AbstractSlowingParticle sprite) {
		super(world, x, y, z, sprite, world.t.nextFloat() * .5f);
		B *= 0.75F;
		n = false;

		b(g, h, i);
		originX = (float) (d = g);
		originY = (float) (e = h);
		originZ = (float) (f = i);
		targetX = (float) (x + dx);
		targetY = (float) (y + dy);
		targetZ = (float) (z + dz);
		drag = data.drag;

		twirlRadius = Create.random.nextFloat() / 6;
		twirlAngleOffset = Create.random.nextFloat() * 360;
		twirlAxis = Create.random.nextBoolean() ? Axis.X : Axis.Z;

		// speed in m/ticks
		t = Math.min((int) (new EntityHitResult(dx, dy, dz).f() / data.speed), 60);
		selectSprite(7);
		e(.25f);
	}

	public LavaEmberParticle b() {
		return LavaEmberParticle.c;
	}

	@Override
	public void clearAtlas() {
		this.d = this.g;
		this.e = this.h;
		this.f = this.i;
		if (this.s++ >= this.t) {
			this.j();
			return;
		}

		float progress = (float) Math.pow(((float) s) / t, drag);
		float angle = (progress * 2 * 360 + twirlAngleOffset) % 360;
		EntityHitResult twirl = VecHelper.rotate(new EntityHitResult(0, twirlRadius, 0), angle, twirlAxis);
		
		float x = (float) (afj.g(progress, originX, targetX) + twirl.entity);
		float y = (float) (afj.g(progress, originY, targetY) + twirl.c);
		float z = (float) (afj.g(progress, originZ, targetZ) + twirl.d);
		
		j = x - g;
		k = y - h;
		l = z - i;

		b(textureList);
		this.a(this.j, this.k, this.l);
	}

	public int a(float partialTick) {
		BlockPos blockpos = new BlockPos(this.g, this.h, this.i);
		return this.c.p(blockpos) ? JsonGlProgram.a(c, blockpos) : 0;
	}

	private void selectSprite(int index) {
		a(textureList.a(index, 8));
	}

	public static class Factory implements LargeFireSmokeParticle<AirParticleData> {
		private final AbstractSlowingParticle spriteSet;

		public Factory(AbstractSlowingParticle animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public ExplosionLargeParticle makeParticle(AirParticleData data, DragonHeadEntityModel worldIn, double x, double y, double z, double xSpeed,
			double ySpeed, double zSpeed) {
			return new AirParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}

}
