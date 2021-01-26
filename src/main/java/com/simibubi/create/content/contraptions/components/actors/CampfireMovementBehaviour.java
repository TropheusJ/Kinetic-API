package com.simibubi.create.content.contraptions.components.actors;

import java.util.Random;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.particle.ParticleTypes;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;

public class CampfireMovementBehaviour extends MovementBehaviour {
	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void tick(MovementContext context) {
		if (context.world == null || !context.world.v || context.position == null
			|| !context.state.c(AbstractButtonBlock.CEILING_X_SHAPE))
			return;

		// Mostly copied from CampfireBlock and CampfireTileEntity
		Random random = context.world.t;
		if (random.nextFloat() < 0.11F) {
			for (int i = 0; i < random.nextInt(2) + 2; ++i) {
				context.world.b(
					context.state.c(AbstractButtonBlock.CEILING_Z_SHAPE) ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE
						: ParticleTypes.CAMPFIRE_COSY_SMOKE,
					true, context.position.getX() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D),
					context.position.getY() + random.nextDouble() + random.nextDouble(),
					context.position.getZ() + random.nextDouble() / (random.nextBoolean() ? 3D : -3D), 0.0D, 0.07D,
					0.0D);
			}
		}
	}
}
