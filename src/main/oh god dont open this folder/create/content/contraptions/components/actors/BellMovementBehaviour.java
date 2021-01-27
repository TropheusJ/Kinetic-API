package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementBehaviour;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import net.minecraft.client.sound.MusicType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

public class BellMovementBehaviour extends MovementBehaviour {
	@Override
	public boolean hasSpecialMovementRenderer() {
		return false;
	}

	@Override
	public void onSpeedChanged(MovementContext context, EntityHitResult oldMotion, EntityHitResult motion) {
		double dotProduct = oldMotion.b(motion);

		if (dotProduct <= 0 && (context.relativeMotion.f() != 0) || context.firstMovement)
			context.world.a(null, new BlockPos(context.position), MusicType.aJ,
				SoundEvent.e, 2.0F, 1.0F);
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.position != null)
			context.world.a(null, new BlockPos(context.position), MusicType.aJ, SoundEvent.e,
				2.0F, 1.0F);
	}
}
