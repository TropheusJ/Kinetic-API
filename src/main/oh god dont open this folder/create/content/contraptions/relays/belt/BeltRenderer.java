package com.simibubi.create.content.contraptions.relays.belt;

import java.util.Random;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.client.network.DataQueryHandler;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.HorseEntityRenderer;
import net.minecraft.client.render.model.json.ModelElementTexture.b;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3i;
import afj;
import apx;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;
import com.simibubi.create.foundation.utility.ShadowRenderHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import ebv;

public class BeltRenderer extends SafeTileEntityRenderer<BeltTileEntity> {

	public BeltRenderer(ebv dispatcher) {
		super(dispatcher);
	}
	
	@Override
	public boolean isGlobalRenderer(BeltTileEntity te) {
		return BeltBlock.canTransportObjects(te.p());
	}

	@Override
	protected void renderSafe(BeltTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {

		PistonHandler blockState = te.p();
		if (!AllBlocks.BELT.has(blockState))
			return;

		BeltSlope beltSlope = blockState.c(BeltBlock.SLOPE);
		BeltPart part = blockState.c(BeltBlock.PART);
		Direction facing = blockState.c(BeltBlock.HORIZONTAL_FACING);
		AxisDirection axisDirection = facing.getDirection();

		boolean downward = beltSlope == BeltSlope.DOWNWARD;
		boolean upward = beltSlope == BeltSlope.UPWARD;
		boolean diagonal = downward || upward;
		boolean start = part == BeltPart.START;
		boolean end = part == BeltPart.END;
		boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
		boolean alongX = facing.getAxis() == Axis.X;

		MatrixStacker msr = MatrixStacker.of(ms);
		OverlayVertexConsumer vb = buffer.getBuffer(VertexConsumerProvider.c());
		float renderTick = AnimationTickHolder.getRenderTick();

		ms.a();
		msr.centre();
		msr.rotateY(AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0));
		msr.rotateZ(sideways ? 90 : 0);
		msr.rotateX(!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0);
		msr.unCentre();

		if (downward || beltSlope == BeltSlope.VERTICAL && axisDirection == AxisDirection.POSITIVE) {
			boolean b = start;
			start = end;
			end = b;
		}

		for (boolean bottom : Iterate.trueAndFalse) {

			AllBlockPartials beltPartial = diagonal
				? start ? AllBlockPartials.BELT_DIAGONAL_START
					: end ? AllBlockPartials.BELT_DIAGONAL_END : AllBlockPartials.BELT_DIAGONAL_MIDDLE
				: bottom
					? start ? AllBlockPartials.BELT_START_BOTTOM
						: end ? AllBlockPartials.BELT_END_BOTTOM : AllBlockPartials.BELT_MIDDLE_BOTTOM
					: start ? AllBlockPartials.BELT_START
						: end ? AllBlockPartials.BELT_END : AllBlockPartials.BELT_MIDDLE;

			SuperByteBuffer beltBuffer = beltPartial.renderOn(blockState)
				.light(light);
			SpriteShiftEntry spriteShift =
				diagonal ? AllSpriteShifts.BELT_DIAGONAL : bottom ? AllSpriteShifts.BELT_OFFSET : AllSpriteShifts.BELT;
			int cycleLength = diagonal ? 12 : 16;
			int cycleOffset = bottom ? 8 : 0;

			// UV shift
			float speed = te.getSpeed();
			if (speed != 0) {
				float time = renderTick * axisDirection.offset();
				if (diagonal && (downward ^ alongX) || !sideways && !diagonal && alongX
					|| sideways && axisDirection == AxisDirection.NEGATIVE)
					speed = -speed;
				int textureIndex = (int) (((speed * time / 36) + cycleOffset) % cycleLength);
				if (textureIndex < 0)
					textureIndex += cycleLength;

				beltBuffer.shiftUVtoSheet(spriteShift, (textureIndex % 4) / 4f, (textureIndex / 4) / 4f, 4);
			}

			beltBuffer.renderInto(ms, vb);

			// Diagonal belt do not have a separate bottom model
			if (diagonal)
				break;
		}
		ms.b();

		if (te.hasPulley()) {
			// TODO 1.15 find a way to cache this model matrix computation
			BufferVertexConsumer modelTransform = new BufferVertexConsumer();
			Direction dir = blockState.c(BeltBlock.HORIZONTAL_FACING)
				.rotateYClockwise();
			if (sideways)
				dir = Direction.UP;
			msr = MatrixStacker.of(modelTransform);
			msr.centre();
			if (dir.getAxis() == Axis.X)
				msr.rotateY(90);
			if (dir.getAxis() == Axis.Y)
				msr.rotateX(90);
			msr.rotateX(90);
			msr.unCentre();

			SuperByteBuffer superBuffer = CreateClient.bufferCache
				.renderDirectionalPartial(AllBlockPartials.BELT_PULLEY, blockState, dir, modelTransform);
			KineticTileEntityRenderer.standardKineticRotationTransform(superBuffer, te, light)
				.renderInto(ms, vb);
		}

		renderItems(te, partialTicks, ms, buffer, light, overlay);
	}

	protected void renderItems(BeltTileEntity te, float partialTicks, BufferVertexConsumer ms, BackgroundRenderer buffer,
		int light, int overlay) {
		if (!te.isController())
			return;
		if (te.beltLength == 0)
			return;

		ms.a();

		Vec3i directionVec = te.getBeltFacing()
			.getVector();
		EntityHitResult beltStartOffset = EntityHitResult.b(directionVec).a(-.5)
			.b(.5, 13 / 16f + .125f, .5);
		ms.a(beltStartOffset.entity, beltStartOffset.c, beltStartOffset.d);
		BeltSlope slope = te.p()
			.c(BeltBlock.SLOPE);
		int verticality = slope == BeltSlope.DOWNWARD ? -1 : slope == BeltSlope.UPWARD ? 1 : 0;
		boolean slopeAlongX = te.getBeltFacing()
			.getAxis() == Axis.X;

		for (TransportedItemStack transported : te.getInventory()
			.getTransportedItems()) {
			ms.a();
			MatrixStacker.of(ms)
				.nudge(transported.angle);
			float offset = afj.g(partialTicks, transported.prevBeltPosition, transported.beltPosition);
			float sideOffset = afj.g(partialTicks, transported.prevSideOffset, transported.sideOffset);
			float verticalMovement = verticality;

			if (te.getSpeed() == 0) {
				offset = transported.beltPosition;
				sideOffset = transported.sideOffset;
			}

			if (offset < .5)
				verticalMovement = 0;
			verticalMovement = verticalMovement * (Math.min(offset, te.beltLength - .5f) - .5f);
			EntityHitResult offsetVec = EntityHitResult.b(directionVec).a(offset)
				.b(0, verticalMovement, 0);
			boolean onSlope =
				slope != BeltSlope.HORIZONTAL && afj.a(offset, .5f, te.beltLength - .5f) == offset;
			boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ te.getBeltFacing()
				.getDirection() == AxisDirection.POSITIVE) == (te.getBeltFacing()
					.getAxis() == Axis.Z);
			float slopeAngle = onSlope ? tiltForward ? -45 : 45 : 0;

			ms.a(offsetVec.entity, offsetVec.c, offsetVec.d);

			boolean alongX = te.getBeltFacing()
				.rotateYClockwise()
				.getAxis() == Axis.X;
			if (!alongX)
				sideOffset *= -1;
			ms.a(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

			HorseEntityRenderer itemRenderer = KeyBinding.B()
				.ac();
			boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
			boolean blockItem = itemRenderer.a(transported.stack, te.v(), null)
				.b();
			int count = (int) (afj.f((int) (transported.stack.E()))) / 2;
			Random r = new Random(transported.angle);

			if (KeyBinding.B().k.y == DataQueryHandler.expectedTransactionId) {
				EntityHitResult shadowPos = EntityHitResult.b(te.o()).e(beltStartOffset.a(1)
					.e(offsetVec)
					.b(alongX ? sideOffset : 0, .39, alongX ? 0 : sideOffset));
				ShadowRenderHelper.renderShadow(ms, buffer, shadowPos, .75f, blockItem ? .2f : .2f);
			}

			if (renderUpright) {
				apx renderViewEntity = KeyBinding.B().t;
				if (renderViewEntity != null) {
					EntityHitResult positionVec = renderViewEntity.cz();
					EntityHitResult vectorForOffset = BeltHelper.getVectorForOffset(te, offset);
					EntityHitResult diff = vectorForOffset.d(positionVec);
					float yRot = (float) afj.d(diff.d, -diff.entity);
					ms.a(Vector3f.POSITIVE_Y.getRadialQuaternion((float) (yRot + Math.PI / 2)));
				}
				ms.a(0, 3 / 32d, 1 / 16f);
			}
			if (!renderUpright)
				ms.a(new Vector3f(slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0).getDegreesQuaternion(slopeAngle));

			if (onSlope)
				ms.a(0, 1 / 8f, 0);

			for (int i = 0; i <= count; i++) {
				ms.a();

				ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(transported.angle));
				if (!blockItem && !renderUpright) {
					ms.a(0, -.09375, 0);
					ms.a(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
				}

				if (blockItem) {
					ms.a(r.nextFloat() * .0625f * i, 0, r.nextFloat() * .0625f * i);
				}

				ms.a(.5f, .5f, .5f);
				itemRenderer.a(transported.stack, b.i, light, overlay, ms, buffer);
				ms.b();

				if (!renderUpright) {
					if (!blockItem)
						ms.a(Vector3f.POSITIVE_Y.getDegreesQuaternion(10));
					ms.a(0, blockItem ? 1 / 64d : 1 / 16d, 0);
				} else
					ms.a(0, 0, -1 / 16f);

			}

			ms.b();
		}
		ms.b();
	}

}
