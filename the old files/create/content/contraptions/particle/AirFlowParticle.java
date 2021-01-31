package com.simibubi.kinetic_api.content.contraptions.particle;

import javax.annotation.Nonnull;
import afj;
import com.simibubi.kinetic_api.Create;
import com.simibubi.kinetic_api.content.contraptions.components.fan.IAirCurrentSource;
import com.simibubi.kinetic_api.content.logistics.InWorldProcessing;
import com.simibubi.kinetic_api.foundation.utility.ColorHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import net.minecraft.block.BellBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.particle.AbstractSlowingParticle;
import net.minecraft.client.particle.ExplosionLargeParticle;
import net.minecraft.client.particle.LargeFireSmokeParticle;
import net.minecraft.client.particle.LavaEmberParticle;
import net.minecraft.client.particle.ParticleTextureData;
import net.minecraft.client.render.entity.model.DragonHeadEntityModel;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class AirFlowParticle extends ParticleTextureData {

	private final IAirCurrentSource source;

	protected AirFlowParticle(DragonHeadEntityModel world, IAirCurrentSource source, double x, double y, double z,
							  AbstractSlowingParticle sprite) {
		super(world, x, y, z, sprite, world.t.nextFloat() * .5f);
		this.source = source;
		this.B *= 0.75F;
		this.t = 40;
		n = false;
		selectSprite(7);
		EntityHitResult offset = VecHelper.offsetRandomly(EntityHitResult.a, Create.random, .25f);
		this.b(g + offset.entity, h + offset.c, i + offset.d);
		this.d = g;
		this.e = h;
		this.f = i;
		e(.25f);
	}

	@Nonnull
	public LavaEmberParticle b() {
		return LavaEmberParticle.c;
	}

	@Override
	public void clearAtlas() {
		if (source == null || source.isSourceRemoved()) {
			dissipate();
			return;
		}
		this.d = this.g;
		this.e = this.h;
		this.f = this.i;
		if (this.s++ >= this.t) {
			this.j();
		} else {
			if (source.getAirCurrent() == null || !source.getAirCurrent().bounds.g(.25f).e(g, h, i)) {
				dissipate();
				return;
			}

			EntityHitResult directionVec = EntityHitResult.b(source.getAirCurrent().direction.getVector());
			EntityHitResult motion = directionVec.a(1 / 8f);
			if (!source.getAirCurrent().pushing)
				motion = motion.a(-1);

			double distance = new EntityHitResult(g, h, i).d(VecHelper.getCenterOf(source.getAirCurrentPos()))
					.h(directionVec).f() - .5f;
			if (distance > source.getAirCurrent().maxDistance + 1 || distance < -.25f) {
				dissipate();
				return;
			}
			motion = motion.a(source.getAirCurrent().maxDistance - (distance - 1f)).a(.5f);
			selectSprite((int) afj.a((distance / source.getAirCurrent().maxDistance) * 8 + c.t.nextInt(4),
					0, 7));

			morphType(distance);

			j = motion.entity;
			k = motion.c;
			l = motion.d;

			if (this.m) {
				this.j *= 0.7;
				this.l *= 0.7;
			}
			this.a(this.j, this.k, this.l);

		}

	}

	public void morphType(double distance) {
		if(source.getAirCurrent() == null)
			return;
		InWorldProcessing.Type type = source.getAirCurrent().getSegmentAt((float) distance);

		if (type == InWorldProcessing.Type.SPLASHING) {
			b(ColorHelper.mixColors(0x4499FF, 0x2277FF, c.t.nextFloat()));
			e(1f);
			selectSprite(c.t.nextInt(3));
			if (c.t.nextFloat() < 1 / 32f)
				c.addParticle(ParticleTypes.BUBBLE, g, h, i, j * .125f, k * .125f,
						l * .125f);
			if (c.t.nextFloat() < 1 / 32f)
				c.addParticle(ParticleTypes.BUBBLE_POP, g, h, i, j * .125f, k * .125f,
						l * .125f);
		}

		if (type == InWorldProcessing.Type.SMOKING) {
			b(ColorHelper.mixColors(0x0, 0x555555, c.t.nextFloat()));
			e(1f);
			selectSprite(c.t.nextInt(3));
			if (c.t.nextFloat() < 1 / 32f)
				c.addParticle(ParticleTypes.SMOKE, g, h, i, j * .125f, k * .125f,
						l * .125f);
			if (c.t.nextFloat() < 1 / 32f)
				c.addParticle(ParticleTypes.LARGE_SMOKE, g, h, i, j * .125f, k * .125f,
						l * .125f);
		}

		if (type == InWorldProcessing.Type.BLASTING) {
			b(ColorHelper.mixColors(0xFF4400, 0xFF8855, c.t.nextFloat()));
			e(.5f);
			selectSprite(c.t.nextInt(3));
			if (c.t.nextFloat() < 1 / 32f)
				c.addParticle(ParticleTypes.FLAME, g, h, i, j * .25f, k * .25f,
						l * .25f);
			if (c.t.nextFloat() < 1 / 16f)
				c.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, BellBlock.B.n()), g, h,
						i, j * .25f, k * .25f, l * .25f);
		}

		if (type == null) {
			b(0xEEEEEE);
			e(.25f);
			a(.2f, .2f);
		}
	}

	private void dissipate() {
		j();
	}

	public int a(float partialTick) {
		BlockPos blockpos = new BlockPos(this.g, this.h, this.i);
		return this.c.p(blockpos) ? JsonGlProgram.a(c, blockpos) : 0;
	}

	private void selectSprite(int index) {
		a(textureList.a(index, 8));
	}

	public static class Factory implements LargeFireSmokeParticle<AirFlowParticleData> {
		private final AbstractSlowingParticle spriteSet;

		public Factory(AbstractSlowingParticle animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		public ExplosionLargeParticle makeParticle(AirFlowParticleData data, DragonHeadEntityModel worldIn, double x, double y, double z,
				double xSpeed, double ySpeed, double zSpeed) {
			BeehiveBlockEntity te = worldIn.c(new BlockPos(data.posX, data.posY, data.posZ));
			if (!(te instanceof IAirCurrentSource))
				te = null;
			return new AirFlowParticle(worldIn, (IAirCurrentSource) te, x, y, z, this.spriteSet);
		}
	}

}
