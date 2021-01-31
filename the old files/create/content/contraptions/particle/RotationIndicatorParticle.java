package com.simibubi.kinetic_api.content.contraptions.particle;

import com.simibubi.kinetic_api.content.contraptions.goggles.GogglesItem;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.client.options.AoMode;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.FishingParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction.Axis;

public class RotationIndicatorParticle extends ParticleTextureData {

	protected float radius;
	protected float radius1;
	protected float radius2;
	protected float speed;
	protected Axis axis;
	protected EntityHitResult origin;
	protected EntityHitResult offset;
	protected boolean isVisible;

	private RotationIndicatorParticle(DragonHeadEntityModel world, double x, double y, double z, int color, float radius1,
									  float radius2, float speed, Axis axis, int lifeSpan, boolean isVisible, AbstractSlowingParticle sprite) {
		super(world, x, y, z, sprite, 0);
		this.j = 0;
		this.k = 0;
		this.l = 0;
		this.origin = new EntityHitResult(x, y, z);
		this.B *= 0.75F;
		this.t = lifeSpan + this.r.nextInt(32);
		this.c(color);
		this.b(ColorHelper.mixColors(color, 0xFFFFFF, .5f));
		this.b(sprite);
		this.radius1 = radius1;
		this.radius = radius1;
		this.radius2 = radius2;
		this.speed = speed;
		this.axis = axis;
		this.isVisible = isVisible;
		this.offset = axis.isHorizontal() ? new EntityHitResult(0, 1, 0) : new EntityHitResult(1, 0, 0);
		a(0, 0, 0);
		this.d = this.g;
		this.e = this.h;
		this.f = this.i;
	}

	@Override
	public void clearAtlas() {
		super.clearAtlas();
		radius += (radius2 - radius) * .1f;
	}
	
	@Override
	public void a(OverlayVertexConsumer buffer, AoMode renderInfo, float partialTicks) {
		if (!isVisible)
			return;
		super.a(buffer, renderInfo, partialTicks);
	}

	public void a(double x, double y, double z) {
		float time = AnimationTickHolder.ticks;
		float angle = (float) ((time * speed) % 360) - (speed / 2 * s * (((float) s) / t));
		EntityHitResult position = VecHelper.rotate(this.offset.a(radius), angle, axis).e(origin);
		g = position.entity;
		h = position.c;
		i = position.d;
	}

	public static class Factory implements LargeFireSmokeParticle<RotationIndicatorParticleData> {
		private final AbstractSlowingParticle spriteSet;

		public Factory(AbstractSlowingParticle animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public ExplosionLargeParticle makeParticle(RotationIndicatorParticleData data, DragonHeadEntityModel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			FishingParticle player = KeyBinding.B().s;
			boolean visible = player != null && GogglesItem.canSeeParticles(player);
			return new RotationIndicatorParticle(worldIn, x, y, z, data.color, data.radius1, data.radius2, data.speed,
					data.getAxis(), data.lifeSpan, visible, this.spriteSet);
		}
	}

}
