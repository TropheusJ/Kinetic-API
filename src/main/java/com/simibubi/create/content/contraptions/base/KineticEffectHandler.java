package com.simibubi.create.content.contraptions.base;

import java.util.Random;

import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.foundation.advancement.AllTriggers;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.block.BeetrootsBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.GameMode;

public class KineticEffectHandler {

	int overStressedTime;
	float overStressedEffect;
	int particleSpawnCountdown;
	KineticTileEntity kte;

	public KineticEffectHandler(KineticTileEntity kte) {
		this.kte = kte;
	}

	public void tick() {
		GameMode world = kte.v();

		if (world.v) {
			if (overStressedTime > 0)
				if (--overStressedTime == 0)
					if (kte.isOverStressed()) {
						overStressedEffect = 1;
						spawnEffect(ParticleTypes.SMOKE, 0.2f, 5);
					} else {
						overStressedEffect = -1;
						spawnEffect(ParticleTypes.CLOUD, .075f, 2);
					}

			if (overStressedEffect != 0) {
				overStressedEffect -= overStressedEffect * .1f;
				if (Math.abs(overStressedEffect) < 1 / 128f)
					overStressedEffect = 0;
			}

		} else if (particleSpawnCountdown > 0) {
			if (--particleSpawnCountdown == 0)
				spawnRotationIndicators();
		}
	}

	public void queueRotationIndicators() {
		particleSpawnCountdown = 2;
	}

	public void spawnEffect(ParticleEffect particle, float maxMotion, int amount) {
		GameMode world = kte.v();
		if (world == null)
			return;
		if (!world.v)
			return;
		Random r = world.t;
		for (int i = 0; i < amount; i++) {
			EntityHitResult motion = VecHelper.offsetRandomly(EntityHitResult.a, r, maxMotion);
			EntityHitResult position = VecHelper.getCenterOf(kte.o());
			world.addParticle(particle, position.entity, position.c, position.d, motion.entity, motion.c, motion.d);
		}
	}

	public void spawnRotationIndicators() {
		float speed = kte.getSpeed();
		if (speed == 0)
			return;

		PistonHandler state = kte.p();
		BeetrootsBlock block = state.b();
		if (!(block instanceof KineticBlock))
			return;

		KineticBlock kb = (KineticBlock) block;
		float radius1 = kb.getParticleInitialRadius();
		float radius2 = kb.getParticleTargetRadius();

		Axis axis = kb.getRotationAxis(state);
		BlockPos pos = kte.o();
		GameMode world = kte.v();
		if (axis == null)
			return;
		if (world == null)
			return;

		char axisChar = axis.name().charAt(0);
		EntityHitResult vec = VecHelper.getCenterOf(pos);
		SpeedLevel speedLevel = SpeedLevel.of(speed);
		int color = speedLevel.getColor();
		int particleSpeed = speedLevel.getParticleSpeed();
		particleSpeed *= Math.signum(speed);

		if (world instanceof ServerWorld) {
			AllTriggers.triggerForNearbyPlayers(AllTriggers.ROTATION, world, pos, 5);
			RotationIndicatorParticleData particleData =
				new RotationIndicatorParticleData(color, particleSpeed, radius1, radius2, 10, axisChar);
			((ServerWorld) world).spawnParticles(particleData, vec.entity, vec.c, vec.d, 20, 0, 0, 0, 1);
		}
	}

	public void triggerOverStressedEffect() {
		overStressedTime = overStressedTime == 0 ? 2 : 0;
	}

}
