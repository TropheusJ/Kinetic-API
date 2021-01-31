package com.simibubi.kinetic_api.content.contraptions.components.clock;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.content.contraptions.components.clock.CuckooClockTileEntity.Animation;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;

public class CuckooClockRenderer extends KineticTileEntityRenderer {

	public CuckooClockRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (!(te instanceof CuckooClockTileEntity))
			return;

		CuckooClockTileEntity clock = (CuckooClockTileEntity) te;
		PistonHandler blockState = te.p();
		int packedLightmapCoords = JsonGlProgram.a(te.v(), blockState, te.o());
		Direction direction = blockState.c(CuckooClockBlock.HORIZONTAL_FACING);

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		// Render Hands
		SuperByteBuffer hourHand = AllBlockPartials.CUCKOO_HOUR_HAND.renderOn(blockState);
		SuperByteBuffer minuteHand = AllBlockPartials.CUCKOO_MINUTE_HAND.renderOn(blockState);
		float hourAngle = clock.hourHand.get(partialTicks);
		float minuteAngle = clock.minuteHand.get(partialTicks);
		rotateHand(hourHand, hourAngle, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);
		rotateHand(minuteHand, minuteAngle, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);

		// Doors
		SuperByteBuffer leftDoor = AllBlockPartials.CUCKOO_LEFT_DOOR.renderOn(blockState);
		SuperByteBuffer rightDoor = AllBlockPartials.CUCKOO_RIGHT_DOOR.renderOn(blockState);
		float angle = 0;
		float offset = 0;

		if (clock.animationType != null) {
			float value = clock.animationProgress.get(partialTicks);
			int step = clock.animationType == Animation.SURPRISE ? 3 : 15;
			for (int phase = 30; phase <= 60; phase += step) {
				float local = value - phase;
				if (local < -step / 3)
					continue;
				else if (local < 0)
					angle = afj.g(((value - (phase - 5)) / 5), 0, 135);
				else if (local < step / 3)
					angle = 135;
				else if (local < 2 * step / 3)
					angle = afj.g(((value - (phase + 5)) / 5), 135, 0);

			}
		}

		rotateDoor(leftDoor, angle, true, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);
		rotateDoor(rightDoor, angle, false, direction).light(packedLightmapCoords)
			.renderInto(ms, vb);

		// Figure
		if (clock.animationType != Animation.NONE) {
			offset = -(angle / 135) * 1 / 2f + 10 / 16f;
			SuperByteBuffer figure =
				(clock.animationType == Animation.PIG ? AllBlockPartials.CUCKOO_PIG : AllBlockPartials.CUCKOO_CREEPER)
					.renderOn(blockState);
			figure.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(direction.rotateYCounterclockwise())));
			figure.translate(offset, 0, 0);
			figure.light(packedLightmapCoords)
				.renderInto(ms, vb);
		}

	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return transform(AllBlockPartials.SHAFT_HALF, te);
	}

	private SuperByteBuffer transform(AllBlockPartials partial, KineticTileEntity te) {
		return partial.renderOnDirectionalSouth(te.p(), te.p()
			.c(CuckooClockBlock.HORIZONTAL_FACING)
			.getOpposite());
	}

	private SuperByteBuffer rotateHand(SuperByteBuffer buffer, float angle, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 6 / 16f;
		float pivotZ = 8 / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCounterclockwise())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.EAST, angle);
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

	private SuperByteBuffer rotateDoor(SuperByteBuffer buffer, float angle, boolean left, Direction facing) {
		float pivotX = 2 / 16f;
		float pivotY = 0;
		float pivotZ = (left ? 6 : 10) / 16f;
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing.rotateYCounterclockwise())));
		buffer.translate(pivotX, pivotY, pivotZ);
		buffer.rotate(Direction.UP, AngleHelper.rad(angle) * (left ? -1 : 1));
		buffer.translate(-pivotX, -pivotY, -pivotZ);
		return buffer;
	}

}
