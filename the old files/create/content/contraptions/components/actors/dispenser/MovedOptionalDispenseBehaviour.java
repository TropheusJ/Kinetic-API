package com.simibubi.kinetic_api.content.contraptions.components.actors.dispenser;

import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.math.BlockPos;

public class MovedOptionalDispenseBehaviour extends MovedDefaultDispenseItemBehaviour {
	protected boolean successful = true;

	@Override
	protected void playDispenseSound(GrassColors world, BlockPos pos) {
		world.syncWorldEvent(this.successful ? 1000 : 1001, pos, 0);
	}
}
