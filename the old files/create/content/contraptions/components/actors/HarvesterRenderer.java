package com.simibubi.kinetic_api.content.contraptions.components.actors;

import static net.minecraft.block.enums.BambooLeaves.O;

import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.kinetic_api.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.AnimationTickHolder;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import com.simibubi.kinetic_api.foundation.utility.VecHelper;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class HarvesterRenderer extends SafeTileEntityRenderer<HarvesterTileEntity> {

	public HarvesterRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(HarvesterTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		PistonHandler blockState = te.p();
		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOnHorizontal(blockState);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(VertexConsumerProvider.d()));
	}

	public static void renderInContraption(MovementContext context, BufferVertexConsumer ms, BufferVertexConsumer msLocal,
		BackgroundRenderer buffers) {
		PistonHandler blockState = context.state;
		Direction facing = blockState.c(O);
		SuperByteBuffer superBuffer = AllBlockPartials.HARVESTER_BLADE.renderOn(blockState);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
			? context.getAnimationSpeed()
			: 0);

		if (context.contraption.stalled)
			speed = 0;
		float time = AnimationTickHolder.getRenderTick() / 20;
		float angle = (time * speed) % 360;
		float originOffset = 1 / 16f;
		EntityHitResult rotOffset = new EntityHitResult(0, -2 * originOffset, originOffset).e(VecHelper.getCenterOf(BlockPos.ORIGIN));

		superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.translate(rotOffset.entity, rotOffset.c, rotOffset.d)
			.rotate(Direction.WEST, AngleHelper.rad(angle))
			.translate(-rotOffset.entity, -rotOffset.c, -rotOffset.d)
			.light(msLocal.c()
				.a())
			.renderInto(ms, buffers.getBuffer(VertexConsumerProvider.d()));
	}

	public static void transformHead(BufferVertexConsumer ms, float angle) {}

}
