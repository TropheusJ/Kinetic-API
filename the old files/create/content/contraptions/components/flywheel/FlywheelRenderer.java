package com.simibubi.kinetic_api.content.contraptions.components.flywheel;

import static com.simibubi.kinetic_api.content.contraptions.base.HorizontalKineticBlock.HORIZONTAL_FACING;

import afj;
import com.simibubi.kinetic_api.AllBlockPartials;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntity;
import com.simibubi.kinetic_api.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.kinetic_api.content.contraptions.components.flywheel.FlywheelBlock.ConnectionState;
import com.simibubi.kinetic_api.foundation.utility.AngleHelper;
import com.simibubi.kinetic_api.foundation.utility.SuperByteBuffer;
import ebv;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.gl.JsonGlProgram;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;

public class FlywheelRenderer extends KineticTileEntityRenderer {

	public FlywheelRenderer(ebv dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		PistonHandler blockState = te.p();
		FlywheelTileEntity wte = (FlywheelTileEntity) te;

		SuperByteBuffer wheel = AllBlockPartials.FLYWHEEL.renderOnHorizontal(blockState.a(RespawnAnchorBlock.field_26442));
		float speed = wte.visualSpeed.get(partialTicks) * 3 / 10f;
		float angle = wte.angle + speed * partialTicks;

		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());

		if (FlywheelBlock.isConnected(blockState)) {
			Direction connection = FlywheelBlock.getConnection(blockState);
			light = JsonGlProgram.a(te.v(), blockState, te.o()
				.offset(connection));
			float rotation =
				connection.getAxis() == Axis.X ^ connection.getDirection() == AxisDirection.NEGATIVE ? -angle
					: angle;
			boolean flip = blockState.c(FlywheelBlock.CONNECTION) == ConnectionState.LEFT;

			transformConnector(
				rotateToFacing(AllBlockPartials.FLYWHEEL_UPPER_ROTATING.renderOn(blockState), connection), true, true,
				rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(
				rotateToFacing(AllBlockPartials.FLYWHEEL_LOWER_ROTATING.renderOn(blockState), connection), false, true,
				rotation, flip).light(light)
					.renderInto(ms, vb);
			
			transformConnector(rotateToFacing(AllBlockPartials.FLYWHEEL_UPPER_SLIDING.renderOn(blockState), connection),
				true, false, rotation, flip).light(light)
					.renderInto(ms, vb);
			transformConnector(rotateToFacing(AllBlockPartials.FLYWHEEL_LOWER_SLIDING.renderOn(blockState), connection),
				false, false, rotation, flip).light(light)
					.renderInto(ms, vb);
		}

		kineticRotationTransform(wheel, te, blockState.c(HORIZONTAL_FACING)
			.getAxis(), AngleHelper.rad(angle), light);
		wheel.renderInto(ms, vb);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouth(te.p(), te.p()
			.c(HORIZONTAL_FACING)
			.getOpposite());
	}

	protected SuperByteBuffer transformConnector(SuperByteBuffer buffer, boolean upper, boolean rotating, float angle,
		boolean flip) {

		float shift = upper ? 1 / 4f : -1 / 8f;
		float offset = upper ? 1 / 4f : 1 / 4f;
		float radians = (float) (angle / 180 * Math.PI);
		float shifting = afj.a(radians) * shift + offset;

		float maxAngle = upper ? -5 : -15;
		float minAngle = upper ? -45 : 5;
		float barAngle = 0;

		if (rotating)
			barAngle = afj.g((afj.a((float) (radians + Math.PI / 2)) + 1) / 2, minAngle, maxAngle);

		float pivotX = (upper ? 8f : 3f) / 16;
		float pivotY = (upper ? 8f : 2f) / 16;
		float pivotZ = (upper ? 23f : 21.5f) / 16f;

		buffer.translate(pivotX, pivotY, pivotZ + shifting);
		if (rotating)
			buffer.rotate(Direction.EAST, AngleHelper.rad(barAngle));
		buffer.translate(-pivotX, -pivotY, -pivotZ);

		if (flip && !upper)
			buffer.translate(9 / 16f, 0, 0);

		return buffer;
	}

	protected SuperByteBuffer rotateToFacing(SuperByteBuffer buffer, Direction facing) {
		buffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)));
		return buffer;
	}

}
