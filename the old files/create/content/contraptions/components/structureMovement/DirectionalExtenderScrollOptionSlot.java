package com.simibubi.kinetic_api.content.contraptions.components.structureMovement;

import java.util.function.BiPredicate;
import net.minecraft.block.enums.BambooLeaves;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import com.simibubi.kinetic_api.foundation.tileEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.MatrixStacker;

public class DirectionalExtenderScrollOptionSlot extends CenteredSideValueBoxTransform {

	public DirectionalExtenderScrollOptionSlot(BiPredicate<PistonHandler, Direction> allowedDirections) {
		super(allowedDirections);
	}

	@Override
	protected EntityHitResult getLocalOffset(PistonHandler state) {
		return super.getLocalOffset(state)
				.e(EntityHitResult.b(state.c(BambooLeaves.M).getVector()).a(-2 / 16f));
	}

	@Override
	protected void rotate(PistonHandler state, BufferVertexConsumer ms) {
		if (!getSide().getAxis().isHorizontal())
			MatrixStacker.of(ms).rotateY(AngleHelper.horizontalAngle(state.c(BambooLeaves.M)) - 90);
		super.rotate(state, ms);
	}
}