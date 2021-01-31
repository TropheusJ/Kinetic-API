package com.simibubi.kinetic_api.content.contraptions.fluids;

import java.util.Random;

import com.simibubi.kinetic_api.AllParticleTypes;
import com.simibubi.kinetic_api.content.contraptions.fluids.particle.FluidParticleData;
import com.simibubi.kinetic_api.foundation.fluid.FluidHelper;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import cut;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;
import net.minecraftforge.fluids.FluidStack;

public class FluidFX {

	static Random r = new Random();

	public static void splash(BlockPos pos, FluidStack fluidStack) {
		cut fluid = fluidStack.getFluid();
		if (fluid == FlowableFluid.FALLING)
			return;

		EmptyFluid defaultState = fluid.h();
		if (defaultState == null || defaultState.c()) {
			return;
		}

		BlockStateParticleEffect blockParticleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, defaultState.g());
		EntityHitResult center = VecHelper.getCenterOf(pos);

		for (int i = 0; i < 20; i++) {
			EntityHitResult v = VecHelper.offsetRandomly(EntityHitResult.a, r, .25f);
			particle(blockParticleData, center.e(v), v);
		}

	}

	public static ParticleEffect getFluidParticle(FluidStack fluid) {
		return new FluidParticleData(AllParticleTypes.FLUID_PARTICLE.get(), fluid);
	}

	public static ParticleEffect getDrippingParticle(FluidStack fluid) {
		ParticleEffect particle = null;
		if (FluidHelper.isWater(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_WATER;
		if (FluidHelper.isLava(fluid.getFluid()))
			particle = ParticleTypes.DRIPPING_LAVA;
		if (particle == null)
			particle = new FluidParticleData(AllParticleTypes.FLUID_DRIP.get(), fluid);
		return particle;
	}

	public static void spawnRimParticles(GameMode world, BlockPos pos, Direction side, int amount, ParticleEffect particle,
		float rimRadius) {
		EntityHitResult directionVec = EntityHitResult.b(side.getVector());
		for (int i = 0; i < amount; i++) {
			EntityHitResult vec = VecHelper.offsetRandomly(EntityHitResult.a, r, 1)
				.d();
			vec = VecHelper.clampComponentWise(vec, rimRadius)
				.h(VecHelper.axisAlingedPlaneOf(directionVec))
				.e(directionVec.a(.45 + r.nextFloat() / 16f));
			EntityHitResult m = vec.a(.05f);
			vec = vec.e(VecHelper.getCenterOf(pos));

			world.b(particle, vec.entity, vec.c - 1 / 16f, vec.d, m.entity, m.c, m.d);
		}
	}

	public static void spawnPouringLiquid(GameMode world, BlockPos pos, int amount, ParticleEffect particle,
		float rimRadius, EntityHitResult directionVec, boolean inbound) {
		for (int i = 0; i < amount; i++) {
			EntityHitResult vec = VecHelper.offsetRandomly(EntityHitResult.a, r, rimRadius * .75f);
			vec = vec.h(VecHelper.axisAlingedPlaneOf(directionVec))
				.e(directionVec.a(.5 + r.nextFloat() / 4f));
			EntityHitResult m = vec.a(1 / 4f);
			EntityHitResult centerOf = VecHelper.getCenterOf(pos);
			vec = vec.e(centerOf);
			if (inbound) {
				vec = vec.e(m);
				m = centerOf.e(directionVec.a(.5))
					.d(vec)
					.a(1 / 16f);
			}
			world.b(particle, vec.entity, vec.c - 1 / 16f, vec.d, m.entity, m.c, m.d);
		}
	}

	private static void particle(ParticleEffect data, EntityHitResult pos, EntityHitResult motion) {
		world().addParticle(data, pos.entity, pos.c, pos.d, motion.entity, motion.c, motion.d);
	}

	private static GameMode world() {
		return KeyBinding.B().r;
	}

}
